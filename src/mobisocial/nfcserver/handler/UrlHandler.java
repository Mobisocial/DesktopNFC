package mobisocial.nfcserver.handler;

import java.util.List;

import mobisocial.nfc.NdefHandler;
import mobisocial.util.Log;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import edu.stanford.mobisocial.appmanifest.ApplicationManifest;
import edu.stanford.mobisocial.appmanifest.platforms.PlatformReference;

public class UrlHandler implements NdefHandler {
	public static final String TAG = "ndefserver";
	public int handleNdef(NdefMessage[] ndefMessages) {
		NdefMessage ndef = ndefMessages[0];
		try {
			/**
			 * TODO TODO TODO
			 * NdefHandler. Copy paradigm here. Example NdefHandlers:
			 * <li>LaunchBrowser
			 * <li>LaunchApp
			 * <li>SendToBrowserTab
			 * <li>Bt2Http
			 *
			 */
			String page = null;
			byte[] manifestBytes = ndef.getRecords()[0].getPayload();
			NdefRecord firstRecord = ndef.getRecords()[0];
			
			if (firstRecord.getTnf() == NdefRecord.TNF_ABSOLUTE_URI) {
				page = new String(firstRecord.getPayload());
			} else if (firstRecord.getTnf() == NdefRecord.TNF_MIME_MEDIA) {
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
			} else {
				Log.d(TAG, "Could not serve webpage from manifest");
				return NDEF_PROPAGATE;
			}
		} catch (Exception e) {
			Log.e(TAG, "Error launching page", e);
		}
		return NDEF_PROPAGATE;
	}
}
