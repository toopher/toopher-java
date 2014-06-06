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
    private static java.net.URI lastURI;
    public HttpParams lastParams;
    public Semaphore done;

    private HttpUriRequest lastRequest;
    private int expectedResponseStatus;
    private String expectedResponseBody;

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
    public URI getLastCalledEndpoint(){
        if(lastURI != null) {
            return lastURI;
        }
        return null;
    }

    @Override
    public <T> T execute(HttpUriRequest req, ResponseHandler<? extends T> responseHandler) {
        lastRequest = req;
        if (req instanceof HttpPost){
            try {
                lastParams = new BasicHttpParams();
                for(NameValuePair nvp : URLEncodedUtils.parse(((HttpPost) req).getEntity())){
                    lastParams.setParameter(nvp.getName(), nvp.getValue());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            lastParams = req.getParams();
        }
        HttpResponse resp = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, expectedResponseStatus, null));
        BasicHttpEntity entity = new BasicHttpEntity();
        try {
            entity.setContent(new ByteArrayInputStream(expectedResponseBody.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        resp.setEntity(entity);
        lastURI = req.getURI();
        T result;
        try {
            result = responseHandler.handleResponse(resp);
        } catch (IOException e) {
            result = null;
            e.printStackTrace();
        }
        done.release();
        return result;
    }
}
