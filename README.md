#ToopherJava

[![Build
Status](https://travis-ci.org/toopher/toopher-java.png?branch=master)](https://travis-ci.org/toopher/toopher-java)

#### Introduction
ToopherJava is a Toopher API library that simplifies the task of interfacing with the Toopher API from Java code.  This project includes all the dependency libraries and handles the required OAuth and JSON functionality so you can focus on just using the API.

Note: ToopherJava is not meant for use in Android.

#### Learn the Toopher API
Make sure you visit (http://dev.toopher.com) to get acquainted with the Toopher API fundamentals.  The documentation there will tell you the details about the operations this API wrapper library provides.

#### OAuth Authentication

The first step to accessing the Toopher API is to sign up for an account at the development portal (http://dev.toopher.com) and create a "requester". When that process is complete, your requester is issued OAuth 1.0a credentials in the form of a consumer key and secret. Your key is used to identify your requester when Toopher interacts with your customers, and the secret is used to sign each request so that we know it is generated by you.  This library properly formats each request with your credentials automatically.

#### The Toopher Two-Step
Interacting with the Toopher web service involves two steps: pairing and authenticating.

##### Pair
Before you can enhance your website's actions with Toopher, your customers will need to pair their phone's Toopher app with your website.  To do this, they generate a unique, nonsensical "pairing phrase" from within the app on their phone.  You will need to prompt them for a pairing phrase as part of the Toopher enrollment process.  Once you have a pairing phrase, just send it to the Toopher API along with your requester credentials and we'll return a pairing ID that you can use whenever you want to authenticate an action for that user.

##### Authenticate
You have complete control over what actions you want to authenticate using Toopher (for example: logging in, changing account information, making a purchase, etc.).  Just send us the user's pairing ID, a name for the terminal they're using, and a description of the action they're trying to perform and we'll make sure they actually want it to happen.

#### Librarified
This library makes it super simple to do the Toopher two-step.  Check it out:

```java
import com.toopher.*;

// Create an API object using your credentials
ToopherAPI api = new ToopherAPI("<your consumer key>", "<your consumer secret>");

// Step 1 - Pair with their phone's Toopher app
// With pairing phrase
Pairing pairing = api.pair("username@yourservice.com", "pairing phrase");
// With SMS
Pairing pairing = api.pair("username@yourservice.com", "555-555-5555")
// With QR code
Pairing pairing = api.pair("username@yourservice.com")

// Step 2 - Authenticate a log in
// With pairingId
AuthenticationRequest auth = api.authenticate(pairing.id, "my computer");
// With username
AuthenticationRequest auth = api.authenticate("username", "requesterSpecifiedId")

// Once they've responded you can then check the status
auth.refreshFromServer()
if (status.pending == false && status.granted == true) {
    // Success!
}
```

#### Handling Errors
If any request runs into an error a `RequestError` will be thrown with more details on what went wrong.

#### Dependencies
This library uses the Apache Commons HttpClient and OAuth-Signpost libraries, which are included as JARs in the "lib" directory.  Please add these JARs to your classpath when using our library.

##### Maven
Alternatively, you can consume this library using Maven:

    <dependency>
        <groupId>com.toopher</groupId>
        <artifactId>toopher-java</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>

#### Try it out
Check out `com.toopher.ToopherAPIDemo.java` for an example program that walks you through the whole process!  A runnable jar for the demo can be built and executed as follows:
```shell
$ ant
$ java -jar dist/toopher-1.0.0.jar
```
