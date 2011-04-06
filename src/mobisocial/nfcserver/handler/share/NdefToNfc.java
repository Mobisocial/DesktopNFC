package mobisocial.nfcserver.handler.share;

import android.nfc.NdefMessage;

import mobisocial.nfc.NfcInterface;
import mobisocial.nfc.ShareHandler;

public class NdefToNfc implements ShareHandler {
	public static final int PRIORITY = 99;
	final NfcInterface mNfcInterface;
	
	public NdefToNfc(NfcInterface nfcInterface) {
		mNfcInterface = nfcInterface;
	}
	@Override
	public Object handleShare(Object shared) {
		if (shared == null) {
			System.out.println("from [empty] to [nfc]");
			mNfcInterface.setForegroundNdefMessage(null);
		} else if (shared instanceof NdefMessage) {
			System.out.println("from [ndef] to [nfc]");
			mNfcInterface.setForegroundNdefMessage((NdefMessage)shared);
		} else {
			System.err.print("Got a message of unknown type to share.");
		}
		return shared;
	}
}
