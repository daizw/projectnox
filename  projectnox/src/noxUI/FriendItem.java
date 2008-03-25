package noxUI;

import javax.swing.ImageIcon;

public class FriendItem {
	private ImageIcon portrait;
	private String nickname;
	private String sign;
	public FriendItem(ImageIcon portr, String nick, String signstr)
	{
		this.portrait = portr;
		this.nickname = nick;
		this.sign = signstr;
	}
	public ImageIcon getPortrait()
	{
		return portrait;
	}
	public String getNick()
	{
		return nickname;
	}
	public String getSign()
	{
		return sign;
	}
}
