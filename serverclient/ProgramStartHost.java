package serverclient;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.rmi.UnknownHostException;

/**
 * FileClient that runs on the sending machine.
 * 
 * @author Michiel Klitsie
 * @version 06-04-2016
 */
public class ProgramStartHost {

	//--------------------------------------------------------
	// MAIN PROGRAM
	//--------------------------------------------------------
	public static void main(String args[]) {
		// Create the client
		FileHost host = null;
		System.out.print("[FRAMEWORK (MAIN)] Starting host...\n");
		try {
			host = new FileHost();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}
}