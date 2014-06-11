package com.toopher;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A Java binding for the Toopher API
 *
 */
public class ToopherAPI {
    private static final String DEFAULT_URI_SCHEME = "https";
    private static final String DEFAULT_URI_HOST   = "api.toopher.com";
    private static final int DEFAULT_URI_PORT      = 443;
    private static final String DEFAULT_URI_BASE   = "/v1/";

    private final URI baseUri;
    private final OAuthConsumer consumer;
    private final HttpClient httpClient;
    private final AuthenticationStatusFactory authenticationStatusFactory;

    /**
     * The ToopherJava binding library version
     */
    public static final String VERSION = "1.0.0";

    /**
     * Create an API object with the supplied credentials
     *
     * @param consumerKey
     *            The consumer key for a requester (obtained from the developer portal)
     * @param consumerSecret
     *            The consumer secret for a requester (obtained from the developer portal)
     */
    public ToopherAPI(String consumerKey, String consumerSecret) throws URISyntaxException {
        this(new Builder(consumerKey, consumerSecret));
    }

    /**
     * Create an API object with the supplied credentials, overriding the default API URI of https://api.toopher.com/v1/
     *
     * @param consumerKey
     *            The consumer key for a requester (obtained from the developer portal)
     * @param consumerSecret
     *            The consumer secret for a requester (obtained from the developer portal)
     * @param uri
     *            The uri of the Toopher API
     * @throws URISyntaxException
     */
    public ToopherAPI(String consumerKey, String consumerSecret, String uri) throws URISyntaxException {
        this(new Builder(consumerKey, consumerSecret).setBaseUri(uri));
    }

    /**
     * Create an API object with the supplied credentials, overriding the default API URI of https://api.toopher.com/v1/
     *
     * @param consumerKey
     *            The consumer key for a requester (obtained from the developer portal)
     * @param consumerSecret
     *            The consumer secret for a requester (obtained from the developer portal)
     * @param uriScheme
     *            The uri scheme ( http or https )
     * @param uriHost
     *            The uri host ( api.toopher.com )
     * @param uriScheme
     *            The uri base ( /v1/ )
     * @throws URISyntaxException
     */
    public ToopherAPI(String consumerKey, String consumerSecret, String uriScheme, String uriHost, String uriBase) throws URISyntaxException {
        this(new Builder(consumerKey, consumerSecret)
                .setBaseUri(new URIBuilder().setScheme(uriScheme).setHost(uriHost).setPath(uriBase).build()));
    }

    /**
     * Create an API object with the supplied credentials, overriding the default API URI of https://api.toopher.com/v1/
     *
     * @param consumerKey
     *            The consumer key for a requester (obtained from the developer portal)
     * @param consumerSecret
     *            The consumer secret for a requester (obtained from the developer portal)
     * @param uri
     *            The alternate URI
     */
    public ToopherAPI(String consumerKey, String consumerSecret, URI uri) {
        this(new Builder(consumerKey, consumerSecret).setBaseUri(uri));
    }

    /**
     * Create an API object with the supplied credentials and API URI,
     * overriding the default HTTP client.
     *
     * @param consumerKey
     *     The consumer key for a requester (obtained from the developer portal)
     * @param consumerSecret
     *     The consumer secret for a requester (obtained from the developer portal)
     * @param uri
     *     The alternate URI
     * @param httpClient
     *     The alternate HTTP client
     */
    public ToopherAPI(String consumerKey, String consumerSecret, URI uri, HttpClient httpClient) {
        this(new ToopherAPI.Builder(consumerKey, consumerSecret)
                .setBaseUri(uri).setHttpClient(httpClient));
    }

    public ToopherAPI(Builder builder) {
        this(builder.getConsumer(), builder.getBaseUri(), builder.getHttpClient(),
                builder.getAuthenticationStatusFactory());
    }

    private ToopherAPI(OAuthConsumer consumer,
                       URI baseUri,
                       HttpClient httpClient,
                       AuthenticationStatusFactory authenticationStatusFactory) {
        this.consumer = consumer;
        this.baseUri = baseUri;
        this.httpClient = httpClient;
        this.authenticationStatusFactory = authenticationStatusFactory;
    }

    /**
     * Create a pairing
     *
     * @param pairingPhrase
     *            The pairing phrase supplied by the user
     * @param userName
     *            A user-facing descriptive name for the user (displayed in requests)
     * @return A PairingStatus object
     * @throws RequestError
     *             Thrown when an exceptional condition is encountered
     */
    public PairingStatus pair(String pairingPhrase, String userName) throws RequestError {
        return this.pair(pairingPhrase, userName, null);
    }

    /**
     * Create a pairing
     *
     * @param pairingPhrase
     *            The pairing phrase supplied by the user
     * @param userName
     *            A user-facing descriptive name for the user (displayed in requests)
     * @param extras
     *            An optional Map of extra parameters to provide to the API
     * @return A PairingStatus object
     * @throws RequestError
     *             Thrown when an exceptional condition is encountered
     */
    public PairingStatus pair(String pairingPhrase, String userName, Map<String, String> extras) throws RequestError {
        final String endpoint = "pairings/create";

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("pairing_phrase", pairingPhrase));
        params.add(new BasicNameValuePair("user_name", userName));

        JSONObject json = post(endpoint, params, extras);
        try {
            return new PairingStatus(json);
        } catch (Exception e) {
            throw new RequestError(e);
        }
    }

    /**
     * Retrieve the current status of a pairing request
     *
     * @param pairingRequestId
     *            The unique id for a pairing request
     * @return A PairingStatus object
     * @throws RequestError
     *             Thrown when an exceptional condition is encountered
     */
    public PairingStatus getPairingStatus(String pairingRequestId) throws RequestError {
        final String endpoint = String.format("pairings/%s", pairingRequestId);

        JSONObject json = get(endpoint);
        try {
            return new PairingStatus(json);
        } catch (Exception e) {
            throw new RequestError(e);
        }
    }

    /**
     * Create a pairing that is presented as a QR code
     *
     * @param userName
     *            A user-facing descriptive name for the user (displayed in requests)
     * @return A PairingStatus object
     * @throws RequestError
     *             Thrown when an exceptional condition is encountered
     */
    public PairingStatus pairWithQrCode(String userName) throws RequestError {
        return this.pairWithQrCode(userName, null);
    }

    /**
     * Create a pairing that is presented as a QR code
     *
     * @param userName
     *            A user-facing descriptive name for the user (displayed in requests)
     * @param extras
     *            An optional Map of extra parameters to provide to the API
     * @return A PairingStatus object
     * @throws RequestError
     *             Thrown when an exceptional condition is encountered
     */
    public PairingStatus pairWithQrCode(String userName, Map<String, String> extras) throws RequestError {
        final String endpoint = "pairings/create/qr";

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("user_name", userName));

        JSONObject json = post(endpoint, params, extras);
        try {
            return new PairingStatus(json);
        } catch (Exception e) {
            throw new RequestError(e);
        }
    }

    /**
     * Initiate a login authentication request
     *
     * @param pairingId
     *            The pairing id indicating to whom the request should be sent
     * @param terminalName
     *            The user-facing descriptive name for the terminal from which the request originates
     * @return An AuthenticationStatus object
     * @throws RequestError
     *             Thrown when an exceptional condition is encountered
     */
    public AuthenticationStatus authenticate(String pairingId, String terminalName) throws RequestError {
        AuthenticationRequestDetails.Builder builder = new AuthenticationRequestDetails.Builder();
        builder.setPairingId(pairingId).setTerminalName(terminalName);
        return authenticate(builder.build());
    }

    /**
     * Initiate a login authentication request
     *
     * @param pairingId
     *            The pairing id indicating to whom the request should be sent
     * @param terminalName
     *            The user-facing descriptive name for the terminal from which the request originates
     * @param actionName
     *            The user-facing descriptive name for the action which is being authenticated
     * @return An AuthenticationStatus object
     * @throws RequestError
     *             Thrown when an exceptional condition is encountered
     */
    public AuthenticationStatus authenticate(String pairingId, String terminalName, String actionName) throws RequestError {
        AuthenticationRequestDetails.Builder builder = new AuthenticationRequestDetails.Builder();
        builder.setPairingId(pairingId).setTerminalName(terminalName).setActionName(actionName);
        return authenticate(builder.build());
    }

    /**
     * Initiate an authentication request
     *
     * @param pairingId
     *            The pairing id indicating to whom the request should be sent
     * @param terminalName
     *            The user-facing descriptive name for the terminal from which the request originates
     * @param actionName
     *            The user-facing descriptive name for the action which is being authenticated
     * @param extras
     *            An optional Map of extra parameters to provide to the API
     * @return An AuthenticationStatus object
     * @throws RequestError
     *             Thrown when an exceptional condition is encountered
     */
    public AuthenticationStatus authenticate(String pairingId, String terminalName,
                                             String actionName, Map<String, String> extras) throws RequestError {
        AuthenticationRequestDetails.Builder builder = new AuthenticationRequestDetails.Builder(extras);
        builder.setPairingId(pairingId).setTerminalName(terminalName).setActionName(actionName);
        return authenticate(builder.build());
    }

    /**
     * Initiate an authentication request.
     *
     * @param details An AuthenticationRequestDetails object that contains the request parameters.
     * @return An AuthenticationStatus object.
     * @throws RequestError Thrown when an exceptional condition is encountered.
     */
    public AuthenticationStatus authenticate(AuthenticationRequestDetails details) throws RequestError {
        final String endpoint = "authentication_requests/initiate";

        JSONObject json = post(endpoint, details);
        try {
            return authenticationStatusFactory.create(json);
        } catch (JSONException e) {
            throw new RequestError(e);
        }
    }

    /**
     * Initiate a login authentication request by username (instead of PairingID)
     *
     * @param userName
     *            The unique UserName for this user
     * @param terminalNameExtra
     *            Unique identifier for this terminal.  Not displayed to the user.
     * @param actionName
     *            The user-facing descriptive name for the action which is being authenticated
     * @return An AuthenticationStatus object
     * @throws RequestError
     *             Thrown when an exceptional condition is encountered
     */
    public AuthenticationStatus authenticateByUserName(String userName, String terminalNameExtra, String actionName,
                                                       Map<String, String> extras) throws RequestError {
        AuthenticationRequestDetails.Builder builder = new AuthenticationRequestDetails.Builder(extras);
        builder.setUserName(userName).setTerminalNameExtra(terminalNameExtra).setActionName(actionName);
        return authenticate(builder.build());
    }

    /**
     * Retrieve status information for an authentication request
     *
     * @param authenticationRequestId
     *            The authentication request ID
     * @return An AuthenticationStatus object
     * @throws RequestError
     *             Thrown when an exceptional condition is encountered
     */
    public AuthenticationStatus getAuthenticationStatus(String authenticationRequestId) throws RequestError {
        final String endpoint = String.format("authentication_requests/%s", authenticationRequestId);

        JSONObject json = get(endpoint);
        try {
            return new AuthenticationStatus(json);
        } catch (Exception e) {
            throw new RequestError(e);
        }
    }

    public AuthenticationStatus getAuthenticationStatusWithOTP(String authenticationRequestId,String OTP) throws RequestError {
        final String endpoint = String.format("authentication_requests/%s/otp_auth", authenticationRequestId);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("otp", OTP));
        JSONObject json = post(endpoint, params, null);
        try {
            return new AuthenticationStatus(json);
        } catch (Exception e) {
            throw new RequestError(e);
        }
    }


    /**
     * Associates a per-user "Friendly Name" to a given terminal
     *
     * @param userName
     *            The name of the user
     * @param terminalName
     *            The user-facing descriptive name for the terminal from which the request originates
     * @param terminalNameExtra
     *            The requester-specific key that uniquely identifies this terminal.  Can be shared
     *            across multiple users.  The combination of userName and terminalNameExtra should
     *            be unique for a requester
     * @throws RequestError
     *             Thrown when an exceptional condition is encountered, or the
     */
    public void assignUserFriendlyNameToTerminal(String userName, String terminalName, String terminalNameExtra) throws RequestError {
        final String endpoint = "user_terminals/create";

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("user_name", userName));
        params.add(new BasicNameValuePair("name", terminalName));
        params.add(new BasicNameValuePair("name_extra", terminalNameExtra));

        post(endpoint, params, null);
    }

    /**
     * Toggle whether a user is Toopher-Enabled
     *
     * @param userName
     *            The name of the user
     * @param toopherEnabled
     *            Whether or not the user is Toopher-enabled
     * @throws RequestError
     *             Thrown when an exceptional condition is encountered, or the
     */
    public void setToopherEnabledForUser(String userName, boolean toopherEnabled) throws RequestError {
        final String searchEndpoint = "users";
        final String updateEndpoint = "users/%s";

        // first, look up the Toopher User ID
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("name", userName));

        JSONArray result = get(searchEndpoint, params, null);

        // user name should be a unique field per requester - if more than one object is returned, this is gonna be a problem
        if (result.length() > 1) {
            throw new RequestError("More than one user with username {0}".format(userName));
        }
        if (result.length() == 0) {
            throw new RequestError("No users with user name {0}".format(userName));
        }

        String userId;
        try {
            JSONObject userJson = result.getJSONObject(0);
            userId = userJson.getString("id");
        } catch (JSONException e) {
            throw new RequestError(e);
        }


        // now, we can use that User ID to update the disable_toopher_auth field
        params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("disable_toopher_auth", toopherEnabled ? "false" : "true"));

        post(String.format(updateEndpoint, userId), params, null);
    }


    private <T> T get(String endpoint) throws RequestError {
        return request(new HttpGet(), endpoint, null);
    }
    private <T> T get(String endpoint, List<NameValuePair> params, Map<String, String> extras) throws RequestError {
        if (params == null) {
            params = new ArrayList<NameValuePair>();
        }
        if (extras != null && extras.size() > 0) {
        	for (Map.Entry<String, String> e : extras.entrySet()){
        		params.add(new BasicNameValuePair(e.getKey(), e.getValue()));
        	}
        }
    	return request(new HttpGet(), endpoint, params);
    }

    private <T> T post(String endpoint, List<NameValuePair> params, Map<String, String> extras) throws RequestError {
        HttpPost post = new HttpPost();
        if (extras != null && extras.size() > 0) {
        	for (Map.Entry<String, String> e : extras.entrySet()){
        		params.add(new BasicNameValuePair(e.getKey(), e.getValue()));
        	}
        }
        if (params != null && params.size() > 0) {
            try {
                post.setEntity(new UrlEncodedFormEntity(params));
            } catch (Exception e) {
                throw new RequestError(e);
            }
        }
        return request(post, endpoint, null);
    }

    private JSONObject post(String endpoint, ApiRequestDetails details) throws RequestError {
        return post(endpoint, details.getParams(), details.getExtras());
    }

    private <T> T request(HttpRequestBase httpRequest, String endpoint, List<NameValuePair> queryStringParameters) throws RequestError {
        try {
            URIBuilder uriBuilder = new URIBuilder(this.baseUri).setPath(this.baseUri.getPath() + endpoint);
            if (queryStringParameters != null && queryStringParameters.size() > 0) {
                for (NameValuePair nvp : queryStringParameters) {
                    uriBuilder.setParameter(nvp.getName(), nvp.getValue());
                }
            }
    	    httpRequest.setURI(uriBuilder.build());
    	    consumer.sign(httpRequest);
        } catch (Exception e) {
            throw new RequestError(e);
        }

        try {
    	    return (T) httpClient.execute(httpRequest, jsonHandler);
        } catch (RequestError re) {
            throw re;
        } catch (Exception e) {
            throw new RequestError(e);
        }
    }

    private static ResponseHandler<Object> jsonHandler = new ResponseHandler<Object>() {

        @Override
        public Object handleResponse(HttpResponse response) throws IOException, ClientProtocolException {
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() >= 300) {
                parseRequestError(statusLine, response);
            }

            HttpEntity entity = response.getEntity(); // TODO: check entity == null
            String json;
            json = (entity != null) ? EntityUtils.toString(entity) : null;

            if (json != null && !json.isEmpty()) {
                try {
                    return new JSONTokener(json).nextValue();
                } catch (JSONException jex) {
                    throw new RequestError(jex);
                }
            } else {
                throw new RequestError("Empty response body returned");
            }

        }
    };


    public static String getBaseURL() {
        return String.format("%s://%s%s", DEFAULT_URI_SCHEME,
                             DEFAULT_URI_HOST, DEFAULT_URI_BASE);
    }

    private static void parseRequestError(StatusLine statusLine, HttpResponse response) throws RequestError {
        HttpEntity errEntity = response.getEntity();
        String errBody;
        try {
            errBody = (errEntity != null) ? EntityUtils.toString(errEntity) : null;
        } catch (IOException iex) {
            throw new RequestError(iex);
        }

        // first, see if we can parse this into a more meaningful error
        if (errBody != null && !errBody.isEmpty()) {
            try {
                JSONObject errObj = (JSONObject) new JSONTokener(errBody).nextValue();
                int toopherErrorCode = errObj.getInt("error_code");
                String toopherErrorMessage = errObj.getString("error_message");
                switch (toopherErrorCode) {
                    case ToopherUserDisabledError.ERROR_CODE:
                        throw new ToopherUserDisabledError(toopherErrorMessage);
                    case ToopherUnknownUserError.ERROR_CODE:
                        throw new ToopherUnknownUserError(toopherErrorMessage);
                    case ToopherUnknownTerminalError.ERROR_CODE:
                        throw new ToopherUnknownTerminalError(toopherErrorMessage);
                    default:
                        throw new ToopherClientError(toopherErrorCode, toopherErrorMessage,
                                new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase()));
                }

            } catch (JSONException _jex) {
                // extended error information was supplied as non-JSON body text
                throw new RequestError(errBody, new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase()));
            }
        } else {

            // Complete error info is in the HTTP StatusLine
            throw new RequestError(new HttpResponseException(statusLine.getStatusCode(),
                        statusLine.getReasonPhrase()));
        }
    }

    public static class Builder {
        public String consumerKey;
        public String consumerSecret;
        public URI baseUri;
        public HttpClient httpClient;
        public AuthenticationStatusFactory authenticationStatusFactory;


        public Builder(String consumerKey, String consumerSecret) {
            this.consumerKey = consumerKey;
            this.consumerSecret = consumerSecret;
        }

        public Builder setAuthenticationStatusFactory(AuthenticationStatusFactory authenticationStatusFactory) {
            this.authenticationStatusFactory = authenticationStatusFactory;
            return this;
        }

        public Builder setBaseUri(String baseUri) throws URISyntaxException {
            this.baseUri = new URI(baseUri);
            return this;
        }

        public Builder setBaseUri(URI baseUri) {
            this.baseUri = baseUri;
            return this;
        }

        public Builder setHttpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public ToopherAPI build() {
            return new ToopherAPI(this);
        }

        /**
         * Returns the AuthenticationStatus factory, or the default AuthenticationStatus factory.
         *
         * @return An AuthenticationStatusFactory object.
         */
        public AuthenticationStatusFactory getAuthenticationStatusFactory() {
            if (authenticationStatusFactory == null) {
                authenticationStatusFactory = new AuthenticationStatusFactory();
            }

            return authenticationStatusFactory;
        }

        /**
         * Returns the base URI, or the default base URI.
         *
         * @return A URI object.
         */
        public URI getBaseUri() {
            if (baseUri == null) {
                try {
                    baseUri = new URIBuilder()
                            .setScheme(DEFAULT_URI_SCHEME)
                            .setHost(DEFAULT_URI_HOST)
                            .setPort(DEFAULT_URI_PORT)
                            .setPath(DEFAULT_URI_BASE)
                            .build();
                } catch (URISyntaxException urise) {
                    // There's not really any way to recover when the DEFAULT_URI_* components are invalid and
                    // the client tries to use the default URI, so preemptively crash and try not to do that again.
                    throw new RuntimeException("DEFAULT_URI_* components are invalid.");
                }
            }

            return baseUri;
        }

        public OAuthConsumer getConsumer() {
            return new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);
        }

        /**
         * Returns the HTTP client, or the default HTTP client.
         *
         * @return An HttpClient object.
         */
        public HttpClient getHttpClient() {
            if (httpClient == null) {
                httpClient = new DefaultHttpClient();
                HttpProtocolParams.setUserAgent(this.httpClient.getParams(),
                        String.format("Toopher-Java/%s", VERSION));
            }

            return httpClient;
        }
    }
}
