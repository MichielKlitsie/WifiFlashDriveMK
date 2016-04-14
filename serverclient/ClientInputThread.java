package serverclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import protocol.DataTransferProtocol;

//import clientserver.FileServer;

// TODO: Auto-generated Javadoc
/**
 * ClientHandler.
 * @author  Michiel Klitsie
 * @version $Revision: 1.1 $
 */
public class ClientInputThread extends Thread {

	//--------------------------------------------------------
	// Instance variables
	//--------------------------------------------------------
	/** The server. */
	private FileHost host;
	private DataTransferProtocol protocolImp;

	/** The in. */
	private BufferedReader in;

	/** The out. */
	private BufferedWriter out;

	/** The client name. */
//	private String clientName;
	private Logger logger;

	/** The m network io parser. */
	private UserInputParser mParser;
	private TUI mTUI;

	/** The terminated. */
	private boolean terminated = false;


	//--------------------------------------------------------
	// Constructor
	//--------------------------------------------------------
	/**
	 * Constructs a ClientHandler object, who is a server-thread handling the communication
	 * Initialises both Data streams.
	 *
	 * @param serverArg the server arg
	 * @param sockArg the sock arg
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	//@ requires serverArg != null && sockArg != null;
	public ClientInputThread(FileHost host) throws IOException {
		super("ClientInputThread");
		this.host = host;
		this.protocolImp = host.getProtocolImp();
		this.in = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
		this.out = new BufferedWriter( new OutputStreamWriter(System.out, "UTF-8"));

		//logger.log(Level.INFO,clientName + "'s thread started");
		this.mParser = new UserInputParser(this);
		this.mTUI = new TUI();
	}

	//--------------------------------------------------------
	// RUN
	//--------------------------------------------------------
	/**
	 * This method takes care of receiving messages from the Client.
	 * Every message that is received, is preprended with the name
	 * of the Client, and the new message is offered to the Server
	 * for broadcasting. If an IOException is thrown while reading
	 * the message, the method concludes that the socket connection is
	 * broken and shutdown() will be called. 
	 */
	public void run() {
		System.out.println("[TUI] ("+ getClass().getName() + ") \n" + mTUI.getStartTUI());

		// Chat-loop input
		char[] inputChars = new char[1024];
		int charsRead = 0;
		try {
			while (!terminated) {
				if ((charsRead = in.read(inputChars)) != -1) {
					// Get string 
					String temp = new String(inputChars).substring(0, charsRead);

					// Show the command on the server side
					//					logger.log(Level.INFO,"Command received: " + temp);
					mParser.parseInput(temp);
					this.host.resetTimer();
				}
			} 

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	//--------------------------------------------------------
	// PRINT
	//--------------------------------------------------------
	public void print(String message) {
		System.out.println(message);
	}

	//--------------------------------------------------------
	// SHUTDOWN
	//--------------------------------------------------------
	/**
	 * This ClientHandler signs off from the Server and subsequently
	 * sends a last broadcast to the Server to inform that the Client
	 * is no longer participating in the chat. 
	 */
	public void shutdown() {

		//		this.server.removeHandler(this);
		// Thread terminates
		terminated = true;
		this.interrupt();
		logger.log(Level.INFO, "Shutdown command clienthandler executed");
	}

	//--------------------------------------------------------
	// GETTERS AND SETTERS
	//--------------------------------------------------------
	public FileHost getHost() {
		return host;
	}
}
