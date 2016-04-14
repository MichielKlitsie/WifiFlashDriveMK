package utils;


import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

import javax.imageio.ImageIO;

import model.FileUDPPackets;
import model.Flag;
import model.UDPPacketMK;
import protocol.DataTransferProtocol;
import serverclient.FileHost;
import serverclient.TUI;



import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;

import javax.imageio.ImageIO;

import model.FileUDPPackets;
import serverclient.FileHost;
//
public class TestUtils implements protocol.Constants {


	public static void main(String[] args) throws IOException {
		
		
//				DatagramSocket clientSocket;
		//		DatagramSocket serverSocket;
//		String filename = "testTestJapieoJeahTest.png";
//		byte[] filenameBytes = new byte[256];
//		filenameBytes = utils.Utils.string2byteArray(filename, filenameBytes.length);
//		System.out.println(utils.Utils.byteArray2String(filenameBytes));
//		

		try {
			
			
			
			
//			HashMap <Integer, Boolean> resendPackets = new HashMap<Integer, Boolean>();
//			resendPackets.put(5, false);
//			resendPackets.put(6, false);
//			resendPackets.put(10, false);
//			int[] array = utils.Utils.hashSet2intArray(resendPackets.keySet());
			
			// Create a filehost
			FileHost fh1 = new FileHost();
			//			fh1.sendDiscoveryBroadcast();

			// Create a DUMMY SOCKET
			DatagramSocket clientSocket = new DatagramSocket(30000, InetAddress.getLocalHost());
			DataTransferProtocol protocolImp = new DataTransferProtocol();

//			// Create a file
			String dataPath = new File(System.getProperty("user.dir")).getAbsolutePath();
			FileUDPPackets newFile = new FileUDPPackets(dataPath+"/tempData/testImage.png");
//			
//			System.out.println(dataPath);
//			newFile.saveFile(dataPath);

			// Create setuppacket
			UDPPacketMK setupPacketMK = protocolImp.createSetupPacket(newFile);
			System.out.println(utils.Utils.byteArray2String(setupPacketMK.getPacketData()));
			DatagramPacket sendSetupPacket = new DatagramPacket(setupPacketMK.getAsRegularUDPData(),
					setupPacketMK.getAsRegularUDPData().length,
					fh1.getLocalIPAddress(),
					TRANSFERPORT);
			clientSocket.send(sendSetupPacket);
			//
			// TEST SETUP ACKNOWLEDGMENT
			System.out.println(">>>Waiting for acknowledgement");
			byte[] receiveData = new byte[1024];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			clientSocket.receive(receivePacket);
			UDPPacketMK receiveSetupAck = new UDPPacketMK(receivePacket.getData());

			System.out.println(">>>Packet with flag: " + receiveSetupAck.getFlag() + " for file of " + receiveSetupAck.getAmountPackets() + " packets. Expected window: [" + receiveSetupAck.getWindow()[0] + "," + receiveSetupAck.getWindow()[1] + "]");

			// TEST RETHROW SETUP ACKNOWLEGDMENT
			System.out.println(">>>Waiting for second acknowledgement");
			receivePacket = new DatagramPacket(receiveData, receiveData.length);
			clientSocket.receive(receivePacket);
			receiveSetupAck = new UDPPacketMK(receivePacket.getData());

			System.out.println(">>>Packet with flag: " + receiveSetupAck.getFlag() + " for file of " + receiveSetupAck.getAmountPackets() + " packets. Expected window: [" + receiveSetupAck.getWindow()[0] + "," + receiveSetupAck.getWindow()[1] + "]");
			
			
			
			
			
//			// Test FILETRANSFER 
//			//			byte[] receiveDataFile = new byte[1024];
			System.out.println(">>>Starting file transfer");
			UDPPacketMK dataPacketMK = newFile.getFilePacket(0);
			DatagramPacket sendDataPacket = new DatagramPacket(dataPacketMK.getAsRegularUDPData(),
					dataPacketMK.getAsRegularUDPData().length,
					fh1.getLocalIPAddress(),
					TRANSFERPORT);

			boolean transferFinished = false;
			int[] currentWindow = new int[]{receiveSetupAck.getWindow()[0], receiveSetupAck.getWindow()[1]};
			
			// TEST MISSING PACKAGES
//			while(!transferFinished) {
				
				// FIRST BATCH GOOD
				for (int i = 0; i<5; i++) {
					dataPacketMK = newFile.getFilePacket(i);
					System.out.println(">>> Packet nr " + i + " send with flag " + dataPacketMK.getFlag() + " with data size of " + dataPacketMK.getDataLength() + " bytes... Or " + dataPacketMK.getPacketData().length);
					sendDataPacket = new DatagramPacket(dataPacketMK.getAsRegularUDPData(),
							dataPacketMK.getAsRegularUDPData().length,
							fh1.getLocalIPAddress(),
							TRANSFERPORT);	
					clientSocket.send(sendDataPacket);
				}

				System.out.println(">>>Waiting for transfer acknowledgment...");
				clientSocket.receive(receivePacket);
				UDPPacketMK receiveTransferAck = new UDPPacketMK(receivePacket.getData());

							
			// SECOND BATCH WRONG
				int i = 5;
				dataPacketMK = newFile.getFilePacket(i);
				System.out.println(">>> Packet nr " + i + " send with flag " + dataPacketMK.getFlag() + " with data size of " + dataPacketMK.getDataLength() + " bytes... Or " + dataPacketMK.getPacketData().length);
				sendDataPacket = new DatagramPacket(dataPacketMK.getAsRegularUDPData(),
						dataPacketMK.getAsRegularUDPData().length,
						fh1.getLocalIPAddress(),
						TRANSFERPORT);	
				clientSocket.send(sendDataPacket);
				
				i = 6;
				dataPacketMK = newFile.getFilePacket(i);
				System.out.println(">>> Packet nr " + i + " send with flag " + dataPacketMK.getFlag() + " with data size of " + dataPacketMK.getDataLength() + " bytes... Or " + dataPacketMK.getPacketData().length);
				sendDataPacket = new DatagramPacket(dataPacketMK.getAsRegularUDPData(),
						dataPacketMK.getAsRegularUDPData().length,
						fh1.getLocalIPAddress(),
						TRANSFERPORT);	
				clientSocket.send(sendDataPacket);
				
				i = 9;
				dataPacketMK = newFile.getFilePacket(i);
				System.out.println(">>> Packet nr " + i + " send with flag " + dataPacketMK.getFlag() + " with data size of " + dataPacketMK.getDataLength() + " bytes... Or " + dataPacketMK.getPacketData().length);
				sendDataPacket = new DatagramPacket(dataPacketMK.getAsRegularUDPData(),
						dataPacketMK.getAsRegularUDPData().length,
						fh1.getLocalIPAddress(),
						TRANSFERPORT);	
				clientSocket.send(sendDataPacket);
//			}
			
			System.out.println(">>>Waiting for transfer acknowledgment...");
			clientSocket.receive(receivePacket);
			receiveTransferAck = new UDPPacketMK(receivePacket.getData());
			
			System.out.println(">>>Packet with flag: " + receiveTransferAck.getFlag() + " received");
			receiveTransferAck.getAcknowlegdment();
			
			if (receiveTransferAck.getFlag() == Flag.RESEND) {
				System.out.println(">>> Packet with flag: " + receiveTransferAck.getFlag() + ". Resending missing packages.");
				// Get the ack window of seq nrs to be resend
				int[] resendPackagesPadded = receiveTransferAck.getAcknowlegdment();
				int[] resendPackages = utils.Utils.trimIntArray(resendPackagesPadded);
				for (int j = 0; j<resendPackages.length; j++) {
					int seqNr = resendPackages[j];
					dataPacketMK = newFile.getFilePacket(seqNr);

					sendDataPacket = new DatagramPacket(dataPacketMK.getAsRegularUDPData(),
							dataPacketMK.getAsRegularUDPData().length,
							fh1.getLocalIPAddress(),
							TRANSFERPORT);	
					clientSocket.send(sendDataPacket);
					System.out.println(">>> Packet nr " + (seqNr+1) + " resend. Datasize: " + dataPacketMK.getDataLength() + " bytes. Checksum: " + dataPacketMK.getChecksumPacket());	
				}

				// OPTION 2: LAST PACKAGES HAS BEEN SEND
			}
			
			// TEST NORMAL FILE TRANSFER
//			while(!transferFinished) {
//				for (int i = currentWindow[0]; i<currentWindow[1]; i++) {
//					dataPacketMK = newFile.getFilePacket(i);
//					System.out.println(">>> Packet nr " + i + " send with flag " + dataPacketMK.getFlag() + " with data size of " + dataPacketMK.getDataLength() + " bytes... Or " + dataPacketMK.getPacketData().length);
//					sendDataPacket = new DatagramPacket(dataPacketMK.getAsRegularUDPData(),
//							dataPacketMK.getAsRegularUDPData().length,
//							fh1.getLocalIPAddress(),
//							TRANSFERPORT);	
//					clientSocket.send(sendDataPacket);
//				}
//
//				System.out.println(">>>Waiting for transfer acknowledgment...");
//				clientSocket.receive(receivePacket);
//				UDPPacketMK receiveTransferAck = new UDPPacketMK(receivePacket.getData());
////				System.out.println(">>>Packet with flag: " + receiveTransferAck.getFlag() + " for file of " + receiveTransferAck.getAmountPackets() + " packets. Expected window: [" + receiveTransferAck.getWindow()[0] + "," + receiveTransferAck.getWindow()[1] + "]");
//				
//				
//				currentWindow = new int[]{receiveTransferAck.getWindow()[0], receiveTransferAck.getWindow()[1]};
//				
//				if (receiveTransferAck.getWindow()[1] > newFile.getAmountPackets()) {
////					currentWindow = new int[]{receiveTransferAck.getWindow()[0], receiveTransferAck.getWindow()[1]};
//					transferFinished = true;
//				}
//			}
//
//			// Last phase, check if file is complete
//			UDPPacketMK transferFinalMK = protocolImp.createTransferFinalPacket(newFile);
//			System.out.println(">>> Whole file has been send!!! Sending file acknowledgment");
//			DatagramPacket sendTransferFinalPacket = new DatagramPacket(transferFinalMK.getAsRegularUDPData(),
//					transferFinalMK.getAsRegularUDPData().length,
//					fh1.getLocalIPAddress(),
//					TRANSFERPORT);	
//			clientSocket.send(sendTransferFinalPacket);
//			
//			
//			System.out.println(">>> Whole file has been send!!!");
			System.out.println(">>>Waiting for transfer acknowledgment...");
			clientSocket.receive(receivePacket);
			receiveTransferAck = new UDPPacketMK(receivePacket.getData());
			System.out.println(">>>Packet with flag: " + receiveTransferAck.getFlag() + " received");
			
			System.out.println("[SENDER] Packet with flag: " + receiveTransferAck.getFlag() + " for file of " + receiveTransferAck.getAmountPackets() + " packets. Expected window: [" + receiveTransferAck.getWindow()[0] + "," + receiveTransferAck.getWindow()[1] + "].");
			currentWindow = new int[]{receiveTransferAck.getWindow()[0], receiveTransferAck.getWindow()[1]};


			
			// Third BATCH GOOD
			for (i = currentWindow[0]; i<currentWindow[1]; i++) {
				dataPacketMK = newFile.getFilePacket(i);
				System.out.println(">>> Packet nr " + i + " send with flag " + dataPacketMK.getFlag() + " with data size of " + dataPacketMK.getDataLength() + " bytes... Or " + dataPacketMK.getPacketData().length);
				sendDataPacket = new DatagramPacket(dataPacketMK.getAsRegularUDPData(),
						dataPacketMK.getAsRegularUDPData().length,
						fh1.getLocalIPAddress(),
						TRANSFERPORT);	
				clientSocket.send(sendDataPacket);
			}

			System.out.println(">>>Waiting for transfer acknowledgment...");
			clientSocket.receive(receivePacket);
			receiveTransferAck = new UDPPacketMK(receivePacket.getData());

						
		// Fourth BATCH WRONG: 1 file
			i = 18;
			dataPacketMK = newFile.getFilePacket(i);
			System.out.println(">>> Packet nr " + i + " send with flag " + dataPacketMK.getFlag() + " with data size of " + dataPacketMK.getDataLength() + " bytes... Or " + dataPacketMK.getPacketData().length);
			sendDataPacket = new DatagramPacket(dataPacketMK.getAsRegularUDPData(),
					dataPacketMK.getAsRegularUDPData().length,
					fh1.getLocalIPAddress(),
					TRANSFERPORT);	
			clientSocket.send(sendDataPacket);
			
			
			System.out.println(">>>Waiting for timeout...");
			clientSocket.receive(receivePacket);
			receiveTransferAck = new UDPPacketMK(receivePacket.getData());
			
			

			System.out.println(">>>Packet with flag: " + receiveTransferAck.getFlag() + " received");
			receiveTransferAck.getAcknowlegdment();
			
			if (receiveTransferAck.getFlag() == Flag.RESEND) {
				System.out.println(">>> Packet with flag: " + receiveTransferAck.getFlag() + ". Resending missing packages.");
				// Get the ack window of seq nrs to be resend
				int[] resendPackagesPadded = receiveTransferAck.getAcknowlegdment();
				int[] resendPackages = utils.Utils.trimIntArray(resendPackagesPadded);
				for (int j = 0; j<resendPackages.length; j++) {
					int seqNr = resendPackages[j];
					dataPacketMK = newFile.getFilePacket(seqNr);

					sendDataPacket = new DatagramPacket(dataPacketMK.getAsRegularUDPData(),
							dataPacketMK.getAsRegularUDPData().length,
							fh1.getLocalIPAddress(),
							TRANSFERPORT);	
					clientSocket.send(sendDataPacket);
					System.out.println(">>> Packet nr " + (seqNr+1) + " resend. Datasize: " + dataPacketMK.getDataLength() + " bytes. Checksum: " + dataPacketMK.getChecksumPacket());	
				}

				// OPTION 2: LAST PACKAGES HAS BEEN SEND
			}
			
			
			System.out.println(">>>WAIT!!!");
			clientSocket.receive(receivePacket);
			receiveTransferAck = new UDPPacketMK(receivePacket.getData());
	
			if (receiveTransferAck.getFlag() == Flag.RESEND) {
				System.out.println(">>> Packet with flag: " + receiveTransferAck.getFlag() + ". Resending missing packages.");
				// Get the ack window of seq nrs to be resend
				int[] resendPackagesPadded = receiveTransferAck.getAcknowlegdment();
				int[] resendPackages = utils.Utils.trimIntArray(resendPackagesPadded);
				for (int j = 0; j<resendPackages.length; j++) {
					int seqNr = resendPackages[j];
					dataPacketMK = newFile.getFilePacket(seqNr);

					sendDataPacket = new DatagramPacket(dataPacketMK.getAsRegularUDPData(),
							dataPacketMK.getAsRegularUDPData().length,
							fh1.getLocalIPAddress(),
							TRANSFERPORT);	
					clientSocket.send(sendDataPacket);
					System.out.println(">>> Packet nr " + (seqNr+1) + " resend. Datasize: " + dataPacketMK.getDataLength() + " bytes. Checksum: " + dataPacketMK.getChecksumPacket());	
				}

				// OPTION 2: LAST PACKAGES HAS BEEN SEND
			}
			
			System.out.println(">>> Whole file has been send!!!");
			System.out.println(">>>Waiting for transfer acknowledgment...");
			clientSocket.receive(receivePacket);
			receiveTransferAck = new UDPPacketMK(receivePacket.getData());
			System.out.println(">>>Packet with flag: " + receiveTransferAck.getFlag() + " received");
			
			System.out.println("[SENDER] Packet with flag: " + receiveTransferAck.getFlag() + " for file of " + receiveTransferAck.getAmountPackets() + " packets. Expected window: [" + receiveTransferAck.getWindow()[0] + "," + receiveTransferAck.getWindow()[1] + "].");
			currentWindow = new int[]{receiveTransferAck.getWindow()[0], receiveTransferAck.getWindow()[1]};


			
			// Third BATCH GOOD
			for (i = currentWindow[0]; i<currentWindow[1]; i++) {
				dataPacketMK = newFile.getFilePacket(i);
				System.out.println(">>> Packet nr " + i + " send with flag " + dataPacketMK.getFlag() + " with data size of " + dataPacketMK.getDataLength() + " bytes... Or " + dataPacketMK.getPacketData().length);
				sendDataPacket = new DatagramPacket(dataPacketMK.getAsRegularUDPData(),
						dataPacketMK.getAsRegularUDPData().length,
						fh1.getLocalIPAddress(),
						TRANSFERPORT);	
				clientSocket.send(sendDataPacket);
			}
			
			
			clientSocket.close();
//
//		} catch (IOException e) {
//			System.out.println("[FRAMEWORK] Could not start the client, because: ");
//			e.printStackTrace();
		} catch (InterruptedException e) {
			System.out.println("[FRAMEWORK] Operation interrupted.");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("[FRAMEWORK] Unexpected Exception: ");
			e.printStackTrace();
		} finally {
			System.out.println(">>>Terminating program.");
		} 
	}
}
