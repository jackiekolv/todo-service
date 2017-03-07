package treepay.todo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import treepay.todo.util.DatabaseUtil;

public class RequestHandler implements RequestStreamHandler {

    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
    	
    	System.out.println("Loading Java Lambda handler of RequestHandler");
    	
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        JSONObject handlerResponse = null;

        try {
        	JSONTokener token = new JSONTokener(reader);
        	JSONObject event = new JSONObject(token);

    		System.out.println(" event: " + event.toString());

            String receive_httpMethod = (String) event.get("httpMethod");
            
            String id_fromPathParameters = null;
            if(event.get("pathParameters")!=null && !event.getString("pathParameters").equals("null"))
            	id_fromPathParameters = event.getJSONObject("pathParameters").getString("id");
            
            JSONObject body = null;
            if(event.get("body")!=null && !event.getString("body").equals("null"))
            	body = new JSONObject(event.getString("body"));
            
            JSONObject returnResult = DatabaseUtil.exceuteSql(receive_httpMethod, id_fromPathParameters, body);
            
            try {
                if("true".equals(returnResult.get("is_success")))
                	returnResult.put("data", body);
                if(returnResult.get("last_inserted_id") != null){
                	body.put("id", returnResult.get("last_inserted_id"));
                	returnResult.put("data", body);
                }
			} catch (JSONException e) {
				// Do nothing.
			}
            
        	handlerResponse = new JSONObject();

        	JSONObject response_hearder = new JSONObject();
            response_hearder.put("x-custom-response-header", "my custom response header value");
            
        	handlerResponse.put("statusCode", "200");
        	handlerResponse.put("headers", response_hearder);
        	handlerResponse.put("body", returnResult.toString());

        } catch(Exception e) {
        	e.printStackTrace();
			try {
				handlerResponse = new JSONObject();
				handlerResponse.put("statusCode", "400");
	            handlerResponse.put("exception", e);
			} catch (JSONException exjson) {
				exjson.printStackTrace();
			}
        }

		System.out.println(" handlerResponse: " + handlerResponse.toString());
    	OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
    	writer.write(handlerResponse.toString());
    	writer.close();
    }
    
}