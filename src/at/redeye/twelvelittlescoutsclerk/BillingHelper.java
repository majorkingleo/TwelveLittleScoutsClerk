/**
 * TwelveLittleScoutsClerk bill generation from ODT template
 * @author Copyright (c) 2023-2026 Martin Oberzalek
 */
package at.redeye.twelvelittlescoutsclerk;

import at.redeye.FrameWork.base.Root;
import at.redeye.FrameWork.base.transaction.Transaction;
import at.redeye.SqlDBInterface.SqlDBIO.impl.TableBindingNotRegisteredException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.UnsupportedDBDataTypeException;
import at.redeye.SqlDBInterface.SqlDBIO.impl.WrongBindFileFormatException;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBBillingPeriod;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBBillTemplate;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBContact;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBEvent;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBEventMember;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMember;
import at.redeye.twelvelittlescoutsclerk.bindtypes.DBMembers2Contacts;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.odftoolkit.odfdom.doc.OdfTextDocument;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BillingHelper {

    /**
     * Generates a bill ODT (and optionally a PDF) from the ODT template
     * referenced by {@code event.billing_template}.
     *
     * After calling this method the caller must persist the updated
     * {@code eventMember} via {@code trans.updateValues(eventMember)}.
     *
     * @param trans          active transaction
     * @param event          the event (must have {@code billing_template} set)
     * @param eventMember    the event-member record whose bill is being created
     * @param convertToPdf   when {@code true} also convert the ODT to PDF via
     *                       LibreOffice and return the PDF file
     * @return the generated {@code .odt} or {@code .pdf} file
     */
    public static File generateBill(Root root, Transaction trans, DBEvent event,
            DBEventMember eventMember, boolean convertToPdf)
            throws SQLException, TableBindingNotRegisteredException,
                   UnsupportedDBDataTypeException, WrongBindFileFormatException,
                   IOException, Exception {

        // 1. Fetch DBMember
        DBMember member = new DBMember();
        member.idx.loadFromCopy(eventMember.member_idx.getValue());
        trans.fetchTableWithPrimkey(member);

        // 2. Fetch first DBContact linked to the member (may be null)
        DBContact contact = fetchFirstContact(trans, member);

        // 2b. Fetch DBBillingPeriod for the billing period title
        DBBillingPeriod billingPeriod = new DBBillingPeriod();
        billingPeriod.idx.loadFromCopy(eventMember.bp_idx.getValue());
        trans.fetchTableWithPrimkey(billingPeriod);

        // 3. Build replacement map
        Map<String, String> replacements = buildReplacementMap(root, member, contact, event, eventMember, billingPeriod);

        // 4+5. Load ODT template
        String templatePath = event.billing_template.getValue();
        File templateFile = new File(templatePath);
        OdfTextDocument doc = OdfTextDocument.loadDocument(templateFile);

        // 6. Walk all text nodes in content and styles (headers/footers) and apply replacements
        replaceAcrossSpans(doc.getContentDom(), replacements);
        replaceInNode(doc.getContentDom(), replacements);
        replaceAcrossSpans(doc.getStylesDom(), replacements);
        replaceInNode(doc.getStylesDom(), replacements);

        // 7. Save to a temp ODT file
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        File odtFile = File.createTempFile(
                "bill_" + eventMember.member_idx.getValue() + "_", ".odt", tmpDir);
        doc.save(odtFile);

        File outputFile = odtFile;

        // 8. Optionally convert to PDF
        if (convertToPdf) {
            outputFile = convertToPdf(odtFile, tmpDir);
        }

        // 9. Store output path in eventMember (caller must persist)
        eventMember.bill.loadFromString(outputFile.getAbsolutePath());

        // 10. Return output file
        return outputFile;
    }

    /**
     * Generates a bill ODT from a {@link DBBillTemplate} whose ODT is stored
     * in the database blob rather than as a file on disk.
     *
     * After calling this method the caller must persist the updated
     * {@code eventMember} via {@code trans.updateValues(eventMember)}.
     *
     * @param trans         active transaction
     * @param template      the bill template (must have non-empty {@code odt_data})
     * @param event         the event
     * @param eventMember   the event-member record whose bill is being created
     * @param convertToPdf  when {@code true} also convert the ODT to PDF via
     *                      LibreOffice and return the PDF file
     * @return the generated {@code .odt} or {@code .pdf} file
     */
    // generated by AI (Claude Sonnet 4.6)
    public static File generateBillFromTemplate(Root root, Transaction trans,
            DBBillTemplate template, DBEvent event, DBEventMember eventMember,
            boolean convertToPdf, String billingNumber)
            throws SQLException, TableBindingNotRegisteredException,
                   UnsupportedDBDataTypeException, WrongBindFileFormatException,
                   IOException, Exception {
        return generateBillFromTemplate(root, trans, template, event, eventMember,
                convertToPdf, billingNumber, 0.0, 0);
    }

    public static File generateBillFromTemplate(Root root, Transaction trans,
            DBBillTemplate template, DBEvent event, DBEventMember eventMember,
            boolean convertToPdf, String billingNumber, double registrationPayment)
            throws SQLException, TableBindingNotRegisteredException,
                   UnsupportedDBDataTypeException, WrongBindFileFormatException,
                   IOException, Exception {
        return generateBillFromTemplate(root, trans, template, event, eventMember,
                convertToPdf, billingNumber, registrationPayment, 0);
    }

    public static File generateBillFromTemplate(Root root, Transaction trans,
            DBBillTemplate template, DBEvent event, DBEventMember eventMember,
            boolean convertToPdf, String billingNumber, double registrationPayment,
            int registrationNumber)
            throws SQLException, TableBindingNotRegisteredException,
                   UnsupportedDBDataTypeException, WrongBindFileFormatException,
                   IOException, Exception {

        // 1. Fetch DBMember
        DBMember member = new DBMember();
        member.idx.loadFromCopy(eventMember.member_idx.getValue());
        trans.fetchTableWithPrimkey(member);

        // 2. Fetch first DBContact linked to the member (may be null)
        DBContact contact = fetchFirstContact(trans, member);

        // 2b. Fetch DBBillingPeriod for the billing period title
        DBBillingPeriod billingPeriod = new DBBillingPeriod();
        billingPeriod.idx.loadFromCopy(eventMember.bp_idx.getValue());
        trans.fetchTableWithPrimkey(billingPeriod);

        // 3. Build replacement map
        Map<String, String> replacements = buildReplacementMap(root, member, contact, event, eventMember, billingPeriod, billingNumber, registrationPayment, registrationNumber);

        // 4+5. Load ODT template from blob bytes
        OdfTextDocument doc = OdfTextDocument.loadDocument(
                new java.io.ByteArrayInputStream(template.odt_data.value));

        // 6. Walk all text nodes in content and styles (headers/footers) and apply replacements
        replaceAcrossSpans(doc.getContentDom(), replacements);
        replaceInNode(doc.getContentDom(), replacements);
        replaceAcrossSpans(doc.getStylesDom(), replacements);
        replaceInNode(doc.getStylesDom(), replacements);

        // 6b. Inject EPC QR code image
        double amount = eventMember.costs.getValue() - (eventMember.paid.getValue() - eventMember.paid_cash.getValue());

        if( registrationPayment > 0.0 ) {
            amount = registrationPayment;
        }

        // : instead of - to get stats how often the QR code is used for payment
        String remittance = event.name.getValue() + " : " + member.forname.getValue() + " " + member.name.getValue();
        injectEpcQrCode(root, doc, amount, remittance);

        // 7. Save to a temp ODT file
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        File odtFile = File.createTempFile(
                "bill_" + eventMember.member_idx.getValue() + "_", ".odt", tmpDir);
        doc.save(odtFile);

        File outputFile = odtFile;

        // 8. Optionally convert to PDF
        if (convertToPdf) {
            outputFile = convertToPdf(odtFile, tmpDir);
        }

        // 9. Store output path in eventMember (caller must persist)
        eventMember.bill.loadFromString(outputFile.getAbsolutePath());

        // 10. Return output file
        return outputFile;
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private static DBContact fetchFirstContact(Transaction trans, DBMember member)
            throws SQLException, TableBindingNotRegisteredException,
                   UnsupportedDBDataTypeException, WrongBindFileFormatException,
                   IOException {

        DBMembers2Contacts m2c = new DBMembers2Contacts();
        List<DBMembers2Contacts> links = trans.fetchTable2(m2c,
                "where " + trans.markColumn(m2c.member_idx) + " = " + member.idx.toString());

        if (links.isEmpty()) {
            return null;
        }

        DBContact contact = new DBContact();
        contact.idx.loadFromCopy(links.get(0).contact_idx.getValue());
        trans.fetchTableWithPrimkey(contact);
        return contact;
    }

    static Map<String, String> buildReplacementMap(Root root, DBMember member,
            DBContact contact, DBEvent event, DBEventMember eventMember,
            DBBillingPeriod billingPeriod) {
        return buildReplacementMap(root, member, contact, event, eventMember, billingPeriod, "", 0.0, 0);
    }

    static Map<String, String> buildReplacementMap(Root root, DBMember member,
            DBContact contact, DBEvent event, DBEventMember eventMember,
            DBBillingPeriod billingPeriod, String billingNumber) {
        return buildReplacementMap(root, member, contact, event, eventMember, billingPeriod, billingNumber, 0.0, 0);
    }

    static Map<String, String> buildReplacementMap(Root root, DBMember member,
            DBContact contact, DBEvent event, DBEventMember eventMember,
            DBBillingPeriod billingPeriod, String billingNumber, double registrationPayment) {
        return buildReplacementMap(root, member, contact, event, eventMember, billingPeriod, billingNumber, registrationPayment, 0);
    }

    static Map<String, String> buildReplacementMap(Root root, DBMember member,
            DBContact contact, DBEvent event, DBEventMember eventMember,
            DBBillingPeriod billingPeriod, String billingNumber, double registrationPayment,
            int registrationNumber) {

        Map<String, String> map = new HashMap<>();

        // Member
        map.put("${member.name}",                member.name.getValue());
        map.put("${member.forname}",             member.forname.getValue());
        map.put("${member.registration_number}", member.member_registration_number.getValue());
        map.put("${member.group}",               member.group.getValue());
        map.put("${member.fullname}",            (member.forname.getValue() + " " + member.name.getValue()).trim());

        // Contact (empty string when no contact is linked)
        String contactName    = contact != null ? contact.name.getValue()    : "";
        String contactForname = contact != null ? contact.forname.getValue() : "";
        String contactEmail   = contact != null ? contact.email.getValue()   : "";
        String contactTel     = contact != null ? contact.tel.getValue()     : "";
        map.put("${contact.name}",     contactName);
        map.put("${contact.forname}",  contactForname);
        map.put("${contact.email}",    contactEmail);
        map.put("${contact.tel}",      contactTel);
        map.put("${contact.fullname}", (contactForname + " " + contactName).trim());

        // Billing number
        map.put("${billing_number}", billingNumber);

        // Event
        map.put("${event.name}",          event.name.getValue());
        map.put("${event.costs}",               event.costs.getValue().toString());
        map.put("${event.planned_costs}",        event.planned_costs.getValue().toString());
        map.put("${event.registration_costs}",   String.format(java.util.Locale.GERMANY, "%.2f", event.registration_costs.getValue()));

        // EventMember
        map.put("${event_member.costs}",      eventMember.costs.getValue().toString());
        map.put("${event_member.paid}",       eventMember.paid.getValue().toString());
        map.put("${event_member.paid_cash}",  eventMember.paid_cash.getValue().toString());
        // AI modification start (Claude Sonnet 4.6)
        map.put("${event_member.costs_open}",
                Double.toString(eventMember.costs.getValue()
                        - eventMember.paid.getValue()
                        + eventMember.paid_cash.getValue()));
        // AI modification end
        map.put("${event_member.comment}",    eventMember.comment.getValue());

        // Billing period
        map.put("${billing_period.title}", billingPeriod.title.getValue());

        // Registration payment (deposit already paid; 0.00 when not applicable)
        map.put("${registration_payment}",
                String.format(java.util.Locale.GERMANY, "%.2f", registrationPayment));

        // Registration number (sequential number from REGISTRATION_IDX_SEQ; 0 when not applicable)
        map.put("${registration_number}", registrationNumber > 0 ? String.valueOf(registrationNumber) : "");

        // Organisation (from global config)
        map.put("${org.name}",                root.getSetup().getConfig(AppConfigDefinitions.Organisation));
        map.put("${org.name4bank_transfer}",  root.getSetup().getConfig(AppConfigDefinitions.OrganisationName4BankTransfer));
        map.put("${org.address_street}",      root.getSetup().getConfig(AppConfigDefinitions.OrganisationAddressStreet));
        map.put("${org.address_postal_code}", root.getSetup().getConfig(AppConfigDefinitions.OrganisationAddressPostalCode));
        map.put("${org.address_city}",        root.getSetup().getConfig(AppConfigDefinitions.OrganisationAddressCity));
        map.put("${org.iban}",                root.getSetup().getConfig(AppConfigDefinitions.OrganisaiontIBAN));

        String remittance = event.name.getValue() + " - " + member.forname.getValue() + " " + member.name.getValue();
        map.put("${remittance}", remittance);

        return map;
    }

    /**
     * Finds a draw:frame named "qr_code" (fallback: "Bild2") in the ODT content,
     * generates an EPC QR code PNG for the given amount and remittance, and
     * replaces the image bytes inside the ODF package at the existing href path.
     */
    // generated by AI (Claude Sonnet 4.6)
    private static void injectEpcQrCode(Root root, OdfTextDocument doc, double amountEur, String remittance) {
        try {
            NodeList frames = doc.getContentDom().getElementsByTagNameNS(
                    "urn:oasis:names:tc:opendocument:xmlns:drawing:1.0", "frame");

            Element targetFrame = null;
            for (int i = 0; i < frames.getLength(); i++) {
                Element frame = (Element) frames.item(i);
                String name = frame.getAttributeNS(
                        "urn:oasis:names:tc:opendocument:xmlns:drawing:1.0", "name");
                if ("qr_code".equals(name) || "Bild2".equals(name)) {
                    targetFrame = frame;
                    break;
                }
            }

            if (targetFrame == null) {
                // No named frame found — skip silently
                return;
            }

            // Find the draw:image child
            NodeList images = targetFrame.getElementsByTagNameNS(
                    "urn:oasis:names:tc:opendocument:xmlns:drawing:1.0", "image");
            if (images.getLength() == 0) {
                return;
            }
            Element imageElem = (Element) images.item(0);
            String href = imageElem.getAttributeNS("http://www.w3.org/1999/xlink", "href");
            if (href == null || href.isBlank()) {
                return;
            }

            // Generate EPC QR PNG bytes
            byte[] qrPng = EpcQrHelper.generateBillQrPng( root, amountEur, remittance, 300);

            // Replace the image bytes in the ODF package at the existing path
            doc.getPackage().insert(qrPng, href, "image/png");

        } catch (Exception ex) {
            // Non-fatal: log and continue without QR code
            java.util.logging.Logger.getLogger(BillingHelper.class.getName())
                    .log(java.util.logging.Level.WARNING, "Failed to inject EPC QR code", ex);
        }
    }

    /**
     * Collects all text nodes that are descendants of {@code node} (in document order).
     */
    private static void collectTextNodes(Node node, List<Node> result) {
        if (node.getNodeType() == Node.TEXT_NODE) {
            result.add(node);
        }
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            collectTextNodes(children.item(i), result);
        }
    }

    /**
     * Handles placeholders that LibreOffice has split across multiple
     * {@code <text:span>} elements within a single paragraph or heading.
     * For each {@code text:p} / {@code text:h} element whose concatenated
     * text content contains a placeholder key, the replacement is applied to
     * the full text and the paragraph's child nodes are replaced with a single
     * plain text node.  Paragraphs without any placeholder are left untouched.
     */
    static void replaceAcrossSpans(Node root, Map<String, String> replacements) {
        final String NS_TEXT = "urn:oasis:names:tc:opendocument:xmlns:text:1.0";
        if (root.getNodeType() == Node.ELEMENT_NODE) {
            String ns = root.getNamespaceURI();
            String local = root.getLocalName();
            if (NS_TEXT.equals(ns) && ("p".equals(local) || "h".equals(local))) {
                replaceAcrossSpansInParagraph(root, replacements, NS_TEXT);
                return;
            }
        }
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            replaceAcrossSpans(children.item(i), replacements);
        }
    }

    /**
     * Splits a paragraph's children into "runs" separated by structural void
     * elements (text:tab, text:line-break, text:s).  For each run, if a
     * placeholder key is split across multiple text nodes, the run is collapsed
     * to a single text node with the replacement applied.  Structural separators
     * and runs that need no cross-span fix are left untouched.
     */
    private static void replaceAcrossSpansInParagraph(Node para,
            Map<String, String> replacements, String NS_TEXT) {
        // Snapshot children — NodeList is live, changes during modification
        List<Node> children = new ArrayList<>();
        NodeList nl = para.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            children.add(nl.item(i));
        }

        List<Node> currentRun = new ArrayList<>();
        for (int i = 0; i <= children.size(); i++) {
            Node child = i < children.size() ? children.get(i) : null;
            boolean isSep = child != null && isSeparatorElement(child, NS_TEXT);
            if (child == null || isSep) {
                if (!currentRun.isEmpty()) {
                    collapseRunIfNeeded(currentRun, para, child, replacements);
                    currentRun = new ArrayList<>();
                }
            } else {
                currentRun.add(child);
            }
        }
    }

    /** True for void elements that separate text runs and must be preserved. */
    private static boolean isSeparatorElement(Node node, String NS_TEXT) {
        if (node.getNodeType() != Node.ELEMENT_NODE) return false;
        String local = node.getLocalName();
        return NS_TEXT.equals(node.getNamespaceURI())
                && ("tab".equals(local) || "line-break".equals(local) || "s".equals(local));
    }

    /**
     * If any placeholder key is split across multiple text nodes in this run,
     * removes all run nodes from {@code para} and inserts a single collapsed
     * text node (with replacements applied) before {@code beforeSibling}.
     */
    private static void collapseRunIfNeeded(List<Node> run, Node para,
            Node beforeSibling, Map<String, String> replacements) {
        List<Node> textNodes = new ArrayList<>();
        for (Node n : run) {
            collectTextNodes(n, textNodes);
        }
        StringBuilder sb = new StringBuilder();
        for (Node tn : textNodes) {
            String v = tn.getNodeValue();
            sb.append(v != null ? v : "");
        }
        String full = sb.toString();

        boolean hasSplitPlaceholder = false;
        for (String key : replacements.keySet()) {
            if (full.contains(key)) {
                boolean inSingleNode = false;
                for (Node tn : textNodes) {
                    String v = tn.getNodeValue();
                    if (v != null && v.contains(key)) {
                        inSingleNode = true;
                        break;
                    }
                }
                if (!inSingleNode) {
                    hasSplitPlaceholder = true;
                    break;
                }
            }
        }

        if (hasSplitPlaceholder) {
            String replaced = full;
            for (Map.Entry<String, String> e : replacements.entrySet()) {
                replaced = replaced.replace(e.getKey(), e.getValue());
            }
            for (Node n : run) {
                para.removeChild(n);
            }
            Node textNode = para.getOwnerDocument().createTextNode(replaced);
            if (beforeSibling != null) {
                para.insertBefore(textNode, beforeSibling);
            } else {
                para.appendChild(textNode);
            }
        }
    }


    static void replaceInNode(Node node, Map<String, String> replacements) {
        if (node.getNodeType() == Node.TEXT_NODE) {
            String text = node.getNodeValue();
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                text = text.replace(entry.getKey(), entry.getValue());
            }
            node.setNodeValue(text);
        }
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            replaceInNode(children.item(i), replacements);
        }
    }

    public static File convertToPdf(File odtFile, File outDir) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                "libreoffice", "--headless", "--convert-to", "pdf",
                "--outdir", outDir.getAbsolutePath(),
                odtFile.getAbsolutePath());
        pb.redirectErrorStream(true);
        Process proc = pb.start();

        // Drain stdout/stderr to avoid blocking
        try (InputStream is = proc.getInputStream()) {
            is.transferTo(java.io.OutputStream.nullOutputStream());
        }

        int exitCode = proc.waitFor();
        if (exitCode != 0) {
            throw new IOException("LibreOffice conversion failed with exit code " + exitCode);
        }

        String pdfName = odtFile.getName().replaceFirst("\\.odt$", ".pdf");
        return new File(outDir, pdfName);
    }
}
