package nox.encrypt;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.security.*;

public class TestDSA {
	
	public static void main(String[] args)
			throws java.security.NoSuchAlgorithmException, java.lang.Exception {
		TestDSA my = new TestDSA();
		my.run();
	}

	public void run() {
		// ����ǩ��������Կ
		// ��һ��������Կ��,����Ѿ����ɹ�,�����̾Ϳ�������,���û�����myprikey.datҪ�����ڱ���
		// ��mypubkey.dat�������������û�
		if ((new java.io.File("myprikey.dat")).exists() == false) {
			if (generatekey() == false) {
				System.out.println("������Կ�԰�");
				return;
			}
		}
		// �ڶ���,���û�
		// ���ļ��ж���˽Կ,��һ���ַ�������ǩ���󱣴���һ���ļ�(myinfo.dat)��
		// �����ٰ�myinfo.dat���ͳ�ȥ
		// Ϊ�˷�������ǩ��Ҳ�Ž���myifno.dat�ļ���,��ȻҲ�ɷֱ���
		try {
			java.io.ObjectInputStream in = new java.io.ObjectInputStream(
					new java.io.FileInputStream("myprikey.dat"));
			PrivateKey myprikey = (PrivateKey) in.readObject();
			in.close();
			// java.security.spec.X509EncodedKeySpec pubX509=new
			// java.security.spec.X509EncodedKeySpec(bX509);
			// java.security.spec.X509EncodedKeySpec
			// pubkeyEncode=java.security.spec.X509EncodedKeySpec
			String myinfo = "�����ҵ���Ϣ"; // Ҫǩ������Ϣ
			// ��˽Կ����Ϣ��������ǩ��
			java.security.Signature signet = java.security.Signature
					.getInstance("DSA");
			signet.initSign(myprikey);
			signet.update(myinfo.getBytes());
			byte[] signed = signet.sign(); // ����Ϣ������ǩ��
			System.out.println("signed(ǩ������)=" + byte2hex(signed));
			// ����Ϣ������ǩ��������һ���ļ���
			java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(
					new java.io.FileOutputStream("myinfo.dat"));
			out.writeObject(myinfo);
			out.writeObject(signed);
			out.close();
			System.out.println("ǩ���������ļ��ɹ�");
		} catch (java.lang.Exception e) {
			e.printStackTrace();
			System.out.println("ǩ���������ļ�ʧ��");
		}
		;
		// ������
		// ������ͨ��������ʽ�õ��˻��Ĺ�Կ���ļ�
		// �������ô˻��Ĺ�Կ,���ļ����м��,����ɹ�˵���Ǵ��û���������Ϣ.
		//
		try {
			java.io.ObjectInputStream in = new java.io.ObjectInputStream(
					new java.io.FileInputStream("mypubkey.dat"));
			PublicKey pubkey = (PublicKey) in.readObject();
			in.close();
			System.out.println(pubkey.getFormat());
			in = new java.io.ObjectInputStream(new java.io.FileInputStream(
					"myinfo.dat"));
			String info = (String) in.readObject();
			byte[] signed = (byte[]) in.readObject();
			in.close();
			Signature signetcheck = Signature.getInstance("DSA");
			signetcheck.initVerify(pubkey);
			signetcheck.update(info.getBytes());
			if (signetcheck.verify(signed)) {
				System.out.println("info=" + info);
				System.out.println("ǩ������");
			} else
				System.out.println("��ǩ������");
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}
		;
	}

	// ����һ���ļ�myprikey.dat��mypubkey.dat---˽Կ�͹�Կ,
	// ��ԿҪ�û�����(�ļ�,����ȷ���)�������û�,˽Կ�����ڱ���
	public boolean generatekey() {
		try {
			KeyPairGenerator keygen = java.security.KeyPairGenerator.getInstance("DSA");
			// SecureRandom secrand=new SecureRandom();
			// secrand.setSeed("tttt".getBytes()); //��ʼ�����������
			// keygen.initialize(576,secrand); //��ʼ����Կ������
			keygen.initialize(512);
			KeyPair keys = keygen.genKeyPair();
			// KeyPair keys=keygen.generateKeyPair(); //������Կ��
			PublicKey pubkey = keys.getPublic();
			PrivateKey prikey = keys.getPrivate();
			ObjectOutputStream out = new ObjectOutputStream(
					new  FileOutputStream("myprikey.dat"));
			out.writeObject(prikey);
			out.close();
			System.out.println("д����� prikeys ok");
			out = new ObjectOutputStream(new FileOutputStream(
					"mypubkey.dat"));
			out.writeObject(pubkey);
			out.close();
			System.out.println("д����� pubkeys ok");
			System.out.println("������Կ�Գɹ�");
			return true;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
			System.out.println("������Կ��ʧ��");
			return false;
		}
	}

	public static String byte2hex(byte[] b) {
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