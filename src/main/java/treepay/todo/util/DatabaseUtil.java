package treepay.todo.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONObject;


public class DatabaseUtil {
	
    public static JSONObject exceuteSql(String receive_httpMethod, String id_fromPathParameter, JSONObject data) {
    	
    	
    	Connection conn = null;
    	String connectionString = System.getenv("connection_string");
    	
    	try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(connectionString);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
        
        JSONObject result = new JSONObject();
    	PreparedStatement prest = null;
    	try {
        	String sqlString = "";
            ResultSet rs = null;
            if(receive_httpMethod.equals("GET") && id_fromPathParameter != null){
            	sqlString = "SELECT * FROM todo WHERE id=?";
                prest = conn.prepareStatement(sqlString);
                prest.setInt(1, Integer.parseInt(id_fromPathParameter));
                rs = prest.executeQuery();
            	if (rs != null && rs.next()) {
                    result.put("id", rs.getString("id"));
                    result.put("name", rs.getString("name"));
                    result.put("description", rs.getString("description"));
//                    result.put("is_done", rs.getString("is_done"));
                    result.put("due_date", rs.getString("due_date"));
                    result.put("order_id", rs.getString("order_id"));
                }else{
                    result.put("message", "No data found.");
                }
            }else if(receive_httpMethod.equals("POST")){
            	sqlString = "INSERT INTO todo (name, description, is_done, due_date, order_id) "
            					 + "VALUES (?, ?, ?, ?, ?)";
                prest = conn.prepareStatement(sqlString, Statement.RETURN_GENERATED_KEYS);
                prest.setString(1, data.getString("name"));
                prest.setString(2, data.getString("description"));
                prest.setString(3, "");
                prest.setString(4, data.getString("due_date"));
                prest.setString(5, data.getString("order_id"));
                int is_success = prest.executeUpdate();
                if(is_success == 1) {
                	result.put("is_success", "true");
                    rs = prest.getGeneratedKeys();
                    if(rs.next())
                    {
                        int last_inserted_id = rs.getInt(1);
                        result.put("last_inserted_id", last_inserted_id);
                    }
                }
                else {
                	result.put("is_success", "false");
                }
            }else if(receive_httpMethod.equals("PUT")){
            	sqlString = "UPDATE todo SET name=?, description=?, is_done=?, due_date=?, order_id=? "
            					 + "WHERE id=?";
                prest = conn.prepareStatement(sqlString);
                prest.setString(1, data.getString("name"));
                prest.setString(2, data.getString("description"));
                prest.setString(3, "");
                prest.setString(4, data.getString("due_date"));
                prest.setString(5, data.getString("order_id"));
                prest.setString(6, data.getString("id"));
                int is_success = prest.executeUpdate();
                if(is_success == 1) result.put("is_success", "true");
                else {
                	result.put("is_success", "false");
                	result.put("message", "No data found.");
                }
            }else if(receive_httpMethod.equals("DELETE")){
            	sqlString = "DELETE FROM todo WHERE id=?";
                prest = conn.prepareStatement(sqlString);
                prest.setString(1, id_fromPathParameter);
                int is_success = prest.executeUpdate();
                if(is_success == 1) result.put("is_success", "true");
                else {
                	result.put("is_success", "false");
                	result.put("message", "No data found.");
                }
            }
            System.out.println("sqlString: "+sqlString);

    	}
    	catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		    try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				conn = null;
			}
		}
    	
    	
    	return result;
    }
}
