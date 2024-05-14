package com.task10;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;

import java.util.HashMap;
import java.util.Map;

class SignInHandler {

    private static SignInHandler instance;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private SignInHandler() {
    }

    public static SignInHandler getInstance() {
        if (instance == null) {
            instance = new SignInHandler();
        }
        return instance;
    }

    public APIGatewayProxyResponseEvent signIn(CognitoIdentityProviderClient cognitoClient, String userPoolId, String body) {
        try {
            Map<String, String> map = objectMapper.readValue(body, HashMap.class);
            Map<String, String> authParameters = new HashMap<>();
            authParameters.put("USERNAME", map.get("email"));
            authParameters.put("PASSWORD", map.get("password"));

            var userPoolHandler = UserPoolHandler.getInstance();
            var userPoolClientId = userPoolHandler.getUserPoolClientId(cognitoClient);
            if (userPoolClientId.isPresent()) {
                AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                        .clientId(userPoolClientId.get())
                        .userPoolId(userPoolId)
                        .authParameters(authParameters)
                        .authFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH)
                        .build();

                AdminInitiateAuthResponse response = cognitoClient.adminInitiateAuth(authRequest);
                String accessToken = response.authenticationResult().idToken();
                String responseBody = "{\"accessToken\": \"" + accessToken + "\"}";
                return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody(responseBody);
            }else {
                System.out.println("Client Id not found");
                return new APIGatewayProxyResponseEvent().withStatusCode(400);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new APIGatewayProxyResponseEvent().withStatusCode(400);
        }
    }
}
