package com.toopher;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestToopherAPI {
    private boolean isValidURL(String url) {
        try {
            new URL(url).toURI();
        } catch (MalformedURLException e) {
            return false;
        } catch (URISyntaxException e) {
            return false;
        }

        return true;
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
        assertTrue("True is not true.", true);
    }

}
