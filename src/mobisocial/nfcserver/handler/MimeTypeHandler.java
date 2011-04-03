package mobisocial.nfcserver.handler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import mobisocial.nfc.NdefHandler;
import mobisocial.nfc.PrioritizedHandler;
import mobisocial.util.Log;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

public class MimeTypeHandler implements NdefHandler, PrioritizedHandler {
	public static final int PRIORITY = 20;
	public static final String TAG = "ndefserver";
	public int handleNdef(NdefMessage[] ndefMessages) {
		NdefMessage ndef = ndefMessages[0];
		try {
			byte[] manifestBytes = ndef.getRecords()[0].getPayload();
			NdefRecord firstRecord = ndef.getRecords()[0];
			if (firstRecord.getTnf() == NdefRecord.TNF_MIME_MEDIA) {
				String extension = getExtension(new String(firstRecord.getType()));
				File fileOut = new File("nfcfiles/" + System.currentTimeMillis() + "." + extension);
				fileOut.getParentFile().mkdirs();
				FileOutputStream fileOutStream = new FileOutputStream(fileOut);
				BufferedOutputStream buffered = new BufferedOutputStream(fileOutStream);
				buffered.write(manifestBytes);
				buffered.close();
				fileOutStream.close();
				
				openFile(fileOut);
				return NDEF_CONSUME;
			}
		} catch (Exception e) {
			Log.e(TAG, "Error launching page", e);
		}
		return NDEF_PROPAGATE;
	}
	@Override
	public int getPriority() {
		return PRIORITY;
	}
	
	public static void openFile(File file) throws IOException {
		System.out.println("Opening file " + file.getAbsolutePath() + ".");
		if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
			Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL "+ file.getAbsolutePath());
		} else {
			java.awt.Desktop.getDesktop().open(file);
		}
	}
	
	public static String getExtension(String type) {
		if (MIME_EXTENSIONS.containsKey(type)) {
			return MIME_EXTENSIONS.get(type);
		}

		System.err.println("Unknown mime type " + type + ".");
		return "tmp";
	}
	
	public static final Map<String, String> MIME_EXTENSIONS = 
		ImmutableMap.<String,String>builder()
		.put("audio/mpegurl", "m3u")
		.put("audio/x-mpegurl", "m3u")
		.build();
}
