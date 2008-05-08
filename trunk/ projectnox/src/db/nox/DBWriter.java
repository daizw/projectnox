package db.nox;

import java.sql.SQLException;
import java.sql.Statement;

import noxUI.NoxJListItem;
import noxUI.ObjectList;

public class DBWriter {
	Statement statement;

	DBWriter(Statement stmt) {
		this.statement = stmt;
	}

	public void doWrite(ObjectList list, String tablename) {
		int length = list.getModel().getSize();//getRealSize
		NoxJListItem item;
		try {
			for (int i = 0; i < length; i++) {
				item = (NoxJListItem) (list.getModel().getElementAt(i));
				// if(item instanceof )
				statement.execute("INSERT INTO " + tablename + " VALUES "
						+ "('" + item.getNick() + "', '" + item.getSign()
						+ "', '" + item.getUUID().toString() + "')");
			}
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
