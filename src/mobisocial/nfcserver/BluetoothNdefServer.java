package mobisocial.nfcserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import javax.bluetooth.LocalDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;


/**
 * Emulates an Nfc device, accepting an NDEF message
 * over Bluetooth to trigger an application invocation.
 *
 */
public class BluetoothNdefServer {
	final LocalDevice mLocalDevice;
	final String mServiceUrl;
	final AcceptThread mAcceptThread;
	
	public static final UUID SERVICE_UUID = UUID.fromString("8ceaa480-4dfc-11e0-b8af-0800200c9a66");

	public static void main(String[] args) {
		try {
			BluetoothNdefServer server = new BluetoothNdefServer();
			String serverRef = "ndef+bluetooth://" + server.getLocalAddress() + "/" + SERVICE_UUID;
			System.out.println("Server running on " + serverRef);
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public BluetoothNdefServer() throws IOException {
		mLocalDevice = LocalDevice.getLocalDevice();
		StringBuilder serviceUrlBuilder = new StringBuilder();
		serviceUrlBuilder.append("btspp://localhost:" + SERVICE_UUID.toString().replace("-", ""));
		serviceUrlBuilder.append(";name=NFCHandover;encrypt=false;authenticate=false");
		mServiceUrl = serviceUrlBuilder.toString();
		mAcceptThread = new AcceptThread();
	}
	
	public void start() {
		mAcceptThread.start();
	}
	
	public void stop() {
		
	}
	
	private String getLocalAddress() {
		String addr = mLocalDevice.getBluetoothAddress();
		StringBuilder addrBuilder = new StringBuilder();
		for (int i = 0; i < addr.length(); i += 2) {
			addrBuilder.append(addr.substring(i, i + 2) + ":");
		}
		return addrBuilder.substring(0, addrBuilder.length() - 1);
	}
	
	class AcceptThread extends Thread {
		private boolean mmRunning = true;
		@Override
		public void run() {
			while (mmRunning) {
				try {
					StreamConnectionNotifier scn = (StreamConnectionNotifier) Connector.open(mServiceUrl);
					StreamConnection conn = scn.acceptAndOpen();
					new HandoverConnectedThread(new StreamConnectionSocket(conn), DesktopNdefProxy.getInstance()).start();
					scn.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	class StreamConnectionSocket implements DuplexSocket {
		private StreamConnection mmStreamConnection;
		private InputStream mmInputStream;
		private OutputStream mmOutputStream;
		
		public StreamConnectionSocket(StreamConnection conn) {
			mmStreamConnection = conn;
		}
		
		@Override
		public void close() throws IOException {
			mmStreamConnection.close();
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return mmInputStream;
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			return mmOutputStream;
		}

		@Override
		public void connect() throws IOException {
			mmInputStream = mmStreamConnection.openInputStream();
			mmOutputStream = mmStreamConnection.openOutputStream();
		}
	}
}