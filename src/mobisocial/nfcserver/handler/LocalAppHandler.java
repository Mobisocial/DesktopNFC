package mobisocial.nfcserver.handler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import mobisocial.nfc.NdefHandler;
import mobisocial.nfc.NfcInterface;
import mobisocial.util.Log;
import android.nfc.NdefMessage;

/**
 * Provides access to NFC messaging for applications
 * over a local HTTP port. Any interested local client
 * can send and receive NDEF messages, and messages may
 * be sent to more than one application.
 *
 */
public class LocalAppHandler implements NdefHandler {
	public static final String TAG = "ndefserver";
	public static final int LOCAL_APP_PORT = 8923;
	
	private NfcInterface mNfcInterface;
	private AcceptThread mAcceptThread;
	private static LocalAppHandler sInstance;
	
	public int handleNdef(NdefMessage[] ndefMessages) {
		// TODO
		// if (socket connected)
			// send message to listening client
			// return NDEF_CONSUME
		return NDEF_PROPAGATE;
	}
	
	private LocalAppHandler(NfcInterface nfcInterface) {
		if (mAcceptThread == null) {
			mNfcInterface = nfcInterface;
			
			mAcceptThread = new AcceptThread();
			mAcceptThread.start();
		}
	}
	
	public LocalAppHandler getInstance(NfcInterface nfcInterface) {
		if (sInstance == null) {
			sInstance = new LocalAppHandler(nfcInterface);
		}
		return sInstance;
	}
	
	
	/**
	 * TODO
	 * 
	 * On inbound NDEF, trigger mNfcInterface.handleNdef(ndef)
	 * or mNfcInterface.setForegroundNdefMessage(ndef);
	 *
	 */
	private class AcceptThread extends Thread {
        // The local server socket
        private final ServerSocket mmServerSocket;

        public AcceptThread() {
            ServerSocket tmp = null;
            
            // Create a new listening server socket
            try {
                tmp = new ServerSocket(LOCAL_APP_PORT);
            } catch (IOException e) {
                System.err.println("Could not open server socket");
                e.printStackTrace(System.err);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            setName("AcceptThread");
            Socket socket = null;

            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (SocketException e) {

                } catch (IOException e) {
                    Log.e(TAG, "accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket == null) {
                	break;
                }
                
                
            }
        }

        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of server failed", e);
            }
        }
    }
}
