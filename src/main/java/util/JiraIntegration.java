package util;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPut;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.ByteArrayBody;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class JiraIntegration extends Initialize {

    // --- Config helpers ---
    private static String jiraBaseUrl() { 
        String base = getConfigValue("JiraURL");
        if (!base.endsWith("/")) base += "/";
        return base;
    }
    private static String jiraEmail()    { return getConfigValue("JiraEmail"); }
    private static String jiraApiToken() { return getConfigValue("JiraAPItoken"); }
    private static String jiraProjectKey(){ return getConfigValue("JiraProjectKey"); }

    // --- Auth header ---
    private static String getAuthHeader() {
        String token = jiraEmail() + ":" + jiraApiToken();
        String encoded = Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

    // --- Reusable client with timeouts ---

private static final CloseableHttpClient client = HttpClients.custom()
    .setDefaultRequestConfig(
        RequestConfig.custom()
            .setConnectTimeout(Timeout.ofSeconds(30))  // or Timeout.ofMilliseconds(30_000)
            .setResponseTimeout(Timeout.ofSeconds(60)) // or Timeout.ofMilliseconds(60_000)
            .build()
    )
    .build();


    // --- Utility: handle response and throw on non-2xx ---

private static JSONObject handleJsonResponse(CloseableHttpResponse response, String operation) throws Exception {
    int status = response.getCode();
    String body = response.getEntity() != null ? EntityUtils.toString(response.getEntity()) : "";

    System.out.println("Raw Jira Response for " + operation + ": " + body);

    if (status >= 200 && status < 300) {
        // ✅ If body is empty, return an empty JSON object
        if (body.isEmpty() || !body.trim().startsWith("{")) {
            return new JSONObject(); // Avoid JSONException
        }
        return new JSONObject(body);
    } else {
        String message = "Jira " + operation + " failed (HTTP " + status + "): " + body;
        try {
            JSONObject err = new JSONObject(body);
            if (err.has("errorMessages")) {
                message += "\n errorMessages=" + err.getJSONArray("errorMessages").toString();
            }
            if (err.has("errors")) {
                message += "\n errors=" + err.getJSONObject("errors").toString();
            }
        } catch (Exception ignore) {}
        throw new RuntimeException(message);
    }
}

    /**
     * Create a new Jira issue (Cloud: v3)
     * @return issueKey (e.g., "ABC-123")
     */




public static String createIssue(String summary, String description, List<String> screenshotLinks, String tcName, String product) throws Exception {
    String url = jiraBaseUrl() + "rest/api/3/issue";
    HttpPost post = new HttpPost(url);
    post.setHeader("Authorization", getAuthHeader());
    post.setHeader("Content-Type", "application/json");

    // Build ADF description with multiple screenshot links
    JSONArray contentArray = new JSONArray();

    // Add Test Case details
    contentArray.put(new JSONObject()
        .put("type", "paragraph")
        .put("content", new JSONArray()
            .put(new JSONObject().put("type", "text").put("text", "Test Case: " + tcName))
            .put(new JSONObject().put("type", "text").put("text", "\nProduct: " + product))
            .put(new JSONObject().put("type", "text").put("text", "\nError: " + description))
        )
    );

    // Add clickable links for all screenshots
    if (screenshotLinks != null && !screenshotLinks.isEmpty()) {
        for (String link : screenshotLinks) {
            contentArray.put(new JSONObject()
                .put("type", "paragraph")
                .put("content", new JSONArray()
                    .put(new JSONObject()
                        .put("type", "text")
                        .put("text", "View Screenshot")
                        .put("marks", new JSONArray()
                            .put(new JSONObject()
                                .put("type", "link")
                                .put("attrs", new JSONObject().put("href", link))
                            )
                        )
                    )
                )
            );
        }
    }

    JSONObject adfDescription = new JSONObject()
        .put("type", "doc")
        .put("version", 1)
        .put("content", contentArray);

    JSONObject fields = new JSONObject()
        .put("project", new JSONObject().put("key", jiraProjectKey()))
        .put("summary", summary)
        .put("description", adfDescription)
        .put("issuetype", new JSONObject().put("name", "Bug"));

    JSONObject payload = new JSONObject().put("fields", fields);
    post.setEntity(new StringEntity(payload.toString(), ContentType.APPLICATION_JSON));

    try (CloseableHttpResponse response = client.execute(post)) {
        JSONObject json = handleJsonResponse(response, "createIssue");
        String issueKey = json.optString("key", "");
        if (issueKey.isEmpty()) {
            throw new RuntimeException("Jira createIssue succeeded but no 'key' returned: " + json.toString());
        }
        System.out.println("✅ Jira Issue Created: " + issueKey);
        return issueKey;
    }
}





    /**
     * Add a comment to an existing Jira issue
     */


public static void addComment(String issueKey, String comment) throws Exception {
    String url = jiraBaseUrl() + "rest/api/2/issue/" + issueKey + "/comment";
    HttpPost post = new HttpPost(url);
    post.setHeader("Authorization", getAuthHeader());
    post.setHeader("Content-Type", "application/json");
    post.setHeader("Accept", "application/json");

    JSONObject payload = new JSONObject().put("body", comment);
    post.setEntity(new StringEntity(payload.toString(), ContentType.APPLICATION_JSON));

    try (CloseableHttpResponse response = client.execute(post)) {
        String rawResponse = EntityUtils.toString(response.getEntity());
        System.out.println("Raw Jira Response: " + rawResponse);

        // ✅ Parse JSON directly here instead of calling handleJsonResponse()
        JSONObject json = new JSONObject(rawResponse);
        System.out.println("✅ Comment added to Jira Issue: " + issueKey + " | Comment ID: " + json.optString("id"));
    }
}



    /**
     * Attach a screenshot or file to Jira issue (multipart/form-data)
     */

public static void attachScreenshot(String issueKey, String filePath) throws Exception {
    String url = jiraBaseUrl() + "rest/api/2/issue/" + issueKey + "/attachments";
    HttpPost post = new HttpPost(url);
    post.setHeader("Authorization", getAuthHeader());
    post.setHeader("X-Atlassian-Token", "no-check");

    File file = new File(filePath);
    if (!file.exists()) {
        throw new IllegalArgumentException("❌ File not found: " + filePath);
    }

    System.out.println("➡ Jira API Request: " + url);
    System.out.println("➡ Attaching file: " + file.getAbsolutePath() + " (" + file.length() + " bytes)");

    HttpEntity entity = MultipartEntityBuilder.create()
            .addPart("file", new FileBody(file, ContentType.DEFAULT_BINARY, file.getName()))
            .build();
    post.setEntity(entity);

    try (CloseableHttpResponse response = client.execute(post)) {
        String rawResponse = EntityUtils.toString(response.getEntity());
        System.out.println("⬅ Jira API Response (" + response.getCode() + "): " + rawResponse);

        if (response.getCode() >= 200 && response.getCode() < 300) {
            System.out.println("✅ Attachment uploaded to Jira Issue: " + issueKey + " (" + file.getName() + ")");
        } else {
            System.err.println("❌ Failed to attach file to Jira Issue: " + issueKey + " | Status: " + response.getCode());
        }
    }
}

    /**
     * Transition Jira issue to a new status by transitionId
     */
    public static void transitionIssue(String issueKey, String transitionId) throws Exception {
        String url = jiraBaseUrl() + "rest/api/2/issue/" + issueKey + "/transitions";
        HttpPost post = new HttpPost(url);
        post.setHeader("Authorization", getAuthHeader());
        post.setHeader("Content-Type", "application/json");

        JSONObject payload = new JSONObject()
            .put("transition", new JSONObject().put("id", transitionId));

        post.setEntity(new StringEntity(payload.toString(), ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse response = client.execute(post)) {
            handleJsonResponse(response, "transitionIssue");
            System.out.println("✅ Transitioned Jira Issue: " + issueKey + " to ID: " + transitionId);
        }
    }

    /**
     * Helper: List available transitions for an issue
     */
    public static JSONArray listTransitions(String issueKey) throws Exception {
        String url = jiraBaseUrl() + "rest/api/2/issue/" + issueKey + "/transitions";
        HttpGet get = new HttpGet(url);
        get.setHeader("Authorization", getAuthHeader());

        try (CloseableHttpResponse response = client.execute(get)) {
            JSONObject json = handleJsonResponse(response, "listTransitions");
            return json.optJSONArray("transitions");
        }
    }

    /**
     * Helper: Transition by name (e.g., "Done", "In Progress")
     */
    public static void transitionIssueByName(String issueKey, String transitionName) throws Exception {
        JSONArray transitions = listTransitions(issueKey);
        if (transitions == null) {
            throw new RuntimeException("No transitions available for issue: " + issueKey);
        }
        String id = null;
        for (int i = 0; i < transitions.length(); i++) {
            JSONObject t = transitions.getJSONObject(i);
            if (transitionName.equalsIgnoreCase(t.optString("name"))) {
                id = t.optString("id");
                break;
            }
        }
        if (id == null) {
            throw new RuntimeException("Transition '" + transitionName + "' not found for issue " + issueKey);
        }
        transitionIssue(issueKey, id);
    }
    


 // Evidence store for collecting screenshots during test execution
 private static final Map<String, List<String>> EVIDENCE_STORE = new ConcurrentHashMap<>();

 
public static void addStepComment(String issueKey, String stepDescription) throws Exception {
    addComment(issueKey, "Step executed: " + stepDescription);
}

/**
 * Collects evidence paths during test execution for later attachment.
 * Use tcName as key (not Jira issueKey).
 */

public static void addEvidencePath(String tcName, String filePath) {
    EVIDENCE_STORE.computeIfAbsent(tcName, k -> new CopyOnWriteArrayList<>()).add(filePath);
}


/**
 * Retrieves and clears all evidence paths for a test case.
 */
public static List<String> drainEvidence(String tcName) {
    List<String> paths = EVIDENCE_STORE.getOrDefault(tcName, List.of());
    EVIDENCE_STORE.remove(tcName);
    return paths;

}





//In JiraIntegration.java

/**
* Update step-level execution in Xray Cloud for a Test inside a Test Execution.
* Requires Xray Cloud OAuth token (Bearer). Will auto-authenticate if none is present.
*/
public static void updateXrayStepResults(String testExecKey,
                                      String testKey,
                                      List<Map<String, String>> steps) throws Exception {

 final String url = "https://xray.cloud.getxray.app/api/v2/import/execution";
 HttpPost post = new HttpPost(url);
 post.setHeader("Content-Type", "application/json");

 // 1) Get a valid Bearer token (from config or auto-generate)
 String token = Initialize.getConfigValue("XrayBearerToken");
 if (token == null || token.isBlank()) {
     token = obtainXrayBearerToken(); // auto-authenticate and cache
 }
 post.setHeader("Authorization", "Bearer " + token);

 // 2) Build payload (minimal fields Xray expects)
 // {
 //   "testExecutionKey": "EXEC-123",
 //   "tests": [{
 //      "testKey": "TEST-456",
 //      "steps": [
 //        {"status": "PASS", "actualResult": "As expected result"}
 //      ]
 //   }]
 // }
 JSONObject payload = new JSONObject().put("testExecutionKey", testExecKey);
 JSONArray testsArray = new JSONArray();
 JSONObject testObj = new JSONObject().put("testKey", testKey);

 JSONArray stepsArray = new JSONArray();
 for (Map<String, String> step : steps) {
     // Ensure we always send strings and defaults
     String status = String.valueOf(step.getOrDefault("status", "PASS")).toUpperCase();
     String actual = String.valueOf(step.getOrDefault("actualResult", "As expected result"));

     JSONObject stepObj = new JSONObject()
             .put("status", status)               // PASS / FAIL
             .put("actualResult", actual);        // your fixed text

     stepsArray.put(stepObj);
 }

 testObj.put("steps", stepsArray);
 testsArray.put(testObj);
 payload.put("tests", testsArray);

 post.setEntity(new StringEntity(payload.toString(), ContentType.APPLICATION_JSON));

 // 3) Execute with robust error handling
 try (CloseableHttpResponse response = client.execute(post)) {
     int code = response.getCode();
     String raw = response.getEntity() != null ? EntityUtils.toString(response.getEntity()) : "";

     // Log full response for diagnostics
     System.out.println("Xray Step Update HTTP " + code + " | Response: " + raw);

     if (code == 401 || code == 403) {
         // Token expired/invalid -> refresh once and retry automatically
         System.out.println("Xray token invalid/expired. Re-authenticating...");
         String refreshed = obtainXrayBearerToken();
         HttpPost retry = new HttpPost(url);
         retry.setHeader("Content-Type", "application/json");
         retry.setHeader("Authorization", "Bearer " + refreshed);
         retry.setEntity(new StringEntity(payload.toString(), ContentType.APPLICATION_JSON));
         try (CloseableHttpResponse retryResp = client.execute(retry)) {
             int rCode = retryResp.getCode();
             String rRaw = retryResp.getEntity() != null ? EntityUtils.toString(retryResp.getEntity()) : "";
             System.out.println("Xray Step Update (retry) HTTP " + rCode + " | Response: " + rRaw);
             if (rCode < 200 || rCode >= 300) {
                 throw new RuntimeException("Xray update failed after re-authenticate. HTTP " + rCode + " Body: " + rRaw);
             }
         }
     } else if (code < 200 || code >= 300) {
         // 503 CloudFront or other errors -> throw clearly
         throw new RuntimeException("Xray update failed. HTTP " + code + " Body: " + raw);
     }
     // Success: Xray returns JSON; keep the log for traceability
     System.out.println("✅ Xray step results updated for " + testKey + " in " + testExecKey);
 }
}

/**
* Obtain an Xray Cloud Bearer token using Client ID/Secret and cache it in memory.
* Make sure XrayClientId and XrayClientSecret exist in your properties.
*/
public static String obtainXrayBearerToken() throws Exception {
 final String authUrl = "https://xray.cloud.getxray.app/api/v2/authenticate";
 String clientId = Initialize.getConfigValue("XrayClientId");
 String clientSecret = Initialize.getConfigValue("XrayClientSecret");

 if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
     throw new IllegalStateException("XrayClientId / XrayClientSecret are not configured.");
 }

 HttpPost authPost = new HttpPost(authUrl);
 authPost.setHeader("Content-Type", "application/json");

 JSONObject body = new JSONObject()
         .put("client_id", clientId)
         .put("client_secret", clientSecret);

 authPost.setEntity(new StringEntity(body.toString(), ContentType.APPLICATION_JSON));

 try (CloseableHttpResponse resp = client.execute(authPost)) {
     int code = resp.getCode();
     String raw = resp.getEntity() != null ? EntityUtils.toString(resp.getEntity()) : "";
     if (code < 200 || code >= 300 || raw == null || raw.isBlank()) {
         throw new RuntimeException("Xray authentication failed. HTTP " + code + " Body: " + raw);
     }
     // Response is a quoted JWT string: "eyJhbGciOiJIUzI1NiIs..."
     String token = raw.replace("\"", "").trim();
     // Cache for reuse in current JVM
     System.setProperty("XrayBearerToken", token);
     System.out.println("✅ Obtained Xray Bearer token.");
     return token;
 }
}



public static String getXrayAuthToken() throws Exception {
    String url = "https://xray.cloud.getxray.app/api/v2/authenticate";
    HttpPost post = new HttpPost(url);
    post.setHeader("Content-Type", "application/json");

    JSONObject payload = new JSONObject()
        .put("client_id", Initialize.getConfigValue("XrayClientId"))
        .put("client_secret", Initialize.getConfigValue("XrayClientSecret"));

    post.setEntity(new StringEntity(payload.toString(), ContentType.APPLICATION_JSON));

    try (CloseableHttpResponse response = client.execute(post)) {
        String token = EntityUtils.toString(response.getEntity()).replace("\"", "");
        System.out.println("✅ Xray Auth Token: " + token);
        return token;
    }
}


public static void updateDescriptionWithLinks(String issueKey, String tcName, String product, String errorMessage, List<String> screenshotLinks) throws Exception {
    String url = jiraBaseUrl() + "rest/api/3/issue/" + issueKey;
    HttpPut put = new HttpPut(url);
    put.setHeader("Authorization", getAuthHeader());
    put.setHeader("Content-Type", "application/json");

    JSONArray contentArray = new JSONArray()
        .put(new JSONObject()
            .put("type", "paragraph")
            .put("content", new JSONArray()
                .put(new JSONObject().put("type", "text").put("text", "Test Case: " + tcName))
                .put(new JSONObject().put("type", "text").put("text", "\nProduct: " + product))
                .put(new JSONObject().put("type", "text").put("text", "\nError: " + errorMessage))
            )
        );

    for (String link : screenshotLinks) {
        contentArray.put(new JSONObject()
            .put("type", "paragraph")
            .put("content", new JSONArray()
                .put(new JSONObject()
                    .put("type", "text")
                    .put("text", "View Screenshot")
                    .put("marks", new JSONArray()
                        .put(new JSONObject()
                            .put("type", "link")
                            .put("attrs", new JSONObject().put("href", link))
                        )
                    )
                )
            )
        );
    }

    JSONObject adfDescription = new JSONObject()
        .put("type", "doc")
        .put("version", 1)
        .put("content", contentArray);

    JSONObject payload = new JSONObject()
        .put("fields", new JSONObject().put("description", adfDescription));

    put.setEntity(new StringEntity(payload.toString(), ContentType.APPLICATION_JSON));

    try (CloseableHttpResponse response = client.execute(put)) {
        System.out.println("✅ Jira description updated with screenshot links for issue: " + issueKey);
    }
}


//util.JiraIntegration.java



//...

/**
* Create a formal Jira issue-to-issue link (e.g., "Relates") between two issues.
* @param inwardIssueKey  typically the BUG (e.g., PVII-20287)
* @param outwardIssueKey typically the Test Execution (e.g., PVII-7860)
* @param linkTypeName    link type name as configured in Jira (e.g., "Relates", "Blocks")
*/
public static void linkIssues(String inwardIssueKey, String outwardIssueKey, String linkTypeName) throws Exception {
 String url = jiraBaseUrl() + "rest/api/3/issueLink";
 HttpPost post = new HttpPost(url);
 post.setHeader("Authorization", getAuthHeader());
 post.setHeader("Content-Type", "application/json");

 JSONObject payload = new JSONObject()
     .put("type", new JSONObject().put("name", linkTypeName))     // e.g., "Relates"
     .put("inwardIssue", new JSONObject().put("key", inwardIssueKey))   // BUG
     .put("outwardIssue", new JSONObject().put("key", outwardIssueKey)); // TE

 post.setEntity(new StringEntity(payload.toString(), ContentType.APPLICATION_JSON));
 try (CloseableHttpResponse resp = client.execute(post)) {
     handleJsonResponse(resp, "issueLink");
     System.out.println("✅ Linked " + inwardIssueKey + " ↔ " + outwardIssueKey + " (" + linkTypeName + ")");
 }
}

    
}