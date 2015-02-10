package com.toopher;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.util.*;

import static org.junit.Assert.*;


public class TestPairing {
    private static final String DEFAULT_BASE_URL = "https://api.toopher.test/v1/";

    private String id;
    private String userName;
    private JSONObject jsonResponse;

    @Before
    public void setUp() {
        this.id = UUID.randomUUID().toString();

        JSONObject user = new JSONObject();
        user.put("id", UUID.randomUUID().toString());
        user.put("name", "userName");
        this.userName = user.getString("name");

        this.jsonResponse = new JSONObject();
        jsonResponse.put("id", id);
        jsonResponse.put("enabled", true);
        jsonResponse.put("pending", false);
        jsonResponse.put("user", user);
    }

    @Test
    public void testRefreshFromServer() throws InterruptedException, RequestError {
        JSONObject newJsonResponse = new JSONObject();
        newJsonResponse.put("enabled", true);
        newJsonResponse.put("pending", false);
        JSONObject newUser = new JSONObject();
        newUser.put("name", "userNameChanged");
        newJsonResponse.put("user", newUser);

        HttpClientMock httpClient = new HttpClientMock(200, newJsonResponse.toString());
        ToopherApi toopherApi = new ToopherApi("key", "secret",
                URI.create(DEFAULT_BASE_URL), httpClient);
        Pairing pairing = new Pairing(jsonResponse, toopherApi);

        assertEquals(id, pairing.id);
        assertEquals(userName, pairing.user.name);

        pairing.refreshFromServer();

        assertEquals(id, pairing.id);
        assertEquals("userNameChanged", pairing.user.name);
    }

    @Test
    public void testGetQrCodeImage() throws InterruptedException, IOException {
        BufferedImage bi = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bi, "png", baos);
        baos.flush();
        byte[] imageInByte = baos.toByteArray();
        baos.close();
        bi.flush();

        HttpClientMock httpClient = new HttpClientMock(200, imageInByte.toString());
        ToopherApi toopherApi = new ToopherApi("key", "secret",
                URI.create(DEFAULT_BASE_URL), httpClient);
        Pairing pairing = new Pairing(jsonResponse, toopherApi);
        pairing.getQrCodeImage();

        assertEquals("GET", httpClient.getLastCalledMethod());
        assertEquals(200, httpClient.getExpectedResponseStatus());
    }

    @Test
    public void testGetResetLink() throws InterruptedException, RequestError {
        String resetLink = String.format("http://api.toopher.test/v1/pairings/%s/reset?reset_authorization=abcde", id);
        JSONObject urlJsonResponse = new JSONObject();
        urlJsonResponse.put("url", resetLink);

        HttpClientMock httpClient = new HttpClientMock(200, urlJsonResponse.toString());
        ToopherApi toopherApi = new ToopherApi("key", "secret",
                URI.create(DEFAULT_BASE_URL), httpClient);
        Pairing pairing = new Pairing(jsonResponse, toopherApi);
        String returnedResetLink = pairing.getResetLink();

        assertEquals("POST", httpClient.getLastCalledMethod());
        assertEquals(resetLink, returnedResetLink);
    }

    @Test
    public void testGetResetLinkWithExtras() throws InterruptedException, RequestError {
        String resetLink = String.format("http://api.toopher.test/v1/pairings/%s/reset?reset_authorization=abcde", id);
        JSONObject urlJsonResponse = new JSONObject();
        urlJsonResponse.put("url", resetLink);
        Map<String, String> extras = new HashMap<String, String>();
        extras.put("security_question", "is this a test?");
        extras.put("security_answer", "yes!");

        HttpClientMock httpClient = new HttpClientMock(200, urlJsonResponse.toString());
        ToopherApi toopherApi = new ToopherApi("key", "secret",
                URI.create(DEFAULT_BASE_URL), httpClient);
        Pairing pairing = new Pairing(jsonResponse, toopherApi);
        String returnedResetLink = pairing.getResetLink(extras);

        assertEquals("POST", httpClient.getLastCalledMethod());
        assertEquals(resetLink, returnedResetLink);
        assertEquals("is this a test?", httpClient.getLastCalledData("security_question"));
        assertEquals("yes!", httpClient.getLastCalledData("security_answer"));
    }

    @Test
    public void testEmailResetLink() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(201, "[]");
        ToopherApi toopherApi = new ToopherApi("key", "secret",
                URI.create(DEFAULT_BASE_URL), httpClient);
        Pairing pairing = new Pairing(jsonResponse, toopherApi);
        pairing.emailResetLink("email");

        assertEquals("POST", httpClient.getLastCalledMethod());
        assertEquals("email", httpClient.getLastCalledData("reset_email"));
    }

    @Test
    public void testEmailResetLinkWithExtras() throws InterruptedException, RequestError {
        Map<String, String> extras = new HashMap<String, String>();
        extras.put("one_extra_key", "one_extra_value");

        HttpClientMock httpClient = new HttpClientMock(201, "[]");
        ToopherApi toopherApi = new ToopherApi("key", "secret",
                URI.create(DEFAULT_BASE_URL), httpClient);
        Pairing pairing = new Pairing(jsonResponse, toopherApi);
        pairing.emailResetLink("email", extras);

        assertEquals("POST", httpClient.getLastCalledMethod());
        assertEquals("email", httpClient.getLastCalledData("reset_email"));
        assertEquals("one_extra_value", httpClient.getLastCalledData("one_extra_key"));
    }
}
