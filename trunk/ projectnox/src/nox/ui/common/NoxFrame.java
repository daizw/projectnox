package nox.ui.common;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.RoundRectangle2D;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.MenuElement;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import nox.net.common.NoxToolkit;

import com.sun.jna.examples.WindowUtils;

/**
 * 
 * @author shinysky <a href="mailto: shinysky1986@gmail.com"/>
 * 
 */
public class NoxFrame extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4611481416817988409L;
	/**
	 * Ĭ�ϳߴ糣��
	 */
	public static final int WIDTH_MIN = 120;
	public static final int HEIGHT_MIN = 60;
	public static final int TITLE_HEIGHT = 20;
	public static final int FOOT_HEIGHT = 15;

	/**
	 * ǰ��������ɫ
	 */
	private Color foregrdColor = Color.WHITE;
	private Color backgrdColor = Color.BLACK;

	/**
	 * ����͸����
	 */
	private float opacity = 100;
	/**
	 * �߿�
	 */
	private MatteBorder paneEdge;
	/**
	 * ������ȡͼƬ
	 */
	private Toolkit tk;
	private Image background;
	private Image img_logo;
	/**
	 * ��JPanel
	 */
	private JImgPanel rootpane;
	private JPanel fakeFace;
	private Titlebar titlebar;
	private JPanel container;
	private FootPane footpane;

	/**
	 * NoxFrame ����, ���б�������״̬��; ���ô����ƶ�/��С��/�ر�/�������Ź���
	 * 
	 * @param title
	 *            ���ڱ���
	 * @param path_background
	 *            ����ͼƬ·��
	 * @param path_logo
	 *            logoͼƬ·��
	 * @param path_title
	 *            ����ͼƬ·��
	 * @param path_minimize
	 *            ��С����ťͼƬ·��
	 * @param path_minimize_rollover
	 *            ��꾭����С����ťͼƬ·��
	 * @param path_maximize
	 *            ��󻯰�ťͼƬ·��
	 * @param path_maximize_rollover
	 *            ��꾭����󻯰�ťͼƬ·��
	 * @param path_normalimize
	 *            �ָ����ڰ�ťͼƬ·��
	 * @param path_normalimize_rollover
	 *            ��꾭���ָ����ڰ�ťͼƬ·��
	 * @param path_close
	 *            �رմ��ڰ�ťͼƬ·��
	 * @param path_close_rollover
	 *            ��꾭���رմ��ڰ�ťͼƬ·��
	 * @param IAmBase
	 *            true: �Ǹ�����, �رհ�ť�Ƴ�����ϵͳ; false: ���Ǹ�����, �رհ�ťֻ�رձ�����
	 * 
	 * @see Titlebar
	 * @see JFrame
	 */
	protected NoxFrame(String title, String path_background, 
			String path_logo, String path_logo_big,
			String path_title, final boolean IAmBase) {
		super(title);
		/*
		 * try{
		 * UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		 * }catch(Exception e) { System.out.println(e.toString()); }
		 */

		this.setUndecorated(true);

		/*
		 * try { UIManager.setLookAndFeel(new SubstanceLookAndFeel()); } catch
		 * (UnsupportedLookAndFeelException ex) { ex.printStackTrace(); }
		 */

		tk = Toolkit.getDefaultToolkit();
		background = tk.getImage(path_background);
		img_logo = tk.getImage(path_logo_big);
		//����ͼ��
		this.setIconImage(img_logo);

		// ׼��ͼƬ
		this.prepareImage(background, rootpane);

		// Container contentPane = getContentPane();

		this.setMinimumSize(new Dimension(WIDTH_MIN, HEIGHT_MIN));
		/**
		 * ���������
		 */
		titlebar = new Titlebar(this, path_logo, path_title, IAmBase);
		/**
		 * �в�����
		 */
		container = new JPanel();
		container.setOpaque(false);

		/**
		 * �ײ���״̬�����, ���ڰ���resize��ť
		 */
		footpane = new FootPane(this);

		/**
		 * ���� ������, miniprofile, �б���, ״̬�� �����
		 */
		rootpane = new JImgPanel(background);
		rootpane.setLayout(new BoxLayout(rootpane, BoxLayout.Y_AXIS));
		rootpane.setDoubleBuffered(true);
		rootpane.add(titlebar);
		rootpane.add(container);
		rootpane.add(footpane);
		// rootpane.setBackground(Color.BLACK);
		rootpane.setOpaque(false);

		/*
		 * ����ͼƬ�߿���� ImageBorder image_border = new ImageBorder( new
		 * ImageIcon("resrc/upper_left.png").getImage(), new
		 * ImageIcon("resrc/upper.png").getImage(), new
		 * ImageIcon("resrc/upper_right.png").getImage(),
		 * 
		 * new ImageIcon("resrc/left_center.png").getImage(), new
		 * ImageIcon("resrc/right_center.png").getImage(),
		 * 
		 * new ImageIcon("resrc/bottom_left.png").getImage(), new
		 * ImageIcon("resrc/bottom_center.png").getImage(), new
		 * ImageIcon("resrc/bottom_right.png").getImage() );
		 */

		paneEdge = BorderFactory.createMatteBorder(2, 2, 2, 2, backgrdColor);// ��ɫ������Ϊ��������
		/**
		 * ������ײ��JPanel, �����Ϊ2�ĺ�ɫ�߿� ������rootpane
		 */
		fakeFace = new JPanel();
		fakeFace.setBorder(paneEdge);
		fakeFace.setLayout(new BoxLayout(fakeFace, BoxLayout.Y_AXIS));
		fakeFace.add(rootpane);
		fakeFace.setBackground(backgrdColor);

		// rootpane.setBorder(paneEdge);
		this.setContentPane(fakeFace);
		/**
		 * �������ƶ���������ʵ�ִ����ƶ�
		 */
		MoveMouseListener mml = new MoveMouseListener(this.getRootPane(), this);
		this.getRootPane().addMouseListener(mml);
		this.getRootPane().addMouseMotionListener(mml);

		/**
		 * ����Ĭ�ϴ��ڲ���(ALT+F4) TODO: ���ʹ������ͼ��, ��Ӧ���رղ�����Ϊdispose
		 */
		if (IAmBase)
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		else
			this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	/**
	 * ���ô���ǰ����ɫ
	 */
	public void setForegroundColor() {
		if ((float) (backgrdColor.getRed() * 0.3f + backgrdColor.getGreen()
				* 0.59f + backgrdColor.getBlue() * 0.11f) < 128)
			foregrdColor = Color.WHITE;
		else
			foregrdColor = Color.BLACK;
		// �ƺ�ֻ�б�������Ҫ����
		titlebar.setForegroundColor(foregrdColor.equals(Color.WHITE));
	}

	/**
	 * ��ȡ����ǰ����ɫ
	 * 
	 * @return ǰ��ɫ
	 */
	public Color getForegroundColor() {
		return foregrdColor;
	}

	/**
	 * ���ô��ڱ�����ɫ, Ȼ����ݱ���ɫ����ǰ��ɫ
	 * 
	 * @param color
	 *            ����ɫ
	 */
	public void setBackgroundColor(Color color) {
		fakeFace.setBackground(color);
		paneEdge = BorderFactory.createMatteBorder(2, 2, 2, 2, color);// ��ɫ������Ϊ��������
		fakeFace.setBorder(paneEdge);
		backgrdColor = color;
		// ͬʱ����ǰ��ɫ
		setForegroundColor();
	}

	/**
	 * ��ȡ���ڱ�����ɫ
	 * 
	 * @return ����ɫ
	 */
	public Color getBackgroundColor() {
		return backgrdColor;
	}

	/**
	 * ���ò�͸����
	 * 
	 * @return �Ƿ����óɹ�
	 */
	public boolean setOpacity(float alpha) {
		if (WindowUtils.isWindowAlphaSupported()) {
			WindowUtils.setWindowAlpha(this, alpha);
			opacity = alpha;
			return true;
		} else {
			System.out.println("Sorry, WindowAlpha is not Supported");
			return false;
		}
	}

	/**
	 * ��ȡ��͸����
	 * 
	 * @return ��͸����ֵ
	 */
	public float getOpacity() {
		return opacity;
	}

	/**
	 * ��JFrame��ΪԲ�� ���ÿ��resize�������������CPUռ����; Ŀǰû�ҵ��ϺõĽ������, ���Ը÷�����ʱ����.
	 */
	public void setFrameMask() {
		// Բ��Mask
		RoundRectangle2D.Float mask = new RoundRectangle2D.Float(0, 0, this
				.getWidth(), this.getHeight(), 5, 5);
		WindowUtils.setWindowMask(this, mask);
	}

	public JPanel getContainer() {
		return container;
	}

	public void resetMaximizeIcon() {
		titlebar.setToMaximizeIcon();
	}

	public void resetNormalizeIcon() {
		titlebar.setToNormalizeIcon();
	}

	/**
	 * �ò�����ʱ������ת; ������������Ӽ�����
	 */
	public void removeResizeListener() {
		footpane.removeResizeListener();
	}
}

/**
 * ��������� Ӧ�����پ߱����ֹ���: 1. �����������, �������ΪͼƬ 2. ����Ƿ�֧����, �������Ϊ����
 * 
 * @author shinysky
 * 
 */
class Titlebar extends JPanel {
	/**
	 * what's this svID?
	 */
	private static final long serialVersionUID = -538877811148092522L;
	JButton blogo;
	JLabel lab_title;
	//JButton bconfig;
	// JSlider slider;
	JButton bminimize;
	JButton bmaximize;
	JButton bclose;

	FrameConfigDialog transparencyConfigBar;

	String ttl;

	// boolean windowStateIsMax;

	/**
	 * ������JPanel��, �߶�20;
	 * 
	 * @param parent
	 *            �����, ���ڿ����������С��
	 * @param path_logo
	 *            logoͼƬ·��
	 * @param title
	 *            ����ͼƬ·��(IAmBase==true)/��������(IAmBase==false)
	 * @param path_minimize
	 *            ��С����ťͼƬ·��
	 * @param path_minimize_rollover
	 *            ��꾭����С����ťͼƬ·��
	 * @param path_maximize
	 *            ��󻯰�ťͼƬ·��
	 * @param path_maximize_rollover
	 *            ��꾭����󻯰�ťͼƬ·��
	 * @param path_normalimize
	 *            �ָ����ڰ�ťͼƬ·��
	 * @param path_normalimize_rollover
	 *            ��꾭���ָ����ڰ�ťͼƬ·��
	 * @param path_close
	 *            �رմ��ڰ�ťͼƬ·��
	 * @param path_close_rollover
	 *            ��꾭���رմ��ڰ�ťͼƬ·��
	 * @param IAmBase
	 *            true: �Ǹ�����, �رհ�ť�Ƴ�����ϵͳ���ұ�������ʾͼƬ; false: ���Ǹ�����,
	 *            �رհ�ťֻ�رձ����ڲ��ұ�������ʾ����.
	 */
	Titlebar(final NoxFrame parent, String path_logo, String title, final boolean IAmBase) {
		transparencyConfigBar = new FrameConfigDialog(parent);

		Dimension btnsize = new Dimension(NoxFrame.TITLE_HEIGHT, NoxFrame.TITLE_HEIGHT); 
		blogo = new JButton(new ImageIcon(path_logo));
		blogo.setToolTipText(getHtmlText("About NoX"));
		blogo.setSize(btnsize);
		blogo.setPreferredSize(btnsize);
		blogo.setMaximumSize(btnsize);
		blogo.setMinimumSize(btnsize);
		blogo.setBorderPainted(false);
		blogo.setContentAreaFilled(false);
		blogo.setOpaque(false);

		/*
		 * if (IAmBase) { bconfig = new JButton(new
		 * ImageIcon("resrc\\buttons\\config.png")); bconfig.setRolloverIcon(new
		 * ImageIcon( "resrc\\buttons\\config_rollover.png"));
		 * bconfig.setSize(new Dimension(20, 20)); bconfig.setPreferredSize(new
		 * Dimension(20, 20)); bconfig.setMaximumSize(new Dimension(20, 20));
		 * bconfig.setMinimumSize(new Dimension(20, 20));
		 * bconfig.setBorderPainted(false); bconfig.setContentAreaFilled(false);
		 * bconfig.setOpaque(false); bconfig.addActionListener(new
		 * ActionListener() { public void actionPerformed(ActionEvent e) {
		 * System.out.println("You just clicked the config button"); //
		 * parent.setBackground(Color.BLUE); // slider.setValue(100);
		 *  } }); }
		 */

		blogo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final JPopupMenu m = new JPopupMenu();
				// use a heavyweight popup to avoid having it clipped
				// by the window mask
				if (IAmBase) {
					m.add(new AbstractAction("Set Window's Color") {
						/**
						 * 
						 */
						private static final long serialVersionUID = -729947600305959488L;

						public void actionPerformed(ActionEvent e) {
							Color color = JColorChooser.showDialog(parent,
									"Select a color for the GUI", Color.orange);
							if (color != null) {
								parent.setBackgroundColor(color);
							}
						}
					});
					m.add(new AbstractAction("Set Window's Transparency") {
						/**
						 * 
						 */
						private static final long serialVersionUID = 8141980952424431845L;

						public void actionPerformed(ActionEvent e) {
							transparencyConfigBar.setLocation(blogo
									.getLocationOnScreen().x, blogo
									.getLocationOnScreen().y + NoxFrame.TITLE_HEIGHT);
							transparencyConfigBar.Show();
						}
					});
					m.addSeparator();
				}
				m.add(new AbstractAction("About NoX") {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					public void actionPerformed(ActionEvent e) {
						AboutDialog about = new AboutDialog();
						DialogEarthquakeCenter dec = new DialogEarthquakeCenter(
								about);
						about.pack();
						about.setModal(false);
						about.setSize(new Dimension(500, 350));
						about.setPreferredSize(new Dimension(500, 350));
						about.setLocation(new Point(300, 150));
						about.setVisible(true);
						dec.startShake();// �Ի������setModal (false)�ſ��Զ���, ������
					}
				});
				m.add(new AbstractAction("Exit") {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					public void actionPerformed(ActionEvent e) {
						if(IAmBase){
							NoxToolkit.getNetwork().StopNetwork();
							System.exit(0);
						}
						else
							parent.dispose();
					}
				});
				MenuElement els[] = m.getSubElements();
				for(int i = 0; i < els.length; i++)
					els[i].getComponent().setBackground(Color.WHITE);
				m.setLightWeightPopupEnabled(true);
				//m.setBackground(Color.YELLOW);
				//m.setForeground(Color.RED);
				m.pack();
				// λ��Ӧ���������JButton��λ��
				m.show((Component) e.getSource(), 0, NoxFrame.TITLE_HEIGHT);
			}
		});

		ttl = title;
		if (IAmBase) {// �����������
			lab_title = new JLabel(new ImageIcon(title));
		} else {// �������������
			// lab_title = new JLabel("NoX");
			lab_title = new JLabel(title);
			Font font = new Font("����-���������ַ���", Font.BOLD, 24);
			// Font font = new Font("Times New Roman", Font.BOLD, 24);
			lab_title.setForeground(Color.WHITE);
			lab_title.setFont(font);
		}

		/*
		 * btitle = new JButton(new ImageIcon("resrc\\nox.png"));
		 * btitle.setSize(new Dimension(60,20)); btitle.setPreferredSize(new
		 * Dimension(60,20)); btitle.setMaximumSize(new Dimension(60,20));
		 * btitle.setMinimumSize(new Dimension(60,20));
		 * btitle.setBorderPainted(false); btitle.setContentAreaFilled(false);
		 * btitle.setOpaque(false);
		 */
		bminimize = new JButton(new ImageIcon(SystemPath.BUTTONS_RESOURCE_PATH + "minimize.png"));
		bminimize.setRolloverIcon(new ImageIcon(SystemPath.BUTTONS_RESOURCE_PATH + "minimize_rollover.png"));
		bminimize.setToolTipText(getHtmlText("Minimize"));
		bminimize.setSize(btnsize);
		bminimize.setPreferredSize(btnsize);
		bminimize.setMaximumSize(btnsize);
		bminimize.setMinimumSize(btnsize);
		bminimize.setBorderPainted(false);
		bminimize.setContentAreaFilled(false);
		bminimize.setOpaque(false);
		bminimize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int state = parent.getExtendedState();
				// ����ͼ�껯(iconifies)λ
				// Set the iconified bit
				state |= JFrame.ICONIFIED;

				// ͼ�껯Frame
				// Iconify the frame
				parent.setExtendedState(state);
			}
		});

		bmaximize = new JButton(new ImageIcon(SystemPath.BUTTONS_RESOURCE_PATH + "maximize.png"));
		bmaximize.setRolloverIcon(new ImageIcon(SystemPath.BUTTONS_RESOURCE_PATH + "maximize_rollover.png"));
		bmaximize.setToolTipText(getHtmlText("Maximize"));
		bmaximize.setSize(btnsize);
		bmaximize.setPreferredSize(btnsize);
		bmaximize.setMaximumSize(btnsize);
		bmaximize.setMinimumSize(btnsize);
		bmaximize.setBorderPainted(false);
		bmaximize.setContentAreaFilled(false);
		bmaximize.setOpaque(false);
		bmaximize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int state = parent.getExtendedState();

				// ����ͼ�껯(iconifies)λ
				// Set the iconified bit
				// System.out.println("window state: " + state);
				switch (state) {
				// �����ǰ�����״̬, ��������
				case JFrame.MAXIMIZED_BOTH:
					state &= JFrame.NORMAL;// '&', not '|'
					// System.out.println("max->normal");
					setToMaximizeIcon();
					break;
				// �����ǰ�������״̬, �����
				default:
					state |= JFrame.MAXIMIZED_BOTH;
					// System.out.println("normal->max");
					setToNormalizeIcon();
					// Dimension dim =
					// Toolkit.getDefaultToolkit().getScreenSize();
					// parent.setBounds(0, 0, dim.width, dim.height );
					break;
				}
				// System.out.println("window state: " + state);
				// System.out.println("system: " +
				// Toolkit.getDefaultToolkit().isFrameStateSupported(JFrame.MAXIMIZED_BOTH)
				// );
				// ���ô���״̬
				parent.setExtendedState(state);
			}
		});

		bclose = new JButton(new ImageIcon(SystemPath.BUTTONS_RESOURCE_PATH + "close.png"));
		bclose.setRolloverIcon(new ImageIcon(SystemPath.BUTTONS_RESOURCE_PATH + "close_rollover.png"));
		bclose.setToolTipText(getHtmlText("Close"));
		bclose.setSize(btnsize);
		bclose.setPreferredSize(btnsize);
		bclose.setMaximumSize(btnsize);
		bclose.setMinimumSize(btnsize);
		bclose.setBorderPainted(false);
		bclose.setContentAreaFilled(false);
		bclose.setOpaque(false);
		bclose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//parent.dispose();
				parent.setVisible(false);
				/*if (IAmBase)// ����Ǹ�����
				{
					new NoxToolkit().getNetwork().StopNetwork();
					System.exit(0);// .............
				}*/
			}
		});

		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.setAlignmentX(JComponent.CENTER_ALIGNMENT);// ���ö��뷽ʽ
		this.add(blogo);
		this.add(Box.createHorizontalGlue());
		this.add(lab_title);
		// this.add(btitle);
		this.add(Box.createHorizontalGlue());
		/*
		 * if (IAmBase) { this.add(bconfig); // this.add(slider); }
		 */
		this.add(bminimize);
		this.add(bmaximize);
		this.add(bclose);
		this.setOpaque(false);
	}
	
	/**
	 * ����TooltipTxt��html��ʽ
	 * @param text
	 * @return
	 */
	private String getHtmlText(String text) {
		return ("<html><BODY bgColor=#ffffff><Font color=black>" + text + "</Font></BODY></html>");
	}

	public void setToMaximizeIcon() {
		bmaximize.setIcon(new ImageIcon(SystemPath.BUTTONS_RESOURCE_PATH + "maximize.png"));
		bmaximize.setRolloverIcon(new ImageIcon(SystemPath.BUTTONS_RESOURCE_PATH + "maximize_rollover.png"));
	}

	public void setToNormalizeIcon() {
		bmaximize.setIcon(new ImageIcon(SystemPath.BUTTONS_RESOURCE_PATH + "normalize.png"));
		bmaximize.setRolloverIcon(new ImageIcon(SystemPath.BUTTONS_RESOURCE_PATH + "normalize_rollover.png"));
	}

	public void setForegroundColor(boolean white) {
		// ����Ҫ��!!!!!!!!!!!!!!!
		if (white)
			lab_title.setIcon(new ImageIcon(ttl));
		else
			lab_title.setIcon(new ImageIcon(ttl + ".png"));
	}
}

class FootPane extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9009828062432005570L;

	// λ�ڴ������½�����resize
	JButton resizeButn;
	ResizeListener resizer;

	/**
	 * �ײ����, ���ڰ�������ߴ簴ť
	 * 
	 * @param parent
	 *            �����, ��������ߴ�
	 */
	FootPane(JFrame parent) {
		resizeButn = new JButton(new AngledLinesWindowsCornerIcon());
		resizeButn.setBorderPainted(false);
		resizeButn.setContentAreaFilled(false);
		Dimension btnsize = new Dimension(NoxFrame.FOOT_HEIGHT, NoxFrame.FOOT_HEIGHT);
		resizeButn.setSize(btnsize);
		resizeButn.setPreferredSize(btnsize);
		resizeButn.setMaximumSize(btnsize);
		resizeButn.setMinimumSize(btnsize);
		// resizeButn.setOpaque(false);
		//resizeButn.setMargin(new Insets(0,0,0,0));

		resizer = new ResizeListener(parent, resizeButn);
		resizeButn.addMouseListener(resizer);
		resizeButn.addMouseMotionListener(resizer);

		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.add(Box.createHorizontalGlue());
		this.add(resizeButn);
		this.setOpaque(false);
	}

	/**
	 * �ò�����ʱ������ת; ������������Ӽ�����
	 */
	public void removeResizeListener() {
		resizeButn.removeMouseListener(resizer);
		resizeButn.removeMouseMotionListener(resizer);
	}
}

/**
 * ����͸���ȵ��ڶԻ���
 * 
 * @author shinysky
 * 
 */
class FrameConfigDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5405688695983281310L;
	NoxFrame parent;
	JLabel transparent;
	JLabel opaque;
	JSlider slider;
	JButton close;
	JPanel root;

	FrameConfigDialog(NoxFrame nf) {
		// super(nf, "", true);
		parent = nf;
		this.setUndecorated(true);
		transparent = new JLabel("Transparent");
		opaque = new JLabel("Opaque");

		slider = new JSlider(20, 100);
		slider.setValue(100);
		slider.setOpaque(false);
		slider.requestFocus();
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				float value = slider.getValue();
				parent.setOpacity(value * 0.01f);
				//new NoxToolkit().setOpacity(value * 0.01f);
			}
		});
		close = new JButton(new ImageIcon(SystemPath.BUTTONS_RESOURCE_PATH + "close.png"));
		close.setPressedIcon(new ImageIcon(SystemPath.BUTTONS_RESOURCE_PATH + "close_rollover.png"));
		close.setOpaque(false);
		close.setContentAreaFilled(false);
		Dimension bnsize = new Dimension(20, 20);
		close.setSize(bnsize);
		close.setPreferredSize(bnsize);
		close.setMaximumSize(bnsize);
		close.setMinimumSize(bnsize);
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((JDialog) FrameConfigDialog.this).setVisible(false);
			}
		});

		root = new JPanel();
		root.setLayout(new BoxLayout(root, BoxLayout.X_AXIS));
		root.add(transparent);
		root.add(slider);
		root.add(opaque);
		root.add(close);
		root.setBackground(parent.getBackgroundColor());
		
		this.setContentPane(root);
		Dimension size = new Dimension(250, 20);
		this.setSize(size);
		this.setPreferredSize(size);
		this.setMaximumSize(size);
		this.setMinimumSize(size);
		slider.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				System.out.println("slider get focusssssssssss");
			}

			@Override
			public void focusLost(FocusEvent e) {
				System.out.println("slider lost focusssssssssss");
				FrameConfigDialog.this.setVisible(false);
			}
		});
	}

	public void Show() {
		Color color = parent.getBackgroundColor();
		// �Ȱѱ���ɫRGBֵתΪ�Ҷ�ֵ, �ж��Ƿ�����ɫ,
		// �������ɫ��ѱ�ǩ������Ϊ��ɫ, ������Ϊ��ɫ
		// ----------�����Ի�, ft
		if ((float) (color.getRed() * 0.3f + color.getGreen() * 0.59f + color
				.getBlue() * 0.11f) < 128) {
			System.out
					.println("GRAY: "
							+ (float) (color.getRed() * 0.3f + color.getGreen()
									* 0.59f + color.getBlue() * 0.11f));
			transparent.setForeground(Color.WHITE);
			opaque.setForeground(Color.WHITE);
		} else {
			transparent.setForeground(Color.BLACK);
			opaque.setForeground(Color.BLACK);
		}
		root.setBackground(parent.getBackgroundColor());
		this.setVisible(true);
	}
}
