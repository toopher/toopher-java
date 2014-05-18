package com.toopher;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.*;
import org.apache.http.entity.*;
import org.apache.http.impl.client.*;
import org.apache.http.message.*;
import org.apache.http.params.*;
import org.apache.http.protocol.*;
import org.junit.*;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

@Ignore
public class HttpClientMock extends DefaultHttpClient {
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
