package com.task09;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.TracingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(lambdaName = "processor",
        roleName = "processor-role",
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED,
        tracingMode = TracingMode.Active
)
@LambdaUrlConfig
public class Processor implements RequestHandler<Object, Map<String, Object>> {

    private static final Logger log = LoggerFactory.getLogger(Processor.class);
    private final OpenMeteoClient weatherClient = new OpenMeteoClient();
    private static final String TABLE_NAME = "cmtr-580435c6-Weather-test";
    private final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
    private final DynamoDB dynamoDB = new DynamoDB(client);

    public Map<String, Object> handleRequest(Object request, Context context) {
        //get weather
        HttpResponse<String> currentWeather = weatherClient.getCurrentWeather();
        int statusCode = currentWeather.statusCode();
        String body = currentWeather.body();
        //add to dynamoDB
        addWeatherToDynamoDB(body);

        log.info("Hello from Processor");
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("statusCode", statusCode);
        resultMap.put("body", body);
        return resultMap;


    }

    private void addWeatherToDynamoDB(String body) {
        //add to dynamoDB
        log.info("Body: {}", body);
        String id = UUID.randomUUID().toString();
        Item item = new Item()
                .withString("id", id)
                .withJSON("forecast", body);
        try {
            Table weatherTable = dynamoDB.getTable(TABLE_NAME);
            weatherTable.putItem(item);
            log.info("Item added to DynamoDB");
        } catch (Exception e) {
            log.error("Error adding item to DynamoDB", e);
        }
    }
}
