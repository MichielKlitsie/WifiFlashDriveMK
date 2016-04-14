package tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import model.FileUDPPackets;

public class testUtils {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testSplitFullFile() {
		String fullfile = "src/data/testImage.png";
		Path p = Paths.get(fullfile);
		String filename = p.getFileName().toString();
		String path = p.getParent().toString();
		String ext = utils.Utils.createExtension(fullfile);
		System.out.println(filename);
		System.out.println(path);
		System.out.println(ext);
	}
	
	@Test
	public void testFilenameToBytes() {
		try {
			String filename = "testImagetestImagetestImagetestImagetestImagetestImagetestImagetestImagetestImagetestImagetestImagetestImagetestImagetestImagetestImagetestImagetestImagetestImagetestImagetestImagetestImagetestImagetestImagetestImagetestImagetestImagetestImagetestImage.png";
			FileUDPPackets testFile = new FileUDPPackets(filename);
			String filetype = testFile.getExtension();
			
			byte[] filenameBytes = filename.getBytes();
			
			System.out.println("Testing filename: " + filename + ". Extracted extention: " + filetype + ".");
			System.out.println("Byte array length: " + filenameBytes.length + ".");
			
			String reconFilename = new String(filenameBytes, StandardCharsets.UTF_8);
			System.out.println("Reconstructing filename (variable): " + reconFilename + ".\n");
			
			
			byte[] filenameBytesFixed = Arrays.copyOf(filenameBytes, 32);
			System.out.println("New byte array length: " + filenameBytesFixed.length + ".");
			String reconFilename2 = new String(filenameBytesFixed, StandardCharsets.UTF_8);
			System.out.println("Reconstructing filename (fixed): " + reconFilename2 + ".\n");
			
			// LENGTH AND PAYLOAD
			byte[] lengthFilename = utils.Utils.int2ByteArray(filenameBytes.length, 4);
			ByteBuffer buffer = ByteBuffer.allocate(lengthFilename.length + filenameBytes.length);
		    buffer.put(lengthFilename);
		    buffer.put(filenameBytes);
		    byte[] filenameBytesLP = buffer.array();
		    System.out.println("Length/Payload: New byte array length: " + filenameBytesLP.length + ".");
		    byte[] reconLengthFilenameLP = Arrays.copyOfRange(filenameBytesLP, 0, 4);
		    int reconLengthLP = utils.Utils.byteArray2Int(reconLengthFilenameLP);
		    System.out.println("Reconstructed filename byte array length: " + reconLengthLP + ".");
		    byte[] reconFilenameBytesLP = Arrays.copyOfRange(filenameBytesLP, 4, (4+reconLengthLP));
		    String reconFilenameLP = new String(reconFilenameBytesLP, StandardCharsets.UTF_8);
		    System.out.println("Reconstructed filename (LP): " + reconFilenameLP + ". \n");
		    
		    // Using utils
		    byte[] bytesFilenameLP2 = utils.Utils.string2byteArray(filename);
		    String filenameReconLP2 = utils.Utils.byteArray2String(bytesFilenameLP2);
		    System.out.println("Reconstructed filename (LP-utils): " + filenameReconLP2 + ". \n");
		    
		} catch (IOException e) {
			e.printStackTrace();
		}
//		fail("Not yet implemented");
	}

}
