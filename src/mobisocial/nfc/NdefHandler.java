package mobisocial.nfc;

import android.nfc.NdefMessage;

/**
 * A callback issued when an Nfc tag is read.
 */
public interface NdefHandler {
	public static final int NDEF_PROPAGATE = 0;
	public static final int NDEF_CONSUME = 1;
	public static final int DEFAULT_PRIORITY = 50;

	/**
	 * Callback issued after an NFC tag is read or an NDEF message is received
	 * from a remote device. This method is executed off the main thread, so be
	 * careful when updating UI elements as a result of this callback.
	 * 
	 * @return {@link #NDEF_CONSUME} to indicate this handler has consumed the
	 *         given message, or {@link #NDEF_PROPAGATE} to pass on to the next
	 *         handler.
	 */
	public abstract int handleNdef(NdefMessage[] ndefMessages);
}
