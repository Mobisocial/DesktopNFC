package mobisocial.nfcserver;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import mobisocial.nfc.NdefHandler;
import mobisocial.nfc.NfcInterface;
import mobisocial.nfcserver.handler.AppManifestHandler;
import mobisocial.nfcserver.handler.UrlHandler;
import mobisocial.util.QR;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

public class DesktopNfcServer implements NfcInterface {
	public static DesktopNfcServer sInstance;
	private final Map<Integer, Set<NdefHandler>> mNdefHandlers = new TreeMap<Integer, Set<NdefHandler>>();
	private NdefMessage mForegroundNdef = null;

	public interface Contract {
		public void start();
		public void stop();
		public String getHandoverUrl();
	}
	
	private static final String TAG = "nfcserver";
	static DesktopNfcServer sNdefProxy;

	public static void main(String[] args) {
		try {
			Contract server;
			if (args.length > 0 && args[0].equals("tcp")) { // recoverable failure; if args is null, length == 0.
				server = new TcpNdefServer(args);
			} else {
				server = new BluetoothNdefServer(args); //Builder<BluetoothNdefServer>().build();
			}
			System.out.println("Welcome to DesktopNfc!");
			System.out.println("Your configuration code is: " + QR.getQrl(server.getHandoverUrl()));
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private DesktopNfcServer() {
		addNdefHandler(new UrlHandler());
		addNdefHandler(new AppManifestHandler());
	}
	
	public static NfcInterface getInstance() {
		if (sInstance == null) {
			sInstance = new DesktopNfcServer();
		}
		return sInstance;
	}

	public void addNdefHandler(NdefHandler handler) {
		addNdefHandler(NdefHandler.DEFAULT_PRIORITY, handler);
	}
	
	public synchronized void addNdefHandler(Integer priority, NdefHandler handler) {
		if (!mNdefHandlers.containsKey(priority)) {
			mNdefHandlers.put(priority, new LinkedHashSet<NdefHandler>());
		}
		Set<NdefHandler> handlers = mNdefHandlers.get(priority);
		handlers.add(handler);
	}
	
	public synchronized void handleNdef(NdefMessage ndef) {
		mForegroundNdef = ndef;
		Iterator<Integer> bins = mNdefHandlers.keySet().iterator();
		while (bins.hasNext()) {
			Integer priority = bins.next();
			Iterator<NdefHandler> handlers = mNdefHandlers.get(priority).iterator();
			NdefMessage[] ndefs = new NdefMessage[] { ndef }; // compatibility with NdefHandler.
			while (handlers.hasNext()) {
				NdefHandler handler = handlers.next();
				if (handler.handleNdef(ndefs) == NdefHandler.NDEF_CONSUME) {
					return;
				}
			}
		}
	}

	@Override
	public NdefMessage getForegroundNdefMessage() {
		return mForegroundNdef;
	}
}