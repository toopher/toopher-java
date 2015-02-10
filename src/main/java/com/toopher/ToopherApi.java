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
import java.util.UUID;

/**
 * A Java binding for the Toopher API
 */
public class ToopherApi {
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
    public static final String VERSION = "2.0.0";

    /**
     * Create an API object with the supplied credentials
     *
     * @param consumerKey    The consumer key for a requester (obtained from the developer portal)
     * @param consumerSecret The consumer secret for a requester (obtained from the developer portal)
     */
    public ToopherApi(String consumerKey, String consumerSecret) {
        this(consumerKey, consumerSecret, (URI) null);
    }


    /**
     * Create an API object with the supplied credentials, overriding the default API URI of https://api.toopher.com/v1/
     *
     * @param consumerKey    The consumer key for a requester (obtained from the developer portal)
     * @param consumerSecret The consumer secret for a requester (obtained from the developer portal)
     * @param uri            The uri of the Toopher API
     * @throws java.net.URISyntaxException
     */
    public ToopherApi(String consumerKey, String consumerSecret, String uri) throws URISyntaxException {
        this(consumerKey, consumerSecret, new URI(uri));
    }

    /**
     * Create an API object with the supplied credentials, overriding the default API URI of https://api.toopher.com/v1/
     *
     * @param consumerKey    The consumer key for a requester (obtained from the developer portal)
     * @param consumerSecret The consumer secret for a requester (obtained from the developer portal)
     * @param uriScheme      The uri scheme ( http or https )
     * @param uriHost        The uri host ( api.toopher.com )
     * @param uriScheme      The uri base ( /v1/ )
     * @throws java.net.URISyntaxException
     */
    public ToopherApi(String consumerKey, String consumerSecret, String uriScheme, String uriHost, String uriBase) throws URISyntaxException {
        this(consumerKey, consumerSecret, new URIBuilder().setScheme(uriScheme).setHost(uriHost).setPath(uriBase).build());
    }

    /**
     * Create an API object with the supplied credentials, overriding the default API URI of https://api.toopher.com/v1/
     *
     * @param consumerKey    The consumer key for a requester (obtained from the developer portal)
     * @param consumerSecret The consumer secret for a requester (obtained from the developer portal)
     * @param uri            The alternate URI
     */
    public ToopherApi(String consumerKey, String consumerSecret, URI uri) {
        this(consumerKey, consumerSecret, uri, null);
    }

    /**
     * Create an API object with the supplied credentials and API URI,
     * overriding the default HTTP client.
     *
     * @param consumerKey    The consumer key for a requester (obtained from the developer portal)
     * @param consumerSecret The consumer secret for a requester (obtained from the developer portal)
     * @param uri            The alternate URI
     * @param httpClient     The alternate HTTP client
     */
    public ToopherApi(String consumerKey, String consumerSecret, URI uri, HttpClient httpClient) {
        this.advanced = new AdvancedApiUsageFactory(this);
        if (httpClient == null) {
            this.httpClient = new DefaultHttpClient();
            HttpProtocolParams.setUserAgent(this.httpClient.getParams(),
                    String.format("Toopher-Java/%s", VERSION));
        } else {
            this.httpClient = httpClient;
        }

        consumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);
        if (uri == null) {
            this.uriScheme = ToopherApi.DEFAULT_URI_SCHEME;
            this.uriHost = ToopherApi.DEFAULT_URI_HOST;
            this.uriPort = ToopherApi.DEFAULT_URI_PORT;
            this.uriBase = ToopherApi.DEFAULT_URI_BASE;
        } else {
            this.uriScheme = uri.getScheme();
            this.uriHost = uri.getHost();
            this.uriPort = uri.getPort();
            this.uriBase = uri.getPath();
        }
    }

    /**
     * Create a QR pairing
     *
     * @param userName A user-facing descriptive name for the user (displayed in requests)
     * @return A Pairing object
     * @throws com.toopher.RequestError Thrown when an exceptional condition is encountered
     */
    public Pairing pair(String userName) throws RequestError, JSONException {
        return this.pair(userName, null, null);
    }

    /**
     * Create an SMS pairing or regular pairing
     *
     * @param pairingPhraseOrNum The pairing phrase or phone number supplied by the user
     * @param userName           A user-facing descriptive name for the user (displayed in requests)
     * @return A Pairing object
     * @throws com.toopher.RequestError Thrown when an exceptional condition is encountered
     */
    public Pairing pair(String userName, String pairingPhraseOrNum) throws RequestError, JSONException {
        return this.pair(userName, pairingPhraseOrNum, null);
    }

    /**
     * Create an SMS pairing, QR pairing or regular pairing
     *
     * @param pairingPhraseOrNum The pairing phrase or phone number supplied by the user
     * @param userName           A user-facing descriptive name for the user (displayed in requests)
     * @param extras             An optional Map of extra parameters to provide to the API
     * @return A Pairing object
     * @throws com.toopher.RequestError Thrown when an exceptional condition is encountered
     */
    public Pairing pair(String userName, String pairingPhraseOrNum, Map<String, String> extras) throws RequestError, JSONException {
        String endpoint;
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        params.add(new BasicNameValuePair("user_name", userName));

        if (pairingPhraseOrNum != null) {
            if (pairingPhraseOrNum.matches("\\d+")) {
                params.add(new BasicNameValuePair("phone_number", pairingPhraseOrNum));
                endpoint = "pairings/create/sms";
            } else {
                params.add(new BasicNameValuePair("pairing_phrase", pairingPhraseOrNum));
                endpoint = "pairings/create";
            }
        } else {
            endpoint = "pairings/create/qr";
        }

        JSONObject result = advanced.raw.post(endpoint, params, extras);
        return new Pairing(result, this);
    }

    /**
     * Initiate a login authentication request
     *
     * @param pairingIdOrUsername             The pairing id indicating to whom the request should be sent
     * @param terminalNameOrTerminalNameExtra The user-facing descriptive name for the terminal from which the request originates
     * @return An AuthenticationRequest object
     * @throws com.toopher.RequestError Thrown when an exceptional condition is encountered
     */
    public AuthenticationRequest authenticate(String pairingIdOrUsername, String terminalNameOrTerminalNameExtra) throws RequestError, JSONException {
        return authenticate(pairingIdOrUsername, terminalNameOrTerminalNameExtra, null, null);
    }

    /**
     * Initiate a login authentication request
     *
     * @param pairingIdOrUsername             The pairing id indicating to whom the request should be sent
     * @param terminalNameOrTerminalNameExtra The user-facing descriptive name for the terminal from which the request originates
     * @param actionName                      The user-facing descriptive name for the action which is being authenticated
     * @return An AuthenticationRequest object
     * @throws com.toopher.RequestError Thrown when an exceptional condition is encountered
     */
    public AuthenticationRequest authenticate(String pairingIdOrUsername, String terminalNameOrTerminalNameExtra, String actionName) throws RequestError, JSONException {
        return authenticate(pairingIdOrUsername, terminalNameOrTerminalNameExtra, actionName, null);
    }

    /**
     * Initiate an authentication request by pairing id or username
     *
     * @param pairingIdOrUsername             The pairing id or username indicating to whom the request should be sent
     * @param terminalNameOrTerminalNameExtra The user-facing descriptive name for the terminal from which the request originates
     *                                        or the unique identifier for this terminal.  Not displayed to the user.
     * @param actionName                      The user-facing descriptive name for the action which is being authenticated
     * @param extras                          An optional Map of extra parameters to provide to the API
     * @return An AuthenticationRequest object
     * @throws com.toopher.RequestError Thrown when an exceptional condition is encountered
     */
    public AuthenticationRequest authenticate(String pairingIdOrUsername, String terminalNameOrTerminalNameExtra, String actionName, Map<String, String> extras) throws RequestError, JSONException {
        final String endpoint = "authentication_requests/initiate";
        List<NameValuePair> params = new ArrayList<NameValuePair>();

        try {
            UUID.fromString(pairingIdOrUsername);
            params.add(new BasicNameValuePair("pairing_id", pairingIdOrUsername));
            params.add(new BasicNameValuePair("terminal_name", terminalNameOrTerminalNameExtra));
        } catch (Exception e) {
            params.add(new BasicNameValuePair("user_name", pairingIdOrUsername));
            params.add(new BasicNameValuePair("terminal_name_extra", terminalNameOrTerminalNameExtra));
        }

        if (actionName != null && actionName.length() > 0) {
            params.add(new BasicNameValuePair("action_name", actionName));
        }

        JSONObject json = advanced.raw.post(endpoint, params, extras);
        return new AuthenticationRequest(json, this);
    }

    public static String getBaseURL() {
        return String.format("%s://%s%s", DEFAULT_URI_SCHEME,
                DEFAULT_URI_HOST, DEFAULT_URI_BASE);
    }

    /**
     * Extracts JSON response object from API response
     */
    private static ResponseHandler<Object> jsonHandler = new ResponseHandler<Object>() {

        @Override
        public Object handleResponse(HttpResponse response) throws IOException, ClientProtocolException {
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() >= 300) {
                parseRequestError(statusLine, response);
            }

            HttpEntity entity = response.getEntity();
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

    /**
     * Extracts QR image byte[] from API response
     */
    private static ResponseHandler<Object> qrResponseHandler = new ResponseHandler<Object>() {

        @Override
        public Object handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() >= 300) {
                parseRequestError(statusLine, response);
            }

            HttpEntity entity = response.getEntity();
            byte[] content;
            content = (entity != null) ? EntityUtils.toByteArray(entity) : null;

            if (content != null) {
                return content;
            } else {
                throw new RequestError("Empty response body returned");
            }
        }
    };

    /**
     * Throws new error message based on status code provided in API response
     *
     * @param statusLine StatusLine object from API response
     * @param response   API response
     * @throws com.toopher.RequestError Thrown when exceptional condition is encountered
     */
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
        public final ApiRawRequester raw;

        public AdvancedApiUsageFactory(ToopherApi api) {
            this.pairings = new Pairings(api);
            this.authenticationRequests = new AuthenticationRequests(api);
            this.users = new Users(api);
            this.userTerminals = new UserTerminals(api);
            this.raw = new ApiRawRequester();
        }

        class Pairings {
            public final ToopherApi api;

            public Pairings(ToopherApi toopherApi) {
                this.api = toopherApi;
            }

            /**
             * Retrieve the current status of a pairing
             *
             * @param pairingId The unique id for a pairing
             * @return A Pairing object
             * @throws com.toopher.RequestError Thrown when an exceptional condition is encountered
             */
            public Pairing getById(String pairingId) throws RequestError, JSONException {
                final String endpoint = String.format("pairings/%s", pairingId);
                JSONObject json = advanced.raw.get(endpoint);
                return new Pairing(json, api);
            }
        }

        class AuthenticationRequests {
            public final ToopherApi api;

            public AuthenticationRequests(ToopherApi toopherApi) {
                this.api = toopherApi;
            }

            /**
             * Retrieve the current status of an authentication request
             *
             * @param authenticationRequestId The unique id for an authentication request
             * @return An AuthenticationRequest object
             * @throws com.toopher.RequestError Thrown when an exceptional condition is encountered
             */
            public AuthenticationRequest getById(String authenticationRequestId) throws RequestError, JSONException {
                final String endpoint = String.format("authentication_requests/%s", authenticationRequestId);
                JSONObject json = advanced.raw.get(endpoint);
                return new AuthenticationRequest(json, api);
            }
        }

        class Users {
            public final ToopherApi api;

            public Users(ToopherApi toopherApi) {
                this.api = toopherApi;
            }

            /**
             * Create a new user with a userName
             *
             * @param userName The name of the user
             * @return A User object
             * @throws com.toopher.RequestError Thrown when an exceptional condition is encountered
             */
            public User create(String userName) throws RequestError, JSONException {
                return create(userName, null);
            }

            /**
             * Create a new user with a userName
             *
             * @param userName The name of the user
             * @param extras   An optional Map of extra parameters to provide to the API
             * @return A User object
             * @throws com.toopher.RequestError Thrown when an exceptional condition is encountered
             */
            public User create(String userName, Map<String, String> extras) throws RequestError, JSONException {
                final String endpoint = "users/create";
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("name", userName));

                JSONObject result = advanced.raw.post(endpoint, params, extras);
                return new User(result, api);
            }

            /**
             * Retrieve the current status of a user with the user id
             *
             * @param userId The unique id for a user
             * @return A User object
             * @throws com.toopher.RequestError Thrown when an exceptional condition is encountered
             */
            public User getById(String userId) throws RequestError, JSONException {
                final String endpoint = String.format("users/%s", userId);
                JSONObject json = advanced.raw.get(endpoint);
                return new User(json, api);
            }

            /**
             * Retrieve the current status of a user with the user name
             *
             * @param name The name of the user
             * @return A User object
             * @throws com.toopher.RequestError Thrown when an exceptional condition is encountered
             */
            public User getByName(String name) throws RequestError, JSONException {
                final String endpoint = "users";
                List params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("name", name));

                JSONArray result = (JSONArray) advanced.raw.get(endpoint, params);

                if (result.length() > 1) {
                    throw new RequestError(String.format("More than one user with name %s", name));
                }
                if (result.length() == 0) {
                    throw new RequestError(String.format("No users with name %s", name));
                }

                String userId = result.getJSONObject(0).getString("id");
                return getById(userId);
            }
        }

        class UserTerminals {
            public final ToopherApi api;

            public UserTerminals(ToopherApi toopherApi) {
                this.api = toopherApi;
            }

            /**
             * @param userName             The name of the user
             * @param terminalName         The user-facing descriptive name for the terminal from which the request originates
             * @param requesterSpecifiedId The requester specified id that uniquely identifies this terminal. Can be shared
             *                             across multiple users. The combination of userName and requesterSpecifiedId should
             *                             be unique for a requester
             * @return A UserTerminal object
             * @throws com.toopher.RequestError Thrown when an exceptional condition is encountered
             */
            public UserTerminal create(String userName, String terminalName, String requesterSpecifiedId) throws RequestError, JSONException {
                return create(userName, terminalName, requesterSpecifiedId, null);
            }

            /**
             * Create a new user terminal
             *
             * @param userName             The name of the user
             * @param terminalName         The user-facing descriptive name for the terminal from which the request originates
             * @param requesterSpecifiedId The requester specified id that uniquely identifies this terminal.  Can be shared
             *                             across multiple users.  The combination of userName and terminalNameExtra should
             *                             be unique for a requester
             * @param extras               An optional Map of extra parameters to provide to the API
             * @return A UserTerminal object
             * @throws com.toopher.RequestError Thrown when an exceptional condition is encountered
             */
            public UserTerminal create(String userName, String terminalName, String requesterSpecifiedId, Map<String, String> extras) throws RequestError, JSONException {
                final String endpoint = "user_terminals/create";
                List<NameValuePair> params = new ArrayList<NameValuePair>();

                params.add(new BasicNameValuePair("user_name", userName));
                params.add(new BasicNameValuePair("name", terminalName));
                params.add(new BasicNameValuePair("name_extra", requesterSpecifiedId));

                JSONObject result = advanced.raw.post(endpoint, params, extras);
                return new UserTerminal(result, api);
            }

            /**
             * Retrieve the current status of a user terminal by terminal id
             *
             * @param terminalId The unique id for a user terminal
             * @return A UserTerminal object
             * @throws com.toopher.RequestError Thrown when an exceptional condition is encountered
             */
            public UserTerminal getById(String terminalId) throws RequestError, JSONException {
                final String endpoint = String.format("user_terminals/%s", terminalId);
                JSONObject result = advanced.raw.get(endpoint);
                return new UserTerminal(result, api);
            }
        }

        class ApiRawRequester {
            public <T> T get(String endpoint) throws RequestError, JSONException {
                return request(new HttpGet(), endpoint, null);
            }

            public <T> T get(String endpoint, List<NameValuePair> params) throws RequestError, JSONException {
                return request(new HttpGet(), endpoint, params);
            }

            public <T> T get(String endpoint, List<NameValuePair> params, Map<String, String> extras) throws RequestError, JSONException {
                if (extras != null && extras.size() > 0) {
                    for (Map.Entry<String, String> e : extras.entrySet()) {
                        params.add(new BasicNameValuePair(e.getKey(), e.getValue()));
                    }
                }
                return request(new HttpGet(), endpoint, params);
            }

            public <T> T post(String endpoint) throws RequestError, JSONException {
                return request(new HttpPost(), endpoint, null);
            }

            public <T> T post(String endpoint, List<NameValuePair> params) throws RequestError, JSONException {
                return post(endpoint, params, null);
            }

            public <T> T post(String endpoint, Map<String, String> extras) throws RequestError, JSONException {
                return post(endpoint, null, extras);
            }

            public <T> T post(String endpoint, List<NameValuePair> params, Map<String, String> extras) throws RequestError, JSONException {
                HttpPost post = new HttpPost();
                if (params == null) {
                    params = new ArrayList<NameValuePair>();
                }
                if (extras != null && extras.size() > 0) {
                    for (Map.Entry<String, String> e : extras.entrySet()) {
                        params.add(new BasicNameValuePair(e.getKey(), e.getValue()));
                    }
                }
                if (params.size() > 0) {
                    try {
                        post.setEntity(new UrlEncodedFormEntity(params));
                    } catch (Exception e) {
                        throw new RequestError(e);
                    }
                }
                return request(post, endpoint, null);
            }

            private <T> T request(HttpRequestBase httpRequest, String endpoint, List<NameValuePair> queryStringParameters) throws RequestError, JSONException {
                Boolean isQrImageEndpoint = endpoint.contains("qr/pairings/");
                try {
                    URIBuilder uriBuilder = new URIBuilder().setScheme(uriScheme).setHost(uriHost)
                            .setPort(uriPort)
                            .setPath(uriBase + endpoint);
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
                    if (isQrImageEndpoint) {
                        return (T) httpClient.execute(httpRequest, qrResponseHandler);
                    } else {
                        return (T) httpClient.execute(httpRequest, jsonHandler);
                    }
                } catch (RequestError re) {
                    throw re;
                } catch (Exception e) {
                    throw new RequestError(e);
                }
            }
        }
    }
}
