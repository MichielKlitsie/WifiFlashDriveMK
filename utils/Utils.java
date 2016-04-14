package utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.CRC32;

/**
 * Helper utilities. Supplied for convenience.
 * 
 * @author Michiel Klitsie
 * @version 06-04-2016
 */
public class Utils implements protocol.Constants {
	private Utils() {
	}

	//	/**
	//	 * Helper method to get the current process ID
	//	 *
	//	 * @return process id
	//	 */
	//	public static int getProcessId() {
	//		final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
	//		final int index = jvmName.indexOf('@');
	//
	//		if (index < 1) {
	//			return 0;
	//		}
	//
	//		try {
	//			return Integer.parseInt(jvmName.substring(0, index));
	//		} catch (NumberFormatException e) {
	//			return 0;
	//		}
	//	}
	//	/**
	//	 * Gets the contents of the specified file.
	//	 * @param id the file ID
	//	 * @return the array of integers, representing the contents of the file to transmit
	//	 */
	//	public static Integer[] getFileContents(int id) {
	//		File fileToTransmit = new File(String.format("rdtcInput%d.png", id));
	//		try (FileInputStream fileStream = new FileInputStream(fileToTransmit)) {
	//			Integer[] fileContents = new Integer[(int) fileToTransmit.length()];
	//
	//			for (int i = 0; i < fileContents.length; i++) {
	//				int nextByte = fileStream.read();
	//				if (nextByte == -1) {
	//					throw new Exception("File size is smaller than reported");
	//				}
	//
	//				fileContents[i] = nextByte;
	//			}
	//			return fileContents;
	//		} catch (Exception e) {
	//			System.err.println(e.getMessage());
	//			System.err.println(e.getStackTrace());
	//			return null;
	//		}
	//	}
	//
	//	/**
	//	 * Writes the contents of the fileContents array to the specified file.
	//	 * @param fileContents the contents to write
	//	 * @param id the file ID
	//	 */
	//	public static void setFileContents(Integer[] fileContents, int id) {
	//		File fileToWrite = new File(String.format("rdtcOutput%d.%d.png", id, Utils.getProcessId()));
	//		try (FileOutputStream fileStream = new FileOutputStream(fileToWrite)) {
	//			for (Integer fileContent : fileContents) {
	//				fileStream.write(fileContent);
	//			}
	//		} catch (Exception e) {
	//			System.err.println(e.getMessage());
	//			System.err.println(e.getStackTrace());
	//		}
	//	}

	/// TODO: HIER GEBLEVEN <--------
	public static byte[] int2ByteArray(int value, int byteArrayLength) {
		byte[] bytes = ByteBuffer.allocate(byteArrayLength).putInt(value).array();
		//		int a = ByteBuffer.wrap(bytes).getInt();
		return bytes;
	}

	public static int byteArray2Int(byte[] b) {
		return ByteBuffer.wrap(b).getInt();
	}

	public static byte[] intArray2ByteArray(int[] values, int byteArrayLength) {

		ByteBuffer byteBuffer = ByteBuffer.allocate(byteArrayLength);
		IntBuffer intBuffer = byteBuffer.asIntBuffer();
		intBuffer.put(values);

		byte[] array = byteBuffer.array();
		return array;
	}

	public static int[] byteArray2IntArray(byte[] b) {
		IntBuffer intBuf =
				ByteBuffer.wrap(b)
				.order(ByteOrder.BIG_ENDIAN)
				.asIntBuffer();
		int[] array = new int[intBuf.remaining()];
		intBuf.get(array);

		//				 int[] arr = new int[intBuf.limit()];
		//				    intBuf.get(arr);

		return array;
	}

	public static byte[] long2byteArray(long value, int byteArrayLength) {
		byte[] bytes = ByteBuffer.allocate(byteArrayLength).putLong(value).array();
		return bytes;
	}

	public static long byteArray2Long(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
		buffer.put(bytes);
		buffer.flip();//need flip 
		return buffer.getLong();
	}

	public static long createChecksumFile(File file) {
		long checksumInputLength = file.length();
		byte[] checksumInputContent = new byte[(int) checksumInputLength];
		// TODO: read and arraycopy (see FileHost.java)?
		CRC32 crc = new CRC32();
		crc.update(checksumInputContent);
		long checksum = crc.getValue();

		return checksum;                                   
	}

	public static byte[] string2byteArray(String str) throws IOException {
		// USING LENGTH/PAYLOAD ALGORITM, WITH int NUMBYTELP for storing length of string
		byte[] filenameBytes = str.getBytes(Charset.forName("UTF-8"));
		byte[] lengthFilenameBytes = int2ByteArray(filenameBytes.length, NUMBYTESLP);

		ByteBuffer buffer = ByteBuffer.allocate(lengthFilenameBytes.length + filenameBytes.length);
		buffer.put(lengthFilenameBytes);
		buffer.put(filenameBytes);
		byte[] filenameBytesLP = buffer.array();

		return filenameBytesLP;
	}

	public static String byteArray2String(byte[] bytesLP) {
		byte[] lengthBytes = Arrays.copyOfRange(bytesLP, 0, NUMBYTESLP);
		int length = byteArray2Int(lengthBytes);
		byte[] stringBytes = Arrays.copyOfRange(bytesLP, NUMBYTESLP, NUMBYTESLP+length);
		String str = new String(stringBytes, StandardCharsets.UTF_8);
		return str;
	}

	public static byte[] file2byteArray(File file) throws IOException {
		FileInputStream fin = null;


		// create FileInputStream object

		fin = new FileInputStream(file);
		byte[] fileContent = new byte[(int)file.length()];
		// Reads up to certain bytes of data from this input stream into an array of bytes.
		fin.read(fileContent);
		//            
		//        } catch (FileNotFoundException e) {
		//            System.out.println("File not found" + e);
		//        } catch (IOException ioe) {
		//            System.out.println("Exception while reading file " + ioe);
		//        } finally {
		//            // close the streams using close method
		//            try {
		if (fin != null) {
			fin.close();
		}
		//            } catch (IOException ioe) {
		//                System.out.println("Error while closing stream: " + ioe);
		//            }
		//        }

		return fileContent;
	}


	// TOT HIER <---------
	public static String[] splitFullFile(String fullfile) {
		Path p = Paths.get(fullfile);
		String path = p.getParent().toString();
		String filename = p.getFileName().toString();
		String ext = utils.Utils.createExtension(fullfile);
		String[] fullFileSplit = {path, filename, ext};
		return fullFileSplit;
	}

	public static String createExtension(String filename) {
		if (filename == null) {
			return null;
		}
		int extensionPos = filename.lastIndexOf('.');
		int lastUnixPos = filename.lastIndexOf('/');
		int lastWindowsPos = filename.lastIndexOf('\\');
		int lastSeparator = Math.max(lastUnixPos, lastWindowsPos);
		int index = lastSeparator > extensionPos ? -1 : extensionPos;
		if (index == -1) {
			return "";
		} else {
			return filename.substring(index + 1);
		}
	}

}
