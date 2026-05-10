/**
 * TwelveLittleScoutsClerk mail job creation helper
 * @author Copyright (c) 2023-2026 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk;

import at.redeye.FrameWork.base.DefaultInsertOrUpdater;
import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.SqlDBInterface.SqlDBIO.impl.TableBindingNotRegisteredException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.UnsupportedDBDataTypeException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.WrongBindFileFormatException;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBBill;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBBillingPeriod;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBBillTemplate;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBContact;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBEvent;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBEventMember;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMailJob;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMember;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMembers2Contacts;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.odftoolkit.odfdom.doc.OdfTextDocument;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MailJobHelper {

    /**
     * Creates and inserts {@link DBMailJob} records for every contact e-mail
     * address linked to the member of the given event-member.
     *
     * @param trans        active transaction (caller must commit afterwards)
     * @param mainwin      used to obtain new sequence values
     * @param bill         the bill that was just generated
     * @param template     the bill template (unused here but kept for symmetry)
     * @param event        the event
     * @param eventMember  the event-member record
     * @param member       the member
     */
    public static void createMailJobs(Transaction trans, MainWinInterface mainwin,
            DBBill bill, DBBillTemplate template,
            DBEvent event, DBEventMember eventMember, DBMember member)
            throws Exception {

        // 1. Load mail body ODT bytes from the configured DBBillTemplate
        byte[] odtBytes = loadMailBodyOdtBytes(trans, eventMember.bp_idx.getValue(), mainwin.getRoot());

        // Fetch DBBillingPeriod (needed for buildReplacementMap)
        DBBillingPeriod billingPeriod = new DBBillingPeriod();
        billingPeriod.idx.loadFromCopy(eventMember.bp_idx.getValue());
        trans.fetchTableWithPrimkey(billingPeriod);

        // 3. Collect all contacts with a non-blank e-mail
        List<DBContact> allRecipients = fetchRecipientsWithEmail(trans, eventMember);
        if (allRecipients.isEmpty()) {
            return;
        }

        // Build subject (same for all recipients)
        String billingNumber = bill.billingnr.getValue();
        String subject = "Rechnung " + billingNumber + " \u2013 "
                + mainwin.getRoot().getSetup().getConfig(AppConfigDefinitions.Organisation);

        // 5. Compute payment note (same for all recipients)
        double totalPaid = eventMember.paid.getValue() + eventMember.paid_cash.getValue();
        double costs = eventMember.costs.getValue();
        String paymentNote;
        String transferRequest;
        if (totalPaid >= costs) {
            paymentNote = "Note: This invoice has already been paid in full. "
                    + "No further transfer is required.";
            transferRequest = "";
        } else if (totalPaid > 0) {
            double remaining = costs - totalPaid;
            paymentNote = String.format(
                    "Note: A partial amount of %.2f EUR has already been paid. "
                    + "Outstanding balance: %.2f EUR.", totalPaid, remaining);
            transferRequest = "Please transfer the amount to the following account:";
        } else {
            paymentNote = "";
            transferRequest = "Please transfer the amount to the following account:";
        }

        // 8. Create a DBMailJob for each recipient address
        // Collect all individual addresses for the also_sent_to note
        List<String> allAddresses = new ArrayList<>();
        for (DBContact c : allRecipients) {
            allAddresses.addAll(splitEmails(c.email.getValue()));
        }

        for (DBContact contact : allRecipients) {
            List<String> addresses = splitEmails(contact.email.getValue());
            String recipientName = (contact.forname.getValue() + " " + contact.name.getValue()).trim();

            for (String address : addresses) {

                // 4. Build ${mail.also_sent_to} for this specific address
                String alsoSentTo = buildAlsoSentTo(allAddresses, address);

                // 2. Build replacement map with current contact
                Map<String, String> replacements = BillingHelper.buildReplacementMap(
                        member, contact, event, eventMember, billingPeriod);
                replacements.put("${billing_number}", billingNumber);

                // 6. Add mail-specific placeholders
                replacements.put("${mail.also_sent_to}", alsoSentTo);
                replacements.put("${mail.payment_note}", paymentNote);
                replacements.put("${mail.transfer_request}", transferRequest);

                // 7. Load fresh ODT copy, apply replacements, extract plain text
                OdfTextDocument doc = OdfTextDocument.loadDocument(
                        new ByteArrayInputStream(odtBytes));
                BillingHelper.replaceInNode(doc.getContentDom(), replacements);
                String body = extractText(doc.getContentDom());

                // Build and insert DBMailJob
                DBMailJob job = new DBMailJob();
                job.idx.loadFromCopy(mainwin.getNewSequenceValue(DBMailJob.MAIL_JOB_IDX_SEQUENCE));
                job.bp_idx.loadFromCopy(bill.bp_idx.getValue());
                job.bill_idx.loadFromCopy(bill.idx.getValue());
                job.recipient_email.loadFromString(address);
                job.recipient_name.loadFromString(recipientName);
                job.subject.loadFromString(subject);
                job.body.value = body.getBytes(StandardCharsets.UTF_8);
                job.pdf_data.value = bill.pdf_data.value;
                job.state.handler.setValue(DBMailJob.State.PENDING.ordinal());
                job.retry_count.loadFromCopy(0);

                DefaultInsertOrUpdater.insertOrUpdateValuesWithPrimKey(
                        trans, job, job.hist, MailJobHelper.class.getSimpleName());
            }
        }
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private static byte[] loadMailBodyOdtBytes(Transaction trans, int bpIdx, at.redeye.FrameWork.base.Root root)
            throws SQLException, TableBindingNotRegisteredException,
                   UnsupportedDBDataTypeException, WrongBindFileFormatException, IOException {
        String templateName = root.getSetup().getConfig(AppConfigDefinitions.MailBodyTemplateName);
        if (templateName == null || templateName.isBlank()) {
            throw new IOException(
                    "Kein E-Mail-Vorlagenname konfiguriert. "
                    + "Bitte 'MailBodyTemplateName' in den Einstellungen setzen.");
        }
        DBBillTemplate tmpl = new DBBillTemplate();
        List<DBBillTemplate> results = trans.fetchTable2(tmpl,
                "where " + trans.markColumn(tmpl, tmpl.bp_idx) + " = " + bpIdx
                + " and " + trans.markColumn(tmpl, tmpl.name) + " = '" + templateName + "'");
        if (results.isEmpty()) {
            throw new IOException(
                    "E-Mail-Vorlage '" + templateName + "' nicht gefunden.");
        }
        DBBillTemplate found = results.get(0);
        if (found.odt_data.value == null || found.odt_data.value.length == 0) {
            throw new IOException(
                    "E-Mail-Vorlage '" + templateName + "' hat keine ODT-Datei.");
        }
        return found.odt_data.value;
    }

    private static List<DBContact> fetchRecipientsWithEmail(Transaction trans,
            DBEventMember eventMember)
            throws SQLException, TableBindingNotRegisteredException,
                   UnsupportedDBDataTypeException, WrongBindFileFormatException, IOException {

        DBMembers2Contacts m2c = new DBMembers2Contacts();
        List<DBMembers2Contacts> links = trans.fetchTable2(m2c,
                "where " + trans.markColumn(m2c.member_idx) + " = "
                + eventMember.member_idx.toString());

        List<DBContact> recipients = new ArrayList<>();
        for (DBMembers2Contacts link : links) {
            DBContact contact = new DBContact();
            contact.idx.loadFromCopy(link.contact_idx.getValue());
            trans.fetchTableWithPrimkey(contact);
            if (contact.email.getValue() != null && !contact.email.getValue().isBlank()) {
                recipients.add(contact);
            }
        }
        return recipients;
    }

    private static List<String> splitEmails(String raw) {
        List<String> result = new ArrayList<>();
        if (raw == null || raw.isBlank()) {
            return result;
        }
        for (String part : raw.split("[,;\\s]+")) {
            String addr = part.trim();
            if (!addr.isEmpty()) {
                result.add(addr);
            }
        }
        return result;
    }

    private static String buildAlsoSentTo(List<String> allAddresses, String currentAddress) {
        List<String> others = new ArrayList<>();
        for (String addr : allAddresses) {
            if (!addr.equalsIgnoreCase(currentAddress)) {
                others.add(addr);
            }
        }
        if (others.isEmpty()) {
            return "";
        }
        return "Note: This invoice was also sent to the following addresses:\n"
                + String.join(", ", others);
    }

    private static String extractText(Node node) {
        StringBuilder sb = new StringBuilder();
        extractTextFrom(node, sb);
        return sb.toString();
    }

    private static void extractTextFrom(Node node, StringBuilder sb) {
        if (node.getNodeType() == Node.TEXT_NODE) {
            sb.append(node.getNodeValue());
        }
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            extractTextFrom(children.item(i), sb);
        }
    }
}
