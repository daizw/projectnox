package noxUI;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;

public class MoveMouseListener implements MouseListener, MouseMotionListener{
	JComponent target;
	Cheyenne frame;
	Point start_drag;
	Point start_loc;
	
	public MoveMouseListener(JComponent target, Cheyenne frame)
	{
		this.target = target;
		this.frame = frame;		
	}
	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
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

	private Cheyenne getFrame(JComponent target2) {
		// TODO Auto-generated method stub
		return frame;
	}
	private Point getScreenLocation(MouseEvent e) {
		Point cursor = e.getPoint();
		Point target_location = this.target.getLocationOnScreen();

		return new Point(
				(int) (target_location.getX() +cursor.getX()),
				(int) (target_location.getY() +cursor.getY()));
	}
	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		//frame.setLocation(new Point(e.getX(), e.getY()));
		Point current = this.getScreenLocation(e);
		Point offset = new Point(
				(int)current.getX()-(int)start_drag.getX(),
				(int)current.getY()-(int)start_drag.getY());
		Cheyenne frame = this.getFrame(target);
		Point new_location = new Point(
				(int)(this.start_loc.getX()+offset.getX()),
				(int)(this.start_loc.getY()+offset.getY())	);
		frame.setLocation(new_location);
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
