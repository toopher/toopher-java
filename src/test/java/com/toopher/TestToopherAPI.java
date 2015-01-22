package com.toopher;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.*;

public class TestToopherAPI {
    @Test
    public void testCreatePairing() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(200,
                "{'id':'1','enabled':true,'pending':true,'user':{'id':'1','name':'some user'}}".replace("'", "\""));

        ToopherAPI toopherApi = new ToopherAPI("key", "secret",
                createURI("https://api.toopher.test/v1"), httpClient);
        Pairing pairing = toopherApi.pair("awkward turtle", "some user");

        assertEquals(httpClient.getLastCalledMethod(), "POST");
        assertEquals(httpClient.getLastCalledData("pairing_phrase"), "awkward turtle");
        
        assertEquals(pairing.user.id, "1");
        assertEquals(pairing.user.name, "some user");
        assertTrue(pairing.pending);
        assertTrue(pairing.enabled);
    }

    @Test
    public void testGetPairing() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(200,
                "{'id':'1','enabled':true,'pending':true,'user':{'id':'1','name':'some user'}}".replace("'", "\""));

        ToopherAPI toopherApi = new ToopherAPI("key", "secret",
                createURI("https://api.toopher.test/v1"), httpClient);
        Pairing pairing = toopherApi.getPairingStatus("1");

        assertEquals(httpClient.getLastCalledMethod(), "GET");

        assertEquals(pairing.user.id, "1");
        assertEquals(pairing.user.name, "some user");
        assertTrue(pairing.pending);
        assertTrue(pairing.enabled);
    }

    @Test
    public void testCreateQrPairing() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(200,
                "{'id':'1','enabled':true,'pending':true,'user':{'id':'1','name':'some user'}}".replace("'", "\""));

        ToopherAPI toopherApi = new ToopherAPI("key", "secret",
                createURI("https://api.toopher.test/v1"), httpClient);
        Pairing pairing = toopherApi.pairWithQrCode("some user");

        assertEquals(httpClient.getLastCalledMethod(), "POST");
        assertEquals(pairing.user.id, "1");
        assertEquals(pairing.user.name, "some user");
        assertTrue(pairing.pending);
        assertTrue(pairing.enabled);
    }

    private URI createURI(String url) {
        try {
            return new URL(url).toURI();
        } catch (MalformedURLException e) {
            return null;
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private boolean isValidURL(String url) {
        return createURI(url) != null;
    }

    @Test
    public void testBaseURL() {
        assertNotNull("Base URL is null.", ToopherAPI.getBaseURL());
        assertTrue("Base URL is not valid.", isValidURL(ToopherAPI.getBaseURL()));
    }

    @Test
    public void testVersion() {
        assertNotNull("Version is not null.", ToopherAPI.VERSION);
    }
}
