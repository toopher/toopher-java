package com.toopher;

import java.nio.charset.Charset;
import java.util.*;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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

    private Map<String, String> getAuthenticationRequestPostbackData() {
        Map<String, String> data = new HashMap<String, String>();
        data.put("resource_type", "authentication_request");
        data.put("id", "1");
        data.put("pending", "false");
        data.put("granted", "true");
        data.put("automated", "false");
        data.put("reason", "it is a test");
        data.put("reason_code", "100");
        data.put("terminal_id", "1");
        data.put("terminal_name", "terminal name");
        data.put("terminal_requester_specified_id", "requester specified id");
        data.put("pairing_user_id", "1");
        data.put("user_name", "user name");
        data.put("user_toopher_authentication_enabled", "true");
        data.put("action_id", "1");
        data.put("action_name", "action name");
        data.put("timestamp", String.valueOf(TEST_DATE.getTime() / 1000));
        data.put("session_token", REQUEST_TOKEN);
        data.put("toopher_sig", "s+fYUtChrNMjES5Xa+755H7BQKE=");
        return data;
    }

    private Map<String, String> getPairingPostbackData() {
        Map<String, String> data = new HashMap<String, String>();
        data.put("resource_type", "pairing");
        data.put("id", "1");
        data.put("enabled", "true");
        data.put("pending", "false");
        data.put("pairing_user_id", "1");
        data.put("user_name", "user name");
        data.put("user_toopher_authentication_enabled", "true");
        data.put("timestamp", String.valueOf(TEST_DATE.getTime() / 1000));
        data.put("session_token", REQUEST_TOKEN);
        data.put("toopher_sig", "ucwKhkPpN4VxNbx3dMypWzi4tBg=");
        return data;
    }

    private Map<String, String> getUserPostbackData() {
        Map<String, String> data = new HashMap<String, String>();
        data.put("resource_type", "requester_user");
        data.put("id", "1");
        data.put("name", "user name");
        data.put("toopher_authentication_enabled", "true");
        data.put("toopher_sig", "RszgG9QE1rF9t7DVTGg+1I25yHM=");
        data.put("timestamp", String.valueOf(TEST_DATE.getTime() / 1000));
        data.put("session_token", REQUEST_TOKEN);
        return data;
    }

    private String getUrlEncodedPostbackData(Map<String, String> postbackData) {
        TreeSet<String> sortedKeys = new TreeSet<String>(postbackData.keySet());
        List<NameValuePair> sortedData = new ArrayList<NameValuePair>(postbackData.size());
        for (String key: sortedKeys) {
            sortedData.add(new BasicNameValuePair(key, postbackData.get(key)));
        }
        return URLEncodedUtils.format(sortedData, "UTF-8");
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
        String expectedUrl = "https://api.toopher.test/v1/web/authenticate?username=jdoe&action_name=Log+In&reset_email=&session_token=&requester_metadata=&expires=1300&v=2&oauth_consumer_key=abcdefg&oauth_nonce=12345678&oauth_signature=NkaWUjEPRLwgsQMEJGsIQEpyRT4%3D&oauth_signature_method=HMAC-SHA1&oauth_timestamp=1000&oauth_version=1.0";
        assertEquals(expectedUrl, iframeApi.getAuthenticationUrl("jdoe"));
    }
    @Test
    public void testGetAuthenticationUrlWithExtras() {
        ToopherIframe.setNonceOverride(OAUTH_NONCE);
        Map<String, String> params = nvp2map(URLEncodedUtils.parse(iframeApi.getAuthenticationUrl("jdoe", getExtrasForUrl()), Charset.forName("UTF-8")));
        assertEquals("srO3zYEFEEU9od/w0ZjDZzyDUyI=", params.get("oauth_signature"));
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
    public void testGetUserManagementUrlWithEmail() {
        Map<String, String> extras = new HashMap<String, String>();
        extras.put("resetEmail", "jdoe@example.com");
        ToopherIframe.setNonceOverride(OAUTH_NONCE);
        String expected = "https://api.toopher.test/v1/web/manage_user?username=jdoe&reset_email=jdoe%40example.com&expires=1300&v=2&oauth_consumer_key=abcdefg&oauth_nonce=12345678&oauth_signature=NjwH5yWPE2CCJL8v%2FMNknL%2BeTpE%3D&oauth_signature_method=HMAC-SHA1&oauth_timestamp=1000&oauth_version=1.0";
        String userManagementUrl = iframeApi.getUserManagementUrl("jdoe", extras);
        assertEquals(expected, userManagementUrl);
    }

    @Test
    public void testGetUserManagementUrlWithEmailAndExtras() {
        ToopherIframe.setNonceOverride(OAUTH_NONCE);
        Map<String, String> extras = new HashMap<String, String>();
        extras.put("resetEmail", "jdoe@example.com");
        extras.put("ttl", Long.toString(REQUEST_TTL));
        String expected = "https://api.toopher.test/v1/web/manage_user?username=jdoe&reset_email=jdoe%40example.com&expires=1100&v=2&oauth_consumer_key=abcdefg&oauth_nonce=12345678&oauth_signature=sV8qoKnxJ3fxfP6AHNa0eNFxzJs%3D&oauth_signature_method=HMAC-SHA1&oauth_timestamp=1000&oauth_version=1.0";
        String userManagementUrl = iframeApi.getUserManagementUrl("jdoe", extras);
        assertEquals(expected, userManagementUrl);
    }

    @Test
    public void testGetUserManagementUrlOnlyUsername() {
        ToopherIframe.setNonceOverride(OAUTH_NONCE);
        String expectedUrl = "https://api.toopher.test/v1/web/manage_user?username=jdoe&reset_email=&expires=1300&v=2&oauth_consumer_key=abcdefg&oauth_nonce=12345678&oauth_signature=SA7CAUj%2B5QcGO%2BMmdPv9ubbaozk%3D&oauth_signature_method=HMAC-SHA1&oauth_timestamp=1000&oauth_version=1.0";
        assertEquals(expectedUrl, iframeApi.getUserManagementUrl("jdoe"));
    }

    @Test
    public void testGetUserManagementUrlWithExtras() {
        ToopherIframe.setNonceOverride(OAUTH_NONCE);
        Map<String, String> extras = new HashMap<String, String>();
        extras.put("ttl", Long.toString(REQUEST_TTL));
        String expectedUrl = "https://api.toopher.test/v1/web/manage_user?username=jdoe&reset_email=&expires=1100&v=2&oauth_consumer_key=abcdefg&oauth_nonce=12345678&oauth_signature=CtakenrFTqmVw%2BwPxvrgIM%2BDiwk%3D&oauth_signature_method=HMAC-SHA1&oauth_timestamp=1000&oauth_version=1.0";
        String userManagementUrl = iframeApi.getUserManagementUrl("jdoe", extras);
        assertEquals(expectedUrl, userManagementUrl);
    }

    @Test
    public void testProcessPostbackGoodSignatureReturnsAuthenticationRequest() throws ToopherIframe.SignatureValidationError, RequestError {
        Map<String, String> auth_data = getAuthenticationRequestPostbackData();
        AuthenticationRequest authenticationRequest = (AuthenticationRequest)iframeApi.processPostback(getUrlEncodedPostbackData(getAuthenticationRequestPostbackData()), REQUEST_TOKEN);
        assertTrue(authenticationRequest != null);
        assertEquals(auth_data.get("id"), authenticationRequest.id);
        assertFalse(authenticationRequest.pending);
        assertTrue(authenticationRequest.granted);
        assertFalse(authenticationRequest.automated);
        assertEquals(auth_data.get("reason"), authenticationRequest.reason);
        assertEquals(Integer.parseInt(auth_data.get("reason_code")), authenticationRequest.reasonCode);
        assertEquals(auth_data.get("terminal_id"), authenticationRequest.terminal.id);
        assertEquals(auth_data.get("terminal_name"), authenticationRequest.terminal.name);
        assertEquals(auth_data.get("terminal_requester_specified_id"), authenticationRequest.terminal.requesterSpecifiedId);
        assertEquals(auth_data.get("action_id"), authenticationRequest.action.id);
        assertEquals(auth_data.get("action_name"), authenticationRequest.action.name);
        assertEquals(auth_data.get("pairing_user_id"), authenticationRequest.user.id);
        assertEquals(auth_data.get("user_name"), authenticationRequest.user.name);
        assertTrue(authenticationRequest.user.toopherAuthenticationEnabled);
    }

    @Test
    public void testProcessPostbackGoodSignatureReturnsPairing() throws ToopherIframe.SignatureValidationError, RequestError {
        Map<String, String> pairing_data = getPairingPostbackData();
        Pairing pairing = (Pairing)iframeApi.processPostback(getUrlEncodedPostbackData(pairing_data), REQUEST_TOKEN);
        assertTrue(pairing != null);
        assertEquals(pairing_data.get("id"), pairing.id);
        assertTrue(pairing.enabled);
        assertFalse(pairing.pending);
        assertEquals(pairing_data.get("pairing_user_id"), pairing.user.id);
        assertEquals(pairing_data.get("user_name"), pairing.user.name);
        assertTrue(pairing.user.toopherAuthenticationEnabled);
    }

    @Test
    public void testProcessPostbackGoodSignatureReturnsUser() throws ToopherIframe.SignatureValidationError, RequestError {
        Map <String, String> user_data = getUserPostbackData();
        User user = (User)iframeApi.processPostback(getUrlEncodedPostbackData(user_data), REQUEST_TOKEN);
        assertTrue(user != null);
        assertEquals(user_data.get("id"), user.id);
        assertEquals(user_data.get("name"), user.name);
        assertTrue(user.toopherAuthenticationEnabled);
    }

    @Test
    public void testProcessPostbackGoodSignatureWithExtrasReturnsAuthenticationRequest() throws ToopherIframe.SignatureValidationError, RequestError {
        Map<String, String> extras = new HashMap<String, String>();
        extras.put("ttl", "100");
        Object authenticationRequest = iframeApi.processPostback(getUrlEncodedPostbackData(getAuthenticationRequestPostbackData()), REQUEST_TOKEN, extras);
        assertTrue(authenticationRequest instanceof AuthenticationRequest);
    }

    @Test
    public void testProcessPostbackGoodSignatureNoRequestTokenReturnsAuthenticationRequest() throws ToopherIframe.SignatureValidationError, RequestError {
        Object authenticationRequest = iframeApi.processPostback(getUrlEncodedPostbackData(getAuthenticationRequestPostbackData()));
        assertTrue(authenticationRequest instanceof AuthenticationRequest);
    }

    @Test
    public void testProcessPostbackBadSignatureFails() throws RequestError {
        Map<String, String> data = getAuthenticationRequestPostbackData();
        data.put("toopher_sig", "invalid");

        try {
            iframeApi.processPostback(getUrlEncodedPostbackData(data), REQUEST_TOKEN);
            fail("SignatureValidationError was not thrown for invalid signature");
        } catch (ToopherIframe.SignatureValidationError e) {
            assertTrue(e.getMessage().contains("Computed signature does not match"));
        }
    }

    @Test
    public void testProcessPostbackExpiredSignatureFails() throws RequestError {
        ToopherIframe.setDateOverride(new Date(2000000));
        try {
            iframeApi.processPostback(getUrlEncodedPostbackData(getAuthenticationRequestPostbackData()), REQUEST_TOKEN);
            fail("SignatureValidationError was not thrown for expired signature");
        } catch (ToopherIframe.SignatureValidationError e) {
            assertTrue(e.getMessage().contains("TTL Expired"));
        }
    }

    @Test
    public void testProcessPostbackMissingSignatureFails() throws RequestError {
        Map<String, String> data = getAuthenticationRequestPostbackData();
        data.remove("toopher_sig");
        try {
            iframeApi.processPostback(getUrlEncodedPostbackData(data), REQUEST_TOKEN);
            fail("SignatureValidationError was not thrown for missing signature");
        } catch (ToopherIframe.SignatureValidationError e) {
            assertTrue(e.getMessage().contains("Missing required keys: toopher_sig"));
        }
    }

    @Test
    public void testProcessPostbackMissingTimestampFails() throws RequestError {
        Map<String, String> data = getAuthenticationRequestPostbackData();
        data.remove("timestamp");
        try {
            iframeApi.processPostback(getUrlEncodedPostbackData(data), REQUEST_TOKEN);
            fail("SignatureValidationError was not thrown for missing timestamp");
        } catch (ToopherIframe.SignatureValidationError e) {
            assertTrue(e.getMessage().contains("Missing required keys: timestamp"));
        }
    }

    @Test
    public void testProcessPostbackMissingSessionTokenFails() throws RequestError {
        Map<String, String> data = getAuthenticationRequestPostbackData();
        data.remove("session_token");
        try {
            iframeApi.processPostback(getUrlEncodedPostbackData(data), REQUEST_TOKEN);
            fail("SignatureValidationError was not thrown for missing session token");
        } catch (ToopherIframe.SignatureValidationError e) {
            assertTrue(e.getMessage().contains("Missing required keys: session_token"));
        }
    }

    @Test
    public void testProcessPostbackInvalidSessionTokenFails() throws RequestError {
        Map<String, String> data = getAuthenticationRequestPostbackData();
        data.put("session_token", "invalid");
        try {
            iframeApi.processPostback(getUrlEncodedPostbackData(data), REQUEST_TOKEN);
            fail("SignatureValidationError was not thrown for invalid session token");
        } catch (ToopherIframe.SignatureValidationError e) {
            assertTrue(e.getMessage().contains("Session token does not match expected value"));
        }
    }

    @Test
    public void testProcessPostbackBadResourceTypeFails() throws ToopherIframe.SignatureValidationError, RequestError{
        Map<String, String> data = getAuthenticationRequestPostbackData();
        data.put("resource_type", "invalid");
        data.put("toopher_sig", "xEY+oOtJcdMsmTLp6eOy9isO/xQ=");
        try {
            iframeApi.processPostback(getUrlEncodedPostbackData(data), REQUEST_TOKEN);
            fail("RequestError was not thrown for invalid postback resource type");
        } catch (RequestError e) {
            assertTrue(e.getMessage().contains("The postback resource type is not valid: invalid"));
        }
    }

    @Test
    public void testProcessPostbackWith704Fails() throws ToopherIframe.SignatureValidationError, RequestError {
        Map<String, String> data = getAuthenticationRequestPostbackData();
        data.put("error_code", "704");
        data.put("error_message", "The specified user has disabled Toopher authentication.");
        try {
            iframeApi.processPostback(getUrlEncodedPostbackData(data), REQUEST_TOKEN);
            fail("ToopherUserDisabledError was not thrown for error code 704");
        } catch (ToopherUserDisabledError e) {
            assertTrue(e.getMessage().contains("The specified user has disabled Toopher authentication."));
        }
    }

    @Test
    public void testIsAuthenticationGrantedWithAuthGrantedReturnsTrue() {
        assertTrue(iframeApi.isAuthenticationGranted(getUrlEncodedPostbackData(getAuthenticationRequestPostbackData()), REQUEST_TOKEN));
    }

    @Test
    public void testIsAuthenticationGrantedWithAuthGrantedWithoutRequestTokenReturnsTrue() {
        assertTrue(iframeApi.isAuthenticationGranted(getUrlEncodedPostbackData(getAuthenticationRequestPostbackData())));
    }

    @Test
    public void testIsAuthenticationGrantedWithAuthNotGrantedReturnsFalse() {
        Map<String, String> auth_data = getAuthenticationRequestPostbackData();
        auth_data.put("granted", "false");
        auth_data.put("toopher_sig", "nADNKdly9zA2IpczD6gvDumM48I=");
        assertFalse(iframeApi.isAuthenticationGranted(getUrlEncodedPostbackData(auth_data)));
    }

    @Test
    public void testIsAuthenticationGrantedWithPairingReturnsFalse() {
        assertFalse(iframeApi.isAuthenticationGranted(getUrlEncodedPostbackData(getPairingPostbackData()), REQUEST_TOKEN));
    }

    @Test
    public void testIsAuthenticationGrantedWithUserReturnsFalse() {
        assertFalse(iframeApi.isAuthenticationGranted(getUrlEncodedPostbackData(getUserPostbackData()), REQUEST_TOKEN));
    }

    @Test
    public void testIsAuthenticationGrantedWithSignatureValidationErrorReturnsFalse() {
        Map<String, String> auth_data = getAuthenticationRequestPostbackData();
        auth_data.remove("timestamp");
        assertFalse(iframeApi.isAuthenticationGranted(getUrlEncodedPostbackData(auth_data), REQUEST_TOKEN));
    }

    @Test
    public void testIsAuthenticationGrantedWithRequestErrorReturnsFalse() {
        Map<String, String> auth_data = getAuthenticationRequestPostbackData();
        auth_data.put("resource_type", "invalid");
        auth_data.put("toopher_sig", "xEY+oOtJcdMsmTLp6eOy9isO/xQ=");
        assertFalse(iframeApi.isAuthenticationGranted(getUrlEncodedPostbackData(auth_data), REQUEST_TOKEN));
    }

    @Test
    public void testIsAuthenticationGrantedWithUserDisabledErrorReturnsFalse() {
        Map<String, String> auth_data = getAuthenticationRequestPostbackData();
        auth_data.put("error_code", "704");

        assertFalse(iframeApi.isAuthenticationGranted(getUrlEncodedPostbackData(auth_data), REQUEST_TOKEN));
    }
}
