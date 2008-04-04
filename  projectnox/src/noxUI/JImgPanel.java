package noxUI;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;

import javax.swing.JPanel;

/*
 * Created on 2006-9-14
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * ������ʾ����ͼƬ����,�̳���JPanel
 * 
 * @author shinysky
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
class JImgPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * ���캯��
	 * 
	 * @param img
	 *            ����ͼƬ
	 */
	public JImgPanel(Image img) {
		modal = false;
		img_mp = img;
	}
	public JImgPanel(Image img, Point ori, Dimension dim) {
		modal = true;
		img_mp = img;
		origin = ori;
		square = dim;
	}

	public void setBackImage(Image img) {
		img_mp = img;
		//System.out.println("setting BackImage");
	}

	/**
	 * ���ػ�������ķ���������ͼ��
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	protected void paintComponent(Graphics g) {
		/**
		 * ����N�д������ʵ��ͼƬ�洰�ڴ�С��̬����(9.4) ^_^
		 */
/*		int panelWidth = this.getWidth();
		int panelHeight = this.getHeight();*/
		if(modal)//ָ����ʼλ�úͻ�ͼ�ߴ�
			g.drawImage(img_mp, (int)origin.getX(), (int)origin.getY(), 
				(int)square.getWidth(), (int)square.getHeight(), this);
					//this.getWidth(), this.getHeight(), this);
		else
			g.drawImage(img_mp, 0, 0, 
					this.getWidth(), this.getHeight(), this);

		/**
		 * ע�����������жϾ�,�����ڱ���ͼƬ��̬���ŵ�ͬʱ,��ʹ���������� (��Ȼ�����Ͳ��ù����������!)
		 * ���ǵ�����������(X�����Y����)������ʱ,ͼ��Ϊԭ��С
		 */
		if (firstInvoked) {
			firstInvoked = false;
			getImageSizeAndSet();
			//System.out.println("image size : " + img_width + " * " + img_height);
		}
	}

	/**
	 * ��ȡͼƬ�ߴ粢���������������ѡ�ߴ�
	 * 
	 */
	private void getImageSizeAndSet() {
		img_width = img_mp.getWidth(JImgPanel.this);
		img_height = img_mp.getHeight(JImgPanel.this);

		//System.out.println("In getImageSizeAndSet():" + img_width + " * " + img_height);
		/**
		 * ϣ��ͨ�� if �ж���������һ������ͼ��ʱ����˸ ��Ȼ����"��˸����Ϊδ�õ�ͼƬ��ȷ�ߴ�"����ж���ȷ��ǰ����;
		 * But...��ʱ��������˸; ��ʵ����Ϊ��ͼ��ԭ��,����ͨ����ǰ��ͼ������������.
		 */
		if (img_width == -1 || img_height == -1) {
			this.setPreferredSize(new Dimension(800, 600));
			//System.out.println("Exception at getting size of image");

		} else
			this.setPreferredSize(new Dimension(img_width, img_height));
		//System.out.println("After setPreferredSize():" + img_width + " * "	+ img_height);
	}

	/**
	 * �Ƿ��һ�ε���
	 */
	private boolean firstInvoked = true;
	/**
	 * ����ͼƬ
	 */
	private Image img_mp;
	/**
	 * ͼƬ�߶�
	 */
	private int img_height;
	/**
	 * ͼƬ���
	 */
	private int img_width;
	/**
	 * ��ͼģʽ
	 * false: default, ȫ������
	 * true: ָ����ʼλ�úͻ�ͼ�ߴ�
	 */
	private boolean modal;
	/**
	 * ��ʼ��ͼλ��
	 */
	private Point origin;
	/**
	 * ��ͼ�ߴ�
	 */
	 private Dimension square;
}
