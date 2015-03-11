package com.toopher;

import java.nio.charset.Charset;
import java.util.*;
import java.net.URLEncoder;

import oauth.signpost.http.HttpParameters;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.exception.OAuthException;

import java.io.UnsupportedEncodingException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

/**
 * Java helper library to generate Toopher iframe requests and validate responses.
 * <p/>
 * Register at https://dev.toopher.com to get your Toopher Developer API Credentials.
 */
public final class ToopherIframe {
    public class SignatureValidationError extends Exception {
        public SignatureValidationError(String message) {
            super(message);
        }

        public SignatureValidationError(String message, Exception cause) {
            super(message, cause);
        }
    }

    private static final String IFRAME_VERSION = "2";

    /**
     * Default amount of time that Iframe requests are valid (seconds)
     */
    private static final long DEFAULT_TTL = 300L;

    private static final String DEFAULT_BASE_URI = "https://api.toopher.com/v1/";

    /**
     * Error codes that may be returned by the Toopher API
     */
    public static final String PAIRING_DEACTIVATED = "707";
    public static final String USER_OPT_OUT = "704";
    public static final String USER_UNKNOWN = "705";

    private static Log logger = LogFactory.getLog(ToopherIframe.class);

    /**
     * testability: injection point for Date() object used to validate signatures
     */
    private static Date dateOverride = null;

    public static void setDateOverride(Date dateOverride) {
        ToopherIframe.dateOverride = dateOverride;
    }

    public static Date getDate() {
        if (dateOverride == null) {
            return new Date();
        } else {
            return dateOverride;
        }
    }

    private static String nonceOverride = null;

    public static void setNonceOverride(String nonceOverride) {
        ToopherIframe.nonceOverride = nonceOverride;
    }

    public static String getNonce() { return nonceOverride; }

    private String baseUri;
    private String consumerKey;
    private String consumerSecret;

    /**
     * Creates an instance of the ToopherIframe helper for the default API (https://api.toopher.com/v1)
     *
     * @param consumerKey    Your Toopher API OAuth Consumer Key
     * @param consumerSecret Your Toopher API OAuth Consumer Secret
     */
    public ToopherIframe(String consumerKey, String consumerSecret) {
        this(consumerKey, consumerSecret, DEFAULT_BASE_URI);
    }

    /**
     * Creates an instance of the ToopherIframe helper for the specified API url
     *
     * @param consumerKey    Your Toopher API OAuth Consumer Key
     * @param consumerSecret Your Toopher API OAuth Consumer Secret
     * @param baseUri        The base uri of the Toopher API to target
     */
    public ToopherIframe(String consumerKey, String consumerSecret, String baseUri) {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.baseUri = baseUri;
    }

    /**
     * Generate a URL to retrieve a Toopher Authentication Iframe for a given user
     * @param userName          Unique name that identifies this user.  This will be displayed to the user on
     *                          their mobile device when they pair or authenticate
     * @return URL that can be used to retrieve the Authentication Iframe by the user's browser
     */
    public String getAuthenticationUrl(String userName) {
        return getAuthenticationUrl(userName, new HashMap<String, String>());
    }

    /**
     * Generate a URL to retrieve a Toopher Authentication Iframe for a given user
     *
     * @param userName          Unique name that identifies this user.  This will be displayed to the user on
     *                          their mobile device when they pair or authenticate
     * @param extras            An optional Map of parameters to provide to the API
     * @return URL that can be used to retrieve the Authentication Iframe by the user's browser
     */
    public String getAuthenticationUrl(String userName, Map<String, String> extras) {
        final List<NameValuePair> params = new ArrayList<NameValuePair>();
        final Long ttl = Long.parseLong(getKeyOrDefaultAndDeleteKey(extras, "ttl", DEFAULT_TTL).toString());

        params.add(new BasicNameValuePair("username", userName));
        params.add(new BasicNameValuePair("action_name", (String)getKeyOrDefaultAndDeleteKey(extras, "actionName", "Log In")));
        params.add(new BasicNameValuePair("reset_email", (String)getKeyOrDefaultAndDeleteKey(extras, "resetEmail", "")));
        params.add(new BasicNameValuePair("session_token", (String)getKeyOrDefaultAndDeleteKey(extras, "requestToken", "")));
        params.add(new BasicNameValuePair("requester_metadata", (String)getKeyOrDefaultAndDeleteKey(extras, "requesterMetadata", "")));//        params.add(new BasicNameValuePair("expires", String.valueOf((getDate().getTime() / 1000) + ttl)));

        for (Map.Entry<String, String> entry : extras.entrySet()) {
            params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        return getOAuthUrl(baseUri + "web/authenticate", params, consumerKey, consumerSecret, ttl);
    }

    /**
     * Generate a URL to retrieve a Toopher Pairing Iframe for a given user
     *
     * @param userName   Unique name that identifies this user.  This will be displayed to the user on
     *                   their mobile device when they pair or authenticate
     * @return URL that can be used to retrieve the Pairing Iframe by the user's browser
     */
    public String getUserManagementUrl(String userName) {
        return getUserManagementUrl(userName, "", new HashMap<String, String>());
    }

    /**
     * Generate a URL to retrieve a Toopher Pairing Iframe for a given user
     *
     * @param userName   Unique name that identifies this user.  This will be displayed to the user on
     *                   their mobile device when they pair or authenticate
     * @param extras     An optional Map of extra parameters to provide to the API
     * @return URL that can be used to retrieve the Pairing Iframe by the user's browser
     */
    public String getUserManagementUrl(String userName, Map<String, String> extras) {
        return getUserManagementUrl(userName, "", extras);
    }


    /**
     * Generate a URL to retrieve a Toopher Pairing Iframe for a given user
     *
     * @param userName   Unique name that identifies this user.  This will be displayed to the user on
     *                   their mobile device when they pair or authenticate
     * @param resetEmail Email address that the user has access to.  In case the user has lost or cannot
     *                   access their mobile device, Toopher will send a reset email to this address
     * @return URL that can be used to retrieve the Pairing Iframe by the user's browser
     */
    public String getUserManagementUrl(String userName, String resetEmail) {
        return getUserManagementUrl(userName, resetEmail, new HashMap<String, String>());
    }

    /**
     * Generate a URL to retrieve a Toopher Pairing Iframe for a given user
     *
     * @param userName   Unique name that identifies this user.  This will be displayed to the user on
     *                   their mobile device when they pair or authenticate
     * @param resetEmail Email address that the user has access to.  In case the user has lost or cannot
     *                   access their mobile device, Toopher will send a reset email to this address
     * @param extras     An optional Map of extra parameters to provide to the API
     * @return URL that can be used to retrieve the Pairing Iframe by the user's browser
     */
    public String getUserManagementUrl(String userName, String resetEmail, Map<String, String> extras) {
        final long ttl;
        final List<NameValuePair> params = new ArrayList<NameValuePair>();

        if (!extras.containsKey("ttl")) {
            ttl = DEFAULT_TTL;
        } else {
            ttl = Long.parseLong(extras.get("ttl"));
        }

        params.add(new BasicNameValuePair("username", userName));
        params.add(new BasicNameValuePair("reset_email", resetEmail));
        return getOAuthUrl(baseUri + "web/manage_user", params, consumerKey, consumerSecret, ttl);
    }

    /**
     * Verify the authenticity of data returned from the Toopher Iframe
     *
     * @param params The postback data returned from the Toopher Iframe
     * @param requestToken A randomized string that is included in the signed request to the ToopherAPI
     *                     and returned in the signed response from the Toopher Iframe
     * @param extras An optional Map of extra parameters used to validate the data
     * @return A {@link com.toopher.AuthenticationRequest}, {@link com.toopher.Pairing} or {@link com.toopher.User} object
     * @throws SignatureValidationError Thrown when exceptional condition is encountered while validating data
     * @throws RequestError Thrown when postback resource type is invalid
     */
    public Object processPostback(String params, String requestToken, Map<String, String> extras) throws SignatureValidationError, RequestError {
        Map<String, String> toopherData = urlDecodeIframeData(params);

        if (toopherData.containsKey("error_code")) {
            int errorCode = Integer.parseInt(toopherData.get("error_code"));
            String errorMessage = toopherData.get("error_message");
            if (errorCode == ToopherUserDisabledError.ERROR_CODE) {
                throw new ToopherUserDisabledError(errorMessage);
            } else {
                throw new ToopherClientError(errorCode, errorMessage);
            }
        } else {
            Map<String, String> validatedData = validateData(toopherData, requestToken, extras);

            ToopherApi toopherApi = new ToopherApi(consumerKey, consumerSecret);
            String resourceType = validatedData.get("resource_type");
            if (resourceType.equals("authentication_request")) {
                return new AuthenticationRequest(createAuthenticationRequestJson(validatedData), toopherApi);
            } else if (resourceType.equals("pairing")) {
                return new Pairing(createPairingJson(validatedData), toopherApi);
            } else if (resourceType.equals("requester_user")) {
                return new User(createUserJson(validatedData), toopherApi);
            } else {
                throw new RequestError(String.format("The postback resource type is not valid: %s", resourceType));
            }
        }
    }

    /**
     * Verify the authenticity of data returned from the Toopher Iframe
     *
     * @param params The postback data returned from the Toopher Iframe
     * @param requestToken A randomized string that is included in the signed request to the ToopherAPI
     *                     and returned in the signed response from the Toopher Iframe
     * @return A {@link com.toopher.AuthenticationRequest}, {@link com.toopher.Pairing} or {@link com.toopher.User} object
     * @throws SignatureValidationError Thrown when exceptional condition is encountered while validating data
     * @throws RequestError Thrown when postback resource type is invalid
     */
    public Object processPostback(String params, String requestToken) throws SignatureValidationError, RequestError {
        return processPostback(params, requestToken, new HashMap<String, String>());
    }

    /**
     * Verify the authenticity of data returned from the Toopher Iframe
     *
     * @param params The postback data returned from the Toopher Iframe
     * @return A {@link com.toopher.AuthenticationRequest}, {@link com.toopher.Pairing} or {@link com.toopher.User} object
     * @throws SignatureValidationError Thrown when exceptional condition is encountered while validating data
     * @throws RequestError Thrown when postback resource type is invalid
     */
    public Object processPostback(String params) throws SignatureValidationError, RequestError {
        return processPostback(params, null);
    }

    /**
     * Evaluate whether AuthenticationRequest has been granted
     *
     * @param params The postback data returned from the Toopher Iframe
     * @param requestToken A randomized string that is included in the signed request to the ToopherAPI
     *                     and returned in the signed response from the Toopher Iframe
     * @return boolean indicating whether AuthenticationRequest has been granted and is not pending
     */
    public boolean isAuthenticationGranted(String params, String requestToken) {
        try {
            Object postbackObject = processPostback(params, requestToken);
            if (postbackObject instanceof AuthenticationRequest) {
                AuthenticationRequest authenticationRequest = (AuthenticationRequest)postbackObject;
                return !authenticationRequest.pending && authenticationRequest.granted;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.debug(e);
            return false;
        }
    }

    /**
     * Evaluate whether AuthenticationRequest has been granted
     *
     * @param params The postback data returned from the Toopher Iframe
     * @return boolean indicating whether AuthenticationRequest has been granted and is not pending
     */
    public boolean isAuthenticationGranted(String params) {
        return isAuthenticationGranted(params, null);
    }

    private Object getKeyOrDefaultAndDeleteKey(Map<String, String> extras, String key, Object defaultValue) {
        return extras.containsKey(key) ? extras.remove(key) : defaultValue;
    }

    private Map<String, String> urlDecodeIframeData(String params) {
        List<NameValuePair> decodedParams = URLEncodedUtils.parse(params, Charset.forName("UTF-8"));
        HashMap<String, String> result = new HashMap<String, String>();
        for (NameValuePair nvp : decodedParams) {
            result.put(nvp.getName(), nvp.getValue());
        }
        return result;
    }

    private JSONObject createAuthenticationRequestJson(Map<String, String> data) {
        JSONObject user = new JSONObject();
        user.put("id", data.get("pairing_user_id"));
        user.put("name", data.get("user_name"));
        user.put("toopher_authentication_enabled", data.get("user_toopher_authentication_enabled").equals("true"));

        JSONObject terminal = new JSONObject();
        terminal.put("id", data.get("terminal_id"));
        terminal.put("name", data.get("terminal_name"));
        terminal.put("requester_specified_id", data.get("terminal_requester_specified_id"));
        terminal.put("user", user);

        JSONObject action = new JSONObject();
        action.put("id", data.get("action_id"));
        action.put("name", data.get("action_name"));

        JSONObject authenticationRequest = new JSONObject();
        authenticationRequest.put("id", data.get("id"));
        authenticationRequest.put("pending", data.get("pending").equals("true"));
        authenticationRequest.put("granted", data.get("granted").equals("true"));
        authenticationRequest.put("automated", data.get("automated").equals("true"));
        authenticationRequest.put("reason", data.get("reason"));
        authenticationRequest.put("reason_code", data.get("reason_code"));
        authenticationRequest.put("user", user);
        authenticationRequest.put("terminal", terminal);
        authenticationRequest.put("action", action);
        return authenticationRequest;
    }

    private JSONObject createPairingJson(Map<String, String> data) {
        JSONObject user = new JSONObject();
        user.put("id", data.get("pairing_user_id"));
        user.put("name", data.get("user_name"));
        user.put("toopher_authentication_enabled", data.get("user_toopher_authentication_enabled").equals("true"));

        JSONObject terminal = new JSONObject();
        terminal.put("id", data.get("id"));
        terminal.put("enabled", data.get("enabled").equals("true"));
        terminal.put("pending", data.get("pending").equals("true"));
        terminal.put("user", user);
        return terminal;
    }

    private JSONObject createUserJson(Map<String, String> data) {
        JSONObject user = new JSONObject();
        user.put("id", data.get("id"));
        user.put("name", data.get("name"));
        user.put("toopher_authentication_enabled", data.get("toopher_authentication_enabled").equals("true"));
        return user;
    }

    private Map<String, String> validateData(Map<String, String> params, String requestToken, Map<String, String> extras) throws SignatureValidationError {
        checkForMissingKeys(params);
        verifySessionToken(params.get("session_token"), requestToken);
        checkIfSignatureIsExpired(params.get("timestamp"), extras);
        validateSignature(params);
        return params;
    }

    private void checkForMissingKeys(Map<String, String> data) throws SignatureValidationError {
        List<String> missingKeys = new ArrayList<String>();

        List<String> keys = Arrays.asList("toopher_sig", "timestamp", "session_token");
        for (String key : keys) {
            if (!data.containsKey(key)) {
                missingKeys.add(key);
            }
        }
        if (missingKeys.size() > 0) {
            StringBuilder errorMessageBuilder = new StringBuilder("Missing required keys: ");
            String separator = "";
            for (String missingKey : missingKeys) {
                errorMessageBuilder.append(separator).append(missingKey);
                separator = ",";
            }
            String errorMessage = errorMessageBuilder.toString();
            logger.debug(errorMessage);
            throw new SignatureValidationError(errorMessage);
        }
    }

    private void verifySessionToken(String sessionToken, String requestToken) throws SignatureValidationError {
        if (requestToken != null) {
            boolean sessionTokenValid = sessionToken.equals(requestToken);
            if (!sessionTokenValid) {
                throw new SignatureValidationError("Session token does not match expected value");
            }
        }
    }

    private void checkIfSignatureIsExpired(String timestamp, Map<String, String> extras) throws SignatureValidationError {
        long ttl = extras.containsKey("ttl") ? Long.parseLong(extras.remove("ttl")) : DEFAULT_TTL;
        boolean ttlValid = (getDate().getTime() / 1000) - ttl < Long.parseLong(timestamp);
        if (!ttlValid) {
            throw new SignatureValidationError("TTL Expired");
        }
    }

    private void validateSignature(Map<String, String> data) throws SignatureValidationError {
        String maybeSig = data.remove("toopher_sig");
        boolean signatureValid;
        try {
            String computedSig = signature(consumerSecret, data);
            signatureValid = computedSig.equals(maybeSig);
            logger.debug("Submitted signature = " + maybeSig);
            logger.debug("Computed signature = " + computedSig);
        } catch (Exception e) {
            logger.debug("Error while calculating signature", e);
            signatureValid = false;
        }
        if (!signatureValid) {
            throw new SignatureValidationError("Computed signature does not match");
        }
    }

    private static String signature(String secret, Map<String, String> data) throws NoSuchAlgorithmException, InvalidKeyException {
        TreeSet<String> sortedKeys = new TreeSet<String>(data.keySet());
        List<NameValuePair> sortedParams = new ArrayList<NameValuePair>(data.size());
        for (String key : sortedKeys) {
            sortedParams.add(new BasicNameValuePair(key, data.get(key)));
        }
        String toSign = URLEncodedUtils.format(sortedParams, "UTF-8");

        byte[] secretBytes = secret.getBytes();
        SecretKeySpec signingKey = new SecretKeySpec(secretBytes, "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signingKey);
        return org.apache.commons.codec.binary.Base64.encodeBase64String(mac.doFinal(toSign.getBytes())).trim();
    }

    private static final String getOAuthUrl(String uri, List<NameValuePair> params, String key, String secret, long ttl) {
        params.add(new BasicNameValuePair("expires", String.valueOf((getDate().getTime() / 1000) + ttl)));
        params.add(new BasicNameValuePair("v", IFRAME_VERSION));

        final OAuthConsumer consumer = new DefaultOAuthConsumer(key, secret);
        HttpParameters additionalParameters = new HttpParameters();
        additionalParameters.put("oauth_timestamp", String.valueOf(getDate().getTime() / 1000));
        if (ToopherIframe.getNonce() != null) {
            additionalParameters.put("oauth_nonce", ToopherIframe.nonceOverride);
        }
        consumer.setAdditionalParameters(additionalParameters);
        try {
            String result = consumer.sign(uri + "?" + URLEncodedUtils.format(params, "UTF-8"));
            return result;
        } catch (OAuthException e) {
            try {
                return uri + "web/error.html?message=" + URLEncoder.encode(e.getMessage(), "UTF-8");
            } catch (UnsupportedEncodingException f) {
                return uri + "web/error.html?message=Unknown%20Error";
            }
        }
    }
}
