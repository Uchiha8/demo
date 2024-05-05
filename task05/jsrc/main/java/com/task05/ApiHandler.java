package com.task05;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(lambdaName = "api_handler",
		roleName = "api_handler-role",
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
public class ApiHandler implements RequestHandler<ApiGatewayEvent, Map<String, Object>> {

	private ObjectMapper objectMapper = new ObjectMapper();
	public Map<String, Object> handleRequest(ApiGatewayEvent request, Context context) {

		String id = UUID.randomUUID().toString();
		int principalId = request.getPrincipalId();
		String createdAt =  LocalDateTime.now().toString();
		Map<String, String> content = request.getContent();
		String contentAsJSON = convertToJSON(content);

		System.out.println("Hello from lambda");
		System.out.println(request);

		// add item to dynamo table
		addItem(id, principalId, createdAt, contentAsJSON);

		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("statusCode", 201);
		resultMap.put("event", Map.of(
				"id",id, //generated uuid v4
				"principalId",principalId,
				"createdAt", createdAt,
				"body", content));
		return resultMap;
	}

	public static void addItem(String id, int principalId, String createdAt, String contentAsJSON) {
		final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
		System.out.println("Add Item Started");
		String tableName = "cmtr-580435c6-Events-test";

		DynamoDB dynamoDb = new DynamoDB(client);

		Table table = dynamoDb.getTable(tableName);

		// Create an item with the attributes
		Item item = new Item()
				.withPrimaryKey("id", id)
				.withInt("principalId", principalId)
				.withString("createdAt", createdAt)
				.withJSON("body", contentAsJSON);

		try {
			PutItemOutcome putItemOutcome = table.putItem(item);
			System.out.println("PutItemOutcome: " + putItemOutcome);
		} catch (ResourceNotFoundException e) {
			System.err.format("Error: The table \"%s\" can't be found.\n", tableName);
			System.err.println("Be sure that it exists and that you've typed its name correctly!");
			System.exit(1);
		} catch (AmazonServiceException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}

		System.out.println("Add Item finished: 201");
	}

	private String convertToJSON(Map<String, String> content) {
		String contentAsJSON = null;
		try {
			contentAsJSON = objectMapper.writeValueAsString(content);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return contentAsJSON;
	}

}


class ApiGatewayEvent {
	private int principalId;
	private Map<String, String> content;

	public ApiGatewayEvent() {
	}

	public ApiGatewayEvent(int principalId, Map<String, String> content) {
		this.principalId = principalId;
		this.content = content;
	}

	public int getPrincipalId() {
		return principalId;
	}

	public void setPrincipalId(int principalId) {
		this.principalId = principalId;
	}

	public Map<String, String> getContent() {
		return content;
	}

	public void setContent(Map<String, String> content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "ApiGatewayEvent{" +
				"principalId=" + principalId +
				", content=" + content +
				'}';
	}
}
