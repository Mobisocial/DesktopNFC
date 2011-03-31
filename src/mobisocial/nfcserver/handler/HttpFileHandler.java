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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.android.apps.tag.record.UriRecord;

import mobisocial.nfc.NdefHandler;
import mobisocial.nfc.PrioritizedHandler;
import mobisocial.util.Log;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

public class HttpFileHandler implements NdefHandler, PrioritizedHandler{
	public static final String TAG = "ndefserver";
	
	public int handleNdef(NdefMessage[] ndefMessages) {
		URI page = null;
		NdefRecord firstRecord = ndefMessages[0].getRecords()[0];
		if (UriRecord.isUri(firstRecord)) {
			page = UriRecord.parse(firstRecord).getUri();
		}

		try {	
			if (page != null && (page.getScheme().startsWith("http"))) {
				System.out.println("trying to get " + page);
				if (page.getPath() == null || !page.toString().contains(".")) {
					return NDEF_PROPAGATE;
				}
				
				try {
					String extension = page.toString().substring(page.toString().lastIndexOf(".") + 1);
					if (!sSupportedExtensions.contains(extension)) {
						return NDEF_PROPAGATE;
					}
					
					// Download content
					System.out.println("Downloading " + extension + " file.");
					HttpGet httpGet = new HttpGet(page);
					HttpClient httpClient = new DefaultHttpClient();
					HttpResponse httpResponse = httpClient.execute(httpGet);
					HttpEntity entity = httpResponse.getEntity();
					InputStream content = entity.getContent();
					
					File fileOut = new File("nfcfiles/" + System.currentTimeMillis() + "." + extension);
					fileOut.getParentFile().mkdirs();
					FileOutputStream fileOutStream = new FileOutputStream(fileOut);
					BufferedOutputStream buffered = new BufferedOutputStream(fileOutStream);
					byte[] buf = new byte[1024];
					while (true) {
						int r = content.read(buf);
						if (r <= 0) break;
						buffered.write(buf);
					}
					buffered.close();
					fileOutStream.close();
					System.out.println("Opening file " + fileOut.getAbsolutePath() + ".");
					if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
						Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL "+ fileOut.getAbsolutePath());
					} else {
						java.awt.Desktop.getDesktop().open(fileOut);
					}
					return NDEF_CONSUME;
				} catch (Exception e) {
					e.printStackTrace();
					return NDEF_PROPAGATE;
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "Error launching page", e);
		}
		return NDEF_PROPAGATE;
	}
	
	public static final Set<String> sSupportedExtensions = new LinkedHashSet<String>();
	static {
		sSupportedExtensions.add("m3u");
		sSupportedExtensions.add("mp3");
	}

	@Override
	public int getPriority() {
		return HttpUrlHandler.HTTP_URL_PRIORITY - 1;
	}
}
