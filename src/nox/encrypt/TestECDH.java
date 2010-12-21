package nox.encrypt;

import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.EllipticCurve;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.bouncycastle.jce.ECPointUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

public class TestECDH {

	private static void testECDH(String algorithm) throws Exception {
		KeyPairGenerator g = KeyPairGenerator.getInstance(algorithm, "BC");

		EllipticCurve curve = new EllipticCurve(
				new ECFieldFp(
						new BigInteger(
								"883423532389192164791648750360308885314476597252960362792450860609699839")), // q
				new BigInteger(
						"7fffffffffffffffffffffff7fffffffffff8000000000007ffffffffffc",
						16), // a
				new BigInteger(
						"6b016c3bdcf18941d0d654921475ca71a9db2fb27d1d37796185c2942c0a",
						16)); // b

		ECParameterSpec ecSpec = new ECParameterSpec(
				curve,
				ECPointUtil
						.decodePoint(
								curve,
								Hex
										.decode("020ffa963cdca8816ccc33b8642bedf905c3d358573d3f27fbbd3b3cb9aaaf")), // G
				new BigInteger(
						"883423532389192164791648750360308884807550341691627752275345424702807307"), // n
				1); // h

		g.initialize(ecSpec, new SecureRandom());

		//
		// a side
		//
		KeyPair aKeyPair = g.generateKeyPair();

		KeyAgreement aKeyAgree = KeyAgreement.getInstance(algorithm, "BC");

		aKeyAgree.init(aKeyPair.getPrivate());

		//
		// b side
		//
		g.initialize(ecSpec, new SecureRandom());
		
		KeyPair bKeyPair = g.generateKeyPair();

		KeyAgreement bKeyAgree = KeyAgreement.getInstance(algorithm, "BC");

		bKeyAgree.init(bKeyPair.getPrivate());

		//
		// agreement
		//
		aKeyAgree.doPhase(bKeyPair.getPublic(), true);
		bKeyAgree.doPhase(aKeyPair.getPublic(), true);

		BigInteger k1 = new BigInteger(aKeyAgree.generateSecret());
		BigInteger k2 = new BigInteger(bKeyAgree.generateSecret());

		if (!k1.equals(k2)) {
			fail(algorithm + " 2-way test failed");
		}


		String text = "abc";
//      //数据加密   
        byte[] aBys = aKeyAgree.generateSecret(); 
        KeySpec aKeySpec = new DESKeySpec(aBys);
        SecretKeyFactory aFactory = SecretKeyFactory.getInstance("DES");
        Key aSecretKey = aFactory.generateSecret(aKeySpec);
        
//        SecretKey aSecretKey = aKeyAgree.generateSecret(SECRET_ALGORITHM);
        Cipher aCipher = Cipher.getInstance(aSecretKey.getAlgorithm());   
        aCipher.init(Cipher.ENCRYPT_MODE, aSecretKey);  
        byte[] encText = aCipher.doFinal(text.getBytes());
        
        //数据解密
        byte[] bBys = bKeyAgree.generateSecret(); 
        KeySpec bKeySpec = new DESKeySpec(bBys);
        SecretKeyFactory bFactory = SecretKeyFactory.getInstance("DES");
        Key bSecretKey = bFactory.generateSecret(bKeySpec);
 //       SecretKey bSecretKey = bKeyAgree.generateSecret(SECRET_ALGORITHM);
        Cipher bCipher = Cipher.getInstance(bSecretKey.getAlgorithm());   
        bCipher.init(Cipher.DECRYPT_MODE, bSecretKey);   
  
        byte[] decText =  bCipher.doFinal(encText); 
        text = new String(decText);
        System.err.println("解密: " + text);

		//
		// public key encoding test
		//
		byte[] pubEnc = aKeyPair.getPublic().getEncoded();
		KeyFactory keyFac = KeyFactory.getInstance(algorithm, "BC");
		X509EncodedKeySpec pubX509 = new X509EncodedKeySpec(pubEnc);
		ECPublicKey pubKey = (ECPublicKey) keyFac.generatePublic(pubX509);

		if (!pubKey.getW().equals(((ECPublicKey) aKeyPair.getPublic()).getW())) {
			System.out.println(" expected " + pubKey.getW().getAffineX()
					+ " got "
					+ ((ECPublicKey) aKeyPair.getPublic()).getW().getAffineX());
			System.out.println(" expected " + pubKey.getW().getAffineY()
					+ " got "
					+ ((ECPublicKey) aKeyPair.getPublic()).getW().getAffineY());
			fail(algorithm + " public key encoding (W test) failed");
		}

		if (!pubKey.getParams().getGenerator()
				.equals(
						((ECPublicKey) aKeyPair.getPublic()).getParams()
								.getGenerator())) {
			fail(algorithm + " public key encoding (G test) failed");
		}

		//
		// private key encoding test
		//
		byte[] privEnc = aKeyPair.getPrivate().getEncoded();
		PKCS8EncodedKeySpec privPKCS8 = new PKCS8EncodedKeySpec(privEnc);
		ECPrivateKey privKey = (ECPrivateKey) keyFac.generatePrivate(privPKCS8);

		if (!privKey.getS().equals(
				((ECPrivateKey) aKeyPair.getPrivate()).getS())) {
			fail(algorithm + " private key encoding (S test) failed");
		}

		if (!privKey.getParams().getGenerator().equals(
				((ECPrivateKey) aKeyPair.getPrivate()).getParams()
						.getGenerator())) {
			fail(algorithm + " private key encoding (G test) failed");
		}
	}

	static void fail(String s) {
		System.out.println(s);
	}

	public static void main(String[] args) throws Exception {
		Security.addProvider(new BouncyCastleProvider());

		testECDH("ECDH");
		//testECDH("ECDHC");

	}

}
