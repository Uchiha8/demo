package com.task06;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.events.DynamoDbEvents;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(lambdaName = "audit_producer",
		roleName = "audit_producer-role",
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@DynamoDbTriggerEventSource(targetTable = "Configuration", batchSize = 10)
@DynamoDbEvents
public class AuditProducer implements RequestHandler<Object, Map<String, Object>> {

	private static final String AUDIT_TABLE_NAME = "cmtr-3477d8b3-Audit-test";
	private final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
	private final DynamoDB dynamoDB = new DynamoDB(client);

	public Map<String, Object> handleRequest(Object request, Context context) {
		try {
			System.out.println("Handling request...");
			Map<String, Object> requestBody = (Map<String, Object>) request;
			List<Map<String, Object>> records = (List<Map<String, Object>>) requestBody.get("Records");
			Map<String, Object> record = records.get(0);
			String eventName = (String) record.get("eventName");
			Map<String, Object> dynamoDbDetails = (Map<String, Object>) record.get("dynamodb");
			Map<String, Object> newImage = (Map<String, Object>) dynamoDbDetails.get("NewImage");
			Map<String, Object> keyMap = (Map<String, Object>) newImage.get("key");
			String key = (String) keyMap.get("S");
			int value = Integer.parseInt((String) ((Map<String, Object>) newImage.get("value")).get("N"));

			if ("MODIFY".equals(eventName)) {
				Map<String, Object> oldImage = (Map<String, Object>) dynamoDbDetails.get("OldImage");
				int oldValue = Integer.parseInt((String) ((Map<String, Object>) oldImage.get("value")).get("N"));
				updateAuditItem(key, oldValue, value);
			} else {
				createAuditItem(key, value);
			}
		} catch (Exception e) {
			System.out.println("Error while handling request:");
			e.printStackTrace();
		}

		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("statusCode", 200);
		resultMap.put("body", "Hello from Lambda");
		return resultMap;
	}

	private void createAuditItem(String key, int value) {
		String json = "{\"key\":\"" + key + "\",\"value\":" + value + "}";
		try {
			System.out.println("Creating audit item for key: " + key);
			String id = UUID.randomUUID().toString();
			String modificationTime = getCurrentTime();
			Item auditItem = new Item()
					.withString("id", id)
					.withString("itemKey", key)
					.withString("modificationTime", modificationTime)
					.withJSON("newValue", json);
			insertIntoAuditTable(auditItem);
		} catch (Exception e) {
			System.out.println("Error while creating audit item:");
			e.printStackTrace();
		}
	}

	private void updateAuditItem(String key, int oldValue, int newValue) {
		try {
			System.out.println("Updating audit item for key: " + key);
			String id = UUID.randomUUID().toString();
			String modificationTime = getCurrentTime();
			Item auditItem = new Item()
					.withString("id", id)
					.withString("itemKey", key)
					.withString("modificationTime", modificationTime)
					.withString("updatedAttribute", "value")
					.withNumber("oldValue", oldValue)
					.withNumber("newValue", newValue);
			insertIntoAuditTable(auditItem);
		} catch (Exception e) {
			System.out.println("Error while updating audit item:");
			e.printStackTrace();
		}
	}

	private void insertIntoAuditTable(Item item) {
		try {
			Table auditTable = dynamoDB.getTable(AUDIT_TABLE_NAME);
			auditTable.putItem(item);
			System.out.println("Inserted item into audit table.");
		} catch (Exception e) {
			System.out.println("Error while inserting item into audit table:");
			e.printStackTrace();
		}
	}

	private String getCurrentTime() {
		return LocalDateTime.now().toString();
	}
}
