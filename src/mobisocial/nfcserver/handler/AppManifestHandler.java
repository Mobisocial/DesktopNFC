package mobisocial.nfcserver.handler;

import java.util.List;

import mobisocial.nfc.NdefHandler;
import mobisocial.nfc.PrioritizedHandler;
import mobisocial.util.Log;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import edu.stanford.mobisocial.appmanifest.ApplicationManifest;
import edu.stanford.mobisocial.appmanifest.platforms.PlatformReference;

public class AppManifestHandler implements NdefHandler, PrioritizedHandler {
	public static final String TAG = "ndefserver";
	public int handleNdef(NdefMessage[] ndefMessages) {
		NdefMessage ndef = ndefMessages[0];
		try {
			String page = null;
			byte[] manifestBytes = ndef.getRecords()[0].getPayload();
			NdefRecord firstRecord = ndef.getRecords()[0];
			if (firstRecord.getTnf() == NdefRecord.TNF_MIME_MEDIA) {
				String mimeType = new String(firstRecord.getType());
				if (!ApplicationManifest.MIME_TYPE.equals(mimeType)) {
					return NDEF_PROPAGATE;
				}

		    	ApplicationManifest manifest = new ApplicationManifest(manifestBytes);
		    	List<PlatformReference> platforms = manifest.getPlatformReferences();
		    	for (PlatformReference platform : platforms) {
		    		if (platform.getPlatformIdentifier() == ApplicationManifest.PLATFORM_WEB_GET) {
		    			page = new String(platform.getAppReference());
		    		}
		    	}
			}
			
			if (page != null) {
				java.awt.Desktop.getDesktop().browse(java.net.URI.create(page));
				return NDEF_CONSUME;
			}
		} catch (Exception e) {
			Log.e(TAG, "Error launching page", e);
		}
		return NDEF_PROPAGATE;
	}

	//@Override
	public int getPriority() {
		return MimeTypeHandler.PRIORITY - 1;
	}
}
