package nox.encrypt.testEncryption;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.security.interfaces.RSAPrivateKey;

public class decrypt {
	public String de_code() throws Exception {
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream("c:/Enc_RSA.dat")));
		String ctext = in.readLine();
		BigInteger c = new BigInteger(ctext);

		FileInputStream f = new FileInputStream("c:/Skey_RSA_priv.dat");
		ObjectInputStream b = new ObjectInputStream(f);
		RSAPrivateKey prk = (RSAPrivateKey) b.readObject();
		BigInteger d = prk.getPrivateExponent();
		BigInteger n = prk.getModulus();
		System.out.println("d= " + d);
		System.out.println("n= " + n);

		BigInteger m = c.modPow(d, n);
		System.out.println("m= " + m);
		byte[] mt = m.toByteArray();
		String cs = new String();
		for (int i = 0; i < mt.length; i++) {
			cs = cs + (char) mt[i];
		}
		System.out.println("PlainText is " + cs);
		return cs;
	}
}
