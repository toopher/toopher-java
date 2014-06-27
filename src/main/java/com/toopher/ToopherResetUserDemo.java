package com.toopher;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Scanner;

public class ToopherResetUserDemo {

    public static void main(String[] args) {

        Map<String, String> env = System.getenv();

        Scanner in = new Scanner(System.in);

        System.out.println("======================================");
        System.out.println("User Reset Demo");
        System.out.println("======================================");

        URI base_uri = null;
        if(env.containsKey("TOOPHER_BASE_URL")){
            try {
                base_uri = new URI(env.get("TOOPHER_BASE_URL"));
            } catch (URISyntaxException e) {
                System.out.println("Error parsing environment arg TOOPHER_BASE_URL!  Using default (https://api.toopher.com/v1/)");
                base_uri = null;
            }
        }

        AdvancedToopherAPI api;
        if(env.containsKey("TOOPHER_CONSUMER_KEY") && env.containsKey("TOOPHER_CONSUMER_SECRET")){
            api = new AdvancedToopherAPI (env.get("TOOPHER_CONSUMER_KEY"), env.get("TOOPHER_CONSUMER_SECRET"), base_uri);
        } else {
            System.out.println("");
            System.out.println("Setup Credentials");
            System.out.println("--------------------------------------");
            System.out.println("Enter your requester credentials (from https://dev.toopher.com)");
            System.out.print("Consumer key: ");
            String consumerKey = in.nextLine();
            System.out.print("Consumer secret: ");
            String consumerSecret = in.nextLine();

            api = new AdvancedToopherAPI (consumerKey, consumerSecret, base_uri);
        }

        System.out.print(String.format("Enter a username for this pairing : "));
        String userName = in.nextLine();

        try {
            System.out.print(String.format("Deactivating pairings for %s...", userName));
            api.deactivateUserPairings(userName);
            System.out.println(" Done!");
        } catch (RequestError err) {
            System.out.println(String.format("Failed to deactivate user pairings (reason:%s)", err.getMessage()));
        }

    }
}