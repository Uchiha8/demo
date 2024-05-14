package com.task10;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminSetUserPasswordRequest;

import java.util.HashMap;
import java.util.Map;

class SignUpHandler {
    private static SignUpHandler instance;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private SignUpHandler() {
    }

    public static SignUpHandler getInstance() {
        if (instance == null) {
            instance = new SignUpHandler();
        }
        return instance;
    }

    public APIGatewayProxyResponseEvent signUp(CognitoIdentityProviderClient cognitoClient, String userPoolId, String body) {
        try {
            Map<String, String> map = objectMapper.readValue(body, HashMap.class);
            String email = map.get("email");
            String password = map.get("password");

            AdminCreateUserRequest createUserRequest = AdminCreateUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(email)
                    .temporaryPassword(password)
                    .build();

            cognitoClient.adminCreateUser(createUserRequest);

            AdminSetUserPasswordRequest setPasswordRequest = AdminSetUserPasswordRequest.builder()
                    .userPoolId(userPoolId)
                    .username(email)
                    .password(password)
                    .permanent(true)
                    .build();

            cognitoClient.adminSetUserPassword(setPasswordRequest);

            return new APIGatewayProxyResponseEvent().withStatusCode(200);
        } catch (Exception e) {
            e.printStackTrace();
            return new APIGatewayProxyResponseEvent().withStatusCode(400);
        }
    }
}