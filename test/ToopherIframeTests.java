import com.toopher.ToopherIframe;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import junit.framework.*;

public class ToopherIframeTests extends TestCase {
    static private final String TOOPHER_CONSUMER_KEY = "abcdefg";
    static private final String TOOPHER_CONSUMER_SECRET = "hijklmnop";
    static private final Date TEST_DATE = new Date(1000000);

    private ToopherIframe iframeApi;

    public void setUp() {
        this.iframeApi = new ToopherIframe(TOOPHER_CONSUMER_KEY, TOOPHER_CONSUMER_SECRET);
    }

    public void testValidateGoodSignatureIsSuccessful() {
        Map<String, String> data = new HashMap<String, String>();
        data.put("foo", "bar");
        data.put("timestamp", String.valueOf(TEST_DATE.getTime() / 1000));
        data.put("toopher_sig", "eSD7w5Uv0B8Z+1YVpBwUYFlFbMI=");

        ToopherIframe.setDateOverride(TEST_DATE);

        assertNotNull(iframeApi.validate(data, 5));
    }

    public void testValidateBadSignatureReturnsNull() {
        Map<String, String> data = new HashMap<String, String>();
        data.put("foo", "bar");
        data.put("timestamp", String.valueOf(TEST_DATE.getTime() / 1000));
        data.put("toopher_sig", "invalid");

        ToopherIframe.setDateOverride(TEST_DATE);

        assertNull(iframeApi.validate(data, 5));
    }

    public void testValidateExpiredSignatureReturnsNull() {
        Map<String, String> data = new HashMap<String, String>();
        data.put("foo", "bar");
        data.put("timestamp", String.valueOf(TEST_DATE.getTime() / 1000));
        data.put("toopher_sig", "eSD7w5Uv0B8Z+1YVpBwUYFlFbMI=");

        // set ToopherIframe reference clock 6 seconds ahead
        ToopherIframe.setDateOverride(new Date(TEST_DATE.getTime() + (1000 * 6)));

        assertNull(iframeApi.validate(data, 5));
    }

    public void testValidateMissingTimestampReturnsNull() {
        Map<String, String> data = new HashMap<String, String>();
        data.put("foo", "bar");
        data.put("toopher_sig", "eSD7w5Uv0B8Z+1YVpBwUYFlFbMI=");

        ToopherIframe.setDateOverride(TEST_DATE);

        assertNull(iframeApi.validate(data, 5));
    }

    public void testValidateMissingSignatureReturnsNull() {
        Map<String, String> data = new HashMap<String, String>();
        data.put("foo", "bar");
        data.put("timestamp", String.valueOf(TEST_DATE.getTime() / 1000));

        ToopherIframe.setDateOverride(TEST_DATE);

        assertNull(iframeApi.validate(data, 5));
    }
}
