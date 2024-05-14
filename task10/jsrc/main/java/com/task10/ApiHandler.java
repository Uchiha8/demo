package com.task10;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

import java.util.Optional;

@LambdaHandler(lambdaName = "api_handler",
		roleName = "api_handler-role",
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	static final String COGNITO_POOL_NAME = "cmtr-580435c6-simple-booking-userpool-test";
	static final String COGNITO_CLIENT_NAME = "cmtr-580435c6-task10-client";
	DynamoDBHandler dynamoDBHandler = DynamoDBHandler.getInstance();

	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
		System.out.println("Event: " + event);
		final String resource = event.getResource();
		final String httpMethod = event.getHttpMethod();
		final String body = event.getBody();
		final String tableId = event.getPathParameters() != null ? event.getPathParameters().getOrDefault("tableId", "") : "";

		System.out.println("Resource: " + resource);
		System.out.println("HttpMethod: " + httpMethod);
		System.out.println("body: " + body);
		System.out.println("tableId: " + tableId);

		try (var cognitoClient = CognitoIdentityProviderClient.builder().region(Region.EU_CENTRAL_1).build()) {
			UserPoolHandler userPoolHandler = UserPoolHandler.getInstance();
			Optional<String> userPoolId = userPoolHandler.getUserPoolId(cognitoClient);
			System.out.println("UserPoolId: " + userPoolId.orElse(""));

			if (userPoolId.isPresent()) {
				if (isSignUpRequest(resource, httpMethod)) {
					return signUp(cognitoClient, userPoolId.get(), body);
				} else if (isSignInRequest(resource, httpMethod)) {
					return signIn(cognitoClient, userPoolId.get(), body);
				} else if (isPostTablesRequest(resource, httpMethod)) {
					return dynamoDBHandler.putItemInTables(body);
				} else if (isGetTablesRequest(resource, httpMethod, tableId)) {
					return dynamoDBHandler.getItemsFromTables();
				} else if (isGetTableByIdRequest(resource, httpMethod, tableId)) {
					return dynamoDBHandler.getItemFromTables(tableId);
				}else if (isPostReservationsRequest(resource, httpMethod)) {
					return dynamoDBHandler.putItemInReservations(body);
				}else if (isGetReservationsRequest(resource, httpMethod)) {
					return dynamoDBHandler.getItemsFromReservations();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new APIGatewayProxyResponseEvent().withStatusCode(500);
	}

	private boolean isPostReservationsRequest(String resource, String httpMethod) {
		return ResourcePath.RESERVATIONS.getPath().equals(resource) && HttpMethod.POST.toString().equals(httpMethod);
	}

	private boolean isGetReservationsRequest(String resource, String httpMethod) {
		return ResourcePath.RESERVATIONS.getPath().equals(resource) && HttpMethod.GET.toString().equals(httpMethod);
	}
	private boolean isSignUpRequest(String resource, String httpMethod) {
		return ResourcePath.SIGN_UP.getPath().equals(resource) && HttpMethod.POST.toString().equals(httpMethod);
	}

	private boolean isSignInRequest(String resource, String httpMethod) {
		return ResourcePath.SIGN_IN.getPath().equals(resource) && HttpMethod.POST.toString().equals(httpMethod);
	}

	private boolean isPostTablesRequest(String resource, String httpMethod) {
		return ResourcePath.TABLES.getPath().equals(resource) && HttpMethod.POST.toString().equals(httpMethod);
	}

	private boolean isGetTablesRequest(String resource, String httpMethod, String id) {
		return ResourcePath.TABLES.getPath().equals(resource) && HttpMethod.GET.toString().equals(httpMethod) && id.isEmpty();
	}

	private boolean isGetTableByIdRequest(String resource, String httpMethod, String id) {
		return ResourcePath.TABLE_BY_ID.getPath().equals(resource) && HttpMethod.GET.toString().equals(httpMethod) && !id.isEmpty();
	}

	private APIGatewayProxyResponseEvent signUp(CognitoIdentityProviderClient cognitoClient, String userPoolId, String body) {
		var signUpHandler = SignUpHandler.getInstance();
		return signUpHandler.signUp(cognitoClient, userPoolId, body);
	}

	private APIGatewayProxyResponseEvent signIn(CognitoIdentityProviderClient cognitoClient, String userPoolId, String body) {
		var signInHandler = SignInHandler.getInstance();
		return signInHandler.signIn(cognitoClient, userPoolId, body);
	}
}
