package routingx.utils;

import java.io.Closeable;
import java.io.IOException;

public class IOUtils extends org.apache.commons.io.IOUtils {

	public static void close(final Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (final IOException ioe) {
			// ignore
		}
	}

	public static void close(final Closeable... closeables) {
		if (closeables == null) {
			return;
		}
		for (final Closeable closeable : closeables) {
			close(closeable);
		}
	}
}
