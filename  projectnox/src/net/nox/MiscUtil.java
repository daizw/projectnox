package net.nox;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
public class MiscUtil {
	/** 在获取本机IP地址发生异常时返回该值 */
	public static final String IPADDRESS = "0.0.0.0";
	
    public static String getIPAddress() {
		String ip = IPADDRESS;

		try {
			String ha = InetAddress.getLocalHost().getHostAddress();
			InetAddress[] a = InetAddress.getAllByName(ha);

			if (a.length == 1) {
				ip = a[0].getHostAddress();
			}
		} catch (UnknownHostException uhe) {
		}

		return ip;
    }
    
    public static String getHostName() {
		String name = null;

		try
		{
			name = InetAddress.getLocalHost().getHostName();
		} catch(java.net.UnknownHostException uhe) {
		}

		return name;
    }
    
    public static String getUserName() {
        // TODO: Read user's configuration from property file, For simplicity, here use user's host name instead.
        return getHostName();
    }
    
    /**
     * 由于DataOutputStream.writeUTF每次最多传输65535字节的数据，因此大于65535字节的数据需要手工拆分
     * @param str	进行拆分的字符串
     * @return		字符串数组，数组中的每个元素均刚好小于65535字节(最后一个元素除外)
     * @see java.io.DataOutputStream.writeUTF
     */
    public static Object[] splitUTFString(String str) {
    	int strlen = str.length();
    	int utflen = 0;
    	int c;
    	int prev = 0;
    	ArrayList<String> list = new ArrayList<String>();
    	
    	/* use charAt instead of copying String to char array */
    	for (int i = 0; i < strlen;) {
            c = str.charAt(i);
		    if ((c >= 0x0001) && (c <= 0x007F)) {
		    	utflen++;
		    } else if (c > 0x07FF) {
		    	utflen += 3;
		    } else {
		    	utflen += 2;
		    }
		    
		    if(utflen > 65535) {
		    	list.add(str.substring(prev, i));
		    	
		    	utflen = 0;
		    	prev = i;
		    } else {
		    	i++;
		    }
		}
    	list.add(str.substring(prev));
    	
    	return list.toArray();
    }
}
