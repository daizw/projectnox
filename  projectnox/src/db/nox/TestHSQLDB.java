package db.nox;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.swing.JOptionPane;

public class TestHSQLDB {
	
	public static void main(String [] args){
		String username = (String) JOptionPane.showInputDialog(null,
				"Enter username", "TestHSQLDB", JOptionPane.QUESTION_MESSAGE,
				null, null, "");
		try {
			Class.forName("org.hsqldb.jdbcDriver").newInstance();
			Connection c = DriverManager.getConnection("jdbc:hsqldb:file:db/" +username, "sa", "");
		} catch (SQLException e) {
			System.out.println("数据库不存在?");
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
