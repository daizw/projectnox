package noxUI;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import net.nox.JXTANetwork;
import net.nox.NoxToolkit;
import db.nox.DBTableName;

public class NoX {
	public static void main(String args[]) {
		System.setProperty("sun.java2d.noddraw", "true");// 为半透明做准备
		System.setProperty("net.jxta.logging.Logging", "INFO");
		System.setProperty("net.jxta.level", "INFO");
		System.setProperty("java.util.logging.config.file",
				"logging.properties");

		JXTANetwork MyLogin;
		MyLogin = new JXTANetwork();
		MyLogin.Start();

		//数据库名
		String username = new NoxToolkit().getNetworkConfigurator().getName();
		try {
			Class.forName("org.hsqldb.jdbcDriver").newInstance();
			Connection conn = DriverManager.getConnection(
					"jdbc:hsqldb:file:db/" + username, "sa", "");
			Statement stmt = conn.createStatement();
			try{				
				System.out.println("creating table: " +
						DBTableName.PEER_SQLTABLE_NAME);
				stmt.execute("create table " +
						DBTableName.PEER_SQLTABLE_NAME  +
						" (ID VARCHAR, Good BOOLEAN, Object OTHER)");
				
				System.out.println("creating table: " +
						DBTableName.GROUP_SQLTABLE_NAME);
				stmt.execute("create table " +
						DBTableName.GROUP_SQLTABLE_NAME  +
						" (ID VARCHAR, Good BOOLEAN, Object OTHER)");
				stmt.close();
			} catch(SQLException e) {
				System.out.println("数据库已存在");
				e.printStackTrace();
			}
			/**
			 * 好友列表
			 */
			ObjectList flist = new ObjectList(conn,
					DBTableName.PEER_SQLTABLE_NAME, true);
			/**
			 * 黑名单
			 */
			ObjectList blist = new ObjectList(conn,
					DBTableName.PEER_SQLTABLE_NAME, false);
			/**
			 * 组列表
			 */
			ObjectList glist = new ObjectList(conn,
					DBTableName.GROUP_SQLTABLE_NAME, true);
			
			Cheyenne chyn = new Cheyenne(flist, glist, blist, conn);
			chyn.pack();
			chyn.setVisible(true);
		} catch (SQLException e) {
			System.out.println("数据库异常");
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
