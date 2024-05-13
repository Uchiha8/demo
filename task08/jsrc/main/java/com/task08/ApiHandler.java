package com.task08;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.task08.layer.OpenMeteoClient;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@LambdaHandler(
		lambdaName = "api_handler",
		roleName = "api_handler-role",
		layers = {"weatherClient"}
)
@LambdaLayer(
		layerName = "weatherClient",
		libraries = {"lib/OpenMeteoClient.jar", "lib/commons-lang3-3.14.0.jar"}
)
@LambdaUrlConfig
public class ApiHandler implements RequestHandler<Object, Map<String, Object>> {

	private static final OpenMeteoClient weatherClient = new OpenMeteoClient();

	public Map<String, Object> handleRequest(Object request, Context context) {

		if (StringUtils.isBlank(request.toString())) {
			throw new RuntimeException("null");
		}

		HttpResponse<String> response;
		try {
			response = weatherClient.getCurrentWeather();
		} catch (URISyntaxException | IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}


        Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("statusCode", response.statusCode());
		resultMap.put("body", response.body());

		return resultMap;
	}
}
