package nox.launcher;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.EllipticCurve;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import nox.db.DBTableName;
import nox.encrypt.EncryptUtil;
import nox.net.common.JXTANetwork;
import nox.net.common.NoxToolkit;
import nox.ui.common.ObjectList;
import nox.ui.me.Cheyenne;

import org.bouncycastle.jce.ECPointUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

public class NoX {
	public static final String ALGORITHM = "ECDH";
	public static KeyPair MyKeyPair;
	
	private static final EllipticCurve curve = new EllipticCurve(
			new ECFieldFp(
					new BigInteger(
							"883423532389192164791648750360308885314476597252960362792450860609699839")), // q
			new BigInteger(
					"7fffffffffffffffffffffff7fffffffffff8000000000007ffffffffffc",
					16), // a
			new BigInteger(
					"6b016c3bdcf18941d0d654921475ca71a9db2fb27d1d37796185c2942c0a",
					16)); // b

	private static final ECParameterSpec ecSpec = new ECParameterSpec(
			curve,
			ECPointUtil
					.decodePoint(
							curve,
							Hex
									.decode("020ffa963cdca8816ccc33b8642bedf905c3d358573d3f27fbbd3b3cb9aaaf")), // G
			new BigInteger(
					"883423532389192164791648750360308884807550341691627752275345424702807307"), // n
			1); // h

	public static void main(String args[]) throws Exception {
		System.setProperty("sun.java2d.noddraw", "true");// 为半透明做准备
		System.setProperty("net.jxta.logging.Logging", "INFO");
		System.setProperty("net.jxta.level", "INFO");
		System.setProperty("java.util.logging.config.file",
				"logging.properties");

		JXTANetwork MyLogin;
		MyLogin = new JXTANetwork();
		MyLogin.Start();

		// 数据库名
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
			} catch (SQLException e) {
				System.out.println("数据库表ME已存在");
				e.printStackTrace();
			}

			try {
				System.out.println("creating table: "
						+ DBTableName.PEER_SQLTABLE_NAME);
				stmt
						.execute("create table "
								+ DBTableName.PEER_SQLTABLE_NAME
								+ " (ID VARCHAR not null, Good BOOLEAN not null, Object OTHER not null)");
			} catch (SQLException e) {
				System.out.println("数据库表PEER已存在");
				e.printStackTrace();
			}
		
			try {
				System.out.println("creating table: "
						+ DBTableName.GROUP_SQLTABLE_NAME);
				stmt
						.execute("create table "
								+ DBTableName.GROUP_SQLTABLE_NAME
								+ " (ID VARCHAR not null, Good BOOLEAN, Object OTHER not null)");
				stmt.close();
			} catch (SQLException e) {
				System.out.println("数据库表GROUP已存在");
				e.printStackTrace();
			}
			// TODO 初始化(系统/个人)设置
			// 读取公私钥
			prepareKeyPair();
			//initMyKeyPair(conn, DBTableName.ME_SQLTABLE_NAME);
			initEcryption(conn, DBTableName.ME_SQLTABLE_NAME);
			//或者由Cheyenne初始化
			initMyStatus(conn, DBTableName.ME_SQLTABLE_NAME);

			// 初始化列表
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

	/**
	 * 初始化个人设置
	 * @param conn
	 * @param meSqltableName
	 */
	private static void initMyStatus(Connection conn, String meSqltableName) {
	}

	private static void prepareKeyPair() throws Exception {
		Security.addProvider(new BouncyCastleProvider());
		KeyPairGenerator aliceKpairGen = KeyPairGenerator.getInstance(
				ALGORITHM, "BC");
		aliceKpairGen.initialize(ecSpec, new SecureRandom());

		MyKeyPair = aliceKpairGen.generateKeyPair();
	}
	/**
	 * 从数据库中读取密钥(SecretKey), 保存到EncryptUtil中.</p>
	 * 如果没有则新建并保存到数据库中.</p>
	 * 
	 * @param sqlconn 数据库连接
	 * @param tablename 数据库表名
	 */
	private static void initEcryption(Connection conn, String meSqltableName) {
		//do nothing
	}

	/**
	 * 从数据库中读取公私钥(KeyPair), 保存到EncryptUtil中.</p>
	 * 如果没有则新建并保存到数据库中.</p>
	 * 
	 * @param sqlconn 数据库连接
	 * @param tablename 数据库表名
	 * @deprecated
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
				System.out.println("生成密钥对败");
				return;
			}
				
			EncryptUtil.setKeyPair(mykeys);
			System.out.println("inserting this new keypair into database...");
			try {
				stmt = sqlconn.createStatement();
				// 删除数据库中该Tag表项
				System.out.println("stmt.execute:delete from " + tablename
						+ " where Tag = '" + EncryptUtil.MYKEYPAIR_TAG + "'");
				stmt.execute("delete from " + tablename + " where Tag = '"
						+ EncryptUtil.MYKEYPAIR_TAG + "'");
				// 添加到数据库
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