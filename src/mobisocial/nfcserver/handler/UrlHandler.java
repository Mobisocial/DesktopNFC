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
		try {
			String page = null;
			NdefRecord firstRecord = ndefMessages[0].getRecords()[0];
			if (firstRecord.getTnf() == NdefRecord.TNF_ABSOLUTE_URI) {
				page = new String(firstRecord.getPayload());
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
}
