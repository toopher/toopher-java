package com.toopher;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.json.JSONTokener;

/**
 * A Java binding for the Toopher API
 * 
 */
public class ToopherAPI {
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
    	this(consumerKey, consumerSecret, null);
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
    	httpClient = new DefaultHttpClient();
        HttpProtocolParams.setUserAgent(httpClient.getParams(),
                                        String.format("ToopherJava/%s", VERSION));

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
     * @return An AuthenticationStatus object
     * @throws RequestError
     *             Thrown when an exceptional condition is encountered
     */
    public AuthenticationStatus authenticate(String pairingId, String terminalName, String actionName) throws RequestError {
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
     * @return An AuthenticationStatus object
     * @throws RequestError
     *             Thrown when an exceptional condition is encountered
     */
    public AuthenticationStatus authenticate(String pairingId, String terminalName,
                                             String actionName, Map<String, String> extras) throws RequestError {
        final String endpoint = "authentication_requests/initiate";

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("pairing_id", pairingId));
        params.add(new BasicNameValuePair("terminal_name", terminalName));
        if (actionName != null && actionName.length() > 0) {
            params.add(new BasicNameValuePair("action_name", actionName));
        }

        JSONObject json = post(endpoint, params, extras);
        try {
            return new AuthenticationStatus(json);
        } catch (Exception e) {
            throw new RequestError(e);
        }
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
    public AuthenticationStatus getAuthenticationStatus(String authenticationRequestId)
            throws RequestError {
        final String endpoint = String.format("authentication_requests/%s", authenticationRequestId);

        JSONObject json = get(endpoint);
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
     *            The pairing id indicating to whom the request should be sent
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


    private JSONObject get(String endpoint) throws RequestError {
    	return request(new HttpGet(), endpoint);
    }

    private JSONObject post(String endpoint, List<NameValuePair> params, Map<String, String> extras) throws RequestError {
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
        return request(post, endpoint);
    }
    
    private JSONObject request(HttpRequestBase httpRequest, String endpoint) throws RequestError {
        try {
    	    httpRequest.setURI(new URIBuilder().setScheme(this.uriScheme).setHost(this.uriHost)
    		    	.setPort(this.uriPort)
                    .setPath(this.uriBase + endpoint).build());
    	    consumer.sign(httpRequest);
        } catch (Exception e) {
            throw new RequestError(e);
        }

        try {
    	    return httpClient.execute(httpRequest, jsonHandler);
        } catch (RequestError re) {
            throw re;
        } catch (Exception e) {
            throw new RequestError(e);
        }

    }

    private static ResponseHandler<JSONObject> jsonHandler = new ResponseHandler<JSONObject>() {

        @Override
        public JSONObject handleResponse(HttpResponse response) throws IOException, ClientProtocolException {
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() >= 300) {
                parseRequestError(statusLine, response);
            }

            HttpEntity entity = response.getEntity(); // TODO: check entity == null
            String json;
            json = (entity != null) ? EntityUtils.toString(entity) : null;

            if (json != null && !json.isEmpty()) {
                try {
                    return (JSONObject) new JSONTokener(json).nextValue();
                } catch (JSONException jex) {
                    throw new RequestError(jex);
                }
            } else {
                throw new RequestError("Empty response body returned");
            }

        }
    };

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
}
