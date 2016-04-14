package serverclient;

import java.io.IOException;
import java.net.DatagramPacket;

import model.FileUDPPackets;
import model.Flag;
import model.UDPPacketMK;

public class PacketParser {

	private FileReceiverThread fileReceiverThread;
	private FileSenderThread fileSenderThread;
	private FileHost host;
	private boolean isSender;

	public PacketParser(FileReceiverThread fileReceiverThread) {
		this.fileReceiverThread = fileReceiverThread;
		this.host = fileReceiverThread.getHost();
		this.isSender = false;
	}

	public PacketParser(FileSenderThread fileSenderThread) {
		this.fileSenderThread = fileSenderThread;
		this.host = fileSenderThread.getHost();
		this.isSender = true;
	}

	public void parsePacket(DatagramPacket receivePacket) throws IOException {

		// Interpret packet as UDPPacketMK
		UDPPacketMK subPacket = new UDPPacketMK(receivePacket.getData());
		Flag flag = subPacket.getFlag();
		System.out.println("[RECEIVER]("+ getClass().getName() + ") Parsing packet with flag: " + flag);

		switch(flag) {
		case WAIT:
			System.out.println("[RECEIVER]("+ getClass().getName() + ") Waiting for a package" + "\n");
			break;
		case SETUP:
			fileReceiverThread.setLastAckPacket(receivePacket);
			System.out.println("[RECEIVER]("+ getClass().getName() + ") Setup-packet received from " + receivePacket.getAddress() + ":" +receivePacket.getPort());
			fileReceiverThread.setupFileTransfer(receivePacket);
			//			fileReceiverThread.setupFileTransfer(receivePacket);
			break;
		case SETUPACK:
			System.out.println("[SENDER]("+ getClass().getName() + ") Received SETUP-ACK\n");
			System.out.println("[SENDER]("+ getClass().getName() + ") Start sending packages.\n");


			break;
		case ACK:
			break;
		case TRANSFER:
//						System.out.println("[RECEIVER]("+ getClass().getName() + ") Data-packet received from " + receivePacket.getAddress() + ":" +receivePacket.getPort());
			if (fileReceiverThread.checkChecksumPacket(receivePacket)) {
				fileReceiverThread.writeDataPacket(receivePacket);
				
				if(fileReceiverThread.isReadyForCheck()) {
					fileReceiverThread.setLastAckPacket(receivePacket);
					fileReceiverThread.checkAckWindow(receivePacket);
				}
			} else {
				// Request packet again...
				System.out.println("[SENDER]("+ getClass().getName() + ") Packet was damaged.\n");
				//
				// TODO:				
				// ASK FOR RESEND
				//
			}
			break;
		case TRANSFERFINAL:
			System.out.println("[RECEIVER]("+ getClass().getName() + ") Starting final file check.\n");
			fileReceiverThread.checkFinalFile(receivePacket);
			break;
		}
	}
}
