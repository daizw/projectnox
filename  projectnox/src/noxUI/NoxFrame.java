package noxUI;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.MatteBorder;

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
	private Titlebar titlebar;
	private JPanel container;
	private FootPane footpane;
	private JPanel fakeFace;

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
	NoxFrame(String title, String path_background,
			String path_logo, String path_title,
			String path_minimize, String path_minimize_rollover,
			String path_maximize, String path_maximize_rollover,
			String path_normalize, String path_normalize_rollover,
			String path_close, String path_close_rollover,
			final boolean IAmBase) {
		super(title);
		this.setUndecorated(true);

		/*try {
            UIManager.setLookAndFeel(new SubstanceLookAndFeel());
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }*/
        
		tk = Toolkit.getDefaultToolkit();
		background = tk.getImage(path_background);
		img_logo = tk.getImage(path_logo);
		this.setIconImage(img_logo);

		// ׼��ͼƬ
		this.prepareImage(background, rootpane);

		Container contentPane = getContentPane();

		this.setMinimumSize(new Dimension(WIDTH_MIN, HEIGHT_MIN));
		/**
		 * ���������
		 */
		titlebar = new Titlebar(this, path_logo, path_title, path_minimize,
				path_minimize_rollover, path_maximize, path_maximize_rollover,
				path_normalize, path_normalize_rollover, path_close,
				path_close_rollover, IAmBase);
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
		rootpane.add(titlebar);
		rootpane.add(container);
		rootpane.add(footpane);

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
		/**
		 * ������ײ��JPanel, �����Ϊ2�ĺ�ɫ�߿� ������rootpane
		 */
		fakeFace = new JPanel();
		MatteBorder paneEdge = BorderFactory.createMatteBorder(2, 2, 2, 2,
				Color.BLACK);// ��ɫ������Ϊ��������
		fakeFace.setBorder(paneEdge);
		fakeFace.setLayout(new BoxLayout(fakeFace, BoxLayout.Y_AXIS));
		fakeFace.add(rootpane);

		contentPane.add(fakeFace);

		/**
		 * �������ƶ���������ʵ�ִ����ƶ�
		 */
		MoveMouseListener mml = new MoveMouseListener(this.getRootPane(),
				this);
		this.getRootPane().addMouseListener(mml);
		this.getRootPane().addMouseMotionListener(mml);
		
		/**
		 * ����Ĭ�ϴ��ڲ���(ALT+F4)
		 */
		if(IAmBase)
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		else
			this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	public JPanel getContainer() {
		return container;
	}
	public void resetMaximizeIcon(){
		titlebar.resetMaximizeIcon();
	}
	public void resetNormalizeIcon(){
		titlebar.resetNormalizeIcon();
	}

}

class Titlebar extends JPanel {
	/**
	 * what's this svID?
	 */
	private static final long serialVersionUID = -538877811148092522L;
	JButton blogo;
	JLabel lab_title;
	JButton bminimize;
	JButton bmaximize;
	JButton bclose;
	
	String path_max;
	String path_max_rollover;
	String path_norm;
	String path_norm_rollover;

	// boolean windowStateIsMax;

	/**
	 * ������JPanel��
	 * 
	 * @param parent
	 *            �����, ���ڿ����������С��
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
	 */
	Titlebar(final JFrame parent, String path_logo, String path_title,
			String path_minimize, String path_minimize_rollover,
			final String path_maximize, final String path_maximize_rollover,
			final String path_normalize, final String path_normalize_rollover,
			String path_close, String path_close_rollover, final boolean IAmBase) {
		// windowStateIsMax = false;
		
		path_max = path_maximize;
		path_max_rollover = path_maximize_rollover;
		path_norm = path_normalize;
		path_norm_rollover = path_normalize_rollover;


		blogo = new JButton(new ImageIcon(path_logo));
		blogo.setSize(new Dimension(20, 20));
		blogo.setPreferredSize(new Dimension(20, 20));
		blogo.setMaximumSize(new Dimension(20, 20));
		blogo.setMinimumSize(new Dimension(20, 20));
		blogo.setBorderPainted(false);
		blogo.setContentAreaFilled(false);
		blogo.setOpaque(false);
		blogo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// JOptionPane.showMessageDialog(null, "Hello, I am DaiZW,
				// welcome to the NoX world!");
				AboutDialog about = new AboutDialog();
				DialogEarthquakeCenter dec = new DialogEarthquakeCenter(about);
				about.pack();
				about.setModal(false);
				about.setSize(new Dimension(500, 350));
				about.setPreferredSize(new Dimension(500, 350));
				/*
				 * this.setMaximumSize(new Dimension(400,500));
				 * this.setMinimumSize(new Dimension(400,500));
				 */
				about.setLocation(new Point(300, 150));
				about.setVisible(true);
				dec.startShake();// �Ի������setModal (false)�ſ��Զ���, ������
			}
		});

		/*
		 * lab_title = new JLabel("NoX"); //Font font = new Font("����-���������ַ���",
		 * Font.BOLD, 24); Font font = new Font("Times New Roman", Font.BOLD,
		 * 24); lab_title.setForeground(Color.WHITE); lab_title.setFont(font);
		 */
		lab_title = new JLabel(new ImageIcon(path_title));

		/*
		 * btitle = new JButton(new ImageIcon("resrc\\nox.png"));
		 * btitle.setSize(new Dimension(60,20)); btitle.setPreferredSize(new
		 * Dimension(60,20)); btitle.setMaximumSize(new Dimension(60,20));
		 * btitle.setMinimumSize(new Dimension(60,20));
		 * btitle.setBorderPainted(false); btitle.setContentAreaFilled(false);
		 * btitle.setOpaque(false);
		 */

		bminimize = new JButton(new ImageIcon(path_minimize));
		bminimize.setRolloverIcon(new ImageIcon(path_minimize_rollover));
		bminimize.setSize(new Dimension(20, 20));
		bminimize.setPreferredSize(new Dimension(20, 20));
		bminimize.setMaximumSize(new Dimension(20, 20));
		bminimize.setMinimumSize(new Dimension(20, 20));
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

		bmaximize = new JButton(new ImageIcon(path_maximize));
		bmaximize.setRolloverIcon(new ImageIcon(path_maximize_rollover));
		bmaximize.setSize(new Dimension(20, 20));
		bmaximize.setPreferredSize(new Dimension(20, 20));
		bmaximize.setMaximumSize(new Dimension(20, 20));
		bmaximize.setMinimumSize(new Dimension(20, 20));
		bmaximize.setBorderPainted(false);
		bmaximize.setContentAreaFilled(false);
		bmaximize.setOpaque(false);
		bmaximize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int state = parent.getExtendedState();

				// ����ͼ�껯(iconifies)λ
				// Set the iconified bit
				System.out.println("window state: " + state);
				switch (state) {
				// �����ǰ�����״̬, ��������
				case JFrame.MAXIMIZED_BOTH:
					state &= JFrame.NORMAL;// '&', not '|'
					System.out.println("max->normal");
					resetMaximizeIcon();
					break;
				// �����ǰ�������״̬, �����
				default:
					state |= JFrame.MAXIMIZED_BOTH;
					System.out.println("normal->max");
					resetNormalizeIcon();
					//Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
					//parent.setBounds(0, 0, dim.width, dim.height );
					break;
				}
				System.out.println("window state: " + state);
				//System.out.println("system: " + 
				//Toolkit.getDefaultToolkit().isFrameStateSupported(JFrame.MAXIMIZED_BOTH) );
				// ���ô���״̬
				parent.setExtendedState(state);
			}
		});

		bclose = new JButton(new ImageIcon(path_close));
		bclose.setRolloverIcon(new ImageIcon(path_close_rollover));
		bclose.setSize(new Dimension(20, 20));
		bclose.setPreferredSize(new Dimension(20, 20));
		bclose.setMaximumSize(new Dimension(20, 20));
		bclose.setMinimumSize(new Dimension(20, 20));
		bclose.setBorderPainted(false);
		bclose.setContentAreaFilled(false);
		bclose.setOpaque(false);
		bclose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parent.dispose();
				if (IAmBase)// ����Ǹ�����
					System.exit(0);// .............
			}
		});

		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.setAlignmentX(JComponent.CENTER_ALIGNMENT);// ���ö��뷽ʽ
		this.add(blogo);
		this.add(Box.createHorizontalGlue());
		this.add(lab_title);
		// this.add(btitle);
		this.add(Box.createHorizontalGlue());
		this.add(bminimize);
		this.add(bmaximize);
		this.add(bclose);
		this.setOpaque(false);
	}
	
	public void resetMaximizeIcon(){
		bmaximize.setIcon(new ImageIcon(path_max));
		bmaximize.setRolloverIcon(new ImageIcon(path_max_rollover));
	}
	public void resetNormalizeIcon(){
		bmaximize.setIcon(new ImageIcon(path_norm));
		bmaximize.setRolloverIcon(new ImageIcon(path_norm_rollover));
	}
}

class FootPane extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9009828062432005570L;

	// λ�ڴ������½�����resize
	JButton resizeButn;

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
		resizeButn.setSize(new Dimension(15, 15));
		resizeButn.setPreferredSize(new Dimension(15, 15));
		resizeButn.setMaximumSize(new Dimension(15, 15));
		resizeButn.setMinimumSize(new Dimension(15, 15));
		// resizeButn.setOpaque(false);

		ResizeListener resizer = new ResizeListener(parent, resizeButn);
		resizeButn.addMouseListener(resizer);
		resizeButn.addMouseMotionListener(resizer);

		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.add(Box.createHorizontalGlue());
		this.add(resizeButn);
		this.setOpaque(false);
	}
}
