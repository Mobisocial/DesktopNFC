package mobisocial.nfcserver.handler.share;

import mobisocial.nfc.NdefFactory;
import mobisocial.nfc.PrioritizedHandler;
import mobisocial.nfc.ShareHandler;

public class TextToNdef implements ShareHandler, PrioritizedHandler {
	public static final int PRIORITY = 40;

	//@Override
	public Object handleShare(Object shared) {
		if (shared instanceof String) {
			System.out.println("[string] to [ndef]");
			return NdefFactory.fromText((String)shared);
		}
		return shared;
	}
	
	//@Override
	public int getPriority() {
		return PRIORITY;
	}
}
