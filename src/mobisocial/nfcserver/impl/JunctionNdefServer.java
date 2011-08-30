package mobisocial.nfcserver.impl;

import java.net.URI;
import java.net.URLEncoder;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;

import android.nfc.FormatException;
import android.nfc.NdefMessage;

import edu.stanford.junction.Junction;
import edu.stanford.junction.JunctionException;
import edu.stanford.junction.JunctionMaker;
import edu.stanford.junction.SwitchboardConfig;
import edu.stanford.junction.api.activity.JunctionActor;
import edu.stanford.junction.api.messaging.MessageHeader;
import edu.stanford.junction.provider.xmpp.XMPPSwitchboardConfig;

import mobisocial.nfcserver.DesktopNfcServer;
import mobisocial.util.Log;

/**
 * Emulates an Nfc device, accepting an NDEF message to trigger an application
 * invocation.
 * 
 */
public class JunctionNdefServer implements DesktopNfcServer.Contract {
	private static String TAG = "ndefjunctionserver";
	private AcceptThread mAcceptThread;
	private final String mHandoverUrl;
	private final URI mJunctionUri;
	private final SwitchboardConfig DEFAULT_SWITCHBOARD = new XMPPSwitchboardConfig("prpl.stanford.edu");

	public JunctionNdefServer(String... args) {
		if (args.length > 2) {
			mJunctionUri = URI.create(args[1]);
		} else {
			//mJunctionUri = JunctionMaker.getInstance(DEFAULT_SWITCHBOARD).generateSessionUri();
			mJunctionUri = URI.create("junction://prpl.stanford.edu/ndef");
		}
		mHandoverUrl = "ndef+junction://"
				+ URLEncoder.encode(mJunctionUri.toString());
	}

	public static void main(String[] args) {
		JunctionNdefServer server = new JunctionNdefServer();
		server.start();
	}

	/**
	 * Starts the simple file server
	 */
	public void start() {
		if (mAcceptThread != null)
			return;

		if (mHandoverUrl == null) {
			System.err.println("Error starting server");
			return;
		}

		mAcceptThread = new AcceptThread(mJunctionUri);
		mAcceptThread.start();
	}

	public void stop() {
		if (mAcceptThread != null) {
			mAcceptThread.cancel();
			mAcceptThread = null;
		}
	}

	private class AcceptThread extends Thread {
		// The local server socket
		private Junction mmJunction;

		private JunctionActor mmActor = new JunctionActor() {

			@Override
			public void onMessageReceived(MessageHeader header,
					JSONObject message) {
				if (header.from.equals(getActorID())) {
					return;
				}
				
				final NdefMessage received;
				NdefMessage tmp = null;
				try {
					tmp = new NdefMessage(Base64.decodeBase64(message.optString("ndef")));
				} catch (FormatException e) {
					Log.e(TAG, "Received message not ndef");
				}
				received = tmp;

				sendForegroundNdef();
				new Thread() {
					public void run() {
						DesktopNfcServer.getInstance().handleNdef(new NdefMessage[] { received });
					};
				}.start();
			}
			
			public void onActivityJoin() {
				sendForegroundNdef();
			};
			
			private void sendForegroundNdef() {
				JSONObject ndef = new JSONObject();
				// TODO: Make accessible as member.
				try {
					NdefMessage fore = DesktopNfcServer.getInstance().getForegroundNdefMessage();
					if (fore != null) {
						ndef.put("ndef", Base64.encodeBase64String(
								fore.toByteArray()));						
					}

					sendMessageToSession(ndef);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		public AcceptThread(URI uri) {

		}

		public void run() {
			Junction tmp = null;

			// Create a new listening server socket
			try {
				tmp = JunctionMaker.bind(mJunctionUri, mmActor);
			} catch (JunctionException e) {
				System.err.println("Could not open server socket");
				e.printStackTrace(System.err);
			}
			mmJunction = tmp;
			synchronized (mmJunction) {
				try {
					mmJunction.wait();
				} catch (InterruptedException e) {
				}
			}
		}

		public void cancel() {
			mmActor.leave();
		}
	}

	// @Override
	public String getHandoverUrl() {
		return mHandoverUrl;
	}
}