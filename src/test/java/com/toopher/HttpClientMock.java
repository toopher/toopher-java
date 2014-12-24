package com.toopher;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.junit.Ignore;

import javax.xml.ws.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

@Ignore
public class HttpClientMock extends DefaultHttpClient {
    public HttpParams lastParams;
    public Semaphore done;

    private java.net.URI lastURI;
    private HttpUriRequest lastRequest;
    private int expectedResponseStatus;
    private String expectedResponseBody;
    private Map<URI, ResponseMock> expectedUriResponses;


    public HttpClientMock(int responseStatus, String responseBody) throws InterruptedException {
        this.expectedResponseStatus = responseStatus;
        this.expectedResponseBody = responseBody;
        done = new Semaphore(1);
        done.acquire();
    }

    public HttpClientMock(Map<URI, ResponseMock> responses) throws InterruptedException {
        expectedUriResponses = new HashMap<URI, ResponseMock>();
        for (URI url : responses.keySet()) {
            expectedUriResponses.put(url, responses.get(url));
        }
        done = new Semaphore(1);
        done.acquire();
    }

    public String getLastCalledMethod() {
        if (lastRequest != null) {
            return lastRequest.getMethod();
        }
        return null;
    }

    public String getLastCalledData(String key) {
        if (lastParams != null) {
            return (String) lastParams.getParameter(key);
        }
        return null;
    }

    public URI getLastCalledEndpoint() {
            return lastURI;
    }

    public String getExpectedResponse() {
        if (expectedUriResponses != null) {
            return expectedUriResponses.get(lastURI).getResponseBody();
        }
        return null;
    }

    public int getExpectedResponseStatus() {
        if (expectedUriResponses != null) {
            return expectedUriResponses.get(lastURI).getStatusCode();
        }
        return -1;
    }

    @Override
    public <T> T execute(HttpUriRequest req, ResponseHandler<? extends T> responseHandler) throws IOException {
        lastRequest = req;
        if (req instanceof HttpPost) {
            try {
                lastParams = new BasicHttpParams();
                for (NameValuePair nvp : URLEncodedUtils.parse(((HttpPost) req).getEntity())) {
                    lastParams.setParameter(nvp.getName(), nvp.getValue());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            lastParams = req.getParams();
        }
        BasicHttpEntity entity = new BasicHttpEntity();
        if (expectedResponseBody == null && expectedUriResponses != null) {
            expectedResponseBody = expectedUriResponses.get(req.getURI()).getResponseBody();
            expectedResponseStatus = expectedUriResponses.get(req.getURI()).getStatusCode();
        }
        try {
            entity.setContent(new ByteArrayInputStream(expectedResponseBody.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        HttpResponse resp = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, expectedResponseStatus, null));
        resp.setEntity(entity);
        lastURI = req.getURI();
        T result;
        result = responseHandler.handleResponse(resp);

        done.release();
        return result;
    }

    public URI createURI(String url) {
        try {
            return new URL(url).toURI();
        } catch (MalformedURLException e) {
            return null;
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
