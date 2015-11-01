/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package websocket.chat;

import java.io.IOException;
import java.sql.Connection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import java.sql.*;


import util.DBConnection;
import util.HTMLFilter;

@ServerEndpoint(value = "/websocket/chat")
public class ChatAnnotation {

    private static final Log log = LogFactory.getLog(ChatAnnotation.class);

    private static final String GUEST_PREFIX = "Guest";
    private static final AtomicInteger connectionIds = new AtomicInteger(0);
    private static final Set<ChatAnnotation> connections =
            new CopyOnWriteArraySet<>();

    private final String nickname;
    private Session session;

    public ChatAnnotation() 
    {
    	int increment =0;
    	String user_id = "";
    	increment = connectionIds.getAndIncrement();
    	user_id = getuserId(increment);
    	
    	if(user_id=="")
			user_id = GUEST_PREFIX + increment;
    	
    	nickname = user_id;
    }


    @OnOpen
    public void start(Session session) {
        this.session = session;
        connections.add(this);
        insertChatText(nickname,"Joined chat room");
        String message = String.format("* %s %s", nickname, "has joined.");
        broadcast(message);
    }


    @OnClose
    public void end() {
        connections.remove(this);
        insertChatText(nickname,"Disconnected Chat room");
        String message = String.format("* %s %s",
                nickname, "has disconnected.");
        broadcast(message);
    }


    @OnMessage
    public void incoming(String message) {
        // Never trust the client
        String filteredMessage = String.format("%s: %s",
                nickname, HTMLFilter.filter(message.toString()));
        insertChatText(nickname,filteredMessage);
        
        System.currentTimeMillis();
        
        String adText ="";
        callTopKeyWordProc();
        adText = "Your advertisement here :<<"+getTopKeyWord() + ">>";
    
        filteredMessage = filteredMessage;
        broadcast(filteredMessage);
        
        broadcast(adText);
     }




    @OnError
    public void onError(Throwable t) throws Throwable {
        log.error("Chat Error: " + t.toString(), t);
    }


    private static void broadcast(String msg) {
        for (ChatAnnotation client : connections) {
            try {
                synchronized (client) {
                    client.session.getBasicRemote().sendText(msg);
                }
            } catch (IOException e) {
                log.debug("Chat Error: Failed to send message to client", e);
                connections.remove(client);
                try {
                    client.session.close();
                } catch (IOException e1) {
                    // Ignore
                }
                
                String message = String.format("* %s %s",
                        client.nickname, "has been disconnected.");
                insertChatText(client.nickname,"has been disconnected");
                broadcast(message);
            }
        }
    }
    
    private static String getuserId(int increment) 
    {
    	int chatId = 222114;
    	String sql = "select user_id from vgroup.tbl_userinventory where account_id = ?";
    	chatId = chatId + increment;
    	String user_id = "";
    	try
    	{
    		Connection connection = DBConnection.getDBConnection();
    		ResultSet rs= null;
    		PreparedStatement pstmt = null;
    		pstmt = connection.prepareStatement(sql);
    		pstmt.setInt(1,  chatId);
    		rs = pstmt.executeQuery();
    		
    		while(rs!=null && rs.next())
    		{	
    			System.out.println(rs.toString());
    			user_id = rs.getString("user_id");
    			
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

    	return user_id;
    }
    
    
    private static void callTopKeyWordProc() 
    {
    	
    	
    	String simpleProc = "{ call vgroup.split_string() }";
 
        
    	String sql = "call vgroup.split_string();";
    	try
    	{
    		Connection connection = DBConnection.getDBConnection();
    		PreparedStatement pstmt = null;
    		pstmt = connection.prepareStatement(sql);
            CallableStatement cs = connection.prepareCall(simpleProc);
            cs.execute();
            
    		if (cs!=null)
    			cs.close();
    		if (pstmt!=null)
    			pstmt.close();
    		
    		if(connection!=null && !connection.isClosed())
    			connection.close();
    		
    	}catch (Exception e)
    	{
    		System.out.println(e.toString());
    	}

    }
    
    
    private static String getTopKeyWord() 
    {
    	String sql = "select  splitted_column,insert_ts from vgroup.tbl_finallist where 1 =1 and id ='NFL' order by cnt desc limit 1";
    	String topKeyWord = "";
    	try
    	{
    		Connection connection = DBConnection.getDBConnection();
    		ResultSet rs= null;
    		PreparedStatement pstmt = null;
    		pstmt = connection.prepareStatement(sql);
    		rs = pstmt.executeQuery();
    		String insertTs = "";
    		while(rs!=null && rs.next())
    		{	
    			System.out.println(rs.toString());
    			topKeyWord = rs.getString("splitted_column");
    			insertTs = rs.getString("insert_ts");
    			
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
    	    	
    	return topKeyWord;
    }
    
    
    private static String getAccountId(String user_id) 
    {
    	String account_id="";
    	String sql = "select account_id from vgroup.tbl_userinventory where user_id = ?";
    	try
    	{
    		Connection connection = DBConnection.getDBConnection();
    		ResultSet rs= null;
    		PreparedStatement pstmt = null;
    		pstmt = connection.prepareStatement(sql);
    		pstmt.setString(1,  user_id);
    		rs = pstmt.executeQuery();
    		
    		while(rs!=null && rs.next())
    		{	
    			System.out.println(rs.toString());
    			account_id = rs.getString("account_id");
    			
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

    	return account_id;
    }
    
    private static void insertChatText(String nickname, String chat_text) 
    {
    	
    	String account_id ="";
    	String user_id ="";
    	String chat_group ="NFL";
    	String latitude ="40.91709";
    	String longitude ="-72.709457";
    	String timestamp ="";
    	if(chat_text.contains(nickname))
		{
    		chat_text = chat_text.substring(nickname.length()+1, chat_text.length());
		}
    	account_id  = getAccountId(nickname);
    	user_id = nickname;
    	java.util.Date datetime = new java.util.Date();
    	try 
        {

    		String sql = "insert into  vgroup.tbl_chathistory (Account_id, User_id, Chat_Group,  Chat, Latitude, Longitude ) values (?,?,?,?,?,?)";
        	Connection connection = DBConnection.getDBConnection();
    		ResultSet rs= null;
    		PreparedStatement pstmt = null;
    		pstmt = connection.prepareStatement(sql);
    		pstmt.setString(1, account_id);
        	pstmt.setString(2, user_id);
        	pstmt.setString(3, chat_group);
        	pstmt.setString(4, chat_text);
        	pstmt.setString(5, latitude);
        	pstmt.setString(6, longitude);
        	int updateQuery = pstmt.executeUpdate();

    		if (rs!=null)
    			rs.close();
    		if (pstmt!=null)
    			pstmt.close();
    		
    		if(connection!=null && !connection.isClosed())
    			connection.close();

        } catch (Exception e) 
    	{
        	System.out.println(e.toString());        
        }
    }
    
}
