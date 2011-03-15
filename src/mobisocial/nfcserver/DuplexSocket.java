package mobisocial.nfcserver;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A wrapper for the standard Socket implementation.
 *
 */
interface DuplexSocket extends Closeable {
	public InputStream getInputStream() throws IOException;
	public OutputStream getOutputStream() throws IOException;
	public void connect() throws IOException;
}