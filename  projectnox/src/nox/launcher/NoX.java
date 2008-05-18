package nox.launcher;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import nox.db.DBTableName;
import nox.encrypt.EncryptUtil;
import nox.net.JXTANetwork;
import nox.net.NoxToolkit;
import nox.ui.Cheyenne;
import nox.ui.ObjectList;

public class NoX {
	public static void main(String args[]) {
		System.setProperty("sun.java2d.noddraw", "true");// Ϊ��͸����׼��
		System.setProperty("net.jxta.logging.Logging", "INFO");
		System.setProperty("net.jxta.level", "INFO");
		System.setProperty("java.util.logging.config.file",
				"logging.properties");

		JXTANetwork MyLogin;
		MyLogin = new JXTANetwork();
		MyLogin.Start();

		// ���ݿ���
		String username = NoxToolkit.getNetworkConfigurator().getName();
		try {
			Class.forName("org.hsqldb.jdbcDriver").newInstance();
			Connection conn = DriverManager.getConnection(
					"jdbc:hsqldb:file:db/" + username, "sa", "");
			Statement stmt = conn.createStatement();
			try {
				System.out.println("creating table: "
						+ DBTableName.ME_SQLTABLE_NAME);
				stmt.execute("create table " + DBTableName.ME_SQLTABLE_NAME
						+ " (Tag VARCHAR not null, Object OTHER not null)");

				System.out.println("creating table: "
						+ DBTableName.PEER_SQLTABLE_NAME);
				stmt
						.execute("create table "
								+ DBTableName.PEER_SQLTABLE_NAME
								+ " (ID VARCHAR not null, Good BOOLEAN not null, Object OTHER not null)");

				System.out.println("creating table: "
						+ DBTableName.GROUP_SQLTABLE_NAME);
				stmt
						.execute("create table "
								+ DBTableName.GROUP_SQLTABLE_NAME
								+ " (ID VARCHAR not null, Good BOOLEAN, Object OTHER not null)");
				stmt.close();
			} catch (SQLException e) {
				System.out.println("���ݿ��Ѵ���");
				e.printStackTrace();
			}
			// TODO ��ʼ��(ϵͳ/����)����
			// ��ȡ��˽Կ
			//initMyKeyPair(conn, DBTableName.ME_SQLTABLE_NAME);
			initEcryption(conn, DBTableName.ME_SQLTABLE_NAME);

			// ��ʼ���б�
			/**
			 * �����б�
			 */
			ObjectList flist = new ObjectList(conn,
					DBTableName.PEER_SQLTABLE_NAME, true);
			/**
			 * ������
			 */
			ObjectList blist = new ObjectList(conn,
					DBTableName.PEER_SQLTABLE_NAME, false);
			/**
			 * ���б�
			 */
			ObjectList glist = new ObjectList(conn,
					DBTableName.GROUP_SQLTABLE_NAME, true);

			Cheyenne chyn = new Cheyenne(flist, glist, blist, conn);
			chyn.pack();
			chyn.setVisible(true);
		} catch (SQLException e) {
			System.out.println("���ݿ��쳣");
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * �����ݿ��ж�ȡ��Կ(SecretKey), ���浽EncryptUtil��.</p>
	 * ���û�����½������浽���ݿ���.</p>
	 * 
	 * @param sqlconn ���ݿ�����
	 * @param tablename ���ݿ����
	 */
	private static void initEcryption(Connection conn, String meSqltableName) {
		//do nothing
	}

	/**
	 * �����ݿ��ж�ȡ��˽Կ(KeyPair), ���浽EncryptUtil��.</p>
	 * ���û�����½������浽���ݿ���.</p>
	 * 
	 * @param sqlconn ���ݿ�����
	 * @param tablename ���ݿ����
	 */
	private static void initMyKeyPair(Connection sqlconn, String tablename) {
		Statement stmt;
		try {
			stmt = sqlconn.createStatement();
			ResultSet rs = stmt.executeQuery("select * from " + tablename
					+ " where Tag = " + EncryptUtil.MYKEYPAIR_TAG);

			while (rs.next()) {
				ObjectInputStream objInput = new ObjectInputStream(rs
						.getBinaryStream("Object"));
				EncryptUtil.setKeyPair((KeyPair) objInput.readObject());

				System.out.println("Found object. It's a KeyPair");
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		if (EncryptUtil.getKeyPair() == null) {
			System.out.println("KeyPair == null, now create a new one");
			KeyPair mykeys = EncryptUtil.generateKeyPair();
			if(mykeys == null){
				System.out.println("������Կ�԰�");
				return;
			}
				
			EncryptUtil.setKeyPair(mykeys);
			System.out.println("inserting this new keypair into database...");
			try {
				stmt = sqlconn.createStatement();
				// ɾ�����ݿ��и�Tag����
				System.out.println("stmt.execute:delete from " + tablename
						+ " where Tag = '" + EncryptUtil.MYKEYPAIR_TAG + "'");
				stmt.execute("delete from " + tablename + " where Tag = '"
						+ EncryptUtil.MYKEYPAIR_TAG + "'");
				// ��ӵ����ݿ�
				PreparedStatement pstmt = sqlconn.prepareStatement("insert into " + tablename
								+ " values (?, ?)");
				pstmt.setString(1, EncryptUtil.MYKEYPAIR_TAG);

				ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(byteArrayStream);

				out.writeObject(mykeys);
				ByteArrayInputStream input = new ByteArrayInputStream(
						byteArrayStream.toByteArray());
				pstmt.setBinaryStream(2, input, byteArrayStream.size());
				pstmt.executeUpdate();
				pstmt.close();
				System.out.println("Done! -- succeed in inserting keypair into database");
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
