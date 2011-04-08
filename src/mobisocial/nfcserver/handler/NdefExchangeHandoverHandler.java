package mobisocial.nfcserver.handler;

import mobisocial.nfc.NdefFactory;
import mobisocial.nfc.NdefHandler;
import mobisocial.nfc.NfcInterface;
import mobisocial.nfc.PrioritizedHandler;
import mobisocial.nfc.ShareHandler;
import mobisocial.nfc.ndefexchange.ConnectionHandoverManager;
import mobisocial.nfc.ndefexchange.PendingNdefExchange;
import mobisocial.nfcserver.DesktopNfcServer;
import android.nfc.NdefMessage;

public class NdefExchangeHandoverHandler implements NdefHandler, PrioritizedHandler, ShareHandler {
	public static final int PRIORITY = 5;
	public static final String TAG = "ndefserver";
	private PendingNdefExchange mPendingNdefExchange;
	private final NfcInterface mNfcInterface;
	
	public static void prepare(DesktopNfcServer nfcInterface) {
		NdefExchangeHandoverHandler h = new NdefExchangeHandoverHandler(nfcInterface);
		nfcInterface.addNdefHandler(PRIORITY, h);
		nfcInterface.addShareHandler(PRIORITY, h);
	}
	
	private NdefExchangeHandoverHandler(NfcInterface nfcInterface) {
		mNfcInterface = nfcInterface;
	}

	// From the wire.
	@Override
	public int handleNdef(NdefMessage[] ndefMessages) {
		NdefMessage ndef = ndefMessages[0];
		
		if (mPendingNdefExchange != null) {
			mPendingNdefExchange.exchangeNdef(ndef);
			return NDEF_CONSUME;
		}
		
		if (ConnectionHandoverManager.isHandoverRequest(ndef)) {
			mPendingNdefExchange = new PendingNdefExchange(ndef, mNfcInterface);
			System.out.println("Prepared ndef exchange handover!");
			System.out.println("Hit <enter> to clear the handover.");
			return NDEF_CONSUME;
		}
		return NDEF_PROPAGATE;
	}
	@Override
	public int getPriority() {
		return PRIORITY;
	}


	// From the user.
	@Override
	public Object handleShare(Object shared) {
		if (shared == null ||
				(shared instanceof NdefMessage && NdefFactory.isEmpty((NdefMessage)shared))) {
			System.out.println("Cleared ndef exchange handover.");
			mPendingNdefExchange = null;
			return null;
		}
		return shared;
	}
}
