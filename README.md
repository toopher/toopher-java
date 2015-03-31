#ToopherJava [![Build Status](https://travis-ci.org/toopher/toopher-java.png?branch=master)](https://travis-ci.org/toopher/toopher-java)

ToopherJava is a Toopher API library that simplifies the task of interfacing with the Toopher API from Java code.  This project wrangles all the dependency libraries and handles the required OAuth and JSON functionality so you can focus on just using the API.

*Note: ToopherJava is not meant for use in Android.*

### Java Version
\>=5.0

### Documentation
Make sure you visit [https://dev.toopher.com](https://dev.toopher.com) to get acquainted with the Toopher API fundamentals.  The documentation there will tell you the details about the operations this API wrapper library provides.

## ToopherApi Workflow

### Step 1: Pair
Before you can enhance your website's actions with Toopher, your customers will need to pair their mobile device's Toopher app with your website.  To do this, they generate a unique pairing phrase from within the app on their mobile device.  You will need to prompt them for a pairing phrase as part of the Toopher enrollment process.  Once you have a pairing phrase, just send it to the Toopher API along with their username and we'll return a pairing ID that you can use whenever you want to authenticate an action for that user.

```java
import com.toopher.*;

// Create an API object using your credentials
ToopherAPI api = new ToopherAPI("<your consumer key>", "<your consumer secret>");

// Step 1 - Pair with their mobile device's Toopher app
Pairing pairing = api.pair("username@yourservice.com", "pairing phrase");
```

### Step 2: Authenticate
You have complete control over what actions you want to authenticate using Toopher (logging in, changing account information, making a purchase, etc.).  Just send us the username or pairing ID and we'll make sure they actually want it to happen. You can also choose to provide the following optional parameters: terminal name, requester specified ID and action name (*default: "Log in"*).

```java
// Create an optional Map of extra parameters to provide to the API
Map<String, String> extras = new HashMap<String, String>();
extras.put("terminalName", "terminal name");
extras.put("requesterSpecifiedId", "requester specified ID");
extras.put("actionName", "action name");

// Step 2 - Authenticate a log in
AuthenticationRequest authenticationRequest = api.authenticate("username@yourservice.com", extras);

// Once they've responded you can then check the status
authenticationRequest.refreshFromServer();
if (status.pending == false && status.granted == true) {
    // Success!
}
```

## ToopherIframe Workflow

### Step 1: Embed a request in an IFRAME
1. Generate an authentication URL by providing a username.
2. Display a webpage to your user that embeds this URL within an `<iframe>` element.

```java
import com.toopher.*;

// Create an API object using your credentials
ToopherIframe iframeApi = new ToopherIframe("<your consumer key>", "<your consumer secret>");

String authenticationUrl = iframeApi.getAuthenticationUrl("username@yourservice.com");

// Add an <iframe> element to your HTML:
// <iframe id="toopher_iframe" src=authenticationUrl />
```

### Step 2: Validate the postback data

The simplest way to validate the postback data is to call `isAuthenticationGranted` to check if the authentication request was granted.

```java
// Retrieve the postback data as a string from POST parameter 'iframe_postback_data'

// Returns boolean indicating if authentication request was granted by user
boolean authenticationRequestGranted = iframeApi.isAuthenticationGranted(postback_data);

if (authenticationRequestGranted) {
    // Success!
}
```

### Handling Errors
If any request runs into an error a `RequestError` will be thrown with more details on what went wrong.

### Demo
Check out `com.toopher.ToopherAPIDemo.java` for an example program that walks you through the whole process! Just download the contents of this repo, make sure you have the dependencies installed, and run the command below:

```shell
$ mvn exec:java -Dexec.mainClass="com.toopher.ToopherAPIDemo"
```

## Contributing
### Dependencies
Toopher uses [Maven](https://maven.apache.org/index.html). To install Maven with Homebrew run:

```shell
$ brew install maven
```

This library uses the Apache Commons HttpClient and OAuth-Signpost libraries, which are included as JARs in the "lib" directory.  Please add these JARs to your classpath when using our library.

#### Maven
Alternatively, you can consume this library using Maven:

    <dependency>
        <groupId>com.toopher</groupId>
        <artifactId>toopher-java</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>

### Tests
To run the tests using Maven enter:
```shell
$ mvn test
```

To get coverage reports with the JaCoCo Maven Plugin enter:
```shell
$ mvn clean verify -P all-tests
$ open target/site/jacoco/index.html
```

## License
ToopherJava is licensed under the MIT License. See LICENSE.txt for the full text.
