package nox.encrypt;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;

public class TestDHKey {

	public static void main(String argv[]) {
		try {
			TestDHKey my = new TestDHKey();
			my.run();
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	private void run() throws Exception {
		Security.addProvider(new com.sun.crypto.provider.SunJCE());
		
		/**
		 * alice����DH��, Ȼ�󽫹�Կ����󷢸�bob
		 */
		System.out.println("ALICE: ���� DH �� ...");
		KeyPairGenerator aliceKpairGen = KeyPairGenerator.getInstance("DH");
		aliceKpairGen.initialize(512);
		KeyPair aliceKpair = aliceKpairGen.generateKeyPair(); // ����ʱ�䳤
		// ����(Alice)���ɹ�����Կ alicePubKeyEnc �����͸�����(Bob) ,
		// �������ļ���ʽ,socket.....
		byte[] alicePubKeyEnc = aliceKpair.getPublic().getEncoded();
		
		/**
		 * bob���յ�alice�ı����Ĺ�Կ,�������
		 */
		KeyFactory bobKeyFac = KeyFactory.getInstance("DH");
		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(alicePubKeyEnc);
		PublicKey alicePubKey = bobKeyFac.generatePublic(x509KeySpec);
		System.out.println("alice��Կbob����ɹ�");
		
		/**
		 * bob��alice�����Ĺ�Կ����ȡAlice��ʼ����DH��ʱ�ĳ�ʼ������,
		 * ���Գ�ʼ��bob�Լ���DH��.
		 */
		// bob��������ͬ�Ĳ�����ʼ��������DH KEY��,����Ҫ��Alice�������Ĺ�����Կ,
		// �ж�������,�������������ʼ������ DH key��
		// ��alicePubKye��ȡalice��ʼ��ʱ�õĲ���
		DHParameterSpec dhParamSpec = ((DHPublicKey) alicePubKey).getParams();
		KeyPairGenerator bobKpairGen = KeyPairGenerator.getInstance("DH");
		bobKpairGen.initialize(dhParamSpec);
		KeyPair bobKpair = bobKpairGen.generateKeyPair();
		System.out.println("BOB: ���� DH key �Գɹ�");
		
		/**
		 * bob����Alice�Ĺ�Կ���ɱ���DES��Կ
		 */
		KeyAgreement bobKeyAgree = KeyAgreement.getInstance("DH");
		bobKeyAgree.init(bobKpair.getPrivate());
		System.out.println("BOB: ��ʼ������key�ɹ�");
		// ����(bob) ���ɱ��ص���Կ bobDesKey
		bobKeyAgree.doPhase(alicePubKey, true);
		SecretKey bobDesKey = bobKeyAgree.generateSecret("DES");
		System.out.println("BOB: ��alice�Ĺ�Կ��λ����key,���ɱ���DES��Կ�ɹ�");
		// Bob���ɹ�����Կ bobPubKeyEnc �����͸�Alice,
		// �������ļ���ʽ,socket.....,ʹ�����ɱ�����Կ
		byte[] bobPubKeyEnc = bobKpair.getPublic().getEncoded();
		System.out.println("BOB��ALICE���͹�Կ");
		
		// alice���յ� bobPubKeyEnc������bobPubKey
		// �ٽ��ж�λ,ʹaliceKeyAgree��λ��bobPubKey
		KeyFactory aliceKeyFac = KeyFactory.getInstance("DH");
		x509KeySpec = new X509EncodedKeySpec(bobPubKeyEnc);
		PublicKey bobPubKey = aliceKeyFac.generatePublic(x509KeySpec);
		System.out.println("ALICE����BOB��Կ������ɹ�");
		
		KeyAgreement aliceKeyAgree = KeyAgreement.getInstance("DH");
		aliceKeyAgree.init(aliceKpair.getPrivate());
		System.out.println("ALICE: ��ʼ������key�ɹ�");
		aliceKeyAgree.doPhase(bobPubKey, true);
		// ����(alice) ���ɱ��ص���Կ aliceDesKey
		SecretKey aliceDesKey = aliceKeyAgree.generateSecret("DES");
		System.out.println("ALICE: ��bob�Ĺ�Կ��λ����key,�����ɱ���DES��Կ");
		
		if (aliceDesKey.equals(bobDesKey))
			System.out.println("���ĺ���������Կ��ͬ");
		// �������ĺ������ı��ص�deskey����ͬ������,��ȫ���Խ��з��ͼ���,���պ����,�ﵽ
		// ��ȫͨ���ĵ�Ŀ��
		/**
		 * bob��bobDesKey��Կ������Ϣ
		 */
		Cipher bobCipher = Cipher.getInstance("DES");
		bobCipher.init(Cipher.ENCRYPT_MODE, bobDesKey);
		String bobinfo = "���������Ļ�����Ϣ";
		System.out.println("��������ǰԭ��:" + bobinfo);
		byte[] cleartext = bobinfo.getBytes();
		byte[] ciphertext = bobCipher.doFinal(cleartext);
		/**
		 * alice��aliceDesKey��Կ����
		 */
		Cipher aliceCipher = Cipher.getInstance("DES");
		aliceCipher.init(Cipher.DECRYPT_MODE, aliceDesKey);
		byte[] recovered = aliceCipher.doFinal(ciphertext);
		System.out.println("alice����bob����Ϣ:" + (new String(recovered)));
		if (!java.util.Arrays.equals(cleartext, recovered))
			throw new Exception("���ܺ���ԭ����Ϣ��ͬ");
		System.out.println("���ܺ���ͬ");
	}
}