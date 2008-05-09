package db.nox;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import noxUI.ItemStatus;

public class TestHSQLDB {
	
	public static void main(String [] args){
		String username = (String) JOptionPane.showInputDialog(null,
				"Enter username", "TestHSQLDB", JOptionPane.QUESTION_MESSAGE,
				null, null, "");
		try {
			Class.forName("org.hsqldb.jdbcDriver").newInstance();
			Connection con = DriverManager.getConnection("jdbc:hsqldb:file:db/" +username, "sa", "");
			Statement stmt = con.createStatement();
			try{
				/*System.out.println("dropping table: PEER");
				//stmt.execute("truncate table PEERS");
				stmt.execute("delete from PEERS where Good = true");
				stmt.execute("delete from PEERS where Good = false");
				stmt.execute("drop table PEERS if exists cascade");*/
				
				System.out.println("creating table: " +
						DBTableName.PEER_SQLTABLE_NAME);
				stmt.execute("create table " +
						DBTableName.PEER_SQLTABLE_NAME  +
						" (Good BOOLEAN, Object OTHER)");
			} catch(SQLException e) {
				System.out.println("数据库已存在");
				e.printStackTrace();
			}
			PreparedStatement pstmt = con.prepareStatement("insert into " +
					DBTableName.PEER_SQLTABLE_NAME + " values (?, ?)");
			
			pstmt.setString(1, "true");
			
			TestItem item1 = new TestItem(new ImageIcon("resrc/portrait/user.png"), "Tom", "I'm Tom", "jxta:urn:xxx");
			TestItem item2 = new TestItem(new ImageIcon("resrc/portrait/user.png"), "Jerry", "I'm Jerry", "jxta:urn:xxx");
			TestItem item3 = new TestItem(new ImageIcon("resrc/portrait/user.png"), "Duffy", "I'm Duffy", "jxta:urn:xxx");
			
			ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(byteArrayStream);
			out.writeObject((Object) item1);
			ByteArrayInputStream input = new ByteArrayInputStream(byteArrayStream.toByteArray());
			pstmt.setBinaryStream(2, input, byteArrayStream.size());
			int recCount;
			recCount = pstmt.executeUpdate();
			
			System.out.println("recCount: " + recCount);
			
			pstmt.setString(1, "false");
			byteArrayStream = new ByteArrayOutputStream();
			out = new ObjectOutputStream(byteArrayStream);
			out.writeObject((Object) item2);
			input = new ByteArrayInputStream(byteArrayStream.toByteArray());
			pstmt.setBinaryStream(2, input, byteArrayStream.size());
			recCount = pstmt.executeUpdate();
			System.out.println("recCount: " + recCount);
			
			pstmt.setString(1, "true");
			byteArrayStream = new ByteArrayOutputStream();
			out = new ObjectOutputStream(byteArrayStream);
			out.writeObject((Object) item3);
			input = new ByteArrayInputStream(byteArrayStream.toByteArray());
			pstmt.setBinaryStream(2, input, byteArrayStream.size());
			recCount = pstmt.executeUpdate();
			System.out.println("recCount: " + recCount);
						
			ResultSet rs = stmt.executeQuery("select * from " +
					DBTableName.PEER_SQLTABLE_NAME + 
					" where Good = true");
			
			System.out.println("Good guys:");
			while(rs.next()){
				ObjectInputStream objInput = new ObjectInputStream(rs.getBinaryStream("Object"));
				System.out.println("Found object. Contents: ");
				
				TestItem tmpItem = (TestItem)objInput.readObject();
				System.out.println("Nick:	" + tmpItem.getNick());
				System.out.println("Desc:	" + tmpItem.getSign());
				System.out.println("ID:	" + tmpItem.getUUID());
				System.out.println("Portrait:	" + tmpItem.getPortrait());
				System.out.println("TimeStamp:	" + tmpItem.getTimeStamp());
				System.out.println();
			}
			
			rs = stmt.executeQuery("select * from " +
					DBTableName.PEER_SQLTABLE_NAME + 
					" where Good = false");
			System.out.println("Bad guys:");
			while(rs.next()){
				ObjectInputStream objInput = new ObjectInputStream(rs.getBinaryStream("Object"));
				System.out.println("Found object. Contents: ");
				
				TestItem tmpItem = (TestItem)objInput.readObject();
				System.out.println("Nick:	" + tmpItem.getNick());
				System.out.println("Desc:	" + tmpItem.getSign());
				System.out.println("ID:	" + tmpItem.getUUID());
				System.out.println("Portrait:	" + tmpItem.getPortrait());
				System.out.println("TimeStamp:	" + tmpItem.getTimeStamp());
				System.out.println();
			}
			stmt.execute("SHUTDOWN");
			stmt.close();
		} catch (SQLException e) {
			System.out.println("数据库异常");
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

@SuppressWarnings("serial")
class TestItem implements Serializable{
	private ImageIcon portrait;
	private String nickname;
	private String sign;
	private String UUID;
	protected ItemStatus stat;
	private Long timeStamp;

	TestItem(ImageIcon portr, String nick, String signstr, String uuid) {
		this.portrait = portr;
		this.nickname = nick;
		this.sign = signstr;
		this.UUID = uuid;
		this.timeStamp = new Date().getTime();
	}
	
	/**
	 * 获取头像
	 * @return 头像ImageIcon
	 */
	protected ImageIcon getPortrait() {
		return portrait;
	}
	/**
	 * 获取昵称
	 * @return 昵称Text
	 */
	protected String getNick() {
		return nickname;
	}
	/**
	 * 获取签名档
	 * @return 签名档Text
	 */
	protected String getSign() {
		return sign;
	}
	/**
	 * 获取该Item的ID
	 * @return 组的PeerGroupID,或者Peer的PeerID
	 */
	protected String getUUID(){
		return UUID;
	}
	public Long getTimeStamp(){
		return timeStamp;
	}
}