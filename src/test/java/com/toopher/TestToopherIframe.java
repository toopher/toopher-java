package com.toopher;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.junit.*;
import org.apache.http.client.utils.URLEncodedUtils;

import static org.junit.Assert.*;

public class TestToopherIframe {
    private static final String DEFAULT_BASE_URL = "https://api.toopher.test/v1/";
    private static final String TOOPHER_CONSUMER_KEY = "abcdefg";
    private static final String TOOPHER_CONSUMER_SECRET = "hijklmnop";
    private static final String REQUEST_TOKEN = "s9s7vsb";
    private static final long REQUEST_TTL = 100L;
    private static final String OAUTH_NONCE = "12345678";
    private static final Date TEST_DATE = new Date(1000000);

    private ToopherIframe iframeApi;

    static Map<String, String> nvp2map(List<NameValuePair> lnvp) {
        HashMap<String, String> result = new HashMap<String, String>();
        for (NameValuePair nvp : lnvp) {
            result.put(nvp.getName(), nvp.getValue());
        }
        return result;
    }

    private Map<String, String> getExtrasForUrl() {
        Map<String, String> extras = new HashMap<String, String>();
        extras.put("allow_inline_pairing", "false");
        extras.put("automation_allowed", "false");
        extras.put("challenge_required", "true");
        extras.put("ttl", Long.toString(REQUEST_TTL));
        return extras;
    }

    private Map<String, String[]> getPostbackData() {
        Map<String, String[]> data = new HashMap<String, String[]>();
        data.put("foo", new String[]{"bar"});
        data.put("timestamp", new String[]{String.valueOf(TEST_DATE.getTime() / 1000)});
        data.put("session_token", new String[]{REQUEST_TOKEN});
        data.put("toopher_sig", new String[]{"6d2c7GlQssGmeYYGpcf+V/kirOI="});
        return data;
    }

    @Before
    public void setUp() {
        this.iframeApi = new ToopherIframe(TOOPHER_CONSUMER_KEY, TOOPHER_CONSUMER_SECRET, DEFAULT_BASE_URL);
        ToopherIframe.setDateOverride(TEST_DATE);
    }

    @Test
    public void testCreateToopherIframe() {
        ToopherIframe iframe = new ToopherIframe(TOOPHER_CONSUMER_KEY, TOOPHER_CONSUMER_SECRET);
        assertTrue(iframe instanceof ToopherIframe);
    }

    @Test
    public void testGetDateNoOverride() {
        iframeApi.setDateOverride(null);
        assertEquals(new Date(), iframeApi.getDate());
    }

    @Test
    public void testDateOverride() {
        assertEquals(TEST_DATE, ToopherIframe.getDate());
    }

    @Test
    public void testNonceOverride() {
        ToopherIframe.setNonceOverride(OAUTH_NONCE);
        assertEquals(OAUTH_NONCE, ToopherIframe.getNonce());
    }

    @Test
    public void testGetAuthenticationUrlOnlyUsername() {
        ToopherIframe.setNonceOverride(OAUTH_NONCE);
        String expectedUrl = "https://api.toopher.test/v1/web/authenticate?v=2&username=jdoe&action_name=Log+In&reset_email=None&session_token=None&requester_metadata=None&expires=1300&oauth_consumer_key=abcdefg&oauth_nonce=12345678&oauth_signature=Udj%2BxFeLQgSKzKyntCIOq5mODSs%3D&oauth_signature_method=HMAC-SHA1&oauth_timestamp=1000&oauth_version=1.0";
        assertEquals(expectedUrl, iframeApi.getAuthenticationUrl("jdoe"));
    }
    @Test
    public void testGetAuthenticationUrlWithExtras() {
        ToopherIframe.setNonceOverride(OAUTH_NONCE);
        Map<String, String> params = nvp2map(URLEncodedUtils.parse(iframeApi.getAuthenticationUrl("jdoe", getExtrasForUrl()), Charset.forName("UTF-8")));
        assertEquals("jesSqRK9OisqeBNWNu69s8tCyRY=", params.get("oauth_signature"));
    }

    @Test
    public void testGetAuthenticationUrlWithEmailTokenActionMetadata() {
        Map<String, String> extras = new HashMap<String, String>();
        extras.put("resetEmail", "jdoe@example.com");
        extras.put("requestToken", REQUEST_TOKEN);
        extras.put("actionName", "it is a test");
        extras.put("requesterMetadata", "metadata");
        ToopherIframe.setNonceOverride(OAUTH_NONCE);
        Map<String, String> params = nvp2map(URLEncodedUtils.parse(iframeApi.getAuthenticationUrl("jdoe", extras), Charset.forName("UTF-8")));
        assertEquals("2TydgMnUwWoiwfpljKpSaFg0Luo=", params.get("oauth_signature"));
    }

    @Test
    public void testGetAuthenticationUrlWithEmailTokenActionMetadataAndExtras() {
        Map<String, String> extras = getExtrasForUrl();
        extras.put("resetEmail", "jdoe@example.com");
        extras.put("requestToken", REQUEST_TOKEN);
        extras.put("actionName", "it is a test");
        extras.put("requesterMetadata", "metadata");
        ToopherIframe.setNonceOverride(OAUTH_NONCE);
        Map<String, String> params = nvp2map(URLEncodedUtils.parse(iframeApi.getAuthenticationUrl("jdoe", extras), Charset.forName("UTF-8")));
        assertEquals("61dqeQNPFxNy8PyEFB9e5UfgN8s=", params.get("oauth_signature"));
    }

    @Test
    public void testGetUserManagementUrl() {
        ToopherIframe.setNonceOverride(OAUTH_NONCE);
        String expected = "https://api.toopher.test/v1/web/manage_user?v=2&username=jdoe&reset_email=jdoe%40example.com&expires=1300&oauth_consumer_key=abcdefg&oauth_nonce=12345678&oauth_signature=NjwH5yWPE2CCJL8v%2FMNknL%2BeTpE%3D&oauth_signature_method=HMAC-SHA1&oauth_timestamp=1000&oauth_version=1.0";
        String userManagementUrl = iframeApi.getUserManagementUrl("jdoe", "jdoe@example.com");
        assertEquals(expected, userManagementUrl);
    }

    @Test
    public void testGetUserManagementUrlWithExtras() {
        ToopherIframe.setNonceOverride(OAUTH_NONCE);
        Map<String, String> extras = new HashMap<String, String>();
        extras.put("ttl", Long.toString(REQUEST_TTL));
        String expected = "https://api.toopher.test/v1/web/manage_user?v=2&username=jdoe&reset_email=jdoe%40example.com&expires=1100&oauth_consumer_key=abcdefg&oauth_nonce=12345678&oauth_signature=sV8qoKnxJ3fxfP6AHNa0eNFxzJs%3D&oauth_signature_method=HMAC-SHA1&oauth_timestamp=1000&oauth_version=1.0";
        String userManagementUrl = iframeApi.getUserManagementUrl("jdoe", "jdoe@example.com", extras);
        assertEquals(expected, userManagementUrl);
    }

    @Test
    public void getUserManagementUrlOnlyUsername() {
        ToopherIframe.setNonceOverride(OAUTH_NONCE);
        String expectedUrl = "https://api.toopher.test/v1/web/manage_user?v=2&username=jdoe&reset_email=None&expires=1300&oauth_consumer_key=abcdefg&oauth_nonce=12345678&oauth_signature=yX3zPLJeLnc5Scdrz0juB2FO2hQ%3D&oauth_signature_method=HMAC-SHA1&oauth_timestamp=1000&oauth_version=1.0";
        assertEquals(expectedUrl, iframeApi.getUserManagementUrl("jdoe"));
    }

    @Test
    public void getUserManagementUrlUsernameWithExtras() {
        ToopherIframe.setNonceOverride(OAUTH_NONCE);
        Map<String, String> extras = new HashMap<String, String>();
        extras.put("ttl", Long.toString(REQUEST_TTL));
        String expectedUrl = "https://api.toopher.test/v1/web/manage_user?v=2&username=jdoe&reset_email=None&expires=1100&oauth_consumer_key=abcdefg&oauth_nonce=12345678&oauth_signature=zaKqXft5sCDx4SGr%2BWI9MMlefS8%3D&oauth_signature_method=HMAC-SHA1&oauth_timestamp=1000&oauth_version=1.0";
        String userManagementUrl = iframeApi.getUserManagementUrl("jdoe", extras);
        assertEquals(expectedUrl, userManagementUrl);
    }

    @Test
    public void testValidateGoodSignatureIsSuccessful() throws ToopherIframe.SignatureValidationError {
        assertNotNull(iframeApi.processPostback(getPostbackData(), REQUEST_TOKEN, 5));
    }

    @Test
    public void testValidateGoodSignatureNoTTLIsSuccessful() throws ToopherIframe.SignatureValidationError {
        assertNotNull(iframeApi.validatePostback(getPostbackData(), REQUEST_TOKEN));
    }

    @Test
    public void testValidateGoodSignatureNoRequestTokenIsSuccessful() throws ToopherIframe.SignatureValidationError {
        assertNotNull(iframeApi.validatePostback(getPostbackData()));
    }

    @Test
    public void testValidateBadSignatureFails() {
        Map<String, String[]> data = getPostbackData();
        data.put("toopher_sig", new String[]{"invalid"});

        try {
            iframeApi.processPostback(data, REQUEST_TOKEN, 5);
            fail();
        } catch (ToopherIframe.SignatureValidationError e) {
            assertTrue(e.getMessage().contains("Computed signature does not match"));
        }
    }

    @Test
    public void testValidateExpiredSignatureFails() {
        // set ToopherIframe reference clock 6 seconds ahead
        ToopherIframe.setDateOverride(new Date(TEST_DATE.getTime() + (1000 * 6)));
        try {
            iframeApi.processPostback(getPostbackData(), REQUEST_TOKEN, 5);
            fail();
        } catch (ToopherIframe.SignatureValidationError e) {
            assertTrue(e.getMessage().contains("TTL Expired"));
        }
    }

    @Test
    public void testValidateMissingTimestampFails() {
        Map<String, String[]> data = getPostbackData();
        data.remove("timestamp");

        try {
            iframeApi.processPostback(data, REQUEST_TOKEN, 5);
            fail();
        } catch (ToopherIframe.SignatureValidationError e) {
            assertTrue(e.getMessage().contains("Missing required keys: timestamp"));
        }
    }

    @Test
    public void testValidateMissingSignatureFails() {
        Map<String, String[]> data = getPostbackData();
        data.remove("toopher_sig");

        try {
            iframeApi.processPostback(data, REQUEST_TOKEN, 5);
            fail();
        } catch (ToopherIframe.SignatureValidationError e) {
            assertTrue(e.getMessage().contains("Missing required keys: toopher_sig"));
        }
    }

    @Test
    public void testInvalidSessionTokenFails() {
        Map<String, String[]> data = getPostbackData();
        data.put("session_token", new String[]{"invalid"});

        try {
            iframeApi.processPostback(data, REQUEST_TOKEN, 5);
            fail();
        } catch (ToopherIframe.SignatureValidationError e) {
            assertTrue(e.getMessage().contains("Session token does not match expected value"));
        }
    }

    @Test
    public void testMissingSessionTokenFails() {
        Map<String, String[]> data = getPostbackData();
        data.remove("session_token");

        try {
            iframeApi.processPostback(data, REQUEST_TOKEN, 5);
            fail();
        } catch (ToopherIframe.SignatureValidationError e) {
            assertTrue(e.getMessage().contains("Missing required keys: session_token"));
        }
    }
}
