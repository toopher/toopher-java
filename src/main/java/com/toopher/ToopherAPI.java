package com.toopher;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONTokener;

/**
 * A Java binding for the Toopher API
 *
 */
public class ToopherAPI {
    private static final String DEFAULT_URI_SCHEME = "https";
    private static final String DEFAULT_URI_HOST = "api.toopher.com";
    private static final String DEFAULT_URI_BASE = "/v1/";
    private static final int DEFAULT_URI_PORT = 443;

    private final HttpClient httpClient;
    private final OAuthConsumer consumer;
    private final String uriScheme;
    private final String uriHost;
    private final int uriPort;
    private final String uriBase;
    public final AdvancedApiUsageFactory advanced;

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
    public ToopherAPI(String consumerKey, String consumerSecret) {
    	this(consumerKey, consumerSecret, (URI)null);
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
    	this(consumerKey, consumerSecret, new URI(uri));
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
    	this(consumerKey, consumerSecret, new URIBuilder().setScheme(uriScheme).setHost(uriHost).setPath(uriBase).build());
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
        this(consumerKey, consumerSecret, uri, null);
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
        this.advanced = new AdvancedApiUsageFactory(consumerKey, consumerSecret);
        if (httpClient == null) {
            this.httpClient = new DefaultHttpClient();
            HttpProtocolParams.setUserAgent(this.httpClient.getParams(),
                                            String.format("Toopher-Java/%s", VERSION));
        } else {
            this.httpClient = httpClient;
        }

        consumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);
        if (uri == null){
            this.uriScheme = ToopherAPI.DEFAULT_URI_SCHEME;
        	this.uriHost = ToopherAPI.DEFAULT_URI_HOST;
        	this.uriPort = ToopherAPI.DEFAULT_URI_PORT;
        	this.uriBase = ToopherAPI.DEFAULT_URI_BASE;
    	} else {
	        this.uriScheme = uri.getScheme();
	        this.uriHost = uri.getHost();
	        this.uriPort = uri.getPort();
	        this.uriBase = uri.getPath();
	    }
    }


    /**
     * Create a pairing
     *
     * @param pairingPhrase
     *            The pairing phrase supplied by the user
     * @param userName
     *            A user-facing descriptive name for the user (displayed in requests)
     * @return A Pairing object
     * @throws RequestError
     *             Thrown when an exceptional condition is encountered
     */
    public Pairing pair(String pairingPhrase, String userName) throws RequestError {
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
     * @return A Pairing object
     * @throws RequestError
     *             Thrown when an exceptional condition is encountered
     */
    public Pairing pair(String pairingPhrase, String userName, Map<String, String> extras) throws RequestError {
        final String endpoint = "pairings/create";

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("pairing_phrase", pairingPhrase));
        params.add(new BasicNameValuePair("user_name", userName));

        JSONObject json = post(endpoint, params, extras);
        try {
            return new Pairing(json);
        } catch (Exception e) {
            throw new RequestError(e);
        }
    }

    /**
     * Create a pairing that is presented as a QR code
     *
     * @param userName
     *            A user-facing descriptive name for the user (displayed in requests)
     * @return A Pairing object
     * @throws RequestError
     *             Thrown when an exceptional condition is encountered
     */
    public Pairing pairWithQrCode(String userName) throws RequestError {
        return this.pairWithQrCode(userName, null);
    }

    /**
     * Create a pairing that is presented as a QR code
     *
     * @param userName
     *            A user-facing descriptive name for the user (displayed in requests)
     * @param extras
     *            An optional Map of extra parameters to provide to the API
     * @return A Pairing object
     * @throws RequestError
     *             Thrown when an exceptional condition is encountered
     */
    public Pairing pairWithQrCode(String userName, Map<String, String> extras) throws RequestError {
        final String endpoint = "pairings/create/qr";

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("user_name", userName));

        JSONObject json = post(endpoint, params, extras);
        try {
            return new Pairing(json);
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
     * @return An AuthenticationRequest object
     * @throws RequestError
     *             Thrown when an exceptional condition is encountered
     */
    public AuthenticationRequest authenticate(String pairingId, String terminalName) throws RequestError {
        return authenticate(pairingId, terminalName, null, null);
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
     * @return An AuthenticationRequest object
     * @throws RequestError
     *             Thrown when an exceptional condition is encountered
     */
    public AuthenticationRequest authenticate(String pairingId, String terminalName, String actionName) throws RequestError {
        return authenticate(pairingId, terminalName, actionName, null);
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
     * @return An AuthenticationRequest object
     * @throws RequestError
     *             Thrown when an exceptional condition is encountered
     */
    public AuthenticationRequest authenticate(String pairingId, String terminalName,
                                             String actionName, Map<String, String> extras) throws RequestError {
        final String endpoint = "authentication_requests/initiate";

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        if (pairingId != null) {
            params.add(new BasicNameValuePair("pairing_id", pairingId));
        }
        if (terminalName != null) {
            params.add(new BasicNameValuePair("terminal_name", terminalName));
        }
        if (actionName != null && actionName.length() > 0) {
            params.add(new BasicNameValuePair("action_name", actionName));
        }

        JSONObject json = post(endpoint, params, extras);
        try {
            return new AuthenticationRequest(json);
        } catch (Exception e) {
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
     * @return An AuthenticationRequest object
     * @throws RequestError
     *             Thrown when an exceptional condition is encountered
     */
    public AuthenticationRequest authenticateByUserName(String userName, String terminalNameExtra, String actionName, Map<String, String> extras) throws RequestError {
        if (extras == null) {
            extras = new HashMap<String, String>();
        }
        extras.put("user_name", userName);
        extras.put("terminal_name_extra", terminalNameExtra);

        return authenticate(null, null, actionName, extras);
    }

    /**
     * Retrieve status information for an authentication request
     *
     * @param authenticationRequestId
     *            The authentication request ID
     * @return An AuthenticationRequest object
     * @throws RequestError
     *             Thrown when an exceptional condition is encountered
     */
    public AuthenticationRequest getAuthenticationStatus(String authenticationRequestId)
            throws RequestError {
        final String endpoint = String.format("authentication_requests/%s", authenticationRequestId);

        JSONObject json = get(endpoint);
        try {
            return new AuthenticationRequest(json);
        } catch (Exception e) {
            throw new RequestError(e);
        }
    }

    public AuthenticationRequest getAuthenticationStatusWithOTP(String authenticationRequestId,String OTP) throws RequestError {
        final String endpoint = String.format("authentication_requests/%s/otp_auth", authenticationRequestId);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("otp", OTP));
        JSONObject json = post(endpoint, params, null);
        try {
            return new AuthenticationRequest(json);
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

    private <T> T request(HttpRequestBase httpRequest, String endpoint, List<NameValuePair> queryStringParameters) throws RequestError {
        try {
            URIBuilder uriBuilder = new URIBuilder().setScheme(this.uriScheme).setHost(this.uriHost)
    		    	.setPort(this.uriPort)
                    .setPath(this.uriBase + endpoint);
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

    class AdvancedApiUsageFactory {
        public final Pairings pairings;
        public final AuthenticationRequests authenticationRequests;
        public final Users users;
        public final UserTerminals userTerminals;

        public AdvancedApiUsageFactory(String consumerKey, String consumerSecret) {
            pairings = new Pairings();
            authenticationRequests = new AuthenticationRequests();
            users = new Users();
            userTerminals = new UserTerminals();
        }

        class Pairings {
            /**
             * Retrieve the current status of a pairing
             *
             * @param pairingId
             *          The unique id for a pairing
             * @return
             *          A Pairing object
             * @throws RequestError
             *          Thrown when an exceptional condition is encountered
             */
            public Pairing getById(String pairingId) throws RequestError {
                final String endpoint = String.format("pairings/%s", pairingId);

                JSONObject json = get(endpoint);
                try {
                    return new Pairing(json);
                } catch (Exception e) {
                    throw new RequestError(e);
                }
            }
        }

        class AuthenticationRequests {
            /**
             * Retrieve the current status of an authentication request
             *
             * @param authenticationRequestId
             *          The unique id for an authentication request
             * @return
             *          An AuthenticationRequest object
             * @throws RequestError
             *          Thrown when an exceptional condition is encountered
             */
            public AuthenticationRequest getById(String authenticationRequestId) throws RequestError {
                final String endpoint = String.format("authentication_requests/%s", authenticationRequestId);

                JSONObject json = get(endpoint);
                try {
                    return new AuthenticationRequest(json);
                } catch (Exception e) {
                    throw new RequestError(e);
                }
            }
        }

        class Users {
            /**
             * Create a new user with a userName
             *
             * @param userName
             *          The name of the user
             * @return
             *          A User object
             * @throws RequestError
             *          Thrown when an exceptional condition is encountered
             */
            public User create(String userName) throws RequestError {
                Map<String, String> extras = new HashMap<String, String>();
                return create(userName, extras);
            }

            /**
             * Create a new user with a userName
             *
             * @param userName
             *          The name of the user
             * @param extras
             *          An optional Map of extra parameters to provide to the API
             * @return
             *          A User object
             * @throws RequestError
             *          Thrown when an exceptional condition is encountered
             */
            public User create(String userName, Map<String, String> extras) throws RequestError {
                final String endpoint = "users/create";
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                JSONObject result;

                for (Map.Entry<String, String> entry : extras.entrySet()) {
                    params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }

                try {
                    result = post(endpoint, params, null);
                } catch (Exception e) {
                    throw new RequestError(e);
                }

                return new User(result);
            }

            /**
             * Retrieve the current status of a user with the user id
             *
             * @param userId
             *          The unique id for a user
             * @return
             *          A User object
             * @throws RequestError
             *          Thrown when an exceptional condition is encountered
             */
            public User getById(String userId) throws RequestError {
                final String endpoint = String.format("users/%s", userId);

                JSONObject json = get(endpoint);
                try {
                    return new User(json);
                } catch (Exception e) {
                    throw new RequestError(e);
                }
            }

            /**
             * Retrieve the current status of a user with the user name
             * @param name
             *          The name of the user
             * @return
             *          A User object
             * @throws RequestError
             *          Thrown when an exceptional condition is encountered
             */
            public User getByName(String name) throws RequestError {
                final String endpoint = "users";
                JSONArray result;

                List params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("user_name", name));
                try {
                    result = (JSONArray) get(endpoint, params, null);
                } catch (Exception e) {
                    throw new RequestError(e);
                }


                if (result.length() > 1) {
                    throw new RequestError("More than one user with name {0}".format(name));
                }
                if (result.length() == 0) {
                    throw new RequestError("No users with name {0}".format(name));
                }
                String userId = result.getJSONObject(0).getString("id");
                return getById(userId);
            }
        }

        class UserTerminals {
            /**
             * Retrieve the current status of a user terminal by terminal id
             *
             * @param terminalId
             *          The unique id for a user terminal
             * @return
             *          A UserTerminal object
             * @throws RequestError
             *          Thrown when an exceptional condition is encountered
             */
            public UserTerminal getById(String terminalId) throws RequestError {
                final String endpoint = String.format("/user_terminals/%s", terminalId);
                JSONObject result = get(endpoint);
                return new UserTerminal(result);
            }

        }
    }
}
