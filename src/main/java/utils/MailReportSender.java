package utils;

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
 * Emails the Extent HTML report to a configured list of recipients after
 * the suite finishes (called from Hooks.tearDownSuite()). Fully optional -
 * if "mailTo" isn't set in TestData.properties, this just logs a message
 * and returns, so it never blocks or breaks a run for anyone who hasn't
 * configured SMTP yet.
 *
 * Required TestData.properties keys to actually send mail:
 *   smtpHost, smtpPort, smtpUsername, smtpPassword, mailFrom, mailTo
 * mailTo supports multiple recipients, comma-separated.
 * mailSubject is optional (has a sensible default).
 *
 * For Gmail specifically: smtpHost=smtp.gmail.com, smtpPort=587, and
 * smtpPassword must be a 16-character Google "App Password" (Google
 * Account -> Security -> 2-Step Verification -> App passwords), NOT your
 * normal account password - Gmail rejects normal passwords for SMTP.
 */
public class MailReportSender {

    public static void sendReportEmail() throws Exception {
        String mailTo = safeGet("mailTo");
        if (mailTo == null || mailTo.isBlank()) {
            System.out.println("mailTo not set in TestData.properties - skipping report email.");
            return;
        }

        String host = safeGet("smtpHost");
        String port = safeGet("smtpPort");
        String username = safeGet("smtpUsername");
        String password = safeGet("smtpPassword");
        String from = safeGet("mailFrom");

        if (host == null || host.isBlank() || port == null || port.isBlank()
                || username == null || username.isBlank()
                || password == null || password.isBlank()
                || from == null || from.isBlank()) {
            System.err.println("SMTP settings incomplete in TestData.properties (need smtpHost, smtpPort, "
                    + "smtpUsername, smtpPassword, mailFrom) - skipping report email.");
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
        for (String recipient : mailTo.split(",")) {
            String trimmed = recipient.trim();
            if (!trimmed.isEmpty()) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(trimmed));
            }
        }

        String subject = safeGetOrDefault("mailSubject", "Automation Test Execution Report");
        message.setSubject(subject + " - " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText("Hi,\n\nThe automated test run has completed. Please find the Extent report attached.\n\n"
                + "Regards,\nAutomation Framework");

        MimeMultipart multipart = new MimeMultipart();
        multipart.addBodyPart(textPart);

        String reportPath = System.getProperty("user.dir") + File.separator + "Testresults" + File.separator + "ExtentReport.html";
        File reportFile = new File(reportPath);
        if (reportFile.exists()) {
            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.attachFile(reportFile);
            multipart.addBodyPart(attachmentPart);
        } else {
            System.err.println("Extent report not found at " + reportPath + " - sending email without attachment.");
        }

        message.setContent(multipart);

        Transport.send(message);
        System.out.println("Report email sent to: " + mailTo);
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
