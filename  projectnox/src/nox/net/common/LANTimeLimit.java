package nox.net.common;

public class LANTimeLimit {
	public static long UNIT_TIME = 500;
	//public static long FETCH_PIPEADV_TIME = 2 * 1000;
	//public static long FETCH_PEERADV_TIME = 2 * 1000;
	//public static long FETCH_GROUPADV_TIME = 2 * 1000;
	public static long INTER_CHECKING_STATUS_SLEEP_TIME = 10 * 1000;
	
	public static long CREATE_OUTPUT_PIPE_WAITTIME = 5 * 1000;
	/**
	 * ��������ʱ����û���յ��Է���״̬��Ϣ, ��Ϊ����
	 */
	public static long OFFLINE_TIMELIMIT = 60 * 1000;
	/**
	 * ������Դ���......���������ʱ�书�����ظ�, ����ʹ��������Դ�����!
	 *  ��Ϊ������Ϊ������(eg. counter--), ��ʹ��ʱ��������ʱ������.
	 */
	public static final int CONNECT_MAXRETRIES = 10;
	public static final int FETCH_PEERADV_MAXRETRIES = 10;
	public static final int FETCH_GROUPADV_MAXRETRIES = 10;
	public static final int FETCH_PIPEADV_MAXRETRIES = 10;
}
