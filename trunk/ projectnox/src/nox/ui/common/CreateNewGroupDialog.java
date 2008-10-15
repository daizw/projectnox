package nox.ui.common;

import java.awt.Component;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

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
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.protocol.PipeAdvertisement;
import nox.net.common.NoxToolkit;
import nox.net.common.PipeUtil;
import nox.net.group.PeerGroupUtil;
import nox.ui.me.Cheyenne;

@SuppressWarnings("serial")
public class CreateNewGroupDialog extends JDialog{
	CreateNewGroupPane cngPane = new CreateNewGroupPane(CreateNewGroupDialog.this);
	private Toolkit tk;
	private Image img_logo;
	private Image img_logo_big;
	private JFrame parent;
	public CreateNewGroupDialog(JFrame parent){
		super(parent, "Create New Group", true);
		this.parent = parent;
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
	public boolean add2GrouList(PeerGroupAdvertisement adv){
		return ((Cheyenne)parent).joinThisGroup(adv);
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

	CreateNewGroupDialog parent;
	
	CreateNewGroupPane(final CreateNewGroupDialog parent){
		this.parent = parent;
		
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
					System.out.println("The passwords you input are not identical, please check your input." + pwdStr + ":" + vPwdStr);
					JOptionPane.showMessageDialog((Component) null,
							"The passwords you input are not identical, please check your input!", "ERROR!",
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
        //expiration days
		int expiration = 3;
        String description = (descTxtFd.getText() + "").trim();
        String password = null;
        if(privateChkBox.isSelected()){
        	password = new String(pwdPwdFd.getPassword());
        }
        
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
            	System.out.println("Creating PGA with: " + name + ":" + password);
                pga = PeerGroupUtil.createPGA(ppg, name, description,
                        password, expiration * MILLISECONDS_IN_A_DAY);
            } catch (Exception e) {
            	e.printStackTrace();
            }

            if (pga != null) {
                PeerGroup pg = null;
                System.out.println("新组所用的广告: \n" + pga);
                // Create the group itself
                try {
                    pg = ppg.newGroup(pga);
                } catch (PeerGroupException pge) {
                	pge.printStackTrace();
                }

                // if the group was successfully created join it
                if (pg != null) {
                	System.out.println("成功创建组, 正在创建该组所用管道广告...");
                	//创建对应的管道广告, 并发布之
                	PipeAdvertisement pia = PipeUtil.createAdv(pg, pg.getPeerGroupID().toString(), PipeService.PropagateType, null);
                	try {
						pg.getDiscoveryService().publish(pia);
					} catch (IOException e) {
						e.printStackTrace();
					}
                	pg.getDiscoveryService().remotePublish(pia);
                	//boolean joined = joinGroup(pg, true, true);
                	boolean joined = PeerGroupUtil.joinPeerGroup(pg, PeerGroupUtil.MEMBERSHIP_ID, password);
                	
                	if(joined){
                		JOptionPane.showMessageDialog((Component) null,
        					"Group creating succeed, you've join it automatically. Please check out the group list.", "Succeed!",
        					JOptionPane.INFORMATION_MESSAGE);
                		if(parent.add2GrouList(pga)){
                			System.out.println("加入成功, 但是没有成功添加到组列表, it's weird!");
                		}
                	}
                	else
                		JOptionPane.showMessageDialog((Component) null,
            					"Group creating succeed, but failed to join it, it's weird!", "Information",
            					JOptionPane.INFORMATION_MESSAGE);
        			return true;
                } else {
                    System.out.println("Error: failed to create new group");
                    System.out.println("创建组失败");
        			JOptionPane.showMessageDialog((Component) null,
        					"Failed to create new group", "Phew~",
        					JOptionPane.WARNING_MESSAGE);
        			return false;
                }
            } else {
            	System.out.println("Error: failed to create new group adv");
            	return false;
            }
        } else {
        	System.out.println("Error: name == null, it's not suggested, please enter the new group name.");
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
     *//*
    public boolean joinGroup(final PeerGroup peerGroup, boolean useAutoRdvMode,
                          boolean discover) {
    	System.out.println("Try to join this group....");

        PipeAdvertisement pipeAdvertisment = PipeUtil.getPipeAdv(peerGroup,
        		peerGroup.getPeerGroupID().toString(), PipeService.UnicastType, null, true);
        
        if (useAutoRdvMode) {
        	peerGroup.getRendezVousService().setAutoStart(useAutoRdvMode,
                    3*1000);
        }
        // now lets add the different dialogs/pipe listeners
 
        // add the one Group Panel to the navigation tree

        PeerGroup cpg = AuthenticationUtil.getTLSPeerGroup(peerGroup);

        if (!AuthenticationUtil.isAuthenticated(cpg)) {
            System.out.println("authenticating");

            AuthenticationUtil.authenticate(cpg);
        }

        if (!AuthenticationUtil.isAuthenticated(cpg)) {
            System.out.println("not authenticated");
            return false;
        }
        return true;
    }*/
}