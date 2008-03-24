package noxUI;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

/*
 * Created on 2006-9-14
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * ������ʾ����ͼƬ����,�̳���JPanel
 * @author shinysky
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
class JMapPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * ���캯��
	 * @param img  ����ͼƬ    
	 */
	public JMapPanel(Image img) {
		img_mp = img;
	}

	public void setBackImage(Image img)
	{
		img_mp = img;
		System.out.println("setting BackImage");
	}
	/**
	 * ���ػ�������ķ���������ͼ��
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	protected void paintComponent(Graphics g) {
		/**
		 * �������д������ʵ��ͼƬ�洰�ڴ�С��̬����(9.4) ^_^
		 */
		int panelWidth = this.getWidth();
		int panelHeight = this.getHeight();
		g.drawImage(img_mp, 0, 0, panelWidth, panelHeight, this);

		/**
		 * ע�����������жϾ�,�����ڱ���ͼƬ��̬���ŵ�ͬʱ,��ʹ���������� (��Ȼ�����Ͳ��ù����������!)
		 * ���ǵ�����������(X�����Y����)������ʱ,ͼ��Ϊԭ��С
		 */
		if (firstInvoked) {
			firstInvoked = false;
			getImageSizeAndSet();
			System.out
					.println("image size : " + img_width + " * " + img_height);
		}
	}

	/**
	 * ��ȡͼƬ�ߴ粢���������������ѡ�ߴ�
	 *
	 */
	private void getImageSizeAndSet() {
		img_width = img_mp.getWidth(JMapPanel.this);
		img_height = img_mp.getHeight(JMapPanel.this);

		System.out.println("In getImageSizeAndSet():" + img_width + " * "
				+ img_height);
		/**
		 * ϣ��ͨ�� if �ж���������һ������ͼ��ʱ����˸ ��Ȼ����"��˸����Ϊδ�õ�ͼƬ��ȷ�ߴ�"����ж���ȷ��ǰ����;
		 * But...��ʱ��������˸;
		 * ��ʵ����Ϊ��ͼ��ԭ��,����ͨ����ǰ��ͼ������������.
		 */
		if (img_width == -1 || img_height == -1) 
		{
			this.setPreferredSize(new Dimension(800, 600));
			System.out.println("Exception at getting size of image");
            
		} 
		else
			this.setPreferredSize(new Dimension(img_width, img_height));
		System.out.println("After setPreferredSize():" + img_width + " * "
				+ img_height);
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
}