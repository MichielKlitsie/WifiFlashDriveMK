package serverclient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import model.FileUDPPackets;
import model.Flag;
import model.UDPPacketMK;
import protocol.DataTransferProtocol;

public class FileReceiverThread extends Thread implements protocol.Constants {

	//--------------------------------------------------------
	// INSTANCE VARIABLES
	//--------------------------------------------------------
	private boolean isConnected = false;
	private boolean isTransferring = false;
	private DatagramSocket dsock;
	private FileHost host;
	private PacketParser mPacketParser;
	private FileUDPPackets newFileReceiver;
	private DataTransferProtocol protocolImp;

	private int[] currentWindow;
	private int headerLength;


	//--------------------------------------------------------
	// Constructor
	//--------------------------------------------------------
	public FileReceiverThread(FileHost host) throws IOException, InterruptedException {
		super("FileReceiverThread");
		this.host = host;
		this.mPacketParser = new PacketParser(this);
		this.protocolImp = new DataTransferProtocol();
		createSocket();
	}

	//--------------------------------------------------------
	// Run
	//--------------------------------------------------------
	@Override
	public void run() {
		//--------------------------------------------------------
		// RUN: LISTENING/SETUP PHASE
		//--------------------------------------------------------
		System.out.println("[RECEIVER] ("+ getClass().getName() + ") Listening on port "+ TRANSFERPORT +" for initial SETUP packet for file transfer.\n");

		// Predefine first empty packet
		byte[] receiveSetupData = new byte[MAXDATASIZEPACKET];   
		DatagramPacket receiveSetupPacket = new DatagramPacket(receiveSetupData,
				receiveSetupData.length);

		// Phase 0: Waiting for Setup-up packet	
		while(!isConnected) {
			try {

				dsock.receive(receiveSetupPacket);
				mPacketParser.parsePacket(receiveSetupPacket);
				
				// CHANGE STATUS
				setConnected(true);
				setIsTransferring(true);
				
				// SET FIRST WINDOW
				setCurrentWindow(new int[]{0, WINDOWSIZE});
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//--------------------------------------------------------
		// RUN: TRANSFER PHASE
		//--------------------------------------------------------
		// Phase 2: Setting up file transfer (creating empty file)
		byte[] receiveTransferData = new byte[getHeaderLength() + MAXDATASIZEPACKET];
		DatagramPacket receiveTransferPacket = new DatagramPacket(receiveTransferData,
				receiveTransferData.length);
		System.out.println("[RECEIVER]("+ getClass().getName() + ") Waiting for arrival of data packets."+"\n");
		
		// Phase 3: [Loop] Receiving file packets and send acknowledgements after ...
		while(isTransferring) {
			try {
				if(this.newFileReceiver.isFinished()) {
					setIsTransferring(false);
				} else {
					//Receiving an actual packet
					dsock.receive(receiveTransferPacket);
					mPacketParser.parsePacket(receiveTransferPacket);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//--------------------------------------------------------
		// RUN: FINAL CHECKING PHASE
		//--------------------------------------------------------
		System.out.println("\n\n[RECEIVER]("+ getClass().getName() + ") File transfer complete.\n - bufferTotalData length: " + this.newFileReceiver.getBufferTotalData().length + "\n - Checksum data: "+ this.newFileReceiver.createChecksumFromData() + "\n");
		
		// TODO: SEND CHECKSUM TO VERIFY FILE AND RECEIVE FINAL ACKNOWLEDGMENT
		
		
		try {
			System.out.println("\n[RECEIVER]("+ getClass().getName() + ") Saving file."+"\n");
			this.newFileReceiver.saveFile(getHost().getDataPath());
//			this.newFileReceiver.createBufferDataFromPackets();
		} catch (IOException e) {
			System.out.println("\n\n[RECEIVER]("+ getClass().getName() + ") Error during final file creation."+"\n");
			e.printStackTrace();
		}
		//--------------------------------------------------------
		//--------------------------------------------------------
		//<<<<<<< HIER GEBLEVEN
		//--------------------------------------------------------
		//--------------------------------------------------------
	}


	//--------------------------------------------------------
	// Connection status
	//--------------------------------------------------------

	/**
	 * Connects to the challenge server
	 * 
	 * @throws IOException
	 *             if the connection failed
	 */
	private void createSocket() throws IOException, InterruptedException {
		try {
			this.dsock = new DatagramSocket(TRANSFERPORT);

		} catch (IOException e) {
			throw e;
		}
	}

	private void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}

	public boolean getConnectionStatus() {
		return this.isConnected;
	}

	private void setIsTransferring(boolean isTransferring) {
		this.isTransferring = isTransferring;
	}

	//--------------------------------------------------------
	// PACKET RECEIVING
	//--------------------------------------------------------
	public void setupFileTransfer(DatagramPacket receivePacket) throws IOException{
		UDPPacketMK setupPacketMK = new UDPPacketMK(receivePacket.getData());
		String filename = utils.Utils.byteArray2String(setupPacketMK.getPacketData());
		System.out.println("[RECEIVER]("+ getClass().getName() + ") Setting up file transfer for " + filename);
		
		// 1. Create base file1
		//		System.out.println("[RECEIVER]("+ getClass().getName() + ") Empty file created");
		createBaseFile(setupPacketMK);
		setHeaderLength(setupPacketMK.getHeaderLength());
		this.newFileReceiver.setFilename(filename);
		// 2. Sending SETUP ACK
		System.out.println("[RECEIVER]("+ getClass().getName() + ") Sending SETUP-ACK"+"\n");
		sendSetupAck(receivePacket);

		// 3. Set connection status
		//		System.out.println("[RECEIVER]("+ getClass().getName() + ") Waiting for first file packet");
		setConnected(true);
	}

	public void createBaseFile(UDPPacketMK subPacket) {
		this.newFileReceiver = new FileUDPPackets(subPacket.getAmountPackets());
		
		System.out.println("[RECEIVER]("+ getClass().getName() + ") Created empty file ready for a total of " + subPacket.getAmountPackets() + " packets."+"\n");
	}

	//--------------------------------------------------------
	// CHECK: CHECKSUM SINGLE PACKET
	//--------------------------------------------------------
	public boolean checkChecksumPacket(DatagramPacket receivePacket) {
		// Extract the subPacketMK
		UDPPacketMK receivePacketMK = new UDPPacketMK(receivePacket.getData());
		
		// Read checksum packet from the header
		long checksumPacketHead = receivePacketMK.getChecksumPacket();
		
		// Calculate the checksum from the data
		long checksumPacketCalc = receivePacketMK.createChecksumPacket();
		
		System.out.println("[RECEIVER]("+ getClass().getName() + ") Data-packet " + (receivePacketMK.getSeqNr() + 1) + " received ("+ receivePacketMK.getDataLength() +" bytes). Checksum calculated: " + checksumPacketCalc);
		
		// Compare
		return checksumPacketHead == checksumPacketCalc;
	}
	
	
	public void writeDataPacket(DatagramPacket receivePacket) {
		// Extract packet
		UDPPacketMK dataPacket = new UDPPacketMK(receivePacket.getData());

		// Add to empty file
		this.newFileReceiver.addFilePacket(dataPacket);
	}

	public void checkAckWindow(DatagramPacket receivePacket) throws IOException {
		// Extract packet
		UDPPacketMK dataPacket = new UDPPacketMK(receivePacket.getData());

		//TODO: ALSO CHECK AFTER CERTAIN AMOUNT OF TIME
		if (dataPacket.getSeqNr() == getCurrentWindow()[1]-1) {
			System.out.println("[RECEIVER]("+ getClass().getName() + ") Checking packets from " + getCurrentWindow()[0] + " to " + (getCurrentWindow()[1]-1) + "\n");
			boolean isReadyForNextBatch = true;

			
			for (int i = getCurrentWindow()[0]; i < getCurrentWindow()[1]; i++) {
				if (this.newFileReceiver.getFilePacket(i) == null) {
					System.out.println("[RECEIVER]("+ getClass().getName() + ") Packet nr " + i + " is missing");
					isReadyForNextBatch = false;
					//TODO: Ask for resend
				}	
			}

			if(isReadyForNextBatch) {
				// Set new window
				setCurrentWindow(new int[]{getCurrentWindow()[1], getCurrentWindow()[1]+WINDOWSIZE});
				
				// If window exceeds last packages, adjust window
				if(getCurrentWindow()[1]+WINDOWSIZE > this.newFileReceiver.getAmountPackets()) {
					setCurrentWindow(new int[]{getCurrentWindow()[0], this.newFileReceiver.getAmountPackets()});
				}
				
				// Feedback on packages
				System.out.println("[RECEIVER]("+ getClass().getName() + ") Batch arrived in good condition. New window from " + getCurrentWindow()[0] + " to " + getCurrentWindow()[1]);
				UDPPacketMK transferAckPackageMK = protocolImp.createTransferACKPacket(dataPacket.getAmountPackets(), getCurrentWindow());
				sendTransferAck(receivePacket, transferAckPackageMK);
			}
		}
	}

	public void checkFinalFile(DatagramPacket receivePacket) {
		System.out.println("[RECEIVER]("+ getClass().getName() + ") Jup... TODO... Checking full file. ALMOST THERE!!!\n");
	}


	//--------------------------------------------------------
	// PACKET SENDING: ACKNOWLEDGEMENTS
	//--------------------------------------------------------
	public void sendSetupAck(DatagramPacket receivePacket) throws IOException {
		Flag flagSetupAck = Flag.SETUPACK;
		UDPPacketMK receivedSetupPacket = new UDPPacketMK(receivePacket.getData());
		UDPPacketMK setupAckPacketMK = protocolImp.createSetupACKPacket(receivedSetupPacket.getAmountPackets());
		//		System.out.println(">>>TEST WINDOW before sending: [" + setupAckPacketMK.getWindow()[0] +"," + setupAckPacketMK.getWindow()[1] +"]");
		DatagramPacket setupAckPacket = new DatagramPacket(setupAckPacketMK.getAsRegularUDPData(),
				setupAckPacketMK.getAsRegularUDPData().length,
				receivePacket.getAddress(),
				receivePacket.getPort());
		this.dsock.send(setupAckPacket);
	}

	public void sendTransferAck(DatagramPacket receivePacket, UDPPacketMK transferAckPackageMK) throws IOException {
		DatagramPacket transferAckPacket = new DatagramPacket(transferAckPackageMK.getAsRegularUDPData(),
				transferAckPackageMK.getAsRegularUDPData().length,
				receivePacket.getAddress(),
				receivePacket.getPort());
		this.dsock.send(transferAckPacket );
	}


	// TODO: packet receiving and sending in DataTransferProtocol
	public void sendPacket(DatagramPacket packet) throws IllegalArgumentException {
		if (packet == null) {
			throw new IllegalArgumentException("packet == null");
		}
		//		for (int i = 0; i < packet.length; i++) {
		//			if (packet[i] == null) {
		//				throw new IllegalArgumentException("packet[" + i + "] == null");
		//			}
		//		}
		//
		//		try {
		//			this.outputPacketBufferLock.lock();
		//			this.outputPacketBuffer.add(packet);
		//		} finally {
		//			this.outputPacketBufferLock.unlock();
		//		}
		try {
			dsock.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//--------------------------------------------------------
	// GETTERS AND SETTERS
	//--------------------------------------------------------
	public FileHost getHost() {
		return this.host;
	}


	public int[] getCurrentWindow() {
		return currentWindow;
	}

	public void setCurrentWindow(int[] currentWindow) {
		this.currentWindow = currentWindow;
	}
	
	
	public int getHeaderLength() {
		return headerLength;
	}

	public void setHeaderLength(int headerLength) {
		this.headerLength = headerLength;
	}


}
