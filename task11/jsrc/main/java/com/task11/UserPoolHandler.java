package com.task11;

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUserPoolClientsRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUserPoolClientsResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUserPoolsRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUserPoolsResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserPoolClientDescription;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserPoolDescriptionType;

import java.util.List;
import java.util.Optional;

import static com.task11.ApiHandler.COGNITO_CLIENT_NAME;
import static com.task11.ApiHandler.COGNITO_POOL_NAME;


public class UserPoolHandler {

    private static UserPoolHandler instance;

    private UserPoolHandler() {
    }

    public static UserPoolHandler getInstance() {
        if (instance == null) {
            instance = new UserPoolHandler();
        }
        return instance;
    }


    protected Optional<String> getUserPoolId(CognitoIdentityProviderClient cognitoClient) {
        System.out.println("Get User Pool Id");
        try {
            ListUserPoolsRequest listUserPoolsRequest = ListUserPoolsRequest.builder().build();
            ListUserPoolsResponse listUserPoolsResponse = cognitoClient.listUserPools(listUserPoolsRequest);
            List<UserPoolDescriptionType> userPools = listUserPoolsResponse.userPools();

            return userPools.stream()
                    .filter(userPool -> userPool.name().equals(COGNITO_POOL_NAME))
                    .map(UserPoolDescriptionType::id)
                    .findFirst();
        } catch (CognitoIdentityProviderException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    protected Optional<String> getUserPoolClientId(CognitoIdentityProviderClient cognitoClient) {
        System.out.println("Get User Pool client Id");
        try {
            ListUserPoolClientsRequest listUserPoolsClientRequest = ListUserPoolClientsRequest.builder()
                    .userPoolId(getUserPoolId(cognitoClient).get())
                    .build();
            ListUserPoolClientsResponse response = cognitoClient.listUserPoolClients(listUserPoolsClientRequest);
            List<UserPoolClientDescription> clients = response.userPoolClients();
            System.out.println("Clients: " + clients);
            return clients.stream()
                    .filter(userPoolClient -> userPoolClient.clientName().equals(COGNITO_CLIENT_NAME))
                    .map(UserPoolClientDescription::clientId)
                    .findFirst();
        } catch (CognitoIdentityProviderException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
