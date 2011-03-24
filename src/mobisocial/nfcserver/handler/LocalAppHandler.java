package mobisocial.nfcserver.handler;

import mobisocial.nfc.NdefHandler;
import android.nfc.NdefMessage;

public class LocalAppHandler implements NdefHandler {
	public static final String TAG = "ndefserver";
	
	public int handleNdef(NdefMessage[] ndefMessages) {
		return NDEF_PROPAGATE;
	}
}
