package serverclient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import protocol.DataTransferProtocol;
import utils.*;

public class DiscoveryThread extends Thread implements protocol.Constants {

	//--------------------------------------------------------
	// INSTANCE VARIABLES
	//--------------------------------------------------------
	private FileHost host;
	private ArrayList<InetAddress> discoveredAddresses;

	//--------------------------------------------------------
	// CONSTRUCTOR
	//--------------------------------------------------------
	public DiscoveryThread(FileHost host) {
		super("DiscoveryThread");
		this.host = host;
		this.discoveredAddresses = new ArrayList<InetAddress>();
	}
	
	//--------------------------------------------------------
	// RUN
	//--------------------------------------------------------
	@Override
	public void run() {
		DatagramSocket socket;

		try {
			//Keep a socket open to listen to all the UDP trafic that is destined for this port
			socket = new DatagramSocket(DISCOVERYPORT, InetAddress.getByName("0.0.0.0"));
			socket.setBroadcast(true);


			//			
			//			Timeout.Start();
			//			Timeout.SetTimeout(1000, new DataTransferProtocol(), 28);


			while (true) {
				System.out.println("[BROADCAST] (" + getClass().getName() + ") Ready to receive broadcast packets!\n");
				//Receive a packet
				byte[] recvBuf = new byte[15000];
				DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
				socket.receive(packet);

				//Packet received
				System.out.println("[BROADCAST] (" + getClass().getName() + ") Discovery packet received from: " + packet.getAddress().getHostAddress());
				//				System.out.println("[BROADCAST] (" + getClass().getName() + ") Packet received; data: " + new String(packet.getData()));

				//See if the packet holds the right command (message)
				String message = new String(packet.getData()).trim();
				if (message.equals(DISCOVER_REQUEST)) {
					byte[] sendData = DISCOVER_RESPONSE.getBytes();

					//Send a response
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
					socket.send(sendPacket);

					System.out.println("[BROADCAST] (" + getClass().getName() + ") Response packet to: " + sendPacket.getAddress().getHostAddress());
				}
			}
		} catch (IOException ex) {
			Logger.getLogger(DiscoveryThread.class.getName()).log(Level.SEVERE, null, ex);
		}
	}


	//	public static DiscoveryThread getInstance() {
	//		return DiscoveryThreadHolder.INSTANCE;
	//	}
	//
	//	private static class DiscoveryThreadHolder {
	//		private static final DiscoveryThread INSTANCE = new DiscoveryThread();
	//	}

	//--------------------------------------------------------
	// SEND BROADCAST MESSAGE TO LOOK FOR MACHINES
	//--------------------------------------------------------
	public void sendDiscoveryBroadcast() {
		// Find the server using UDP broadcast
		try {
			//Open a random port to send the package
			DatagramSocket discoverSocket = new DatagramSocket();
			discoverSocket.setBroadcast(true);

			byte[] sendData = DISCOVER_REQUEST.getBytes();

			//Try the 255.255.255.255 first
			try {
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(DEFAULTADDRESS), DISCOVERYPORT);
				discoverSocket.send(sendPacket);
				System.out.println("[BROADCAST]" + getClass().getName() + ": Request packet sent to:" + DEFAULTADDRESS + "(DEFAULT)");
			} catch (Exception e) {
			}

			// Broadcast the message over all the network interfaces
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface networkInterface = interfaces.nextElement();

				// Don't want to broadcast to the loopback interface
				if (networkInterface.isLoopback() || !networkInterface.isUp()) {
					continue; 
				}


				for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
					InetAddress broadcast = interfaceAddress.getBroadcast();
					if (broadcast == null) {
						continue;
					}

					// Send the broadcast package!
					try {
						DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, 8888);
						discoverSocket.send(sendPacket);
					} catch (Exception e) {
					}

					System.out.println("[BROADCAST](" + getClass().getName() + ") Request packet sent to: " + broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName() + "\n");
				}
			}

			System.out.println("[BROADCAST](" + getClass().getName() + ") Done looping over all network interfaces. Now waiting for a reply!\n");

			//Wait for a response...
			byte[] recvBuf = new byte[15000];
			DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);

			// for a few seconds
			discoverSocket.setSoTimeout(BROADCASTTIMEOUT);
			//			while (true) {
			try {
				discoverSocket.receive(receivePacket);

				//We have a response
				System.out.println("[BROADCAST](" + getClass().getName() + ") Broadcast response from machine: " + receivePacket.getAddress().getHostAddress() + " or " + receivePacket.getSocketAddress());

				//Check if the message is correct
				String message = new String(receivePacket.getData()).trim();
				if (message.equals(DISCOVER_RESPONSE)) {
					//DO SOMETHING WITH THE SERVER'S IP (for example, store it in your controller)
					this.discoveredAddresses.add(receivePacket.getAddress());
					System.out.println("[BROADCAST](" + getClass().getName() + ") Response was correct.\n");
				} else {
					System.out.println("[BROADCAST](" + getClass().getName() + ") Response was not correct.\n");
				}
			} catch (SocketTimeoutException e) {
				System.out.println("[BROADCAST](" + getClass().getName() + ") No response on broadcast within " + BROADCASTTIMEOUT + " milliseconds.\n");
			} finally {
				discoverSocket.close();		
			}
			//			}

			//Close the port!

		} catch (IOException ex) {
			//		  Logger.getLogger(LoginWindow.class.getName()).log(Level.SEVERE, null, ex);
			ex.printStackTrace();
		}
	}
	//--------------------------------------------------------
	// GETTERS AND SETTERS
	//--------------------------------------------------------

}


