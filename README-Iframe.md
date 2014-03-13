Authenticating using the Toopher-Iframe
=======================================
Toopher-Iframe is the simplest way for web developers to integrate Toopher Two-Factor Authentication into an application.  

## Toopher-Iframe Authentication Overview

Toopher-Iframe works by inserting an <iframe> element into the HTML displayed to the user after a successful username/password validation (but before they are actually logged in to the service).  The iframe content (sourced from the Toopher API) guides the user through the process of authenticating with Toopher.  Once complete, the Iframe will return the result of the authentication to your server using HTML form POST.  Upon receiving the returned form data, the cryptographic signature is validated on the server, and the user is authenticated (or not, if the iframe result indicates authentication failure).

Toopher-Iframe generates two distinct types of Iframe request: 
* the *Pairing* request is used to pair a user account with a particular mobile device 
* the *Authentication* request to authenticate a particular action on behalf of a user

## Typical Toopher-Iframe Authentication Workflow

### Step 1: Username/Password validation
1. User submits username/password to your server
1. Server validates username/password, but does not start authenticated session
1. (If username/password valid): Server generates signed authentication `<iframe>` url (see below), displays page to user with embedded Toopher Iframe

### Step 2: Toopher-Iframe postback validation
1. Toopher-Iframe results posted back to server
1. Server calls `ToopherIframe.validate()` to verify that result is valid.  `.validate()` returns a `Map` of trusted data if the signature is valid, or `null` if the signature is invalid.
1. If the result from `.validate()` is not null, the server should check for possible errors returned by the API in the `error_code` map entry
1. If no errors were returned, the result of the authentication is in the `granted` map entry

# Examples

## Generating a Iframe URL for Authenitcation
The Toopher Authentication API provides the requester a rich set of controls over authentication parameters.

    String authIframeUrl = iframeApi.authIframeUrl(userName, resetEmail, actionName, automationAllowed, challengeRequired, sessionToken, requesterMetadata, ttl);

For the simple case of authenticating a user at login, the `loginIframeUrl` helper method is available:

    String loginIframeUrl = iframeApi.loginIframeUrl(userName, resetEmail, sessionToken)

## Generating a Pairing Iframe URL

    String pairIframeUrl = iframeApi.pairIframeUrl(userName, resetEmail)

## Validating postback data from Authentication Iframe and parsing API errors
In this example, `data` is a `Map<String, String>` of the form data POSTed to your server from the Toopher Authentication Iframe.  You should replace the commented blocks with code appropriate for the condition described in the comment.

    Map<String, String> validatedData = iframeApi.validate(data);
    if (validatedData == null) {
        // signature was invalid.  User should not authenticated
    } else if (validatedData.containsKey("error_code")) {
        // check for API errors
        String errorCode = validatedData.get("error_code");
        if (errorCode.equals(ToopherIframe.PAIRING_DEACTIVATED)) {
            // User deleted the pairing on their mobile device.
            // 
            // Your server should display a Toopher Pairing Iframe so their account can be re-paired
            //
        } else if (errorCode.equals(ToopherIframe.USER_OPT_OUT)) {
            // User has been marked as "Opt-Out" in the Toopher API
            //
            // If your service allows opt-out, the user should be granted access.
            //
        } else if (errorCode.equals(ToopherIframe.USER_UNKNOWN)) {
            // User has never authenticated with Toopher on this server
            //
            // Your server should display a Toopher Pairing Iframe so their account can be paired
            //
        }
    } else {
        // signature is valid, and no api errors.  check authentication result
        boolean authPending = validatedData.get("pending").toLowerCase().equals("true");
        boolean authGranted = validatedData.get("granted").toLowerCase().equals("true");

        // authenticationResult is the ultimate result of Toopher second-factor authentication
        boolean authenticationResult = authGranted && !authPending;
    }
