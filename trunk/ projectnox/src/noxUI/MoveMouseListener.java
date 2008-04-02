package noxUI;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class MoveMouseListener implements MouseListener, MouseMotionListener {
	JComponent target;
	NoxFrame frame;
	Point start_drag;
	Point start_loc;

	public MoveMouseListener(JComponent target, NoxFrame frm) {
		this.target = target;
		this.frame = frm;
	}

	@Override
	public void mouseClicked(MouseEvent me) {
		// TODO Auto-generated method stub
		if(me.getClickCount() == 2)//双击
		{
			int state = frame.getExtendedState();

			// 设置图标化(iconifies)位
			// Set the iconified bit
			System.out.println("window state: " + state);
			switch (state) {
			// 如果当前是最大状态, 则正常化
			case JFrame.MAXIMIZED_BOTH:
				state &= JFrame.NORMAL;// '&', not '|'
				System.out.println("max->normal");
				frame.resetMaximizeIcon();
				break;
			// 如果当前不是最大状态, 则最大化
			default:
				state |= JFrame.MAXIMIZED_BOTH;
				System.out.println("normal->max");
				frame.resetNormalizeIcon();
				//Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
				//parent.setBounds(0, 0, dim.width, dim.height );
				break;
			}
			System.out.println("window state: " + state);
			//System.out.println("system: " + 
			//Toolkit.getDefaultToolkit().isFrameStateSupported(JFrame.MAXIMIZED_BOTH) );
			// 设置窗口状态
			frame.setExtendedState(state);
		}		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		this.start_drag = this.getScreenLocation(e);
		this.start_loc = this.getFrame(this.target).getLocation();

	}

	private JFrame getFrame(JComponent target2) {
		// TODO Auto-generated method stub
		return frame;
	}

	private Point getScreenLocation(MouseEvent e) {
		Point cursor = e.getPoint();
		Point target_location = this.target.getLocationOnScreen();

		return new Point((int) (target_location.getX() + cursor.getX()),
				(int) (target_location.getY() + cursor.getY()));
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// frame.setLocation(new Point(e.getX(), e.getY()));
		Point current = this.getScreenLocation(e);
		Point offset = new Point(
				(int) current.getX() - (int) start_drag.getX(), (int) current
						.getY()
						- (int) start_drag.getY());
		JFrame frame = this.getFrame(target);
		Point new_location = new Point((int) (this.start_loc.getX() + offset
				.getX()), (int) (this.start_loc.getY() + offset.getY()));
		frame.setLocation(new_location);
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

}
