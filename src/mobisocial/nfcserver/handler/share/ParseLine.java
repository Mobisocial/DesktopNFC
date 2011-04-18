package mobisocial.nfcserver.handler.share;

import java.net.URI;

import mobisocial.nfc.PrioritizedHandler;
import mobisocial.nfc.ShareHandler;

public class ParseLine implements ShareHandler, PrioritizedHandler {
	public static final int PRIORITY = 0;

	//@Override
	public Object handleShare(Object shared) {
		if (!(shared instanceof String)) {
			// TODO error?
			return shared;
		}
		
		System.out.println("[user]: " + shared);

		String line = (String) shared;
		if (line == null || line.length() == 0) {
			System.out.println("From [parsed] to [empty]");
			return null;
		}

		if (line.contains("://") && !line.contains("\"")) {
			// TODO: better checking for URI
			try {
				System.out.println("From [parsed] to [uri]");
				return URI.create(line);
			} catch (IllegalArgumentException e) {}
		}
		
		System.out.println("[parsed] to [string]");
		return shared;
	}

	//@Override
	public int getPriority() {
		return PRIORITY;
	}
}
