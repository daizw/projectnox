package noxUI;

import java.awt.Component;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.logging.Level;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import net.jxta.exception.PeerGroupException;
import net.jxta.logging.Logging;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.protocol.PipeAdvertisement;
import net.nox.AuthenticationUtil;
import net.nox.NoxToolkit;
import net.nox.PeerGroupUtil;
import net.nox.PipeUtil;

@SuppressWarnings("serial")
public class CreateNewGroupDialog extends JDialog{
	CreateNewGroupPane cngPane = new CreateNewGroupPane(CreateNewGroupDialog.this);
	private Toolkit tk;
	private Image img_logo;
	private Image img_logo_big;
	
	CreateNewGroupDialog(JFrame parent){
		super(parent, "Create New Group", true);
		
		//如果extends JDialog, 则设置大图标没有意义.
		//extends JFrame时才可以显示大图标.
		tk = Toolkit.getDefaultToolkit();
		img_logo = tk.getImage(SystemPath.ICONS_RESOURCE_PATH + "new_group_20.png");
		img_logo_big = tk.getImage(SystemPath.ICONS_RESOURCE_PATH + "new_group_48.png");
		ArrayList<Image> icons = new ArrayList<Image>();
		icons.add(img_logo);
		icons.add(img_logo_big);
		this.setIconImages(icons);
		
		this.setContentPane(cngPane);
		this.setBounds(200, 200, 400, 220);
		
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setResizable(false);
	}
	
	public static void main(String[] args){
		CreateNewGroupDialog cngd = new CreateNewGroupDialog(null);
		cngd.setVisible(true);
	}
}

@SuppressWarnings("serial")
class CreateNewGroupPane extends JPanel{
	private static final long MILLISECONDS_IN_A_DAY = 24 * 60 * 60 * 1000;
	
	JPanel namePane = new JPanel();
	JLabel nameLabel = new JLabel("New Group Name: ");
	JTextField nameTxtFd = new JTextField(20);
	
	JPanel descPane = new JPanel();
	JLabel descLabel = new JLabel("          Description: ");
	JTextField descTxtFd = new JTextField(20);
	
	JCheckBox privateChkBox = new JCheckBox("Create Private Group");
	
	JPanel pwdPane = new JPanel();
	JLabel pwdLabel = new JLabel("Group Password: ");
	JPasswordField pwdPwdFd = new JPasswordField(20);
	
	JPanel verifyPwdPane = new JPanel();
	JLabel verifyPwdLabel = new JLabel("Verify Password: ");
	JPasswordField verifyPwdPwdFd = new JPasswordField(20);
	
	JPanel btnsPane = new JPanel();
	JButton doCreateBtn = new JButton("OK");
	JButton cancelBtn = new JButton("Cancel");

	CreateNewGroupPane(final CreateNewGroupDialog parent){
		namePane.add(nameLabel);
		namePane.add(nameTxtFd);
		
		descPane.add(descLabel);
		descPane.add(descTxtFd);
		
		privateChkBox.setSelected(false);
		
		pwdPwdFd.setText("password");
		verifyPwdPwdFd.setText("password");
		
		pwdPwdFd.setEchoChar('●');
		verifyPwdPwdFd.setEchoChar('●');
		
		pwdPwdFd.setEnabled(false);
		verifyPwdPwdFd.setEnabled(false);
		
		pwdPane.add(pwdLabel);
		pwdPane.add(pwdPwdFd);
		
		verifyPwdPane.add(verifyPwdLabel);
		verifyPwdPane.add(verifyPwdPwdFd);
		
		btnsPane.add(doCreateBtn);
		btnsPane.add(cancelBtn);
		
		privateChkBox.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(privateChkBox.isSelected()){
					pwdPwdFd.setEnabled(true);
					verifyPwdPwdFd.setEnabled(true);
				}else{
					pwdPwdFd.setEnabled(false);
					verifyPwdPwdFd.setEnabled(false);
				}
			}
		});
		
		doCreateBtn.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//TODO 创建新组
				String pwdStr = new String(pwdPwdFd.getPassword());
				String vPwdStr = new String(verifyPwdPwdFd.getPassword());
				
				if(privateChkBox.isSelected() && ! pwdStr.equals(vPwdStr)){
					System.out.println("两次输入的密码不相符,请检查输入.	" + pwdStr + ":" + vPwdStr);
					JOptionPane.showMessageDialog((Component) null,
							"两次输入的密码不相符,请检查输入!", "ERROR!",
							JOptionPane.ERROR_MESSAGE);
				}else {
					if(doCreatNewGroup())
						parent.dispose();
				}
			}
		});
		
		cancelBtn.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				parent.dispose();
			}
		});
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		//this.setAlignmentY(JComponent.LEFT_ALIGNMENT);
		this.add(namePane);
		this.add(descPane);
		this.add(privateChkBox);
		this.add(pwdPane);
		this.add(verifyPwdPane);
		this.add(btnsPane);
	}
	
	public boolean doCreatNewGroup(){
		System.out.println("Name:	" + nameTxtFd.getText() + "");
		System.out.println("Description:	" + descTxtFd.getText() + "") ;
		System.out.println("CheckBox Stat:	" + privateChkBox.isSelected());
		System.out.println("Password:	" + new String(pwdPwdFd.getPassword()));
		System.out.println("Verify Pwd:	" + new String(verifyPwdPwdFd.getPassword()));
		
		String name = (nameTxtFd.getText() + "").trim();
        int expiration = 3;
        String description = (descTxtFd.getText() + "").trim();
        String password = new String(pwdPwdFd.getPassword());
        
        password = password != null ? password.trim() : "";

        if (name.length() == 0) {
            name = null;
        }

        // only create groups which have a  non-empty name
        if (name != null) {
            PeerGroup ppg = NoxToolkit.getNetworkManager().getNetPeerGroup();
            PeerGroupAdvertisement pga = null;
            
            // create the PeerGroupAdvertisement for the new group
            try {
                pga = PeerGroupUtil.create(ppg, name, description,
                        password, expiration * MILLISECONDS_IN_A_DAY);
            } catch (Exception e) {
            	e.printStackTrace();
            }

            if (pga != null) {
                PeerGroup pg = null;

                // Create the group itself
                try {
                    pg = ppg.newGroup(pga);
                } catch (PeerGroupException pge) {
                	pge.printStackTrace();
                }

                // if the group was successfully created join it
                if (pg != null) {
                    //Group g = new Group(this.view.getControl(), pg, parent);

//                    g.setAutoRendezVousPeriod(Constants.AUTO_RENDEZVOUS_CREATE_PERIOD);

                    joinGroup(pg, true, true);
                    
                    System.out.println("成功创建组");
        			JOptionPane.showMessageDialog((Component) null,
        					"成功创建组, 您已自动加入该组. 可在组列表中查看.", "Succeed!",
        					JOptionPane.INFORMATION_MESSAGE);
        			return true;
        			
                } else {
                    System.out.println("Error: failed to create new group");
                    System.out.println("创建组失败");
        			JOptionPane.showMessageDialog((Component) null,
        					"创建组失败", "Phew~",
        					JOptionPane.WARNING_MESSAGE);
        			return false;
                }
            } else {
            	System.out.println("Error: failed to create new group adv");
            	return false;
            }
        }
        ///////////////////////////////////////////////////////
		/*PeerGroup newpg = null;
		if(privateChkBox.isSelected()){
			newpg = NoxToolkit.createNewPeerGroup(nameTxtFd.getText() + "",
					descTxtFd.getText() + "",
					new String(pwdPwdFd.getPassword()));
		} else {
			newpg = NoxToolkit.createNewPeerGroup(nameTxtFd.getText() + "",
					descTxtFd.getText() + "");
		}*/
        return false;
	}
	
	/**
     * Part of group Lifecycle. Joins group. Starts group services and
     * associated resources.
     */
    public void joinGroup(final PeerGroup peerGroup, boolean useAutoRdvMode,
                          boolean discover) {

        PipeAdvertisement pipeAdvertisment = PipeUtil.getPipeAdv(peerGroup,
        		peerGroup.getPeerGroupID().toString(), PipeService.UnicastType, null, true);

        // now lets add the different dialogs/pipe listeners
 
        // add the one Group Panel to the navigation tree

        PeerGroup cpg = AuthenticationUtil.getTLSPeerGroup(peerGroup);

        if (!AuthenticationUtil.isAuthenticated(cpg)) {
            System.out.println("authenticating");

            AuthenticationUtil.authenticate(cpg);
        }

        if (!AuthenticationUtil.isAuthenticated(cpg)) {
            System.out.println("not authenticated");
        }

        if (useAutoRdvMode) {
        	peerGroup.getRendezVousService().setAutoStart(useAutoRdvMode,
                    3*1000);
        }
    }
}
