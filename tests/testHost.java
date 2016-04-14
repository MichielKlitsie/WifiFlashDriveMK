package tests;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import serverclient.FileHost;


public class testHost {

	private static boolean setUpIsDone = false;
	private FileHost fh1;

	@Before
	public void setUp() throws Exception {
		if(setUpIsDone) {
			return;
		}
		fh1 = new FileHost();
		setUpIsDone = true;
	}

	//	@Test
	//	public void testHostProps() {
	//
	//		assertEquals("IP check ", "nvc2857.nedap.local/172.17.2.13", fh1.getLocalIPAddress().getHostAddress());
	//		assertEquals("MAC check ", "ac:bc:32:bb:74:0b", fh1.getMacAddress());
	//	}
	//
	//	@Test
	//	public void testDataPath() {
	//
	//		assertEquals("Start datapath ", new File(System.getProperty("user.dir")).getAbsolutePath(), fh1.getDataPath());
	//		String newDataPath = new File(System.getProperty("user.dir")).getParent();
	//		fh1.setDataPath(newDataPath);
	//		assertEquals("New datapath ", new File(System.getProperty("user.dir")).getParent(), fh1.getDataPath());
	//	}

	@Test
	public void testShutDown() {
		fh1.showStateThreads();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException ie) {
			//Handle exception
			ie.printStackTrace();
		}
		fh1.shutdown();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException ie) {
			//Handle exception
			ie.printStackTrace();
		}
		fh1.showStateThreads();
	}
}
