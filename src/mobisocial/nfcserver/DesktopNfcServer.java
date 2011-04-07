/*
 * Copyright (C) 2011 Stanford University MobiSocial Lab
 * http://mobisocial.stanford.edu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mobisocial.nfcserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.codec.binary.Base64;

import mobisocial.nfc.NdefHandler;
import mobisocial.nfc.NfcInterface;
import mobisocial.nfc.PrioritizedHandler;
import mobisocial.nfc.ShareHandler;
import mobisocial.nfcserver.handler.AppManifestHandler;
import mobisocial.nfcserver.handler.EchoNdefHandler;
import mobisocial.nfcserver.handler.HttpFileHandler;
import mobisocial.nfcserver.handler.HttpUrlHandler;
import mobisocial.nfcserver.handler.LogNdefHandler;
import mobisocial.nfcserver.handler.MimeTypeHandler;
import mobisocial.nfcserver.handler.share.LocalFileToMime;
import mobisocial.nfcserver.handler.share.NdefToNfc;
import mobisocial.nfcserver.handler.share.ParseLine;
import mobisocial.nfcserver.handler.share.TextToNdef;
import mobisocial.nfcserver.handler.share.UriToNdef;
import mobisocial.nfcserver.mockdevice.BluetoothNdefServer;
import mobisocial.nfcserver.mockdevice.TcpNdefServer;
import mobisocial.util.QR;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

public class DesktopNfcServer implements NfcInterface {
	public static DesktopNfcServer sInstance;
	private final Map<Integer, Set<NdefHandler>> mNdefHandlers = new TreeMap<Integer, Set<NdefHandler>>();
	private final Map<Integer, Set<ShareHandler>> mShareHandlers = new TreeMap<Integer, Set<ShareHandler>>();
	private NdefMessage mForegroundNdef = null;
	private boolean ECHO_NDEF = true;

	public interface Contract {
		public void start();
		public void stop();
		public String getHandoverUrl();
	}
	
	private static final String TAG = "nfcserver";
	static DesktopNfcServer sNdefProxy;

	public static void main(String[] args) {
		try {
			Contract server;
			if (args.length > 0 && args[0].equals("tcp")) { // recoverable failure; if args is null, length == 0.
				server = new TcpNdefServer(args);
			} else {
				server = new BluetoothNdefServer(args); //Builder<BluetoothNdefServer>().build();
			}
			String content = "ndefb://" + Base64.encodeBase64URLSafeString(getHandoverNdef(server.getHandoverUrl()).toByteArray());
			System.out.println("Welcome to DesktopNfc!");
			System.out.println("Service running on " + server.getHandoverUrl());
			System.out.println("Your configuration QR is: " + QR.getQrl(content));
			server.start();

			NfcInterface nfc = getInstance();
			final String PROMPT = "> ";
			BufferedReader lineReader = new BufferedReader(new InputStreamReader(System.in));
			while (true) {
				System.out.print(PROMPT);
				String line = lineReader.readLine().trim();
				nfc.share(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private DesktopNfcServer() {
		addDefaultNdefHandlers();
	}
	
	public static NfcInterface getInstance() {
		if (sInstance == null) {
			sInstance = new DesktopNfcServer();
		}
		return sInstance;
	}

	public void addNdefHandler(NdefHandler handler) {
		if (handler instanceof PrioritizedHandler) {
			addNdefHandler(((PrioritizedHandler)handler).getPriority(), handler);
		} else {
			addNdefHandler(PrioritizedHandler.DEFAULT_PRIORITY, handler);
		}
	}

	public synchronized void addNdefHandler(Integer priority, NdefHandler handler) {
		if (!mNdefHandlers.containsKey(priority)) {
			mNdefHandlers.put(priority, new LinkedHashSet<NdefHandler>());
		}
		Set<NdefHandler> handlers = mNdefHandlers.get(priority);
		handlers.add(handler);
	}

	public void addShareHandler(ShareHandler handler) {
		if (handler instanceof PrioritizedHandler) {
			addShareHandler(((PrioritizedHandler)handler).getPriority(), handler);
		} else {
			addShareHandler(PrioritizedHandler.DEFAULT_PRIORITY, handler);
		}
	}

	public synchronized void addShareHandler(Integer priority, ShareHandler handler) {
		if (!mShareHandlers.containsKey(priority)) {
			mShareHandlers.put(priority, new LinkedHashSet<ShareHandler>());
		}
		Set<ShareHandler> handlers = mShareHandlers.get(priority);
		handlers.add(handler);
	}
	
	public synchronized void handleNdef(NdefMessage[] ndef) {
		Iterator<Integer> bins = mNdefHandlers.keySet().iterator();
		while (bins.hasNext()) {
			Integer priority = bins.next();
			Iterator<NdefHandler> handlers = mNdefHandlers.get(priority).iterator();
			while (handlers.hasNext()) {
				NdefHandler handler = handlers.next();
				if (handler.handleNdef(ndef) == NdefHandler.NDEF_CONSUME) {
					return;
				}
			}
		}
	}

	@Override
	public void setForegroundNdefMessage(final NdefMessage ndef) { /* ImutableNdef */
		mForegroundNdef = ndef;
	}
	
	public void share(Object shared) {
		Iterator<Integer> bins = mShareHandlers.keySet().iterator();
		while (bins.hasNext()) {
			Integer priority = bins.next();
			Iterator<ShareHandler> handlers = mShareHandlers.get(priority).iterator();
			while (handlers.hasNext()) {
				ShareHandler handler = handlers.next();
				shared = handler.handleShare(shared);
			}
		}
	}

	@Override
	public NdefMessage getForegroundNdefMessage() {
		return mForegroundNdef;
	}
	
	private static NdefMessage getHandoverNdef(String ref) {
		NdefRecord[] records = new NdefRecord[3];
		
		/* Handover Request */
		byte[] version = new byte[] { (0x1 << 4) | (0x2) };
		records[0] = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
				NdefRecord.RTD_HANDOVER_REQUEST, new byte[0], version);

		/* Collision Resolution */
		records[1] = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, new byte[] {
				0x63, 0x72 }, new byte[0], new byte[] {0x0, 0x0});

		/* Handover record */
		records[2] = new NdefRecord(NdefRecord.TNF_ABSOLUTE_URI,
				NdefRecord.RTD_URI, new byte[0], ref.getBytes());
		
		return new NdefMessage(records);
	}
	
	private void addDefaultNdefHandlers() {
		addNdefHandler(new LogNdefHandler());
		addNdefHandler(new MimeTypeHandler());
		addNdefHandler(new HttpUrlHandler());
		addNdefHandler(new HttpFileHandler());
		addNdefHandler(new AppManifestHandler());
		if (ECHO_NDEF) {
			addNdefHandler(new EchoNdefHandler(this));		
		}
		
		addShareHandler(new ParseLine());
		addShareHandler(new TextToNdef());
		addShareHandler(new UriToNdef());
		addShareHandler(new LocalFileToMime());
		addShareHandler(new NdefToNfc(this));
	}
}
