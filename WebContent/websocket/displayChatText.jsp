<%@ page import="com.mysql.*" %>
<%@ page import="java.sql.*" %>
<%@ page import="util.DBConnection" %>


<html>
<body>
<div id="content">
    <table border="0" cellpadding="10">
        <thead>
            <tr>
                <th>Account_id</th>
                <th>User_id</th>
                <th>Chat_Group</th>
                <th>Chat_TS</th>
                <th>Chat_Text</th>
                <th>Latitude</th>
                <th>Longitude</th>
            </tr>
        </thead>
        <tbody>
            <%
            try
        	{
        		String sql = "select Account_id, User_id, Chat_Group, Chat_TS, Chat, Latitude, Longitude from vgroup.tbl_chathistory";
            	Connection connection = DBConnection.getDBConnection();
        		ResultSet rs= null;
        		PreparedStatement pstmt = null;
        		pstmt = connection.prepareStatement(sql);
        		
        		rs = pstmt.executeQuery();
        		
        		while(rs!=null && rs.next())
        		{	
        			{
        	            %>
        	            <tr>
        	                <%
        	                    int account_id = rs.getInt("Account_id");
        	                    String user_id = rs.getString("User_id");                    
        	                    String chat_Group = rs.getString("Chat_Group");
        	                    String timestamp = rs.getString("Chat_TS");
        	                    String chat = rs.getString("Chat");
        	                    String latitude = rs.getString("Latitude");
        	                    String longitude = rs.getString("Longitude");
        	                %>
        	                <td><%=account_id %></td>
        	                <td><%=user_id %></td>
        	                <td><%=chat_Group %></td>
        	                <td><%=timestamp %></td>
        	                <td><%=chat %></td>
        	                <td><%=latitude %></td>
        	                <td><%=longitude %></td>
        	            </tr>               

        	            <%      
        	                }
        	            
        			
        		}
        		if (rs!=null)
        			rs.close();
        		if (pstmt!=null)
        			pstmt.close();
        		
        		if(connection!=null && !connection.isClosed())
        			connection.close();
        		
        	}catch (Exception e)
        	{
        		System.out.println(e.toString());
        	}
                
            %>

        </tbody>
    </table>
</div>
</body>
</html>
