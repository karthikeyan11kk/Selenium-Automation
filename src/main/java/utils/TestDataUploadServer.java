package utils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Feature 1 - "I need a UI to upload the test data file, it should not take
 * from the path."
 *
 * A small, dependency-free (JDK's own com.sun.net.httpserver - no servlet
 * container, no extra Maven dependency) local web page that lets whoever is
 * running the pipeline BROWSE and UPLOAD the TestData.xls (and optionally
 * Q&P-Numbers.xls / TestData.properties) file instead of the framework
 * silently reading whatever happens to already sit at a hardcoded path on
 * disk. This is meant to run as its own short-lived step BEFORE "mvn test"
 * in the pipeline:
 *
 *   mvn -q compile exec:java@upload-ui       (see pom.xml exec-maven-plugin)
 *   -> opens http://localhost:9090 (port configurable: -DuploadServerPort=NNNN)
 *   -> user/pipeline uploads TestData.xls (and optionally Q&P-Numbers.xls)
 *   -> files are saved to the exact paths Initialize.getDataTablePath()/
 *      Initialize.getQPNumbersFilePath() expect
 *   -> server exits automatically once at least the TestData file has been
 *      received (so a CI job doesn't hang waiting forever), or after an
 *      idle timeout
 *   mvn test                                  (now runs against the uploaded file)
 *
 * Run standalone with: java -cp <classpath> utils.TestDataUploadServer
 */
public class TestDataUploadServer {
    private static final Logger log = LoggerFactory.getLogger(TestDataUploadServer.class);

    private static volatile boolean testDataReceived = false;
    private static final CountDownLatch shutdownLatch = new CountDownLatch(1);

    public static void main(String[] args) throws Exception {
        int port = resolvePort();
        long idleTimeoutMinutes = resolveIdleTimeoutMinutes();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new UploadPageHandler());
        server.createContext("/upload", new UploadHandler());
        server.setExecutor(null);
        server.start();

        String url = "http://localhost:" + port + "/";
        log.info("=================================================================");
        log.info(" Test Data Upload UI is running:  " + url);
        log.info(" Open that URL, choose your TestData.xls (and, optionally,");
        log.info(" Q&P-Numbers.xls), and click Upload. This window will close");
        log.info(" automatically once the file has been received, or after "
                + idleTimeoutMinutes + " minute(s) of inactivity.");
        log.info("=================================================================");

        // Best-effort: open the user's default browser automatically when a
        // display is available (local/dev runs). Headless CI agents simply
        // fall through - the URL above is still printed to the pipeline log.
        tryAutoOpenBrowser(url);

        boolean finishedInTime = shutdownLatch.await(idleTimeoutMinutes, TimeUnit.MINUTES);
        if (!finishedInTime) {
            log.error("No upload received within " + idleTimeoutMinutes
                    + " minute(s) - shutting down upload server. The pipeline will "
                    + "fall back to whatever TestData.xls already exists on disk, if any.");
        }
        server.stop(0);
    }

    private static int resolvePort() {
        String sys = System.getProperty("uploadServerPort");
        if (sys != null && !sys.isBlank()) {
            try {
                return Integer.parseInt(sys.trim());
            } catch (NumberFormatException ignored) {
                // fall through to config file value
            }
        }
        try {
            Initialize.loadConfig();
            String fromConfig = Initialize.getConfigValue("uploadServerPort");
            if (fromConfig != null && !fromConfig.isBlank()) {
                return Integer.parseInt(fromConfig.trim());
            }
        } catch (Exception ignored) {
            // TestData.properties may not exist yet on a brand-new pipeline
            // checkout - that's fine, default below still works.
        }
        return 9090;
    }

    private static long resolveIdleTimeoutMinutes() {
        try {
            Initialize.loadConfig();
            String fromConfig = Initialize.getConfigValue("uploadServerTimeoutMinutes");
            if (fromConfig != null && !fromConfig.isBlank()) {
                return Long.parseLong(fromConfig.trim());
            }
        } catch (Exception ignored) {
            // use default below
        }
        return 15L;
    }

    private static void tryAutoOpenBrowser(String url) {
        try {
            if (java.awt.GraphicsEnvironment.isHeadless()) {
                return;
            }
            if (java.awt.Desktop.isDesktopSupported()
                    && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
            }
        } catch (Exception e) {
            log.info("Could not auto-open a browser (headless pipeline agent, most likely) - "
                    + "open the URL above manually. Detail: " + e.getMessage());
        }
    }

    /** Serves the upload HTML form. */
    private static class UploadPageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = "<!DOCTYPE html><html><head><title>Upload Test Data</title>"
                    + "<style>body{font-family:Segoe UI,Arial,sans-serif;max-width:640px;margin:60px auto;padding:0 20px;color:#222}"
                    + "h2{color:#1a3c6e}label{display:block;margin:18px 0 6px;font-weight:600}"
                    + "input[type=file]{display:block;margin-bottom:6px}"
                    + ".hint{color:#666;font-size:13px;margin-bottom:10px}"
                    + "button{background:#1a73e8;color:#fff;border:0;padding:10px 22px;border-radius:4px;font-size:15px;cursor:pointer;margin-top:18px}"
                    + "button:hover{background:#1558b0}</style></head><body>"
                    + "<h2>Intact / New1C Automation &mdash; Test Data Upload</h2>"
                    + "<p>Upload your test data file(s) for this pipeline run. Nothing is read from a "
                    + "fixed path on the build agent &mdash; whatever you upload here is what the suite uses.</p>"
                    + "<form method='POST' action='/upload' enctype='multipart/form-data'>"
                    + "<label>TestData.xls (required)</label>"
                    + "<div class='hint'>The main data-driven Excel workbook (New1CShop / New1CPropertyOwners sheets).</div>"
                    + "<input type='file' name='testDataFile' accept='.xls,.xlsx' required>"
                    + "<label>Q&amp;P-Numbers.xls (optional)</label>"
                    + "<div class='hint'>Leave blank to keep using the existing one on the agent, if any.</div>"
                    + "<input type='file' name='qpNumbersFile' accept='.xls,.xlsx'>"
                    + "<button type='submit'>Upload &amp; Continue</button>"
                    + "</form></body></html>";
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (var os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    /** Accepts the multipart POST, saves each part to the path the framework expects. */
    private static class UploadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) {
            String resultMessage;
            boolean ok = true;
            try {
                if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    sendPlainText(exchange, 405, "Method Not Allowed");
                    return;
                }
                String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
                if (contentType == null || !contentType.toLowerCase().contains("multipart/form-data")) {
                    sendPlainText(exchange, 400, "Expected multipart/form-data upload.");
                    return;
                }
                String boundary = extractBoundary(contentType);
                if (boundary == null) {
                    sendPlainText(exchange, 400, "No multipart boundary found in Content-Type header.");
                    return;
                }

                byte[] body = readAll(exchange.getRequestBody());
                List<Part> parts = parseMultipart(body, boundary);

                StringBuilder saved = new StringBuilder();
                for (Part part : parts) {
                    if (part.fileName == null || part.fileName.isBlank()) {
                        continue; // not a file field, or an empty optional file input
                    }
                    String targetPath = resolveTargetPathFor(part.fieldName);
                    if (targetPath == null) {
                        continue;
                    }
                    File target = new File(targetPath);
                    File parentDir = target.getParentFile();
                    if (parentDir != null && !parentDir.exists()) {
                        parentDir.mkdirs();
                    }
                    try (FileOutputStream fos = new FileOutputStream(target)) {
                        fos.write(part.data);
                    }
                    log.info("Saved uploaded file '" + part.fileName + "' -> " + target.getAbsolutePath());
                    saved.append(part.fieldName).append(" -> ").append(target.getAbsolutePath()).append("\n");
                    if ("testDataFile".equals(part.fieldName)) {
                        testDataReceived = true;
                    }
                }

                if (!testDataReceived) {
                    ok = false;
                    resultMessage = "No TestData file was received - please choose a file and try again.";
                    sendHtmlResult(exchange, ok, resultMessage, "");
                    return;
                }

                resultMessage = "Upload successful. The pipeline will continue using:";
                sendHtmlResult(exchange, true, resultMessage, saved.toString());
                shutdownLatch.countDown();
            } catch (Exception e) {
                log.error("Upload failed: ", e);
                try {
                    sendHtmlResult(exchange, false, "Upload failed: " + e.getMessage(), "");
                } catch (IOException ignored) {
                    // response already unusable
                }
            }
        }
    }

    /**
     * Where an uploaded field actually gets written - matches exactly what
     * Initialize.getDataTablePath("TestData") / Initialize.getQPNumbersFilePath()
     * resolve to (including any testDataFilePath/qpNumbersFilePath override
     * already configured in TestData.properties), so the rest of the
     * framework needs zero changes to pick it up.
     */
    private static String resolveTargetPathFor(String fieldName) {
        try {
            if ("testDataFile".equals(fieldName)) {
                return Initialize.getDataTablePath("TestData");
            } else if ("qpNumbersFile".equals(fieldName)) {
                return Initialize.getQPNumbersFilePath();
            }
        } catch (Exception e) {
            log.error("Could not resolve target path for field '" + fieldName + "': " + e.getMessage());
        }
        return null;
    }

    private static void sendPlainText(HttpExchange exchange, int status, String msg) throws IOException {
        byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (var os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendHtmlResult(HttpExchange exchange, boolean ok, String message, String detail) throws IOException {
        String html = "<!DOCTYPE html><html><head><title>Upload Result</title>"
                + "<style>body{font-family:Segoe UI,Arial,sans-serif;max-width:640px;margin:60px auto;padding:0 20px;color:#222}"
                + ".ok{color:#1a7a3c}.err{color:#c0392b}pre{background:#f4f4f4;padding:12px;border-radius:4px;white-space:pre-wrap}</style>"
                + "</head><body><h2 class='" + (ok ? "ok" : "err") + "'>" + (ok ? "Success" : "Error") + "</h2>"
                + "<p>" + escapeHtml(message) + "</p>"
                + (detail.isBlank() ? "" : "<pre>" + escapeHtml(detail) + "</pre>")
                + (ok ? "<p>You can close this tab. The pipeline will continue automatically.</p>" : "<p><a href='/'>Go back</a></p>")
                + "</body></html>";
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (var os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) != -1) {
            bos.write(buf, 0, n);
        }
        return bos.toByteArray();
    }

    private static String extractBoundary(String contentType) {
        for (String token : contentType.split(";")) {
            token = token.trim();
            if (token.startsWith("boundary=")) {
                String b = token.substring("boundary=".length());
                if (b.startsWith("\"") && b.endsWith("\"") && b.length() >= 2) {
                    b = b.substring(1, b.length() - 1);
                }
                return b;
            }
        }
        return null;
    }

    private static class Part {
        String fieldName;
        String fileName;
        byte[] data;
    }

    /**
     * Minimal hand-rolled multipart/form-data parser - deliberately avoids
     * pulling in a servlet container or Apache FileUpload just for this one
     * small form. Splits the raw body on "--boundary" markers, then reads
     * each part's headers (to get name=/filename=) and body.
     */
    private static List<Part> parseMultipart(byte[] body, String boundary) {
        List<Part> parts = new ArrayList<>();
        byte[] boundaryBytes = ("--" + boundary).getBytes(StandardCharsets.ISO_8859_1);
        List<Integer> markers = new ArrayList<>();
        for (int i = 0; i <= body.length - boundaryBytes.length; i++) {
            if (matches(body, i, boundaryBytes)) {
                markers.add(i);
            }
        }
        for (int m = 0; m < markers.size() - 1; m++) {
            int start = markers.get(m) + boundaryBytes.length;
            int end = markers.get(m + 1);
            if (start >= end) continue;
            // Skip the trailing "--" + CRLF right after a boundary start, and
            // the leading CRLF before the next boundary.
            if (start + 1 < body.length && body[start] == '-' && body[start + 1] == '-') {
                continue; // final boundary marker
            }
            byte[] section = java.util.Arrays.copyOfRange(body, start, end);
            Part part = parseSection(section);
            if (part != null) {
                parts.add(part);
            }
        }
        return parts;
    }

    private static boolean matches(byte[] haystack, int offset, byte[] needle) {
        for (int i = 0; i < needle.length; i++) {
            if (haystack[offset + i] != needle[i]) {
                return false;
            }
        }
        return true;
    }

    private static Part parseSection(byte[] section) {
        // Section looks like: \r\nContent-Disposition: form-data; name="x"; filename="y"\r\nContent-Type: ...\r\n\r\n<bytes>\r\n
        String headerBlockEndMarker = "\r\n\r\n";
        byte[] headerEndBytes = headerBlockEndMarker.getBytes(StandardCharsets.ISO_8859_1);
        int headerEnd = indexOf(section, headerEndBytes, 0);
        if (headerEnd < 0) {
            return null;
        }
        String headerText = new String(section, 0, headerEnd, StandardCharsets.ISO_8859_1);
        int dataStart = headerEnd + headerEndBytes.length;
        int dataEnd = section.length;
        // Strip the trailing CRLF that precedes the next boundary.
        if (dataEnd >= 2 && section[dataEnd - 2] == '\r' && section[dataEnd - 1] == '\n') {
            dataEnd -= 2;
        }
        if (dataStart > dataEnd) {
            dataStart = dataEnd;
        }

        Part part = new Part();
        for (String line : headerText.split("\r\n")) {
            if (line.toLowerCase().startsWith("content-disposition")) {
                part.fieldName = extractAttribute(line, "name");
                part.fileName = extractAttribute(line, "filename");
            }
        }
        if (part.fieldName == null) {
            return null;
        }
        part.data = java.util.Arrays.copyOfRange(section, dataStart, dataEnd);
        return part;
    }

    private static String extractAttribute(String headerLine, String attr) {
        String marker = attr + "=\"";
        int idx = headerLine.indexOf(marker);
        if (idx < 0) {
            return null;
        }
        int start = idx + marker.length();
        int end = headerLine.indexOf('"', start);
        if (end < 0) {
            return null;
        }
        return headerLine.substring(start, end);
    }

    private static int indexOf(byte[] haystack, byte[] needle, int fromIndex) {
        outer:
        for (int i = fromIndex; i <= haystack.length - needle.length; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}
