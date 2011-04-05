package mobisocial.nfc;

import java.net.URI;
import java.net.URL;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

/**
 * A utility class for generating NDEF messages.
 * @see NdefMessage
 */
public class NdefFactory {
	public static NdefMessage fromUri(URI uri) {
		try {
			NdefRecord record = new NdefRecord(NdefRecord.TNF_ABSOLUTE_URI, NdefRecord.RTD_URI, 
					new byte[0], uri.toString().getBytes());
			NdefRecord[] records = new NdefRecord[] { record };
			return new NdefMessage(records);
		} catch (NoClassDefFoundError e) {
			return null;
		}
	}
	
	public static NdefMessage fromUrl(URL url) {
		try {
			NdefRecord record = new NdefRecord(NdefRecord.TNF_ABSOLUTE_URI,
					NdefRecord.RTD_URI, new byte[0], url.toString().getBytes());
			NdefRecord[] records = new NdefRecord[] { record };
			return new NdefMessage(records);
		} catch (NoClassDefFoundError e) {
			return null;
		}
	}

	public static NdefMessage fromUri(String uri) {
		try {
			NdefRecord record = new NdefRecord(NdefRecord.TNF_ABSOLUTE_URI, NdefRecord.RTD_URI, 
					new byte[0], uri.getBytes());
			NdefRecord[] records = new NdefRecord[] { record };
			return new NdefMessage(records);
		} catch (NoClassDefFoundError e) {
			return null;
		}
	}
	
	public static NdefMessage fromMime(String mimeType, byte[] data) {
		try {
			NdefRecord record = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
					mimeType.getBytes(), new byte[0], data);
			NdefRecord[] records = new NdefRecord[] { record };
			return new NdefMessage(records);
		} catch (NoClassDefFoundError e) {
			return null;
		}
	}
	
	/**
	 * Creates an NDEF message with a single text record, with language
	 * code "en" and the given text, encoded using UTF-8.
	 */
	public static NdefMessage fromText(String text) {
		try {
			byte[] textBytes = text.getBytes();
			byte[] textPayload = new byte[textBytes.length + 3];
			textPayload[0] = 0x02; // Status byte; UTF-8 and "en" encoding.
			textPayload[1] = 'e';
			textPayload[2] = 'n';
			System.arraycopy(textBytes, 0, textPayload, 3, textBytes.length);
			NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
					NdefRecord.RTD_TEXT, new byte[0], textPayload);
			NdefRecord[] records = new NdefRecord[] { record };
			return new NdefMessage(records);
		} catch (NoClassDefFoundError e) {
			return null;
		}
	}
	
	/**
	 * Creates an NDEF message with a single text record, with the given
	 * text content (UTF-8 encoded) and language code. 
	 */
	public static NdefMessage fromText(String text, String languageCode) {
		try {
			int languageCodeLength = languageCode.length();
			int textLength = text.length();
			byte[] textPayload = new byte[textLength + 1 + languageCodeLength];
			textPayload[0] = (byte)(0x3F & languageCodeLength); // UTF-8 with the given language code length.
			System.arraycopy(languageCode.getBytes(), 0, textPayload, 1, languageCodeLength);
			System.arraycopy(text.getBytes(), 0, textPayload, 1 + languageCodeLength, textLength);
			NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
					NdefRecord.RTD_TEXT, new byte[0], textPayload);
			NdefRecord[] records = new NdefRecord[] { record };
			return new NdefMessage(records);
		} catch (NoClassDefFoundError e) {
			return null;
		}
	}
}