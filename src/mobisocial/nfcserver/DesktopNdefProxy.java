package mobisocial.nfcserver;

import java.util.List;

import edu.stanford.mobisocial.appmanifest.ApplicationManifest;
import edu.stanford.mobisocial.appmanifest.platforms.PlatformReference;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

public class DesktopNdefProxy implements NdefProxy {
	private static final String TAG = "nfcserver";
	static DesktopNdefProxy sNdefProxy;

	@Override
	public void handleNdef(NdefMessage ndef) {
		try {
			String page = null;
        	byte[] manifestBytes = ndef.getRecords()[0].getPayload();
        	NdefRecord firstRecord = ndef.getRecords()[0];
        	System.out.println("got TNF " + firstRecord.getTnf());
        	
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
        	} else {
        		System.out.println("Could not serve webpage from manifest");
        	}
        } catch (Exception e) {
        	Log.e(TAG, "Error reading connection header", e);
        }
	}

	@Override
	public NdefMessage getForegroundNdefMessage() {
		return null; // TODO: fix UnsatisfiedLinkError to support NDEF push.
	}

	private DesktopNdefProxy() {}
	
	public static DesktopNdefProxy getInstance() {
		if (sNdefProxy == null) {
			sNdefProxy = new DesktopNdefProxy();
		}
		return sNdefProxy;
	}
}
