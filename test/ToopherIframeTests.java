import com.toopher.ToopherIframe;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import junit.framework.*;

public class ToopherIframeTests extends TestCase {
    static private final String TOOPHER_CONSUMER_KEY = "abcdefg";
    static private final String TOOPHER_CONSUMER_SECRET = "hijklmnop";
    static private final String SESSION_TOKEN = "s9s7vsb";
    static private final Date TEST_DATE = new Date(1000000);

    private ToopherIframe iframeApi;

    public void setUp() {
        this.iframeApi = new ToopherIframe(TOOPHER_CONSUMER_KEY, TOOPHER_CONSUMER_SECRET);
    }

    public void testValidateGoodSignatureIsSuccessful(){
        Map<String, String> data = new HashMap<String, String>();
        data.put("foo", "bar");
        data.put("timestamp", String.valueOf(TEST_DATE.getTime() / 1000));
        data.put("session_token", SESSION_TOKEN);
        data.put("toopher_sig", "6d2c7GlQssGmeYYGpcf+V/kirOI=");

        ToopherIframe.setDateOverride(TEST_DATE);

        try {
            assertNotNull(iframeApi.validate(data, SESSION_TOKEN, 5));
        } catch (ToopherIframe.SignatureValidationError e) {
            fail();
        }
    }

    public void testValidateBadSignatureFails(){
        Map<String, String> data = new HashMap<String, String>();
        data.put("foo", "bar");
        data.put("timestamp", String.valueOf(TEST_DATE.getTime() / 1000));
        data.put("session_token", SESSION_TOKEN);
        data.put("toopher_sig", "invalid");

        ToopherIframe.setDateOverride(TEST_DATE);
        try {
            iframeApi.validate(data, SESSION_TOKEN, 5);
            fail();
        } catch (ToopherIframe.SignatureValidationError e) {
            assertTrue(e.getMessage().contains("Computed signature does not match"));
        }
    }

    public void testValidateExpiredSignatureFails() {
        Map<String, String> data = new HashMap<String, String>();
        data.put("foo", "bar");
        data.put("timestamp", String.valueOf(TEST_DATE.getTime() / 1000));
        data.put("session_token", SESSION_TOKEN);
        data.put("toopher_sig", "6d2c7GlQssGmeYYGpcf+V/kirOI=");

        // set ToopherIframe reference clock 6 seconds ahead
        ToopherIframe.setDateOverride(new Date(TEST_DATE.getTime() + (1000 * 6)));

        try {
            iframeApi.validate(data, SESSION_TOKEN, 5);
            fail();
        } catch (ToopherIframe.SignatureValidationError e) {
            assertTrue(e.getMessage().contains("TTL Expired"));
        }
    }

    public void testValidateMissingTimestampFails() {
        Map<String, String> data = new HashMap<String, String>();
        data.put("foo", "bar");
        data.put("session_token", SESSION_TOKEN);
        data.put("toopher_sig", "6d2c7GlQssGmeYYGpcf+V/kirOI=");

        ToopherIframe.setDateOverride(TEST_DATE);

        try {
            iframeApi.validate(data, SESSION_TOKEN, 5);
            fail();
        } catch (ToopherIframe.SignatureValidationError e) {
            assertTrue(e.getMessage().contains("Missing required keys: timestamp"));
        }
    }

    public void testValidateMissingSignatureFails() {
        Map<String, String> data = new HashMap<String, String>();
        data.put("foo", "bar");
        data.put("session_token", SESSION_TOKEN);
        data.put("timestamp", String.valueOf(TEST_DATE.getTime() / 1000));

        ToopherIframe.setDateOverride(TEST_DATE);
        try {
            iframeApi.validate(data, SESSION_TOKEN, 5);
            fail();
        } catch (ToopherIframe.SignatureValidationError e) {
            assertTrue(e.getMessage().contains("Missing required keys: toopher_sig"));
        }
    }

    public void testInvalidSessionTokenFails() {
        Map<String, String> data = new HashMap<String, String>();
        data.put("foo", "bar");
        data.put("timestamp", String.valueOf(TEST_DATE.getTime() / 1000));
        data.put("session_token", "invalid token");
        data.put("toopher_sig", "6d2c7GlQssGmeYYGpcf+V/kirOI=");

        ToopherIframe.setDateOverride(TEST_DATE);
        try {
            iframeApi.validate(data, SESSION_TOKEN, 5);
            fail();
        } catch (ToopherIframe.SignatureValidationError e) {
            assertTrue(e.getMessage().contains("Session token does not match expected value"));
        }
}

    public void testMissingSessionTokenFails() {
        Map<String, String> data = new HashMap<String, String>();
        data.put("foo", "bar");
        data.put("timestamp", String.valueOf(TEST_DATE.getTime() / 1000));
        data.put("toopher_sig", "6d2c7GlQssGmeYYGpcf+V/kirOI=");

        ToopherIframe.setDateOverride(TEST_DATE);
        try {
            iframeApi.validate(data, SESSION_TOKEN, 5);
            fail();
        } catch (ToopherIframe.SignatureValidationError e) {
            assertTrue(e.getMessage().contains("Missing required keys: session_token"));
        }
    }


}
