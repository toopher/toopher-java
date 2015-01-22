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
    static private final String TOOPHER_CONSUMER_KEY = "abcdefg";
    static private final String TOOPHER_CONSUMER_SECRET = "hijklmnop";
    static private final String REQUEST_TOKEN = "s9s7vsb";
    static private final long REQUEST_TTL = 100L;
    static private final String OAUTH_NONCE = "12345678";
    static private final Date TEST_DATE = new Date(1000000);

    private ToopherIframe iframeApi;

    static Map<String, String> nvp2map(List<NameValuePair> lnvp) {
        HashMap<String,String> result = new HashMap<String, String>();
        for (NameValuePair nvp : lnvp) {
            result.put(nvp.getName(), nvp.getValue());
        }
        return result;
    }
    @Before
    public void setUp() {
        this.iframeApi = new ToopherIframe(TOOPHER_CONSUMER_KEY, TOOPHER_CONSUMER_SECRET, "https://api.toopher.test/v1/");
    }

    @Test
    public void testGetAuthenticationUrl(){
        ToopherIframe.setDateOverride(TEST_DATE);
        ToopherIframe.setNonceOverride(OAUTH_NONCE);
        String expected = "https://api.toopher.test/v1/web/authenticate?v=2&username=jdoe&action_name=Log+In&reset_email=jdoe%40example.com&session_token=s9s7vsb&requester_metadata=None&expires=1010&oauth_consumer_key=abcdefg&oauth_nonce=12345678&oauth_signature=1qBrA0wErtYCrzqQLwYqUDoynXs%3D&oauth_signature_method=HMAC-SHA1&oauth_timestamp=1000&oauth_version=1.0";
        assertEquals(expected, iframeApi.getAuthenticationUrl("jdoe", "jdoe@example.com", REQUEST_TOKEN));
    }

    @Test
    public void testGetAuthenticationUrlWithExtras() {
        ToopherIframe.setDateOverride(TEST_DATE);
        ToopherIframe.setNonceOverride(OAUTH_NONCE);
        String expected = "https://api.toopher.test/v1/web/authenticate?v=2&username=jdoe&action_name=Log+In&reset_email=jdoe%40example.com&session_token=s9s7vsb&requester_metadata=None&expires=1100&automation_allowed=false&challenge_required=true&allow_inline_pairing=false&oauth_consumer_key=abcdefg&oauth_nonce=12345678&oauth_signature=TUgywVd77mWpffzdwjjQJ7ooYPM%3D&oauth_signature_method=HMAC-SHA1&oauth_timestamp=1000&oauth_version=1.0";
        Map<String, String> extras = new HashMap<String, String>();
        extras.put("allow_inline_pairing", "false");
        extras.put("automation_allowed", "false");
        extras.put("challenge_required", "true");
        extras.put("ttl", Long.toString(REQUEST_TTL));
        assertEquals(expected, iframeApi.getAuthenticationUrl("jdoe", "jdoe@example.com", REQUEST_TOKEN,extras));

    }

    @Test
    public void testGetAuthenticationUrlWithOptionalArgsAndExtras() {
        ToopherIframe.setDateOverride(TEST_DATE);
        ToopherIframe.setNonceOverride(OAUTH_NONCE);
        String expected = "https://api.toopher.test/v1/web/authenticate?v=2&username=jdoe&action_name=it+is+a+test&reset_email=jdoe%40example.com&session_token=s9s7vsb&requester_metadata=metadata&expires=1100&automation_allowed=false&challenge_required=true&allow_inline_pairing=false&oauth_consumer_key=abcdefg&oauth_nonce=12345678&oauth_signature=61dqeQNPFxNy8PyEFB9e5UfgN8s%3D&oauth_signature_method=HMAC-SHA1&oauth_timestamp=1000&oauth_version=1.0";
        Map<String, String> extras = new HashMap<String, String>();
        extras.put("allow_inline_pairing", "false");
        extras.put("automation_allowed", "false");
        extras.put("challenge_required", "true");
        extras.put("ttl", Long.toString(REQUEST_TTL));
        assertEquals(expected, iframeApi.getAuthenticationUrl("jdoe", "jdoe@example.com", REQUEST_TOKEN, "it is a test", "metadata", extras));
    }

//    Do we prefer this type of test or the one above?
//    @Test
//    public void testGetAuthenticationUrlWithOptionalArgsAndExtras() {
//        ToopherIframe.setDateOverride(TEST_DATE);
//        ToopherIframe.setNonceOverride(OAUTH_NONCE);
//        Map<String, String> extras = new HashMap<String, String>();
//        extras.put("allowInlinePairing", "false");
//        extras.put("automationAllowed", "false");
//        extras.put("challengeRequired", "true");
//        extras.put("ttl", Long.toString(REQUEST_TTL));
//        Map<String, String> params = nvp2map(URLEncodedUtils.parse(iframeApi.getAuthenticationUrl("jdoe", "jdoe@example.com", REQUEST_TOKEN, "it is a test", "metadata", extras), Charset.forName("UTF-8")));
//        assertEquals("hKogqI/gjKXpYIH+jNDhRSi22b4=", params.get("oauth_signature"));
//    }

    @Test
    public void testGetUserManagementUrl() {
        ToopherIframe.setDateOverride(TEST_DATE);
        ToopherIframe.setNonceOverride(OAUTH_NONCE);
        String expected = "https://api.toopher.test/v1/web/manage_user?v=2&username=jdoe&reset_email=jdoe%40example.com&expires=1010&oauth_consumer_key=abcdefg&oauth_nonce=12345678&oauth_signature=aq1kavm7iBOXNPndjKLnrI5rEXU%3D&oauth_signature_method=HMAC-SHA1&oauth_timestamp=1000&oauth_version=1.0";
        assertEquals(expected, iframeApi.getUserManagementUrl("jdoe", "jdoe@example.com"));
    }

    @Test
    public void testGetUserManagementUrlWithExtras() {
        ToopherIframe.setDateOverride(TEST_DATE);
        ToopherIframe.setNonceOverride(OAUTH_NONCE);
        Map<String, String> extras = new HashMap<String, String>();
        extras.put("ttl", Long.toString(REQUEST_TTL));
        String expected = "https://api.toopher.test/v1/web/manage_user?v=2&username=jdoe&reset_email=jdoe%40example.com&expires=1100&oauth_consumer_key=abcdefg&oauth_nonce=12345678&oauth_signature=sV8qoKnxJ3fxfP6AHNa0eNFxzJs%3D&oauth_signature_method=HMAC-SHA1&oauth_timestamp=1000&oauth_version=1.0";
        assertEquals(expected, iframeApi.getUserManagementUrl("jdoe", "jdoe@example.com", extras));
    }

    @Test
    public void testValidateGoodSignatureIsSuccessful(){
        Map<String, String[]> data = new HashMap<String, String[]>();
        data.put("foo", new String[] {"bar"});
        data.put("timestamp", new String[] { String.valueOf(TEST_DATE.getTime() / 1000)});
        data.put("session_token", new String[] { REQUEST_TOKEN });
        data.put("toopher_sig", new String[] { "6d2c7GlQssGmeYYGpcf+V/kirOI=" });

        ToopherIframe.setDateOverride(TEST_DATE);

        try {
            assertNotNull(iframeApi.validatePostback(data, REQUEST_TOKEN, 5));
        } catch (ToopherIframe.SignatureValidationError e) {
            fail();
        }
    }

    @Test
    public void testValidateBadSignatureFails(){
        Map<String, String[]> data = new HashMap<String, String[]>();
        data.put("foo", new String[] { "bar" });
        data.put("timestamp", new String[] { String.valueOf(TEST_DATE.getTime() / 1000) });
        data.put("session_token", new String[] { REQUEST_TOKEN });
        data.put("toopher_sig", new String[] { "invalid" });

        ToopherIframe.setDateOverride(TEST_DATE);
        try {
            iframeApi.validatePostback(data, REQUEST_TOKEN, 5);
            fail();
        } catch (ToopherIframe.SignatureValidationError e) {
            assertTrue(e.getMessage().contains("Computed signature does not match"));
        }
    }

    @Test
    public void testValidateExpiredSignatureFails() {
        Map<String, String[]> data = new HashMap<String, String[]>();
        data.put("foo", new String[] { "bar" });
        data.put("timestamp", new String[] { String.valueOf(TEST_DATE.getTime() / 1000) });
        data.put("session_token", new String[] { REQUEST_TOKEN });
        data.put("toopher_sig", new String[] { "6d2c7GlQssGmeYYGpcf+V/kirOI=" });

        // set ToopherIframe reference clock 6 seconds ahead
        ToopherIframe.setDateOverride(new Date(TEST_DATE.getTime() + (1000 * 6)));

        try {
            iframeApi.validatePostback(data, REQUEST_TOKEN, 5);
            fail();
        } catch (ToopherIframe.SignatureValidationError e) {
            assertTrue(e.getMessage().contains("TTL Expired"));
        }
    }

    @Test
    public void testValidateMissingTimestampFails() {
        Map<String, String[]> data = new HashMap<String, String[]>();
        data.put("foo", new String[] { "bar" });
        data.put("session_token", new String[] { REQUEST_TOKEN });
        data.put("toopher_sig", new String[] { "6d2c7GlQssGmeYYGpcf+V/kirOI=" });

        ToopherIframe.setDateOverride(TEST_DATE);

        try {
            iframeApi.validatePostback(data, REQUEST_TOKEN, 5);
            fail();
        } catch (ToopherIframe.SignatureValidationError e) {
            assertTrue(e.getMessage().contains("Missing required keys: timestamp"));
        }
    }

    @Test
    public void testValidateMissingSignatureFails() {
        Map<String, String[]> data = new HashMap<String, String[]>();
        data.put("foo", new String[] { "bar" });
        data.put("session_token", new String[] { REQUEST_TOKEN });
        data.put("timestamp", new String[] { String.valueOf(TEST_DATE.getTime() / 1000) });

        ToopherIframe.setDateOverride(TEST_DATE);
        try {
            iframeApi.validatePostback(data, REQUEST_TOKEN, 5);
            fail();
        } catch (ToopherIframe.SignatureValidationError e) {
            assertTrue(e.getMessage().contains("Missing required keys: toopher_sig"));
        }
    }

    @Test
    public void testInvalidSessionTokenFails() {
        Map<String, String[]> data = new HashMap<String, String[]>();
        data.put("foo", new String[] { "bar" });
        data.put("timestamp", new String[] { String.valueOf(TEST_DATE.getTime() / 1000) });
        data.put("session_token", new String[] { "invalid token" });
        data.put("toopher_sig", new String[] { "6d2c7GlQssGmeYYGpcf+V/kirOI=" });

        ToopherIframe.setDateOverride(TEST_DATE);
        try {
            iframeApi.validatePostback(data, REQUEST_TOKEN, 5);
            fail();
        } catch (ToopherIframe.SignatureValidationError e) {
            assertTrue(e.getMessage().contains("Session token does not match expected value"));
        }
    }

    @Test
    public void testMissingSessionTokenFails() {
        Map<String, String[]> data = new HashMap<String, String[]>();
        data.put("foo", new String[] { "bar" });
        data.put("timestamp", new String[] { String.valueOf(TEST_DATE.getTime() / 1000) });
        data.put("toopher_sig", new String[] { "6d2c7GlQssGmeYYGpcf+V/kirOI=" });

        ToopherIframe.setDateOverride(TEST_DATE);
        try {
            iframeApi.validatePostback(data, REQUEST_TOKEN, 5);
            fail();
        } catch (ToopherIframe.SignatureValidationError e) {
            assertTrue(e.getMessage().contains("Missing required keys: session_token"));
        }
    }
}
