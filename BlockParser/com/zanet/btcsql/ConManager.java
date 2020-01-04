package com.zanet.btcsql;
 
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class ConManager  
{
 
	Connection myConnection = null;
	 
	public Connection getConnection() {
	 
		 try {
				Properties props = new Properties();
				FileInputStream in = new FileInputStream("db.properties");
				props.load(in);
				
				
				String SERVER = props.getProperty("server");
				String PORT = props.getProperty("port");
				String USER = props.getProperty("username");
				String PASSWORD = props.getProperty("password");
				String DBNAME  = props.getProperty("database");
				
				in.close();
			Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
		  
			myConnection =  DriverManager.getConnection("jdbc:mysql://"+SERVER+":"+PORT+"/"+DBNAME+"?user="+USER+"&password="+PASSWORD+"&rewriteBatchedStatements=true ");  
			//&allowPublicKeyRetrieval=true&useSSL=false ?rewriteBatchedStatements=true
			 
		} catch (Exception ex) {
		   System.out.println("SQLException: " + ex.getMessage());
		 }
		return myConnection;
	} 
}