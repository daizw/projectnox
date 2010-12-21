package nox.encrypt.testEncryption;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;

public class encrypt {
	public String en_code(String s) throws Exception {
		String text = s;
		FileInputStream f = new FileInputStream("c:/Skey_RSA_pub.dat");
		ObjectInputStream b = new ObjectInputStream(f);
		RSAPublicKey pbk = (RSAPublicKey) b.readObject();// 得到RSA公钥描述结构类
		BigInteger e = pbk.getPublicExponent();// 得到公钥
		BigInteger n = pbk.getModulus();// 得到公共模
		System.out.println("e= " + e);
		System.out.println("n= " + n);
		byte ptext[] = text.getBytes("UTF8");// 将明文转化成UTF8格式
		BigInteger m = new BigInteger(ptext);
		BigInteger c = m.modPow(e, n); // 加密
		System.out.println("c= " + c);
		String cs = c.toString();
		System.out.println("cs= " + cs);
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream("c:/Enc_RSA.dat")));
		out.write(cs, 0, cs.length());
		out.close();
		return cs;
	}
}