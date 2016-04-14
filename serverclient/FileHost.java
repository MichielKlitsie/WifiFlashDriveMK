package serverclient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import java.net.NetworkInterface;

import model.Flag;
import model.UDPPacketMK;
import protocol.DataTransferProtocol;
import utils.Utils;

/**
 * FileClient that runs on the sending machine.
 * 
 * @author Michiel Klitsie
 * @version 06-04-2016
 */
public class FileHost extends Thread implements protocol.Constants {


	//--------------------------------------------------------
	// INSTANCE VARIABLES
	//--------------------------------------------------------
	// protocol string
	private static String protocolString = "WifiFlashDrive";

	// server address
	private String host;
	private DataTransferProtocol protocolImp;

	// ports
	//	private int transferPort;
	//	private int discoveryPort = 8888;

//	// computer client ID
//	private String clientId;
	private String macID;
//
//
//	// student group password
//	private String password;

	// the file id
//	private int fileID;

	//	private boolean isConnected;

	//-------- Instance variables: threads -------------------
	// thread for handling server messages
//	private Thread eventLoopThread;

	// Socket
	//	private DatagramSocket dsock;
	//	//	private Socket socket;

//	// scanner over socket input stream
//	private Scanner inputScanner;
//
//	// socket output stream
//	private PrintStream outputStream;
//
//	/** The in. */
//	private BufferedReader in;
//
//	/** The out. */
//	private BufferedWriter out;

	// currently pending control message from server
	//	private String currentControlMessage = null;


//	// whether this client is the sender
//	private boolean isSender = false;
//
//	// input packet buffer
//	private List<Integer[]> inputPacketBuffer = new ArrayList<>();
//	private final ReentrantLock inputPacketBufferLock = new ReentrantLock();
//
//	// output packet buffer
//	private List<Integer[]> outputPacketBuffer = new ArrayList<>();
//	private final ReentrantLock outputPacketBufferLock = new ReentrantLock();

	private InetAddress LocalIPAddress;

	// Threads
	private DiscoveryThread discoveryThread;
	private FileReceiverThread fileReceiverThread;
	private ClientInputThread inputThread;
	private FileSenderThread fileSenderHandler;

	private String dataPath;

	//--------------------------------------------------------
	// CONSTRUCTOR
	//--------------------------------------------------------
	public FileHost() throws UnknownHostException, IOException, InterruptedException {
		this.protocolImp = new DataTransferProtocol();
		this.LocalIPAddress = InetAddress.getLocalHost();
		this.dataPath = new File(System.getProperty("user.dir")).getAbsolutePath();

//		displayInterfaces();
		displayHostInfo();

		//		Utils.Timeout.Start();
		//		//					eventLoopThread = new Thread("Event Loop Thread");
		//		//					eventLoopThread = new Thread(this, "Event Loop Thread");

		// 1. Open a discovery thread for broadcast receiving on discovery port
		this.discoveryThread = new DiscoveryThread(this);
		discoveryThread.start();

		// 2. Open a file transfer thread for listening to filetransfer port
		this.fileReceiverThread = new FileReceiverThread(this);
		fileReceiverThread.start();

		// 3. Open a user input thread for communicating command line
		this.inputThread = new ClientInputThread(this);
		inputThread.start();
	}

	//--------------------------------------------------------
	// METHODS : DISPLAY INTERFACES / INFO
	//--------------------------------------------------------

	public void displayHostInfo() throws UnknownHostException {
		String hostInfoString = "[HOST] ("+ getClass().getName() + ") Host created on "+ 
				"\n - Local IP address: " + InetAddress.getLocalHost().getHostAddress() +
				"\n - MAC address: " + getMacAddress() +
				"\n - FileTransfer port: " + TRANSFERPORT +
				"\n - Discovery port: " + DISCOVERYPORT + 
				"\n - Current data folder: " + this.dataPath + "\n\n";
		System.out.println(hostInfoString);
	}


	public void displayInterfaces() {
		String ip;
		String strInterfaces = "[HOST] ("+ getClass().getName() + ") Available interfaces on machine: \n";
		try{
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface iface = interfaces.nextElement();
				// filters out 127.0.0.1 and inactive interfaces
				if (iface.isLoopback() || !iface.isUp())
					continue;

				Enumeration<InetAddress> addresses = iface.getInetAddresses();
				while(addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();
					ip = addr.getHostAddress();
					strInterfaces = strInterfaces + " - " + iface.getDisplayName() + " " + ip + "\n";
				}
			}
			System.out.println(strInterfaces);
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}
	}


	public static String generateMacAddress()
			throws UnknownHostException, SocketException {
		//	    InetAddress addr = InetAddress.getByName(ipAddr);
		InetAddress addr = InetAddress.getLocalHost();
		NetworkInterface ni = NetworkInterface.getByInetAddress(addr);
		if (ni == null)
			return null;

		byte[] mac = ni.getHardwareAddress();
		if (mac == null)
			return null;

		StringBuilder sb = new StringBuilder(18);
		for (byte b : mac) {
			if (sb.length() > 0)
				sb.append(':');
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}

	//--------------------------------------------------------
	// METHODS : CREATE FILE SENDING THREAD
	//--------------------------------------------------------
	public void createFileSenderThread(String filename, String IPAddressDest) throws IOException, InterruptedException {
		this.fileSenderHandler = new FileSenderThread(this, filename, IPAddressDest);
		fileSenderHandler.start();
	}

	//--------------------------------------------------------
	// METHODS : DATA FOLDER SELECTION
	//--------------------------------------------------------
	public void setDataPath(String dataPath) {
		this.dataPath = dataPath;
		System.out.println("[HOST] ("+ getClass().getName() + ") Current datapath: '" + dataPath + "'\n");
	}
	
	public String getStringAvailableFiles() {
		List<String> results = new ArrayList<String>();
		File[] files = new File(this.dataPath).listFiles();
		//If this pathname does not denote a directory, then listFiles() returns null. 

		if(files != null) {
			for (File file : files) {
				if (file.isFile()) {
					results.add(file.getName());
				}
			} } else {
				return "No files available";
			}

		String listString = results.size() + " file(s) available in '" + this.dataPath + "'\n";

		for (String s : results)
		{
			listString += s + "\n";
		}

		return listString;
	}

	//--------------------------------------------------------
	//TODO: METHODS : SHUTDOWN
	//--------------------------------------------------------
	/**
	 * Stops the client, and disconnects it from the server.
	 */
	public void shutdown() {
		//		Utils.Timeout.Stop();

		//		try {
		//			socket.setTcpNoDelay(true);
		//
		//			// upload checksum of the received file
		//			if (!isSender) {
		//				sendChecksum("OUT", String.format("rdtcOutput%d.%d.png", fileID, Utils.getProcessId()));
		//			}
		//
		//			// stop simulation
		//			//			simulationStarted = false;
		//			//			simulationFinished = true;
		//
		//			// stop the message loop
		//			eventLoopThread.interrupt();
		//			try {
		//				eventLoopThread.join();
		//			} catch (InterruptedException e) {
		//				e.printStackTrace();
		//			}
		//
		//			// close comms
		//			sendControlMessage("CLOSED");
		//			socket.getOutputStream().flush();
		//			Thread.sleep(1000);
		//			socket.close();
		//		} catch (IOException | InterruptedException e) {
		//			e.printStackTrace();
		//		}
	}

	//--------------------------------------------------------
	// GETTERS AND SETTERS
	//--------------------------------------------------------
	public DataTransferProtocol getProtocolImp() {
		return protocolImp;
	}

	public InetAddress getLocalIPAddress() {
		return LocalIPAddress;
	}

	public boolean checkFile(String filename) {
		File file = new File(filename);
		return file.exists();
	}

	public DiscoveryThread getDiscoveryThread() {
		return discoveryThread;
	}

	public FileReceiverThread getFileReceiverThread() {
		return fileReceiverThread;
	}

	public ClientInputThread getInputThread() {
		return inputThread;
	}

	public FileSenderThread getFileSenderHandler() {
		return fileSenderHandler;
	}

	public String getMacAddress() {
		return this.macID;
	}
	
	public String getDataPath() {
		return this.dataPath;
	}



}