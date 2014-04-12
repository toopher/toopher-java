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
    public void testGetPairUri() {
        ToopherIframe.setDateOverride(TEST_DATE);
        ToopherIframe.setNonceOverride(OAUTH_NONCE);
        Map<String, String> params = nvp2map(URLEncodedUtils.parse(iframeApi.pairUri("jdoe", "jdoe@example.com", REQUEST_TTL), Charset.forName("UTF-8")));
        assertEquals("UGlgBEUF6UZEhYPxevJeagqy6D4=", params.get("oauth_signature"));
    }

    @Test
    public void testGetAuthUri() {
        ToopherIframe.setDateOverride(TEST_DATE);
        ToopherIframe.setNonceOverride(OAUTH_NONCE);
        Map<String, String> params = nvp2map(URLEncodedUtils.parse(iframeApi.authUri("jdoe", "jdoe@example.com", "Log In", true, false, REQUEST_TOKEN, null, REQUEST_TTL), Charset.forName("UTF-8")));
        assertEquals("bpgdxhHLDwpYsbru+nz2p9pFlr4=", params.get("oauth_signature"));
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
            assertNotNull(iframeApi.validate(data, REQUEST_TOKEN, 5));
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
            iframeApi.validate(data, REQUEST_TOKEN, 5);
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
            iframeApi.validate(data, REQUEST_TOKEN, 5);
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
            iframeApi.validate(data, REQUEST_TOKEN, 5);
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
            iframeApi.validate(data, REQUEST_TOKEN, 5);
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
            iframeApi.validate(data, REQUEST_TOKEN, 5);
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
            iframeApi.validate(data, REQUEST_TOKEN, 5);
            fail();
        } catch (ToopherIframe.SignatureValidationError e) {
            assertTrue(e.getMessage().contains("Missing required keys: session_token"));
        }
    }


}
