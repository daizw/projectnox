package net.nox;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
public class MiscUtil {
	/** �ڻ�ȡ����IP��ַ�����쳣ʱ���ظ�ֵ */
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
     * ����DataOutputStream.writeUTFÿ����ഫ��65535�ֽڵ����ݣ���˴���65535�ֽڵ�������Ҫ�ֹ����
     * @param str	���в�ֵ��ַ���
     * @return		�ַ������飬�����е�ÿ��Ԫ�ؾ��պ�С��65535�ֽ�(���һ��Ԫ�س���)
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
