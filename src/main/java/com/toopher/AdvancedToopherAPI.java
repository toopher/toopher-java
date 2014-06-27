package com.toopher;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpProtocolParams;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.ArrayList;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by drew on 6/27/14.
 */
public class AdvancedToopherAPI extends ToopherAPI {

    /**
     * Create an API object with the supplied credentials
     *
     * @param consumerKey
     *            The consumer key for a requester (obtained from the developer portal)
     * @param consumerSecret
     *            The consumer secret for a requester (obtained from the developer portal)
     */
    public AdvancedToopherAPI(String consumerKey, String consumerSecret) {
        super(consumerKey, consumerSecret, (URI) null);
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
     * @throws java.net.URISyntaxException
     */
    public AdvancedToopherAPI(String consumerKey, String consumerSecret, String uri) throws URISyntaxException {
        super(consumerKey, consumerSecret, new URI(uri));
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
    public AdvancedToopherAPI(String consumerKey, String consumerSecret, String uriScheme, String uriHost, String uriBase) throws URISyntaxException {
        super(consumerKey, consumerSecret, new URIBuilder().setScheme(uriScheme).setHost(uriHost).setPath(uriBase).build());
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
    public AdvancedToopherAPI(String consumerKey, String consumerSecret, URI uri) {
        super(consumerKey, consumerSecret, uri, null);
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
    public AdvancedToopherAPI(String consumerKey, String consumerSecret, URI uri, HttpClient httpClient) {
        super(consumerKey, consumerSecret, uri, httpClient);
    }

    public List<PairingStatus> getPairingsForUser(String userName) throws RequestError {
        String userId = getUserId(userName);
        String endpoint = String.format("users/%s/pairings", userId);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("deactivated", "0"));
        JSONArray result = get(endpoint, params, null);

        List<PairingStatus> results = new ArrayList<PairingStatus>(result.length());
        for(int x = 0; x < result.length(); x++) {
            JSONObject o = result.getJSONObject(x);
            results.add(new PairingStatus(o));
        }
        return results;
    }

    public PairingStatus deactivatePairing(String pairingId) throws RequestError {
        String endpoint = String.format("pairings/%s", pairingId);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("deactivated", "True"));
        JSONObject json = post(endpoint, params, null);
        return new PairingStatus(json);
    }

    public void deactivateUserPairings(String userName) throws RequestError {
        for(PairingStatus p : getPairingsForUser(userName)) {
            String endpoint = String.format("pairings/%s", p.id);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("deactivated", "True"));
            post(endpoint, params, null);
        }
    }
}
