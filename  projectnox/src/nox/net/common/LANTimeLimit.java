package nox.net.common;

public class LANTimeLimit {
	public static long UNIT_TIME = 500;
	//public static long FETCH_PIPEADV_TIME = 2 * 1000;
	//public static long FETCH_PEERADV_TIME = 2 * 1000;
	//public static long FETCH_GROUPADV_TIME = 2 * 1000;
	public static long INTER_CHECKING_STATUS_SLEEP_TIME = 10 * 1000;
	
	public static long CREATE_OUTPUT_PIPE_WAITTIME = 5 * 1000;
	/**
	 * 如果在这个时间内没有收到对方的状态消息, 视为离线
	 */
	public static long OFFLINE_TIMELIMIT = 60 * 1000;
	/**
	 * 最大重试次数......和最大重试时间功能有重复, 还是使用最大重试次数吧!
	 *  因为可以作为计数器(eg. counter--), 而使用时间来倒计时不方便.
	 */
	public static final int CONNECT_MAXRETRIES = 10;
	public static final int FETCH_PEERADV_MAXRETRIES = 10;
	public static final int FETCH_GROUPADV_MAXRETRIES = 10;
	public static final int FETCH_PIPEADV_MAXRETRIES = 10;
}
