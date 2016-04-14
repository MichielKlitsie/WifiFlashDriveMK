package protocol;

import java.io.IOException;

import model.FileUDPPackets;
import model.Flag;
import model.UDPPacketMK;

import utils.ITimeoutEventHandler;

public class DataTransferProtocol implements ITimeoutEventHandler {
	
	//--------------------------------------------------------
	// CONSTRUCTOR
	//--------------------------------------------------------
	public DataTransferProtocol() {
	}

	//--------------------------------------------------------
	// CREATE BASE PACKET: SETUP FILETRANSFER
	//--------------------------------------------------------
	public UDPPacketMK createSetupPacket(FileUDPPackets file) {
		// GET NECCESSARY FIELDS
		Flag flag = Flag.SETUP;
		int amountPackets = file.getAmountPackets();
		String filename = file.getFilename();
		// CREATE THE PACKET
		UDPPacketMK setupPacket = new UDPPacketMK(flag, amountPackets, filename);
		
		return setupPacket;
	}
	
	//--------------------------------------------------------
	// CREATE BASE PACKET: SETUP ACKNOWLEDGMENT
	//--------------------------------------------------------
	public UDPPacketMK createSetupACKPacket(int amountPackets) {
		Flag flag = Flag.SETUPACK;
		UDPPacketMK setupACKPacket = new UDPPacketMK(flag, amountPackets);
		return setupACKPacket;
	}
	
	//--------------------------------------------------------
	// CREATE BASE PACKET: TRANSFER ACKNOWLEDGEMENT
	//--------------------------------------------------------
	public UDPPacketMK createTransferACKPacket(int amountPackets, int[] window) {
		Flag flag = Flag.ACK;
		UDPPacketMK transferACKPacket = new UDPPacketMK(flag, amountPackets, window);
		return transferACKPacket;
	}

	public UDPPacketMK createTransferFinalPacket(FileUDPPackets file) throws IOException {
		Flag flag = Flag.TRANSFERFINAL;
		
		UDPPacketMK transferACKPacket = new UDPPacketMK(flag, file.getFilename(), file.getChecksum(), file.getAmountPackets());
		return transferACKPacket;
		
	}

	
	// TIMERS
	@Override
	public void TimeoutElapsed(Object tag) {
		int z=(Integer)tag;
		// handle expiration of the timeout:
		System.out.println("Timer expired with tag="+z);
		
	}
}
