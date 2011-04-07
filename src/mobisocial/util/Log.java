package mobisocial.util;

public class Log {
	public static void d(String TAG, String msg) {
		System.out.println(TAG + ": " + msg);
	}

	public static void e(String TAG, String msg) {
		System.err.println(TAG + ": " + msg);
	}

	public static void e(String TAG, String msg, Exception e) {
		System.err.println(TAG + ": " + msg);
		e.printStackTrace();
	}

	public static void w(String TAG, String msg, Exception e) {
		System.err.println(TAG + ": " + msg);
		e.printStackTrace();
	}
}
