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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.concurrent.Semaphore;

@Ignore
public class HttpClientMock extends DefaultHttpClient {
    private static final String DEFAULT_BASE_URL = "https://api.toopher.test/v1/";

    public HttpParams lastParams;
    public Semaphore done;

    private HttpUriRequest lastRequest;
    private int expectedResponseStatus;
    private String expectedResponseBody;
    private URI lastURI;

    public HttpClientMock(int responseStatus, String responseBody) throws InterruptedException {
        this.expectedResponseStatus = responseStatus;
        this.expectedResponseBody = responseBody;
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

    public String getLastCalledEndpoint() {
        String fullUri = lastURI.toString();
        int param = lastURI.toString().lastIndexOf('?');
        if (param != -1) {
            fullUri = fullUri.substring(0, param);
        }
        return fullUri.replace(DEFAULT_BASE_URL, "");

    }

    public String getExpectedResponse() {
        return expectedResponseBody;
    }

    public int getExpectedResponseStatus() {
        return expectedResponseStatus;
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
        try {
            entity.setContent(new ByteArrayInputStream(expectedResponseBody.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        HttpResponse resp = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, expectedResponseStatus, null));
        resp.setEntity(entity);
        lastURI = req.getURI();
        T result = responseHandler.handleResponse(resp);

        done.release();
        return result;
    }
}
