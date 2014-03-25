package com.toopher.test;

import com.toopher.*;
import org.junit.*;
import java.net.*;

import static org.junit.Assert.*;

public class TestToopherAPI {
    @Test
    public void testCreatePairing() throws InterruptedException, RequestError {
        HttpClientMock httpClient = new HttpClientMock(200,
                "{'id':'1','enabled':true,'pending':true,'user':{'id':'1','name':'some user'}}".replace("'", "\""));

        ToopherAPI toopherApi = new ToopherAPI("key", "secret",
                createURI("https://api.toopher.test/v1"), httpClient);
        PairingStatus pairing = toopherApi.pair("awkward turtle", "some user");

        assertEquals(httpClient.getLastCalledMethod(), "POST");
        assertEquals(httpClient.getLastCalledData("pairing_phrase"), "awkward turtle");
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
        assertTrue("Base URL is not valid.",
                   isValidURL(ToopherAPI.getBaseURL()));
    }

    @Test
    public void testVersion() {
        assertNotNull("Version is not null.", ToopherAPI.VERSION);
    }
}
