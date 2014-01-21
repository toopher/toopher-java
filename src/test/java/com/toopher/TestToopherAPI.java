package com.toopher;

//import com.toopher.ToopherAPI;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import junit.framework.*;

import static org.junit.Assert.*;

import org.junit.Test;

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
