package com.toopher;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.*;


public class TestPairing {
    private static final String DEFAULT_BASE_URL = "https://api.toopher.test/v1/";

    private String id;
    private JSONObject user;
    private String userId;
    private String userName;
    private JSONObject jsonResponse;
    private Pairing pairing;

    @Before
    public void setUp() {
        this.id = UUID.randomUUID().toString();
        this.user = new JSONObject();
        this.user.put("id", UUID.randomUUID().toString());
        this.user.put("name", "userName");
        this.userId = user.getString("id");
        this.userName = user.getString("name");

        this.jsonResponse = new JSONObject();
        jsonResponse.put("id", id);
        jsonResponse.put("enabled", true);
        jsonResponse.put("pending", false);
        jsonResponse.put("user", user);
    }

    @Test
    public void testRefreshFromServer() throws InterruptedException, RequestError {
        JSONObject newResponse = new JSONObject();
        JSONObject newUser = new JSONObject();
        newUser.put("name", "userNameChanged");
        newResponse.put("user", newUser);
        newResponse.put("enabled", false);
        newResponse.put("pending", true);

        HttpClientMock httpClient = new HttpClientMock(200, newResponse.toString());
        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                URI.create(DEFAULT_BASE_URL), httpClient);
        Pairing pairing = new Pairing(jsonResponse, toopherAPI);
        assertEquals(pairing.id, id);
        assertEquals(pairing.user.name, userName);
        assertTrue(pairing.enabled);
        assertFalse(pairing.pending);

        pairing.refreshFromServer();

        assertEquals(pairing.id, id);
        assertEquals(pairing.user.name, "userNameChanged");
        assertFalse(pairing.enabled);
        assertTrue(pairing.pending);
    }

    @Test
    public void testGetQrCodeImage() throws InterruptedException, RequestError, IOException {
        BufferedImage bi = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bi, "png", baos);
        baos.flush();
        byte[] imageInByte = baos.toByteArray();
        baos.close();
        bi.flush();

        HttpClientMock httpClient = new HttpClientMock(200, imageInByte.toString());
        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                URI.create(DEFAULT_BASE_URL), httpClient);
        Pairing pairing = new Pairing(jsonResponse, toopherAPI);
        pairing.getQrCodeImage();

        assertEquals("GET", httpClient.getLastCalledMethod());
        assertEquals(200, httpClient.getExpectedResponseStatus());
    }

    @Test
    public void testGetResetLink() throws InterruptedException, RequestError {
        JSONObject urlJsonResponse = new JSONObject();
        urlJsonResponse.put("url", String.format("http://api.toopher.test/v1/pairings/%s/reset?reset_authorization=abcde", id));

        HttpClientMock httpClient = new HttpClientMock(200, urlJsonResponse.toString());
        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                URI.create(DEFAULT_BASE_URL), httpClient);
        Pairing pairing = new Pairing(jsonResponse, toopherAPI);
        String resetLink = pairing.getResetLink();

        assertEquals(resetLink, String.format("http://api.toopher.test/v1/pairings/%s/reset?reset_authorization=abcde", id));
    }

    @Test
    public void testGetResetLinkWithExtras() throws InterruptedException, RequestError {
        JSONObject urlJsonResponse = new JSONObject();
        urlJsonResponse.put("url", String.format("http://api.toopher.test/v1/pairings/%s/reset?reset_authorization=abcde", id));

        HttpClientMock httpClient = new HttpClientMock(200, urlJsonResponse.toString());
        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                URI.create(DEFAULT_BASE_URL), httpClient);
        Map<String, String> extras = new HashMap<String, String>();
        extras.put("security_question", "is this a test?");
        extras.put("security_answer", "yes!");
        Pairing pairing = new Pairing(jsonResponse, toopherAPI);
        String resetLink = pairing.getResetLink(extras);

        assertEquals(resetLink, String.format("http://api.toopher.test/v1/pairings/%s/reset?reset_authorization=abcde", id));
    }

    @Test
    public void testEmailResetLink() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(201, "[]");
        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                URI.create(DEFAULT_BASE_URL), httpClient);
        Pairing pairing = new Pairing(jsonResponse, toopherAPI);
        pairing.emailResetLink("email");
        assertEquals(httpClient.getLastCalledMethod(), "POST");
        assertEquals(httpClient.getLastCalledData("reset_email"), "email");
    }

    @Test
    public void testEmailResetLinkWithExtras() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(201, "[]");
        ToopherAPI toopherAPI = new ToopherAPI("key", "secret",
                URI.create(DEFAULT_BASE_URL), httpClient);
        Map<String, String> extras = new HashMap<String, String>();
        extras.put("one_extra", "one_extra");
        Pairing pairing = new Pairing(jsonResponse, toopherAPI);
        pairing.emailResetLink("email", extras);
        assertEquals(httpClient.getLastCalledMethod(), "POST");
        assertEquals(httpClient.getLastCalledData("reset_email"), "email");
        assertEquals(httpClient.getLastCalledData("one_extra"), "one_extra");

    }
}
