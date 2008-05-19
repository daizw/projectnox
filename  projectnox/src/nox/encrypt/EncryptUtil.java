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
			// secrand.setSeed("tttt".getBytes()); //初始化随机产生器
			// keygen.initialize(576,secrand); //初始化密钥生成器
			keygen.initialize(512);
			KeyPair keys = keygen.genKeyPair();
			// KeyPair keys=keygen.generateKeyPair(); //生成密钥组
			System.out.println("生成密钥对成功");
			return keys;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
			System.out.println("生成密钥对失败");
			return null;
		}
	}
	
	public static void main(String[] args) {
		EncryptUtil my = new EncryptUtil();
		my.testDigest();
	}

	public void testDigest() {
		try {
			String myinfo = "我的测试信息";
			// java.security.MessageDigest
			// alg=java.security.MessageDigest.getInstance("MD5");
			MessageDigest alga = MessageDigest
					.getInstance("SHA-1");
			alga.update(myinfo.getBytes());
			byte[] digesta = alga.digest();
			System.out.println("本信息摘要是:" + byte2hex(digesta));
			// 通过某中方式传给其他人你的信息(myinfo)和摘要(digesta) 对方可以判断是否更改或传输正常
			MessageDigest algb = MessageDigest
					.getInstance("SHA-1");
			algb.update(myinfo.getBytes());
			if (MessageDigest.isEqual(digesta, algb.digest())) {
				System.out.println("信息检查正常");
			} else {
				System.out.println("摘要不相同");
			}
		} catch (NoSuchAlgorithmException ex) {
			System.out.println("非法摘要算法");
		}
	}

	public static String byte2hex(byte[] b) // 二行制转字符串
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