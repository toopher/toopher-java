package com.toopher;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeSet;
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
     * Default amount of time that iframe requests are valid (seconds)
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
     *
     * @param username     Unique name that identifies this user.  This will be displayed to the user on
     *                     their mobile device when they pair or authenticate
     * @return URL that can be used to retrieve the Authentication iframe by the user's browser
     */
    public String getAuthenticationUrl(String username) {
        return getAuthenticationUrl(username, "None", "None", "Log In", "None", new HashMap<String, String>());
    }

    /**
     * Generate a URL to retrieve a Toopher Authentication Iframe for a given user
     *
     * @param username     Unique name that identifies this user.  This will be displayed to the user on
     *                     their mobile device when they pair or authenticate
     * @param extras       An optional Map of extra parameters to provide to the API
     * @return URL that can be used to retrieve the Authentication iframe by the user's browser
     */
    public String getAuthenticationUrl(String username, Map<String, String> extras) {
        return getAuthenticationUrl(username, "None", "None", "Log In", "None", extras);
    }

    /**
     * Generate a URL to retrieve a Toopher Authentication Iframe for a given user
     *
     * @param userName     Unique name that identifies this user.  This will be displayed to the user on
     *                     their mobile device when they pair or authenticate
     * @param resetEmail   Email address that the user has access to.  In case the user has lost or cannot
     *                     access their mobile device, Toopher will send a reset email to this address
     * @param requestToken Optional, can be empty.  Toopher will include this token in the signed data returned
     *                     with the iframe response.
     * @return URL that can be used to retrieve the Authentication iframe by the user's browser
     */
    public String getAuthenticationUrl(String userName, String resetEmail, String requestToken) {
        return getAuthenticationUrl(userName, resetEmail, requestToken, "Log In", "None", new HashMap<String, String>());
    }

    /**
     * Generate a URL to retrieve a Toopher Authentication Iframe for a given user
     *
     * @param userName     Unique name that identifies this user.  This will be displayed to the user on
     *                     their mobile device when they pair or authenticate
     * @param resetEmail   Email address that the user has access to.  In case the user has lost or cannot
     *                     access their mobile device, Toopher will send a reset email to this address
     * @param requestToken Optional, can be empty.  Toopher will include this token in the signed data returned
     *                     with the iframe response.
     * @param extras       An optional Map of extra parameters to provide to the API
     * @return URL that can be used to retrieve the Authentication iframe by the user's browser
     */
    public String getAuthenticationUrl(String userName, String resetEmail, String requestToken, Map<String, String> extras) {
        return getAuthenticationUrl(userName, resetEmail, requestToken, "Log In", "None", extras);
    }

    /**
     * Generate a URL to retrieve a Toopher Authentication Iframe for a given user/action
     *
     * @param userName          Unique name that identifies this user.  This will be displayed to the user on
     *                          their mobile device when they pair or authenticate
     * @param resetEmail        Email address that the user has access to.  In case the user has lost or cannot
     *                          access their mobile device, Toopher will send a reset email to this address
     * @param requestToken      Optional, can be empty.  Toopher will include this token in the signed data returned
     *                          with the iframe response.
     * @param actionName        The name of the action to authenticate; will be shown to the user.  If blank,
     *                          the Toopher API will default the action to "Log In".
     * @param requesterMetadata Optional, can be empty.  Toopher will include this value in the signed data returned
     *                          with the iframe response
     * @param extras            An optional Map of extra parameters to provide to the API
     * @return URL that can be used to retrieve the Authentication iframe by the user's browser
     */
    public String getAuthenticationUrl(String userName, String resetEmail, String requestToken, String actionName, String requesterMetadata, Map<String, String> extras) {
        final long ttl;
        final List<NameValuePair> params = new ArrayList<NameValuePair>(10);

        if (!extras.containsKey("ttl")) {
            ttl = DEFAULT_TTL;
        } else {
            ttl = Long.parseLong(extras.get("ttl"));
            extras.remove("ttl");
        }

        params.add(new BasicNameValuePair("v", IFRAME_VERSION));
        params.add(new BasicNameValuePair("username", userName));
        params.add(new BasicNameValuePair("action_name", actionName));
        params.add(new BasicNameValuePair("reset_email", resetEmail));
        params.add(new BasicNameValuePair("session_token", requestToken));
        params.add(new BasicNameValuePair("requester_metadata", requesterMetadata));
        params.add(new BasicNameValuePair("expires", String.valueOf((getDate().getTime() / 1000) + ttl)));

        for (Map.Entry<String, String> entry : extras.entrySet()) {
            params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        return getOAuthUrl(baseUri + "web/authenticate", params, consumerKey, consumerSecret);
    }

    /**
     * Generate a URL to retrieve a Toopher Pairing Iframe for a given user
     *
     * @param userName   Unique name that identifies this user.  This will be displayed to the user on
     *                   their mobile device when they pair or authenticate
     * @return URL that can be used to retrieve the Pairing iframe by the user's browser
     */
    public String getUserManagementUrl(String userName) {
        return getUserManagementUrl(userName, "None", new HashMap<String, String>());
    }

    /**
     * Generate a URL to retrieve a Toopher Pairing Iframe for a given user
     *
     * @param userName   Unique name that identifies this user.  This will be displayed to the user on
     *                   their mobile device when they pair or authenticate
     * @param extras     An optional Map of extra parameters to provide to the API
     * @return URL that can be used to retrieve the Pairing iframe by the user's browser
     */
    public String getUserManagementUrl(String userName, Map<String, String> extras) {
        return getUserManagementUrl(userName, "None", extras);
    }


    /**
     * Generate a URL to retrieve a Toopher Pairing Iframe for a given user
     *
     * @param userName   Unique name that identifies this user.  This will be displayed to the user on
     *                   their mobile device when they pair or authenticate
     * @param resetEmail Email address that the user has access to.  In case the user has lost or cannot
     *                   access their mobile device, Toopher will send a reset email to this address
     * @return URL that can be used to retrieve the Pairing iframe by the user's browser
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
     * @return URL that can be used to retrieve the Pairing iframe by the user's browser
     */
    public String getUserManagementUrl(String userName, String resetEmail, Map<String, String> extras) {
        final long ttl;
        final List<NameValuePair> params = new ArrayList<NameValuePair>();

        if (!extras.containsKey("ttl")) {
            ttl = DEFAULT_TTL;
        } else {
            ttl = Long.parseLong(extras.get("ttl"));
        }

        params.add(new BasicNameValuePair("v", IFRAME_VERSION));
        params.add(new BasicNameValuePair("username", userName));
        params.add(new BasicNameValuePair("reset_email", resetEmail));
        params.add(new BasicNameValuePair("expires", String.valueOf((getDate().getTime() / 1000) + ttl)));
        return getOAuthUrl(baseUri + "web/manage_user", params, consumerKey, consumerSecret);
    }

    /**
     * Verify the authenticity of data returned from the Toopher iframe by validating the cryptographic signature
     *
     * @param params The data returned from the Iframe
     * @param requestToken A randomized string that is included in the signed request to the ToopherAPI and returned in
     *                     the signed response from the Toopher Iframe
     * @param ttl    Time-To-Live (seconds) to enforce on the Toopher API signature.  This value sets the maximum duration
     *               between the Toopher API creating the signature and the signature being validated on your server
     * @return A map of the validated data if the signature is valid, or null if the signature is invalid
     */
    public Map<String, String> processPostback(Map<String, String[]> params, String requestToken, long ttl) throws SignatureValidationError {
        Map<String, String> data = flattenParams(params);

        try {
            List<String> missingKeys = new ArrayList<String>();
            if (!data.containsKey("toopher_sig")) {
                missingKeys.add("toopher_sig");
            }
            if (!data.containsKey("timestamp")) {
                missingKeys.add("timestamp");
            }
            if (!data.containsKey("session_token")) {
                missingKeys.add("session_token");
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

            if (requestToken != null) {
                boolean sessionTokenValid = data.get("session_token").equals(requestToken);
                if (!sessionTokenValid) {
                    throw new SignatureValidationError("Session token does not match expected value");
                }
            }

            String maybeSig = data.get("toopher_sig");
            data.remove("toopher_sig");
            boolean signatureValid;
            try {
                String computedSig = signature(consumerSecret, data);
                signatureValid = computedSig.equals(maybeSig);
                logger.debug("submitted = " + maybeSig);
                logger.debug("computed  = " + computedSig);
            } catch (Exception e) {
                logger.debug("error while calculating signature", e);
                signatureValid = false;
            }
            if (!signatureValid) {
                throw new SignatureValidationError("Computed signature does not match");
            }

            boolean ttlValid = (getDate().getTime() / 1000) - ttl < Long.parseLong(data.get("timestamp"));
            if (!ttlValid) {
                throw new SignatureValidationError("TTL Expired");
            }

            return data;
        } catch (SignatureValidationError s) {
            throw s;
        } catch (Exception e) {
            logger.debug("Exception while validating toopher signature", e);
            throw new SignatureValidationError("Exception while validating toopher signature", e);
        }
    }

    /**
     * Verify the authenticity of data returned from the Toopher iframe by validating the cryptographic signature
     *
     * @param params The data returned from the Iframe
     * @return A map of the validated data if the signature is valid, or null if the signature is invalid
     */
    public Map<String, String> validatePostback(Map<String, String[]> params) throws SignatureValidationError {
        return processPostback(params, null, DEFAULT_TTL);
    }

    /**
     * Verify the authenticity of data returned from the Toopher Iframe by validating the cryptographic signature
     *
     * @param params The data returned from the Iframe
     * @param requestToken A randomized string that is included in the signed request to the ToopherAPI and returned in
     *                     the signed response from the Toopher Iframe
     * @return A map of the validated data if the signature is valid, or null if the signature is invalid
     */
    public Map<String, String> validatePostback(Map<String, String[]> params, String requestToken) throws SignatureValidationError {
        return processPostback(params, requestToken, DEFAULT_TTL);
    }

    private static Map<String, String> flattenParams(Map<String, String[]> params) {
        Map<String, String> result = new HashMap<String, String>();
        for (String key : params.keySet()) {
            String[] val = params.get(key);
            if (val.length > 0) {
                result.put(key, val[0]);
            }
        }
        return result;
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

    private static final String getOAuthUrl(String uri, List<NameValuePair> params, String key, String secret) {
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
