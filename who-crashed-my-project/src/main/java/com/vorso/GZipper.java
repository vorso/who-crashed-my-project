package com.vorso;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZipper {

	public void decompressGzipFile(String gzipFile, String newFile) throws IOException {
		FileInputStream fis = new FileInputStream(gzipFile);
		GZIPInputStream gis = new GZIPInputStream(fis);
		FileOutputStream fos = new FileOutputStream(newFile);
		byte[] buffer = new byte[1024];
		int len;
		while ((len = gis.read(buffer)) != -1) {
			fos.write(buffer, 0, len);
		}
		fos.close();
		gis.close();
	}

	public void compressGzipFile(String file, String gzipFile) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		FileOutputStream fos = new FileOutputStream(gzipFile);
		GZIPOutputStream gzipOS = new GZIPOutputStream(fos);
		byte[] buffer = new byte[1024];
		int len;
		while ((len = fis.read(buffer)) != -1) {
			gzipOS.write(buffer, 0, len);
		}
		gzipOS.close();
		fos.close();
		fis.close();
	}
}
