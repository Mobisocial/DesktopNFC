package mobisocial.nfcserver.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import mobisocial.nfcserver.DesktopNfcServer;
import mobisocial.util.Log;

/**
 * Emulates an Nfc device, accepting an NDEF message
 * to trigger an application invocation.
 *
 */
public class TcpNdefServer implements DesktopNfcServer.Contract {
	private static final int SERVER_PORT = 7924;
	private static String TAG = "nfcserver";
	private AcceptThread mAcceptThread;
	private final String mHandoverUrl;
	
	public TcpNdefServer(String... args) {
		mHandoverUrl = "ndef+tcp://" + getLocalIpAddress() + ":" + SERVER_PORT;
	}

	public static void main(String[] args) {		
		TcpNdefServer server = new TcpNdefServer();
		server.start();
	}
	
	/**
	 * Starts the simple file server
	 */
	public void start() {
		if (mAcceptThread != null) return;
		
		if (mHandoverUrl == null) {
			System.err.println("Error starting server");
			return;
		}
		
		mAcceptThread = new AcceptThread();
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
        private final ServerSocket mmServerSocket;

        public AcceptThread() {
            ServerSocket tmp = null;
            
            // Create a new listening server socket
            try {
                tmp = new ServerSocket(SERVER_PORT);
            } catch (IOException e) {
                System.err.println("Could not open server socket");
                e.printStackTrace(System.err);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            //Log.d(TAG, "BEGIN mAcceptThread" + this);
            setName("AcceptThread");
            Socket socket = null;

            // Listen to the server socket always
            while (true) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                	Log.d(TAG, "waiting for client...");
                    socket = mmServerSocket.accept();
                    Log.d(TAG, "Client connected!");
                } catch (SocketException e) {

                } catch (IOException e) {
                    Log.e(TAG, "accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket == null) {
                	break;
                }
                
                new HandoverConnectedThread(new TcpDuplexSocket(socket), DesktopNfcServer.getInstance()).start();
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
	
	class TcpDuplexSocket implements HandoverConnectedThread.DuplexSocket {
		private final Socket mmSocket;
		private InputStream mmInputStream;
		private OutputStream mmOutputStream;
		public TcpDuplexSocket(Socket socket) {
			mmSocket = socket;
		}
		
		//@Override
		public void close() throws IOException {
			mmSocket.close();
		}

		//@Override
		public InputStream getInputStream() throws IOException {
			return mmInputStream;
		}

		//@Override
		public OutputStream getOutputStream() throws IOException {
			return mmOutputStream;
		}

		//@Override
		public void connect() throws IOException {
			mmInputStream = mmSocket.getInputStream();
			mmOutputStream = mmSocket.getOutputStream();
		}
		
	}
    
	public static String getLocalIpAddress() {
	    try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress()) {
	                	// not ready for IPv6, apparently.
	                	if (!inetAddress.getHostAddress().contains(":")) {
	                		return inetAddress.getHostAddress().toString();
	                	}
	                }
	            }
	        }
	    } catch (SocketException ex) {

	    }
	    return null;
	}

	//@Override
	public String getHandoverUrl() {
		return mHandoverUrl;
	}
}