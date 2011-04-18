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

package mobisocial.nfcserver.handler;

import java.io.IOError;
import java.io.IOException;
import java.net.URI;

import com.android.apps.tag.record.UriRecord;

import mobisocial.nfc.NdefHandler;
import mobisocial.nfc.PrioritizedHandler;
import mobisocial.util.Log;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

public class HttpUrlHandler implements NdefHandler, PrioritizedHandler{
	public static final String TAG = "ndefserver";
	public static final int HTTP_URL_PRIORITY = 40;
	
	public int handleNdef(NdefMessage[] ndefMessages) {
		URI page = null;
		NdefRecord firstRecord = ndefMessages[0].getRecords()[0];
		if (UriRecord.isUri(firstRecord)) {
			page = UriRecord.parse(firstRecord).getUri();
		}

		try {	
			if (page != null && (page.getScheme().startsWith("http"))) {
				System.out.println("Opening page " + page);
				openWebpage(page);
				return NDEF_CONSUME;
			}
		} catch (Exception e) {
			Log.e(TAG, "Error launching page", e);
		}
		return NDEF_PROPAGATE;
	}

	//@Override
	public int getPriority() {
		return HTTP_URL_PRIORITY ;
	}

	public static final void openWebpage(URI page) throws IOException {
		String osName = System.getProperty("os.name");
		if (osName.startsWith("Mac OS")) {
			try {
	        Class.forName("com.apple.eio.FileManager").getDeclaredMethod(
	                "openURL", new Class[] {String.class}).invoke(null,
	                new Object[] {page.toString()});
	         } catch (Exception e) {
	        	 System.err.println("Error opening file on mac.");
	        	 e.printStackTrace();
	         }
		} else {
			java.awt.Desktop.getDesktop().browse(page);
		}
	}
}
