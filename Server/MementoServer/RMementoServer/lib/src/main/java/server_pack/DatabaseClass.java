package server_pack;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseClass {
	
	//private static Connection connection;
	public DatabaseClass() {
		// TODO Auto-generated constructor stub
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/example", "postgres", "postgres")) {
			 
        
            System.out.println("Connected to PostgreSQL database!");
            Statement statement = connection.createStatement();
           
            ResultSet resultSet = statement.executeQuery("SELECT * FROM public.Users");
 
        } /*catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC driver not found.");
            e.printStackTrace();
        }*/ catch (SQLException e) {
            System.out.println("Connection failure.");
            e.printStackTrace();
        }
	}

}
