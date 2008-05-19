package nox.encrypt.testEncryption;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class Main implements ActionListener {
	JFrame f;
	JPanel p1, p2, p3, p4, p5;
	JButton encrypt_button, decrypt_button;
	JTextArea area_T, F1;
	JScrollPane jsp;
	int a, b;
	String encrypt_Text;

	public Main() {
		f = new JFrame("RSA���ܽ����㷨��ʾ����");
		Container containerPane = f.getContentPane();
		containerPane.setLayout(new BorderLayout());

		p5 = new JPanel();
		p5.setLayout(new GridLayout(2, 1));
		p1 = new JPanel();
		F1 = new JTextArea(3, 15);
		p1.add(F1);
		p1.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createLineBorder(Color.white, 0), "�������Ļ�����������:",
				TitledBorder.LEFT, TitledBorder.TOP));

		p2 = new JPanel();
		area_T = new JTextArea(3, 15);
		jsp = new JScrollPane(area_T);
		area_T.setLineWrap(true);
		p2.add(jsp);
		p2.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createLineBorder(Color.white, 0), "��/���ܺ������:",
				TitledBorder.LEFT, TitledBorder.TOP));
		p5.add(p1);
		p5.add(p2);

		p4 = new JPanel();
		p4.setLayout(new FlowLayout());
		encrypt_button = new JButton("����");
		decrypt_button = new JButton("����");
		p4.add(encrypt_button);
		p4.add(decrypt_button);

		encrypt_button.addActionListener(this);
		decrypt_button.addActionListener(this);

		containerPane.add(p5, BorderLayout.NORTH);
		containerPane.add(p4, BorderLayout.SOUTH);

		f.setBounds(364, 200, 500, 200);
		f.setVisible(true);
		f.pack();
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		try {
			if (cmd.equals("����")) {
				String Plain_T = F1.getText();
				encrypt en = new encrypt();
				String text_E = en.en_code(Plain_T);
				System.out.println(text_E);
				JOptionPane.showMessageDialog(f, "���ܳɹ�!", "��ʾ",
						JOptionPane.INFORMATION_MESSAGE);
				area_T.setText(text_E);
			}// if
			else if (cmd.equals("����")) {
				area_T.setText("");
				decrypt de = new decrypt();
				String text_D = de.de_code();
				JOptionPane.showMessageDialog(f, "���ܳɹ�!", "��ʾ",
						JOptionPane.INFORMATION_MESSAGE);
				area_T.setText(text_D);
			}
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}

	public static void main(String args[]) {
		new Main();
	}
}
