package util;

import java.sql.*;

public class DBConnection {
	public static Connection getDBConnection() throws SQLException {

		// declare a connection by using Connection interface
		Connection connection = null;

		try {

			String connectionURL = "jdbc:mysql://localhost:3306/vGroup";

			// Load JBBC driver "com.mysql.jdbc.Driver"
			Class.forName("com.mysql.jdbc.Driver").newInstance();

			/*
			 * Create a connection by using getConnection() method that takes
			 * parameters of string type connection url, user name and password
			 * to connect to database.
			 */
			connection = DriverManager.getConnection(connectionURL, "root", "jeevi");

			/*
			 * // check weather connection is established or not by isClosed()
			 * method if(!connection.isClosed()) System.out.println(
			 * "Successfully connected to " + "MySQL server using TCP/IP...");
			 * connection.close();
			 */

			return connection;

		} catch (Exception e) {
			System.out.println(e.toString());
		}
		return connection;
	}
}