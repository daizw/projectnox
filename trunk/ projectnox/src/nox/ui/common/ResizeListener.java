package nox.ui.common;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class ResizeListener implements MouseListener, MouseMotionListener {
	JFrame frame;
	JComponent resizeArea;
	Point start_drag;

	// Cursor defaultCursor;

	public ResizeListener(JFrame parent, JComponent resizeArea) {
		this.frame = parent;
		this.resizeArea = resizeArea;
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// defaultCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
		Cursor cur = Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
		frame.setCursor(cur);
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	@Override
	public void mousePressed(MouseEvent e) {
		start_drag = new Point((int) e.getX(), (int) e.getY());
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		Point frameLoc = frame.getLocationOnScreen();
		Point buttLoc = resizeArea.getLocationOnScreen();
		Dimension newSize = new Dimension(
				(int) (e.getX() + buttLoc.getX() - frameLoc.getX()), (int) (e
						.getY()
						+ (int) buttLoc.getY() - frameLoc.getY()));
		frame.setSize(newSize);
		/*
		 * System.out.println("e: " + (int)e.getX() + " : " + (int)e.getY() + " : " +
		 * (int)buttLoc.getX() + " : " + (int)buttLoc.getY() );
		 */
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
	}

}
