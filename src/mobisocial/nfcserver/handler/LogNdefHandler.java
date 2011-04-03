package mobisocial.nfcserver.handler;

import mobisocial.nfc.NdefHandler;
import mobisocial.nfc.PrioritizedHandler;
import android.nfc.NdefMessage;

public class LogNdefHandler implements NdefHandler, PrioritizedHandler {
	public static final int PRIORITY = -1;
	public static final String TAG = "ndefserver";
	public int handleNdef(NdefMessage[] ndefMessages) {
		System.out.println("Received an ndef with TNF " + ndefMessages[0].getRecords()[0].getTnf());
		return NDEF_PROPAGATE;
	}

	@Override
	public int getPriority() {
		return PRIORITY;
	}
}
