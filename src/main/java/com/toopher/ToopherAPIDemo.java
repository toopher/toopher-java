package com.toopher;

import org.json.JSONException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ToopherAPIDemo {
    public final static String DEFAULT_USERNAME = "demo@toopher.com";
    public final static String DEFAULT_TERMINAL_NAME = "my computer";

    public static void main(String[] args) {

        Map<String, String> env = System.getenv();

        Scanner in = new Scanner(System.in);

        System.out.println();
        System.out.println("Toopher Library Demo");
        System.out.println("======================================");

        URI base_uri = null;
        if (env.containsKey("TOOPHER_BASE_URL")) {
            try {
                base_uri = new URI(env.get("TOOPHER_BASE_URL"));
            } catch (URISyntaxException e) {
                base_uri = null;
            }
        }

        ToopherApi api;
        if (env.containsKey("TOOPHER_CONSUMER_KEY") && env.containsKey("TOOPHER_CONSUMER_SECRET")) {
            api = new ToopherApi(env.get("TOOPHER_CONSUMER_KEY"), env.get("TOOPHER_CONSUMER_SECRET"), base_uri);
        } else {
            System.out.println("");
            System.out.println("Setup Credentials");
            System.out.println("--------------------------------------");
            System.out.println("Enter your requester credential details (from https://dev.toopher.com).");
            System.out.print("TOOPHER_CONSUMER_KEY: ");
            String consumerKey = in.nextLine();
            System.out.print("TOOPHER_CONSUMER_SECRET: ");
            String consumerSecret = in.nextLine();

            api = new ToopherApi(consumerKey, consumerSecret, base_uri);
        }

        Pairing pairing;
        while (true) {
            String pairingPhrase;
            while (true) {
                System.out.println("Step 1: Pair requester with phone");
                System.out.println("--------------------------------------");
                System.out.println("Pairing phrases are generated on the mobile app");
                System.out.print("Enter pairing phrase: ");
                pairingPhrase = in.nextLine();

                if (pairingPhrase.replaceAll("\\s", "").length() == 0) {
                    System.out.println("Please enter a pairing phrase to continue");
                } else {
                    break;
                }
            }

            System.out.print(String.format("Enter a username for this pairing [%s]: ", DEFAULT_USERNAME));
            String userName = in.nextLine();

            if (userName.length() == 0) {
                userName = DEFAULT_USERNAME;
            }

            System.out.println("Sending pairing request...");

            try {
                pairing = api.pair(userName, pairingPhrase);
                break;
            } catch (RequestError err) {
                System.out.println(String.format("The pairing phrase was not accepted (Reason: %s)", err.getMessage()));
            } catch (JSONException je) {
                System.out.println(String.format("The JSON response could not be processed (Reason: %s)", je.getMessage()));
            }
        }

        while (true) {
            System.out.println("Authorize pairing on phone and then press return to continue.");
            in.nextLine();
            System.out.println("Checking status of pairing request...");

            try {
                pairing.refreshFromServer();
                if (pairing.pending) {
                    System.out.println("The pairing has not been authorized by the phone yet.");
                } else if (pairing.enabled) {
                    System.out.println("Pairing complete");
                    System.out.println();
                    break;
                } else {
                    System.out.println("The pairing has been denied");
                    System.exit(0);
                }
            } catch (RequestError err) {
                System.out.println(String.format("Could not check pairing status (Reason: %s)", err.getMessage()));
            } catch (JSONException je) {
                System.out.println(String.format("The JSON response could not be processed (Reason: %s)", je.getMessage()));
            }
        }

        while (true) {
            System.out.println("Step 2: Authenticate log in");
            System.out.println("--------------------------------------");
            System.out.print(String.format("Enter a terminal name for this authentication request [%s]: ", DEFAULT_TERMINAL_NAME));
            String terminalName = in.nextLine();
            if (terminalName.length() == 0) {
                terminalName = DEFAULT_TERMINAL_NAME;
            }

            System.out.println("Sending authentication request...");

            AuthenticationRequest authenticationRequest;
            try {
                Map<String, String> extras = new HashMap<String, String>();
                extras.put("terminalName", terminalName);
                authenticationRequest = api.authenticate(pairing.id, extras);
            } catch (RequestError err) {
                System.out.println(String.format("Error initiating authentication (Reason: %s)", err.getMessage()));
                continue;
            } catch (JSONException je) {
                System.out.println(String.format("The JSON response could not be processed (Reason: %s)", je.getMessage()));
                continue;
            }

            while (true) {
                System.out.println("Respond to authentication request on phone and then press return to continue.");
                in.nextLine();
                System.out.println("Checking status of authentication request...");

                try {
                    authenticationRequest.refreshFromServer();
                } catch (RequestError err) {
                    System.out.println(String.format("Could not check authentication status (Reason: %s)", err.getMessage()));
                    continue;
                } catch (JSONException je) {
                    System.out.println(String.format("The JSON response could not be processed (Reason: %s)", je.getMessage()));
                    continue;
                }

                if (authenticationRequest.pending) {
                    System.out.println("The authentication request has not received a response from the phone yet.");
                } else {
                    String automation = authenticationRequest.automated ? "automatically " : "";
                    String result = authenticationRequest.granted ? "granted" : "denied";
                    System.out.println("The request was " + automation + result + "!");
                    System.out.println();
                    break;
                }
            }

            System.out.println("Press return to authenticate again, or Ctrl-C to exit");
            in.nextLine();
        }
    }
}