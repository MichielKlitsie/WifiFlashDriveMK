package protocol;

import java.io.IOException;


import model.FileUDPPackets;
import model.Flag;
import model.UDPPacketMK;

public class DataTransferProtocol {
	
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
	public UDPPacketMK createTransferResendPacket(int amountPackets, int[] resendPackages) {
		Flag flag = Flag.RESEND;
		UDPPacketMK transferACKPacket = new UDPPacketMK(flag, amountPackets, resendPackages);
		return transferACKPacket;
	}

	//--------------------------------------------------------
	// CREATE BASE PACKET: TRANSFER RESEND
	//--------------------------------------------------------
	public UDPPacketMK createTransferACKPacket(int amountPackets, int[] window) {
		Flag flag = Flag.ACK;
		UDPPacketMK transferACKPacket = new UDPPacketMK(flag, amountPackets, window);
		return transferACKPacket;
	}
	
	//--------------------------------------------------------
	// CREATE BASE PACKET: FINALIZE FILE TRANSFER
	//--------------------------------------------------------
	public UDPPacketMK createTransferFinalPacket(FileUDPPackets file) throws IOException {
		Flag flag = Flag.TRANSFERFINAL;
		
		UDPPacketMK transferACKPacket = new UDPPacketMK(flag, file.getFilename(), file.getChecksum(), file.getAmountPackets());
		return transferACKPacket;
		
	}

	
}
