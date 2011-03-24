package mobisocial.nfcserver.handler;

import java.net.URI;

import com.android.apps.tag.record.UriRecord;

import mobisocial.nfc.NdefHandler;
import mobisocial.util.Log;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

public class WebUrlHandler implements NdefHandler {
	public static final String TAG = "ndefserver";
	
	public int handleNdef(NdefMessage[] ndefMessages) {
		URI page = null;
		NdefRecord firstRecord = ndefMessages[0].getRecords()[0];
		if (UriRecord.isUri(firstRecord)) {
			page = UriRecord.parse(firstRecord).getUri();
		}

		try {	
			if (page != null && (page.getScheme().startsWith("http"))) {
				java.awt.Desktop.getDesktop().browse(page);
				return NDEF_CONSUME;
			}
		} catch (Exception e) {
			Log.e(TAG, "Error launching page", e);
		}
		return NDEF_PROPAGATE;
	}
}
