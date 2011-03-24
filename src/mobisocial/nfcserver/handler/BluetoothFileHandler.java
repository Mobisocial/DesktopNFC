package mobisocial.nfcserver.handler;

import java.net.URI;

import com.android.apps.tag.record.UriRecord;

import mobisocial.nfc.NdefHandler;
import mobisocial.util.Log;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

/**
 * Downloads a file over HTTP served over Bluetooth.
 *
 */
public class BluetoothFileHandler implements NdefHandler {
	public static final String TAG = "ndefserver";
	
	public int handleNdef(NdefMessage[] ndefMessages) {
		// UriRecord bluetooth+http://...
		// TODO, do we need to do a true connection handover to
		// get the socket listening? Or can we just have it listen?
		// plz???
		return NDEF_PROPAGATE;
	}
}
