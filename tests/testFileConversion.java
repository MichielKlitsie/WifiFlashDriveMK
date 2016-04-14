package tests;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.junit.Before;
import org.junit.Test;

import model.FileUDPPackets;

public class testFileConversion {

	private FileUDPPackets newFile;

	@Before
	public void setUp() throws Exception {
		String filename = "src/testdata/testImage.png";
		String filetype = "png";
//		newFile = new FileUDPPackets(filename, filetype);
	}

	@Test
	public void testFileToConverted() {
		
		fail("Not yet implemented");
	}
	
	@Test
	public void testConvertedToFile() {
		fail("Not yet implemented");
	}

}
