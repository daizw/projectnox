package nox.encrypt;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

public class EncryptUtil {
	public final static String DESKEY_TAG = "DESKEY";
	public final static String MYKEYPAIR_TAG = "MYKEYPAIR";
	public final static String MYPUBLICKEY_TAG = "MYPUBLICKEY";
	public final static String MYPRIVATEKEY_TAG = "MYPRIVATEKEY";
	
	private static KeyPair mykeys = null;
	
	public static void setKeyPair(KeyPair keys){
		mykeys = keys;
	}
	
	public static KeyPair getKeyPair(){
		return mykeys;
	}
	
	public static PublicKey getPublicKey(){
		return (mykeys != null)? mykeys.getPublic() : null;
	}
	
	public static PrivateKey getPrivateKey(){
		return (mykeys != null)? mykeys.getPrivate() : null;
	}
	
	public static KeyPair generateKeyPair() {
		try {
			KeyPairGenerator keygen = java.security.KeyPairGenerator.getInstance("DSA");
			// SecureRandom secrand=new SecureRandom();
			// secrand.setSeed("tttt".getBytes()); //��ʼ�����������
			// keygen.initialize(576,secrand); //��ʼ����Կ������
			keygen.initialize(512);
			KeyPair keys = keygen.genKeyPair();
			// KeyPair keys=keygen.generateKeyPair(); //������Կ��
			System.out.println("������Կ�Գɹ�");
			return keys;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
			System.out.println("������Կ��ʧ��");
			return null;
		}
	}
	
	public static void main(String[] args) {
		EncryptUtil my = new EncryptUtil();
		my.testDigest();
	}

	public void testDigest() {
		try {
			String myinfo = "�ҵĲ�����Ϣ";
			// java.security.MessageDigest
			// alg=java.security.MessageDigest.getInstance("MD5");
			MessageDigest alga = MessageDigest
					.getInstance("SHA-1");
			alga.update(myinfo.getBytes());
			byte[] digesta = alga.digest();
			System.out.println("����ϢժҪ��:" + byte2hex(digesta));
			// ͨ��ĳ�з�ʽ���������������Ϣ(myinfo)��ժҪ(digesta) �Է������ж��Ƿ���Ļ�������
			MessageDigest algb = MessageDigest
					.getInstance("SHA-1");
			algb.update(myinfo.getBytes());
			if (MessageDigest.isEqual(digesta, algb.digest())) {
				System.out.println("��Ϣ�������");
			} else {
				System.out.println("ժҪ����ͬ");
			}
		} catch (NoSuchAlgorithmException ex) {
			System.out.println("�Ƿ�ժҪ�㷨");
		}
	}

	public static String byte2hex(byte[] b) // ������ת�ַ���
	{
		String hs = "";
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = (Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1)
				hs = hs + "0" + stmp;
			else
				hs = hs + stmp;
			if (n < b.length - 1)
				hs = hs + ":";
		}
		return hs.toUpperCase();
	}
}