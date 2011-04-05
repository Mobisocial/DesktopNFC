package mobisocial.nfcserver.handler;

import mobisocial.nfc.NdefHandler;
import mobisocial.nfc.NfcInterface;
import mobisocial.nfc.PrioritizedHandler;
import android.nfc.NdefMessage;

/**
 * Sets any received Ndef message as the Nfc interface's foregrounded
 * ndef.
 *
 */
public class EchoNdefHandler implements NdefHandler, PrioritizedHandler {
	public static final int PRIORITY = -1;
	public static final String TAG = "ndefserver";
	private final NfcInterface mNfcInterface;
	
	public EchoNdefHandler(NfcInterface nfcInterface) {
		mNfcInterface = nfcInterface;
	}
	
	public int handleNdef(NdefMessage[] ndefMessages) {
		mNfcInterface.setForegroundNdefMessage(ndefMessages[0]);
		return NDEF_PROPAGATE;
	}

	@Override
	public int getPriority() {
		return PRIORITY;
	}
}
