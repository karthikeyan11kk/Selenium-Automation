package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * Emails the final execution report out once the suite finishes (called from
 * Hooks.tearDownSuite()).
 *
 * Feature 5 - "once the entire execution is completed I need to send mail
 * using outlook to the to mail id and CC mail id mentioned in the test
 * data.properties."
 *
 * Sending "using Outlook" from an unattended Maven/pipeline run means going
 * through Outlook's own SMTP relay (Office 365 / Outlook.com), the same way
 * Outlook itself hands mail off when you hit Send - there is no portable,
 * headless way to drive the Outlook desktop application's own send button
 * from a CI agent. So by default this now points at smtp.office365.com, and
 * mailProvider=Outlook in TestData.properties documents that choice (set it
 * to anything else, e.g. Gmail, and smtpHost/smtpPort below still override
 * freely for teams not on Outlook/Office365).
 *
 * Required TestData.properties keys to actually send mail:
 *   mailTo, smtpUsername, smtpPassword, mailFrom
 *   (smtpHost/smtpPort default to Outlook/Office365 if left blank - see
 *   resolveSmtpHost()/resolveSmtpPort())
 * mailTo / mailCC both support multiple recipients, comma-separated.
 * mailCC is optional - leave blank to send with no CC.
 *
 * For Outlook/Office365: smtpHost=smtp.office365.com, smtpPort=587, and
 * smtpUsername/smtpPassword are the same Outlook/Office365 account
 * credentials used to sign in to Outlook (an App Password if the tenant has
 * MFA enforced - Outlook rejects a normal interactive password for SMTP in
 * that case, same restriction as Gmail's App Passwords).
 */
public class MailReportSender {
    private static final Logger log = LoggerFactory.getLogger(MailReportSender.class);

    private static final String DEFAULT_OUTLOOK_HOST = "smtp.office365.com";
    private static final String DEFAULT_OUTLOOK_PORT = "587";

    /** Back-compat overload - sends with no execution-package attachment. */
    public static void sendReportEmail() throws Exception {
        sendReportEmail(null);
    }

    /**
     * @param executionZipPath absolute path to the Feature-2 execution
     *                         package zip (per-test-case screenshots/report
     *                         folders + overall report + Excel summary), or
     *                         null/blank if it wasn't built this run.
     */
    public static void sendReportEmail(String executionZipPath) throws Exception {
        String mailTo = safeGet("mailTo");
        if (mailTo == null || mailTo.isBlank()) {
            log.info("mailTo not set in TestData.properties - skipping report email.");
            return;
        }

        String mailCC = safeGet("mailCC");

        String host = resolveSmtpHost();
        String port = resolveSmtpPort();
        String username = safeGet("smtpUsername");
        String password = safeGet("smtpPassword");
        String from = safeGetOrDefault("mailFrom", username);

        if (username == null || username.isBlank()
                || password == null || password.isBlank()
                || from == null || from.isBlank()) {
            log.error("SMTP settings incomplete in TestData.properties (need at least smtpUsername, "
                    + "smtpPassword, mailFrom) - skipping report email.");
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        addRecipients(message, Message.RecipientType.TO, mailTo);
        if (mailCC != null && !mailCC.isBlank()) {
            addRecipients(message, Message.RecipientType.CC, mailCC);
        }

        String subject = safeGetOrDefault("mailSubject", "Automation Test Execution Report");
        message.setSubject(subject + " - " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText("Hi,\n\nThe automated test run has completed. Please find attached:\n"
                + " - The overall Extent execution report\n"
                + " - The full execution package (per test case screenshots + report folder, "
                + "step-wise results, quote/policy numbers and any defect IDs)\n\n"
                + "Regards,\nAutomation Framework");

        MimeMultipart multipart = new MimeMultipart();
        multipart.addBodyPart(textPart);

        String reportPath = System.getProperty("user.dir") + File.separator + "Testresults" + File.separator + "ExtentReport.html";
        attachIfExists(multipart, reportPath, "ExtentReport.html");

        if (executionZipPath != null && !executionZipPath.isBlank()) {
            attachIfExists(multipart, executionZipPath, new File(executionZipPath).getName());
        } else {
            log.info("No execution package zip path provided - sending email without the zip attachment.");
        }

        message.setContent(multipart);

        Transport.send(message);
        log.info("Report email sent via " + host + " to: " + mailTo
                + (mailCC != null && !mailCC.isBlank() ? " | CC: " + mailCC : ""));
    }

    private static void addRecipients(MimeMessage message, Message.RecipientType type, String commaSeparated) throws Exception {
        for (String recipient : commaSeparated.split(",")) {
            String trimmed = recipient.trim();
            if (!trimmed.isEmpty()) {
                message.addRecipient(type, new InternetAddress(trimmed));
            }
        }
    }

    private static void attachIfExists(MimeMultipart multipart, String filePath, String attachmentName) throws Exception {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.attachFile(file);
            attachmentPart.setFileName(attachmentName);
            multipart.addBodyPart(attachmentPart);
        } else {
            log.error("Expected attachment not found at " + filePath + " - skipping that attachment.");
        }
    }

    /** smtpHost in TestData.properties always wins if set; otherwise defaults to Outlook/Office365. */
    private static String resolveSmtpHost() {
        String configured = safeGet("smtpHost");
        if (configured != null && !configured.isBlank()) {
            return configured.trim();
        }
        return DEFAULT_OUTLOOK_HOST;
    }

    private static String resolveSmtpPort() {
        String configured = safeGet("smtpPort");
        if (configured != null && !configured.isBlank()) {
            return configured.trim();
        }
        return DEFAULT_OUTLOOK_PORT;
    }

    private static String safeGet(String key) {
        try {
            return Initialize.getTestDataPF(key);
        } catch (Exception e) {
            return null;
        }
    }

    private static String safeGetOrDefault(String key, String def) {
        String v = safeGet(key);
        return (v == null || v.isBlank()) ? def : v;
    }
}
