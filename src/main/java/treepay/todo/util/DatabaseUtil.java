package treepay.todo.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONException;
import org.json.JSONObject;


public class DatabaseUtil {
	
	public static String getSqlString(String receive_httpMethod, String id, JSONObject data) throws JSONException{
        String sqlString = "";
        if(receive_httpMethod.equals("GET") && id != null){
        	sqlString = "SELECT * FROM todo WHERE id=" + id;
        }if(receive_httpMethod.equals("GET") && id == null){
        	sqlString = "SELECT * FROM todo";
        }else if(receive_httpMethod.equals("POST")){
        	sqlString = "INSERT INTO todo (name, description, is_done, due_date, order_id) "
        					 + "VALUES ('"+data.getString("name")+"', '"+data.getString("description")+"', '"+data.getString("is_done")+"', '"+data.getString("due_date")+"', "+data.getString("order_id")+")";
        }else if(receive_httpMethod.equals("PUT")){
        	sqlString = "UPDATE todo SET name='"+data.getString("name")+"', description='"+data.getString("description")+"', is_done='"+data.getString("is_done")+"', due_date='"+data.getString("due_date")+"', order_id='"+data.getString("order_id")+"' "
        					 + "WHERE id=" + ((id!=null)? id : data.getString("id"));
        }else if(receive_httpMethod.equals("DELETE")){
        	sqlString = "DELETE FROM WHERE id=" + ((id!=null)? id : data.getString("id"));
        }
        System.out.println("sqlString: "+sqlString);
		return sqlString;
	}
	
    public static JSONObject exceuteSql(String sqlString) {

    	Connection conn = null;
    	String connectionString = System.getenv("connection_string");
        try {
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(connectionString);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        

//    	PreparedStatement prest;
//        prest = conn.prepareStatement("", Statement.RETURN_GENERATED_KEYS);
//        prest.setString(1,"abc");
//        prest.setInt(2,123);prest.executeUpdate();
//        ResultSet rs = prest.getGeneratedKeys();
//        if(rs.next())
//        {
//            int last_inserted_id = rs.getInt(1);
//        }
        
        
        
    	JSONObject result = new JSONObject();
    	Statement stmt = null;
    	ResultSet rs = null;
    	try {
    	    
    	    stmt = conn.createStatement();
    	    if(sqlString.startsWith("SELECT")){
    	    	
    	    	rs = stmt.executeQuery(sqlString);
    	    	
        	    while (rs != null && rs.next()) {
                    result.put("id", rs.getString("id"));
                    result.put("name", rs.getString("name"));
                    result.put("description", rs.getString("description"));
                    result.put("is_done", rs.getString("is_done"));
                    result.put("due_date", rs.getString("due_date"));
                    result.put("order_id", rs.getString("order_id"));
                }
    	    }
    	    else{
    	    	int returnResult = stmt.executeUpdate(sqlString);
    	    	result.put("is_updated", returnResult==1?"true":"false");
    	    }
    	    
    	    
    	} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		    // it is a good idea to release
		    // resources in a finally{} block
		    // in reverse-order of their creation
		    // if they are no-longer needed

		    if (rs != null) {
		        try { rs.close(); } 
		        catch (SQLException sqlEx) { } // ignore
		        rs = null;
		    }
		    if (stmt != null) {
		        try { stmt.close(); } 
		        catch (SQLException sqlEx) { } // ignore
		        stmt = null;
		    }
		    if (conn != null) {
		        try { conn.close(); } 
		        catch (SQLException sqlEx) { } // ignore
		        conn = null;
		    }
		}
    	
    	return result;
    }
}
