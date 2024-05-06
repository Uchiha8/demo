package com.task07;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.events.RuleEventSource;
import com.syndicate.deployment.annotations.events.RuleEvents;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@LambdaHandler(lambdaName = "uuid_generator",
		roleName = "uuid_generator-role",
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@RuleEventSource(targetRule="uuid_trigger")
@RuleEvents
public class UuidGenerator implements RequestHandler<Object, Map<String, Object>> {

	private final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
	private final String BUCKET_NAME = "cmtr-580435c6-uuid-storage-test";
	private final int NUMBER_OF_IDS_PER_FILE = 10;
	private ObjectMapper objectMapper = new ObjectMapper();

	public Map<String, Object> handleRequest(Object request, Context context) {

		System.out.println("Request: " + request);
		System.out.println("Context: " + context);

		// Fetch Lambda execution start time
		LocalDateTime executionStartTime = LocalDateTime.now();

		// Generate UUIDs
		List<String> generatedIds = generateIds();
		Ids ids = new Ids();
		ids.setIds(generatedIds);

		// Convert obj to json
		String idsJSON = "";
		try {
			idsJSON = objectMapper.writeValueAsString(ids);
		} catch (JsonProcessingException e) {
			System.out.println("Error while parsing");
			e.printStackTrace();
		}

		System.out.println(idsJSON);

		// Format file name
		String fileName = executionStartTime.toString();

		try (InputStream inputStream = new ByteArrayInputStream(idsJSON.getBytes(StandardCharsets.UTF_8))) {
			// Upload JSON content to S3
			s3.putObject(BUCKET_NAME, fileName, inputStream, null);
			System.out.println("JSON content has been uploaded to S3 with key: " + fileName);
		} catch (IOException e) {
			System.out.println("Error while uploading to S3");
			e.printStackTrace();
		}

		System.out.println("Hello from lambda");
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("statusCode", 200);
		resultMap.put("body", "Successfully wrote the content in S3");
		return resultMap;
	}

	private List<String> generateIds() {
		return IntStream.range(0, NUMBER_OF_IDS_PER_FILE)
				.mapToObj(i -> UUID.randomUUID().toString())
				.collect(Collectors.toList());
	}
}

class Ids {
	private List<String> ids;

	public List<String> getIds() {
		return ids;
	}

	public void setIds(List<String> ids) {
		this.ids = ids;
	}

	@Override
	public String toString() {
		return "Ids{" +
				"ids=" + ids +
				'}';
	}
}

