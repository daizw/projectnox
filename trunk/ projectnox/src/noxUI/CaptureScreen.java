/*
 * CaptureScreen.java
 *
 * Created on 2007��8��30��, ����12:46
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package noxUI;

/**
 * derived from online source code
 * 
 * @author lbf
 * @author shinysky
 */
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

// һ����ʱ�࣬������ʾ��ǰ����Ļͼ��
class FakeScreenPane extends JPanel implements MouseListener, MouseMotionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6818414590924411239L;
	private BufferedImage bi;
	private BufferedImage get = null;
	private int width, height;
	private int startX, startY, endX, endY, tempX, tempY;
	private JDialog fakewindow;
	private Rectangle select = new Rectangle(0, 0, 0, 0);// ��ʾѡ�е�����
	private Cursor cs = new Cursor(Cursor.CROSSHAIR_CURSOR);// ��ʾһ������µ����״̬
	private States current = States.DEFAULT;// ��ʾ��ǰ�ı༭״̬
	private Rectangle[] rec;// ��ʾ�˸��༭�������
	// �����ĸ�����,�ֱ��ʾ˭�Ǳ�ѡ�е��������ϵĶ˵�
	public static final int START_X = 1;
	public static final int START_Y = 2;
	public static final int END_X = 3;
	public static final int END_Y = 4;
	private int currentX, currentY;// ��ǰ��ѡ�е�X��Y,ֻ����������Ҫ�ı�
	private Point p = new Point();// ��ǰ����Ƶĵص�
	private boolean showTip = true;// �Ƿ���ʾ��ʾ.���������һ��,����ʾ��������

	public FakeScreenPane(JDialog fakeWindow, BufferedImage bi, int width, int height) {
		this.fakewindow = fakeWindow;
		this.bi = bi;
		this.width = width;
		this.height = height;
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		initRecs();
	}
	
	public BufferedImage getWhatWegot(){
		return get;
	}

	private void initRecs() {
		rec = new Rectangle[8];
		for (int i = 0; i < rec.length; i++) {
			rec[i] = new Rectangle();
		}
	}

	public void paintComponent(Graphics g) {
		g.drawImage(bi, 0, 0, width, height, this);
		g.setColor(Color.RED);
		g.drawLine(startX, startY, endX, startY);
		g.drawLine(startX, endY, endX, endY);
		g.drawLine(startX, startY, startX, endY);
		g.drawLine(endX, startY, endX, endY);
		int x = startX < endX ? startX : endX;
		int y = startY < endY ? startY : endY;
		select = new Rectangle(x, y, Math.abs(endX - startX), Math.abs(endY
				- startY));
		int x1 = (startX + endX) / 2;
		int y1 = (startY + endY) / 2;
		g.fillRect(x1 - 2, startY - 2, 5, 5);
		g.fillRect(x1 - 2, endY - 2, 5, 5);
		g.fillRect(startX - 2, y1 - 2, 5, 5);
		g.fillRect(endX - 2, y1 - 2, 5, 5);
		g.fillRect(startX - 2, startY - 2, 5, 5);
		g.fillRect(startX - 2, endY - 2, 5, 5);
		g.fillRect(endX - 2, startY - 2, 5, 5);
		g.fillRect(endX - 2, endY - 2, 5, 5);
		rec[0] = new Rectangle(x - 5, y - 5, 10, 10);
		rec[1] = new Rectangle(x1 - 5, y - 5, 10, 10);
		rec[2] = new Rectangle((startX > endX ? startX : endX) - 5, y - 5, 10,
				10);
		rec[3] = new Rectangle((startX > endX ? startX : endX) - 5, y1 - 5, 10,
				10);
		rec[4] = new Rectangle((startX > endX ? startX : endX) - 5,
				(startY > endY ? startY : endY) - 5, 10, 10);
		rec[5] = new Rectangle(x1 - 5, (startY > endY ? startY : endY) - 5, 10,
				10);
		rec[6] = new Rectangle(x - 5, (startY > endY ? startY : endY) - 5, 10,
				10);
		rec[7] = new Rectangle(x - 5, y1 - 5, 10, 10);
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				0.3F));
		g2d.setColor(Color.CYAN);
		int sX = Math.min(startX, endX);
		int sY = Math.min(endY, startY);
		g2d.fillRect(sX, sY, Math.abs(endX - startX), Math.abs(endY - startY));
		g2d.setComposite(AlphaComposite
				.getInstance(AlphaComposite.SRC_OVER, 1F));
		boolean drawCTip = endX - startX != 0 && endY - startY != 0;
		if (drawCTip) {
			String cTip = String.format("%dX%d", Math.abs(endX - startX), Math
					.abs(endY - startY));
			int cTipH = 20;
			Font cTipFont = new Font("system", Font.BOLD, 16);
			g2d.setFont(cTipFont);
			int cTipW = SwingUtilities.computeStringWidth(
					getFontMetrics(cTipFont), cTip);
			g2d.setPaint(Color.BLACK);
			int cStartY = sY - cTipH > 0 ? sY - cTipH : sY;
			g2d.fillRect(sX, cStartY, cTipW, cTipH);
			g2d.setPaint(Color.WHITE);
			g2d.drawString(cTip, sX, cStartY == sY ? sY + cTipH - 3 : sY - 3);
		}
		g2d.dispose();
		if (showTip) {
			g.setColor(Color.CYAN);
			g.fillRect(p.x, p.y, 170, 20);
			g.setColor(Color.RED);
			g.drawRect(p.x, p.y, 170, 20);
			g.setColor(Color.BLACK);
			g.drawString("�밴ס����������ѡ���ͼ��", p.x, p.y + 15);
		}
	}

	// ���ݶ��������Ȱ˸��������ѡ�е�Ҫ�޸ĵ�X��Y������
	private void initSelect(States state) {
		switch (state) {
		case DEFAULT:
			currentX = 0;
			currentY = 0;
			break;
		case EAST:
			currentX = (endX > startX ? END_X : START_X);
			currentY = 0;
			break;
		case WEST:
			currentX = (endX > startX ? START_X : END_X);
			currentY = 0;
			break;
		case NORTH:
			currentX = 0;
			currentY = (startY > endY ? END_Y : START_Y);
			break;
		case SOUTH:
			currentX = 0;
			currentY = (startY > endY ? START_Y : END_Y);
			break;
		case NORTH_EAST:
			currentY = (startY > endY ? END_Y : START_Y);
			currentX = (endX > startX ? END_X : START_X);
			break;
		case NORTH_WEST:
			currentY = (startY > endY ? END_Y : START_Y);
			currentX = (endX > startX ? START_X : END_X);
			break;
		case SOUTH_EAST:
			currentY = (startY > endY ? START_Y : END_Y);
			currentX = (endX > startX ? END_X : START_X);
			break;
		case SOUTH_WEST:
			currentY = (startY > endY ? START_Y : END_Y);
			currentX = (endX > startX ? START_X : END_X);
			break;
		default:
			currentX = 0;
			currentY = 0;
			break;
		}
	}

	public void mouseMoved(MouseEvent me) {
		doMouseMoved(me);
		initSelect(current);
		if (showTip) {
			p = me.getPoint();
			repaint();
		}
	}

	// ���ⶨ��һ��������������ƶ�,��Ϊ��ÿ�ζ��ܳ�ʼ��һ����Ҫѡ��ĵ���
	private void doMouseMoved(MouseEvent me) {
		if (select.contains(me.getPoint())) {
			this.setCursor(new Cursor(Cursor.MOVE_CURSOR));
			current = States.MOVE;
		} else {
			States[] st = States.values();
			for (int i = 0; i < rec.length; i++) {
				if (rec[i].contains(me.getPoint())) {
					current = st[i];
					this.setCursor(st[i].getCursor());
					return;
				}
			}
			this.setCursor(cs);
			current = States.DEFAULT;
		}
	}

	public void mouseExited(MouseEvent me) {

	}

	public void mouseEntered(MouseEvent me) {

	}

	public void mouseDragged(MouseEvent me) {
		int x = me.getX();
		int y = me.getY();
		if (current == States.MOVE) {
			startX += (x - tempX);
			startY += (y - tempY);
			endX += (x - tempX);
			endY += (y - tempY);
			tempX = x;
			tempY = y;
		} else if (current == States.EAST || current == States.WEST) {
			if (currentX == START_X) {
				startX += (x - tempX);
				tempX = x;
			} else {
				endX += (x - tempX);
				tempX = x;
			}
		} else if (current == States.NORTH || current == States.SOUTH) {
			if (currentY == START_Y) {
				startY += (y - tempY);
				tempY = y;
			} else {
				endY += (y - tempY);
				tempY = y;
			}
		} else if (current == States.NORTH_EAST || current == States.NORTH_EAST
				|| current == States.SOUTH_EAST || current == States.SOUTH_WEST) {
			if (currentY == START_Y) {
				startY += (y - tempY);
				tempY = y;
			} else {
				endY += (y - tempY);
				tempY = y;
			}
			if (currentX == START_X) {
				startX += (x - tempX);
				tempX = x;
			} else {
				endX += (x - tempX);
				tempX = x;
			}

		} else {
			startX = tempX;
			startY = tempY;
			endX = me.getX();
			endY = me.getY();
		}
		this.repaint();
	}

	public void mousePressed(MouseEvent me) {
		showTip = false;
		tempX = me.getX();
		tempY = me.getY();
	}

	public void mouseReleased(MouseEvent me) {
		if (me.isPopupTrigger()) {
			if (current == States.MOVE) {
				showTip = true;
				p = me.getPoint();
				startX = 0;
				startY = 0;
				endX = 0;
				endY = 0;
				repaint();
			} else {
				fakewindow.dispose();
				//updates();
			}
		}
	}

	public void mouseClicked(MouseEvent me) {
		if (me.getClickCount() == 2) {
			// Rectangle rec=new
			// Rectangle(startX,startY,Math.abs(endX-startX),Math.abs(endY-startY));
			Point p = me.getPoint();
			if (select.contains(p)) {
				if (select.x + select.width < this.getWidth()
						&& select.y + select.height < this.getHeight()) {
					get = bi.getSubimage(select.x, select.y, select.width,
							select.height);
					fakewindow.dispose();
					//updates();
				} else {
					int wid = select.width, het = select.height;
					if (select.x + select.width >= this.getWidth()) {
						wid = this.getWidth() - select.x;
					}
					if (select.y + select.height >= this.getHeight()) {
						het = this.getHeight() - select.y;
					}
					/**
					 * ����ѡ����������Ļ��Χ��"��̬���"
					 * @author shinysky
					 */
					if(select.x < 0)
						select.x = 0;
					if(select.y < 0)
						select.y = 0;
					if(wid + select.x > bi.getWidth())
						wid = bi.getWidth() - select.x;
					if(het + select.y > bi.getWidth())
						het = bi.getHeight() - select.y;
					get = bi.getSubimage(select.x, select.y, wid, het);
					fakewindow.dispose();
					//updates();
				}
			}
		}
	}
}

// һЩ��ʾ״̬��ö��
enum States {
	NORTH_WEST(new Cursor(Cursor.NW_RESIZE_CURSOR)), // ��ʾ������
	NORTH(new Cursor(Cursor.N_RESIZE_CURSOR)), NORTH_EAST(new Cursor(
			Cursor.NE_RESIZE_CURSOR)), EAST(new Cursor(Cursor.E_RESIZE_CURSOR)), SOUTH_EAST(
			new Cursor(Cursor.SE_RESIZE_CURSOR)), SOUTH(new Cursor(
			Cursor.S_RESIZE_CURSOR)), SOUTH_WEST(new Cursor(
			Cursor.SW_RESIZE_CURSOR)), WEST(new Cursor(Cursor.W_RESIZE_CURSOR)), MOVE(
			new Cursor(Cursor.MOVE_CURSOR)), DEFAULT(new Cursor(
			Cursor.DEFAULT_CURSOR));
	private Cursor cs;

	States(Cursor cs) {
		this.cs = cs;
	}

	public Cursor getCursor() {
		return cs;
	}
}
