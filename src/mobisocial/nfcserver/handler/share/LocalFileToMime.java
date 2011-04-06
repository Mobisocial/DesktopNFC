package mobisocial.nfcserver.handler.share;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import mobisocial.nfc.NdefFactory;
import mobisocial.nfc.PrioritizedHandler;
import mobisocial.nfc.ShareHandler;
import mobisocial.nfcserver.handler.MimeTypeHandler;

public class LocalFileToMime implements ShareHandler, PrioritizedHandler {
	public static final int PRIORITY = 30;
	
	@Override
	public Object handleShare(Object shared) {
		// TODO: instanceof uri
		File file = null;
		if (shared instanceof URI) {
			if (((URI) shared).getScheme().equals("file")) {
				file = new File((URI) shared);
			} else {
				return shared;
			}
		}
		
		if (shared instanceof String) {
			if (((String)shared).startsWith("/")) {
				// TODO: cross platform
				file = new File((String)shared);
			}
		}
		
		if (file != null) {
			System.out.println("from [uri] to [mime]");
			// TODO: many improvements here. Proper mime handling.
			// Share InputStream instead of bytes.
			byte[] bytes = null;
			try {
				byte[] buffer = new byte[1024 * 100];
				FileInputStream fin = new FileInputStream(file);
				int r = fin.read(buffer, 0, buffer.length);
				bytes = Arrays.copyOf(buffer, r);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}

			String type = "application/octet-stream";
			int ext = file.getName().lastIndexOf("."); 
			if (ext > 0) {
				String extension = file.getName().substring(ext + 1);
				if (MimeTypeHandler.MIME_EXTENSIONS.containsValue(extension)) {
					// TODO: better implementation.
					for (String key: MimeTypeHandler.MIME_EXTENSIONS.keySet()) {
						if (MimeTypeHandler.MIME_EXTENSIONS.get(key).equals(extension)) {
							type = key;
						}
					}
				}
			}
			return NdefFactory.fromMime(type, bytes);
		}

		return shared;
	}

	@Override
	public int getPriority() {
		return PRIORITY;
	}
}
