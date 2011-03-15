package mobisocial.nfcserver;

public class Log {

	public static void d(String TAG, String msg) {
		System.out.println(TAG + ": " + msg);
	}
	
	public static void e(String TAG, String msg) {
		System.out.println(TAG + ": " + msg);
	}
	
	public static void e(String TAG, String msg, Exception e) {
		System.out.println(TAG + ": " + msg);
		e.printStackTrace();
	}
}
