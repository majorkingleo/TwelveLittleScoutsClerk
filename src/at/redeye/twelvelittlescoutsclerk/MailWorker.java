/**
 * TwelveLittleScoutsClerk background mail dispatch worker
 * @author Copyright (c) 2023-2026 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk;

import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMailJob;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.Logger;

public class MailWorker implements Runnable {

    private static final Logger logger = Logger.getLogger(MailWorker.class.getName());

    private static final int MAX_RETRIES    = 3;
    private static final int POLL_INTERVAL_MS = 30_000;

    private final Transaction trans;

    public MailWorker(Transaction trans) {
        this.trans = trans;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                List<DBMailJob> pending = fetchPendingJobs();
                for (DBMailJob job : pending) {
                    markSending(job);
                    try {
                        sendMail(job);
                        markSent(job);
                    } catch (Exception ex) {
                        logger.warn("Failed to send mail job " + job.idx.getValue(), ex);
                        int retries = job.retry_count.getValue() + 1;
                        job.retry_count.loadFromCopy(retries);
                        if (retries >= MAX_RETRIES) {
                            markFailed(job, ex.getMessage());
                        } else {
                            markPending(job, ex.getMessage());
                        }
                    }
                }
                Thread.sleep(POLL_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception ex) {
                logger.error("MailWorker poll cycle failed", ex);
                try {
                    Thread.sleep(POLL_INTERVAL_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // DB state transitions
    // -----------------------------------------------------------------------

    private List<DBMailJob> fetchPendingJobs() throws Exception {
        DBMailJob proto = new DBMailJob();
        return trans.fetchTable2(proto,
                "where " + trans.markColumn(proto.state) + " = "
                + DBMailJob.State.PENDING.ordinal()
                + " order by " + trans.markColumn(proto.idx));
    }

    private void markSending(DBMailJob job) throws Exception {
        job.state.handler.setValue(DBMailJob.State.SENDING.ordinal());
        trans.updateValues(job);
        trans.commit();
    }

    private void markSent(DBMailJob job) throws Exception {
        job.state.handler.setValue(DBMailJob.State.SENT.ordinal());
        job.error_message.loadFromString("");
        trans.updateValues(job);
        trans.commit();
    }

    private void markFailed(DBMailJob job, String errorMsg) {
        try {
            job.state.handler.setValue(DBMailJob.State.FAILED.ordinal());
            job.error_message.loadFromString(truncate(errorMsg, 2000));
            trans.updateValues(job);
            trans.commit();
        } catch (Exception ex) {
            logger.error("Failed to mark job " + job.idx.getValue() + " as FAILED", ex);
        }
    }

    private void markPending(DBMailJob job, String errorMsg) {
        try {
            job.state.handler.setValue(DBMailJob.State.PENDING.ordinal());
            job.error_message.loadFromString(truncate(errorMsg, 2000));
            trans.updateValues(job);
            trans.commit();
        } catch (Exception ex) {
            logger.error("Failed to re-queue job " + job.idx.getValue(), ex);
        }
    }

    // -----------------------------------------------------------------------
    // SMTP send
    // -----------------------------------------------------------------------

    private void sendMail(DBMailJob job) throws MessagingException {
        String host     = AppConfigDefinitions.MailSmtpHost.getConfigValue();
        String portStr  = AppConfigDefinitions.MailSmtpPort.getConfigValue();
        String startTls = AppConfigDefinitions.MailSmtpStartTls.getConfigValue();
        String from     = AppConfigDefinitions.MailFrom.getConfigValue();
        String fromName = AppConfigDefinitions.MailFromName.getConfigValue();
        String user     = AppConfigDefinitions.MailSmtpUser.getConfigValue();
        String password = AppConfigDefinitions.MailSmtpPassword.getConfigValue();

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", portStr);
        props.put("mail.smtp.auth", (user != null && !user.isBlank()) ? "true" : "false");
        if ("true".equalsIgnoreCase(startTls)) {
            props.put("mail.smtp.starttls.enable", "true");
        }

        Session session;
        if (user != null && !user.isBlank()) {
            final String finalUser = user;
            final String finalPassword = password != null ? password : "";
            session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(finalUser, finalPassword);
                }
            });
        } else {
            session = Session.getInstance(props);
        }

        MimeMessage msg = new MimeMessage(session);

        // From
        if (fromName != null && !fromName.isBlank()) {
            try {
                msg.setFrom(new InternetAddress(from,
                        fromName, StandardCharsets.UTF_8.name()));
            } catch (Exception ex) {
                msg.setFrom(new InternetAddress(from));
            }
        } else {
            msg.setFrom(new InternetAddress(from));
        }

        // To
        try {
            msg.setRecipient(Message.RecipientType.TO,
                    new InternetAddress(job.recipient_email.getValue(),
                            job.recipient_name.getValue(), StandardCharsets.UTF_8.name()));
        } catch (java.io.UnsupportedEncodingException ex) {
            msg.setRecipient(Message.RecipientType.TO,
                    new InternetAddress(job.recipient_email.getValue()));
        }

        // Subject
        msg.setSubject(job.subject.getValue(), StandardCharsets.UTF_8.name());

        // Body + optional PDF attachment
        if (job.pdf_data.value != null && job.pdf_data.value.length > 0) {
            Multipart mp = new MimeMultipart();

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(bodyText(job), StandardCharsets.UTF_8.name());
            mp.addBodyPart(textPart);

            MimeBodyPart pdfPart = new MimeBodyPart();
            pdfPart.setContent(job.pdf_data.value, "application/pdf");
            pdfPart.setFileName("Rechnung.pdf");
            mp.addBodyPart(pdfPart);

            msg.setContent(mp);
        } else {
            msg.setText(bodyText(job), StandardCharsets.UTF_8.name());
        }

        Transport.send(msg);
    }

    private static String bodyText(DBMailJob job) {
        if (job.body.value == null || job.body.value.length == 0) {
            return "";
        }
        return new String(job.body.value, StandardCharsets.UTF_8);
    }

    private static String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen);
    }
}
