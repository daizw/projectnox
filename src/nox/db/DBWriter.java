package nox.db;

import java.sql.SQLException;
import java.sql.Statement;

import nox.ui.common.NoxJListItem;
import nox.ui.common.ObjectList;

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
						+ "('" + item.getName() + "', '" + item.getDesc()
						+ "', '" + item.getUUID().toString() + "')");
			}
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}