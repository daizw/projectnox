package nox.encrypt.testEncryption;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.swing.JOptionPane;

//����RSA��Կ��˽Կ��JAVA����
public class Skey_RSA {

	public static void main(String args[]) throws Exception {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(1024);
		KeyPair kp = kpg.genKeyPair();
		PublicKey pbkey = kp.getPublic();
		PrivateKey prkey = kp.getPrivate();

		FileOutputStream f1 = new FileOutputStream("c:/Skey_RSA_pub.dat");
		ObjectOutputStream b1 = new ObjectOutputStream(f1);
		b1.writeObject(pbkey);
		FileOutputStream f2 = new FileOutputStream("c:/Skey_RSA_priv.dat");
		ObjectOutputStream b2 = new ObjectOutputStream(f2);
		b2.writeObject(prkey);
		JOptionPane.showMessageDialog(null, "��Կ�ѱ�����C�̸�Ŀ¼��!", "��ʾ",
				JOptionPane.OK_OPTION);

		b1.close();
		b2.close();
	}
}
