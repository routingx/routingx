package routingx.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.commons.codec.binary.Hex;

import lombok.extern.slf4j.Slf4j;
import routingx.Note;

@Slf4j
public class ZipUtils {

	public static final int BUFFER_SIZE = 4096;

	@Note("GZIP压缩")
	public static String compress(String src) throws IOException {
		if (null == src || src.length() <= 0) {
			return null;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(out);
		gzip.write(src.getBytes(StandardCharsets.UTF_8));
		gzip.close();
		return Hex.encodeHexString(out.toByteArray());
		// return Base64Utils.encodeToString(out.toByteArray());
		// return out.toString(StandardCharsets.UTF_8);
	}

	@Note("GZIP解压缩")
	public static String unCompress(String gzipStr) throws Exception {
		if (null == gzipStr || gzipStr.trim().length() <= 0) {
			return null;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		// ByteArrayInputStream in = new
		// ByteArrayInputStream(Base64Utils.decodeFromString(gzipStr));
		ByteArrayInputStream in = new ByteArrayInputStream(Hex.decodeHex(gzipStr));
		GZIPInputStream gzip = new GZIPInputStream(in);
		byte[] buffer = new byte[256];
		int n;
		while ((n = gzip.read(buffer)) >= 0) {
			out.write(buffer, 0, n);
		}
		return out.toString(StandardCharsets.UTF_8);
	}

	public static void unZipDirectory(String zipFileDirectory, String outputDirectory)
			throws ZipException, IOException {
		File file = new File(zipFileDirectory);
		File[] files = file.listFiles();
		for (int i = 0; files != null && i < files.length; i++) {
			if (files[i].getName().endsWith(".zip")) {
				unzip(zipFileDirectory + File.separator + files[i].getName(), outputDirectory);
			}
		}
	}

	public static void unzip(String filename, String startsWith, String outputDirectory)
			throws ZipException, IOException {
		// logger.info(filename + " to " + outputDirectory);
		if (startsWith == null) {
			startsWith = "";
		}
		File dir = new File(outputDirectory);
		dir.mkdir();
		List<ZipEntry> entries = new ArrayList<ZipEntry>();
		ZipFile zipFile = new ZipFile(filename);
		@SuppressWarnings("unchecked")
		Enumeration<ZipEntry> enu = (Enumeration<ZipEntry>) zipFile.entries();
		while (enu.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) enu.nextElement();
			if (entry.getName().startsWith(startsWith)) {
				if (entry.isDirectory()) {
					String fileName = entry.getName().substring(0, entry.getName().length() - 1);
					String directoryPath = outputDirectory + File.separator + fileName;
					File directory = new File(directoryPath);
					directory.mkdir();
				}
				entries.add(entry);
			}
		}
		unzip(zipFile, entries, startsWith, outputDirectory);
		log.info(filename + " to " + outputDirectory + " end entries.size=" + entries.size());
	}

	public static void unzip(String filename, String outputDirectory) throws ZipException, IOException {
		unzip(filename, "", outputDirectory);
	}

	private static void unzip(ZipFile zipFile, List<ZipEntry> entries, String startsWith, String outputDirectory)
			throws IOException {

		Iterator<ZipEntry> it = entries.iterator();
		while (it.hasNext()) {
			ZipEntry zipEntry = (ZipEntry) it.next();
			if (!zipEntry.isDirectory()) {
				log.debug(zipEntry.getName());
				unzipFiles(zipFile, zipEntry, outputDirectory);
			}
			// MultiThreadEntry mte = new MultiThreadEntry(zipFile, zipEntry,
			// outputDirectory);
			// Thread thread = new Thread(mte);
			// thread.start();
		}
	}

	// 读取压缩文件中的内容名称
	public static List<String> readZipFile(String file) throws Exception {
		List<String> list = new ArrayList<String>();
		InputStream in = new BufferedInputStream(new FileInputStream(file));
		@SuppressWarnings("resource")
		ZipInputStream zin = new ZipInputStream(in);
		try {
			ZipEntry ze;
			while ((ze = zin.getNextEntry()) != null) {
				if (ze.isDirectory()) {
				} else {
					String zeName = new String(ze.getName().getBytes(StandardCharsets.ISO_8859_1),
							StandardCharsets.UTF_8);
					list.add(zeName);
				}
			}

		} catch (Exception ex) {
			throw ex;
		} finally {
			zin.closeEntry();
		}
		return list;
	}

	public static void unzipFiles(ZipFile zipFile, ZipEntry zipEntry, String outputDirectory) {
		BufferedInputStream bis = null;
		FileOutputStream fos = null;
		try {
			byte[] data = new byte[BUFFER_SIZE];
			bis = new BufferedInputStream(zipFile.getInputStream(zipEntry));
			String entryName = zipEntry.getName();
			entryName = new String(entryName.getBytes("GBK"));
			// entryName = entryName.substring(startsWith.length());
			fos = new FileOutputStream(outputDirectory + File.separator + entryName);
			if (zipEntry.isDirectory()) {

			} else {
				// BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER_SIZE);
				int count = 0;
				while ((count = bis.read(data, 0, BUFFER_SIZE)) != -1) {
					fos.write(data, 0, count);
					// bos.write(data, 0, count);
				}
				fos.flush();
				fos.close();
			}
		} catch (Throwable e) {
			log.error(e.getMessage());
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (Throwable e) {
				log.error(e.getMessage());
			}
			try {
				if (bis != null) {
					bis.close();
				}
			} catch (Throwable e) {
				log.error(e.getMessage());
			}
		}
	}
}

/**
 * 使用多线程，提高效率
 * 
 */
class MultiThreadEntry implements Runnable {

	private BufferedInputStream bis;
	private ZipEntry zipEntry;
	private String outputDirectory;

	public MultiThreadEntry(ZipFile zipFile, ZipEntry zipEntry, String outputDirectory) throws IOException {
		this.zipEntry = zipEntry;
		this.outputDirectory = outputDirectory;
		bis = new BufferedInputStream(zipFile.getInputStream(zipEntry));
	}

	public void run() {
		try {
			unzipFiles(zipEntry, outputDirectory);
		} catch (IOException e) {
			try {
				bis.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} finally {
			try {
				bis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void unzipFiles(ZipEntry zipEntry, String outputDirectory) throws IOException {
		byte[] data = new byte[ZipUtils.BUFFER_SIZE];
		String entryName = zipEntry.getName();
		entryName = new String(entryName.getBytes("GBK"));
		// entryName = entryName.substring(startsWith.length());
		FileOutputStream fos = new FileOutputStream(outputDirectory + File.separator + entryName);
		if (zipEntry.isDirectory()) {

		} else {
			BufferedOutputStream bos = new BufferedOutputStream(fos, ZipUtils.BUFFER_SIZE);
			int count = 0;
			while ((count = bis.read(data, 0, ZipUtils.BUFFER_SIZE)) != -1) {
				bos.write(data, 0, count);
			}
			bos.flush();
			bos.close();
		}
	}

}
