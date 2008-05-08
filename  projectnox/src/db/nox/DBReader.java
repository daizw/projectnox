package db.nox;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import noxUI.NoxJListItem;
import noxUI.ObjectList;

public class DBReader {
	Statement statement;
	
	DBReader(Statement stmt){
		this.statement = stmt;
	}
	public void doRead(ObjectList list, String tablename){
		//NoxJListItem item = new item("");
		try {
			ResultSet results = statement.executeQuery("SELECT * FROM " + tablename);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
