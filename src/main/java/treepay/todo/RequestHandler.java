package treepay.todo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
            
            String sqlString = DatabaseUtil.getSqlString(receive_httpMethod, id_fromPathParameters, body);
            JSONObject returnResult = DatabaseUtil.exceuteSql(sqlString);
            
            try {
                if("true".equals(returnResult.get("is_updated")))
                	returnResult.put("data", body);
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
    
    
    
    public static void main(String[] args) throws SQLException {   
    	
    	Connection conn = null;
    	String connectionString = "jdbc:mysql://dbone.cbvpxlkpqfey.ap-southeast-1.rds.amazonaws.com/test?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&user=root&password=rootroot";
        try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(connectionString);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

    	String sqlInsert = "INSERT INTO todo (name, description, is_done, due_date, order_id) "
    					 + "VALUES ('My wedding', 'My wedding party at Lampang', '0', '20170319', 1)";
    	String sqlSelect = "SELECT * from todo where id=1";
    	JSONObject returnResult = DatabaseUtil.exceuteSql(sqlInsert);
    	System.out.println(returnResult);
    	conn.close();
    }
    
}