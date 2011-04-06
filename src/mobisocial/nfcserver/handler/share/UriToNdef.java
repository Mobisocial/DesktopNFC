package mobisocial.nfcserver.handler.share;

import java.net.URI;

import mobisocial.nfc.NdefFactory;
import mobisocial.nfc.PrioritizedHandler;
import mobisocial.nfc.ShareHandler;

public class UriToNdef implements ShareHandler, PrioritizedHandler {
	public static final int PRIORITY = 40;

	@Override
	public Object handleShare(Object shared) {
		if (shared instanceof URI) {
			System.out.println("From [uri] to [ndef]");
			return NdefFactory.fromUri((URI)shared);
		}

		return shared;
	}
	
	@Override
	public int getPriority() {
		return PRIORITY;
	}
}
