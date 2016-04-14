package serverclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.zip.CRC32;

import model.FileUDPPackets;
import model.Flag;
import model.UDPPacketMK;
import protocol.DataTransferProtocol;

public class FileSenderThread extends Thread implements protocol.Constants {

	//--------------------------------------------------------
	// INSTANCE VARIABLES
	//--------------------------------------------------------
	private boolean isConnected;
	private DatagramSocket dsock;
	private FileHost host;
	private PacketParser mPacketParser;
	private File file;
	private FileUDPPackets newFileSender;
	private String fullfile;
	private String ext;

	private final String piAddress = "172.17.2.3";
	private String strIPAddressDest;
	private InetAddress IPAddressDest;
	private DataTransferProtocol protocolImpl;

	//--------------------------------------------------------
	// Constructor
	//--------------------------------------------------------
	public FileSenderThread(FileHost host, String fullfile, String IPAddressDest) {
		super("FileSenderThread");
		this.host = host;

		// File is already checked before opening thread
		try {
			this.fullfile = fullfile;
			this.file = new File(fullfile);

			// Try constructing destination IP address
			//
			// TODO: CHANGE IPADDRES
			//
			//		this.strIPAddressDest = IPAddressDest;
			this.strIPAddressDest = piAddress;
			this.IPAddressDest = InetAddress.getByName(strIPAddressDest);


			// PacketParser
			this.mPacketParser = new PacketParser(this);
			this.protocolImpl = this.host.getProtocolImp();
		} catch (UnknownHostException e) {
			System.out.println("[SENDER] ("+ getClass().getName() + ") Host " + IPAddressDest + "is not known.\n");
			e.printStackTrace();
		}
	}

	//--------------------------------------------------------
	// RUN
	//--------------------------------------------------------
	@Override
	public void run() {
		//--------------------------------------------------------
		// RUN: SETUP PHASE
		//--------------------------------------------------------
		//		checkHosts(String subnet);

		// Create datagram socket
		try {
			this.dsock = new DatagramSocket();
		} catch (SocketException e) {
			System.out.println("[SENDER] ("+ getClass().getName() + ") Socket cannot be made\n");
			e.printStackTrace();
		}

		try {
			// CREATING PACKETS
			System.out.println("[SENDER] ("+ getClass().getName() + ") Deconstructing " + fullfile + " into packets.\n");
			newFileSender = new FileUDPPackets(this.fullfile);

			// SEND SETUP
			System.out.println("[SENDER]("+ getClass().getName() + ") Sending initial SETUP packet.\n");
			UDPPacketMK setupPacketMK = this.protocolImpl.createSetupPacket(newFileSender);
			DatagramPacket sendSetupPacket = new DatagramPacket(setupPacketMK.getAsRegularUDPData(),
					setupPacketMK.getAsRegularUDPData().length,
					this.IPAddressDest,
					TRANSFERPORT);
			dsock.send(sendSetupPacket);

			// WAIT FOR SETUP ACKNOWLEDGMENT
			System.out.println("[SENDER]("+ getClass().getName() + ") Waiting for acknowledgement");
			byte[] receiveData = new byte[1024];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

			// WAIT FOR ACKTIMEOUT For response
			dsock.setSoTimeout(ACKTIMEOUT);
			try {
				dsock.receive(receivePacket);
			} catch (SocketTimeoutException e) {
				System.out.println("[BROADCAST](" + getClass().getName() + ") No response on setup within " + ACKTIMEOUT + " milliseconds.\n");
				return;
			}

			UDPPacketMK receiveSetupAck = new UDPPacketMK(receivePacket.getData());
			System.out.println("[SENDER]("+ getClass().getName() + ") Packet with flag: " + receiveSetupAck.getFlag() + " for file of " + receiveSetupAck.getAmountPackets() + " packets. Expected window: [" + receiveSetupAck.getWindow()[0] + "," + receiveSetupAck.getWindow()[1] + "]\n\n");

			//--------------------------------------------------------
			// RUN: FILETRANSFER PHASE
			//--------------------------------------------------------
			System.out.println("[SENDER]("+ getClass().getName() + ") Starting file transfer\n\n");
			
			// INITIALIZE VARIABLES WITH FIRST PACKAGE 
			UDPPacketMK dataPacketMK = newFileSender.getFilePacket(0);
			DatagramPacket sendDataPacket = new DatagramPacket(dataPacketMK.getAsRegularUDPData(),
					dataPacketMK.getAsRegularUDPData().length,
					this.IPAddressDest,
					TRANSFERPORT);

			boolean transferFinished = false;
			int[] currentWindow = new int[]{receiveSetupAck.getWindow()[0], receiveSetupAck.getWindow()[1]};
			
			while(!transferFinished) {
				// BATCH: SEND A BATCH OF WINDOW SIZE
				for (int i = currentWindow[0]; i<currentWindow[1]; i++) {
					dataPacketMK = newFileSender.getFilePacket(i);					
					sendDataPacket = new DatagramPacket(dataPacketMK.getAsRegularUDPData(),
							dataPacketMK.getAsRegularUDPData().length,
							this.IPAddressDest,
							TRANSFERPORT);	
					dsock.send(sendDataPacket);
					System.out.println("[SENDER]("+ getClass().getName() + ") Packet nr " + (i+1) + " send. Datasize: " + dataPacketMK.getDataLength() + " bytes. Checksum: " + dataPacketMK.getChecksumPacket());
				}

				// BATCH: WAIT FOR ACKNOWLEDGMENT
				System.out.println("[SENDER]("+ getClass().getName() + ") Waiting for transfer acknowledgment...");
				dsock.receive(receivePacket);

				// BATCH: GET NEW WINDOW
				UDPPacketMK receiveTransferAck = new UDPPacketMK(receivePacket.getData());
				if (receiveTransferAck.getFlag() != Flag.TRANSFERFINAL) {
					System.out.println("[SENDER]("+ getClass().getName() + ") Packet with flag: " + receiveTransferAck.getFlag() + " for file of " + receiveTransferAck.getAmountPackets() + " packets. Expected window: [" + receiveTransferAck.getWindow()[0] + "," + receiveTransferAck.getWindow()[1] + "]");
					currentWindow = new int[]{receiveTransferAck.getWindow()[0], receiveTransferAck.getWindow()[1]};

					// ADJUST IF WINDOW EXCEEDS LAST PACKAGE
					if (receiveTransferAck.getWindow()[1] > newFileSender.getAmountPackets()) {
						currentWindow = new int[]{receiveTransferAck.getWindow()[0],  newFileSender.getAmountPackets()+1};				
					}
				} else {
					System.out.println("[SENDER]("+ getClass().getName() + ") All packets transferred.\n");
					transferFinished = true;
				}
			}

			//--------------------------------------------------------
			// RUN: FILE COMPLETION PHASE
			//--------------------------------------------------------
			UDPPacketMK transferFinalMK = protocolImpl.createTransferFinalPacket(newFileSender);
			System.out.println(">>> Whole file has been send!!! Sending file acknowledgment");
			DatagramPacket sendTransferFinalPacket = new DatagramPacket(transferFinalMK.getAsRegularUDPData(),
					transferFinalMK.getAsRegularUDPData().length,
					this.IPAddressDest,
					TRANSFERPORT);	
			dsock.send(sendDataPacket);


			System.out.println("[SENDER]("+ getClass().getName() + ") Waiting for acknowledgment if file was transferred correctly");
			dsock.receive(receivePacket);

			System.out.println(">>> Whole file has been send!!!");
			dsock.close();


			// Phase 4: [LOOP] Sending file packets and wait for acknowlegdement

			// Phase 5: Finalizing file transfer
		} catch (IOException e) {
			System.out.println("[SENDER] ("+ getClass().getName() + ") File cannot be deconstructed.\n");
			e.printStackTrace();
		}
	}

	public void checkHosts(String subnet) {
		try {
			checkHosts("192.168.0");

			int timeout=1000;
			for (int i=1;i<255;i++){
				String host=subnet + "." + i;
				if (InetAddress.getByName(host).isReachable(timeout)){
					System.out.println(host + " is reachable");
				}
			}
		} catch (IOException e) {
			System.out.println("[SENDER] ("+ getClass().getName() + ") Check hosts gone wrong./n");
			e.printStackTrace();
		}
	}


	public FileHost getHost() {
		return this.host;
	}
}
