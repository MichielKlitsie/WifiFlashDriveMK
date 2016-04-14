package model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.CRC32;

import utils.Utils.*;



public class UDPPacketMK {


	//--------------------------------------------------------
	// Instance variables
	//--------------------------------------------------------

	//Subheader, based on TCP-header
	//[0 			16 			31]
	//[flag 		|amountPackets]
	//[seqNrBytes				  ]
	//[ack	 					  ]
	//[dataLength	|window		  ]		  
	//[checksum		 	  		  ]

	private Flag flag; //16 bytes
	private byte[] flagBytes = new byte[16];
	private int amountPackets; // 16 bytes

	private byte[] amountPacketsBytes = new byte[16];

	private int seqNr; //32 bytes
	private byte[] seqNrBytes = new byte[32];

	private int[] acknowlegdment; //32 bytes
	private byte[] ackBytes = new byte[32];

	private int[] window; //16 bytes
	private byte[] windowBytes = new byte[16];
	private int dataLength; //16 bytes
	private byte[] dataLengthBytes = new byte[16];

	private long checksumData; // 32 bytes
	private byte[] checksumBytes = new byte[32];


	private int headerLength = flagBytes.length + amountPacketsBytes.length 
			+ seqNrBytes.length 
			+ ackBytes.length 
			+ dataLengthBytes.length + windowBytes.length 
			+ checksumBytes.length;// 16 bytes
	//	private byte[] headerLengthBytes = new byte[16];



	private DatagramPacket packetsSingle;
	private byte[] headerBytes = new byte[headerLength];
	private byte[] data;




	//--------------------------------------------------------
	// CONSTRUCTORS
	//--------------------------------------------------------
	//--------------------------------------------------------
	// CONSTRUCTOR: SETUP PACKET
	//--------------------------------------------------------
	public UDPPacketMK(Flag flag, int amountPackets, String filename) {
		this.flag = flag;

		try {
			switch (flag) {
			case SETUP:
				setPacketData(utils.Utils.string2byteArray(filename));
				setSetupHeader(amountPackets);
				break;
				//			case SETUPACK:
				//				setSetupAckHeader(amountPackets);
				//				setPacketData(new byte[0]);
				//				break;
				//			case ACK:
				//				setTransferAckHeader(amountPackets, this.window);
				//				setPacketData(new byte[0]);
				//				break;
			default:
				break;
			}

			createChecksumPacket();

		} catch (IOException e) {
			System.out.println("Error in constructing set-up packet");
			e.printStackTrace();
		}
	}

	//--------------------------------------------------------
	// CONSTRUCTOR: SETUP ACKNOWLEDGEMENT
	//--------------------------------------------------------
	//	Constructor for setup & transfer acknowledgment packets
	public UDPPacketMK(Flag flag, int amountPackets) {
		this.flag = flag;
		// Add dummy data for empty packets

		try {
			switch (flag) {
			case SETUPACK:
				setSetupAckHeader(amountPackets);
				setPacketData(new byte[0]);
				break;
			case ACK:
				setTransferAckHeader(amountPackets, this.window);
				setPacketData(new byte[0]);
				break;
			}
		} catch (IOException e) {
			System.out.println("Error in constructing set-up packet");
			e.printStackTrace();
		}
	}

	public UDPPacketMK(Flag flag, int amountPackets, int[] newWindow) {
		this.flag = flag;
		// Add dummy data for empty packets

		try {
			switch (flag) {
			case ACK:
				setTransferAckHeader(amountPackets, newWindow);
				setPacketData(new byte[0]);
				break;
			case RESEND:
				setResendHeader(amountPackets, newWindow);
				setPacketData(new byte[0]);
				break;
			}
		} catch (IOException e) {
			System.out.println("Error in constructing set-up packet");
			e.printStackTrace();
		}
	}

	// Constructor used for file transfer
	public UDPPacketMK(Flag flag, byte[] data, int amountPackets, int seqNr) throws IOException {
		setPacketData(data);
		setTransferHeader(amountPackets, seqNr);
		this.checksumData = createChecksumPacket();
	}

	// Constructor used for deconstructing received data
	public UDPPacketMK(byte[] data) {
		this.headerBytes = Arrays.copyOfRange(data, 0, headerLength);
		setHeaderFromBytes(headerBytes);
		if (this.dataLength != 0) {
			setPacketData(Arrays.copyOfRange(data, this.headerLength, (this.headerLength+this.dataLength)));
		}
	}

	// Constructor used for finalizing filetransfer
	public UDPPacketMK(Flag flag, String filename, Long fileChecksum, int amountPackets) throws IOException {
		this.flag = flag;
		createDataFinalTransfer(filename, fileChecksum);
		setTransferFinalHeader(amountPackets);
	}

	//--------------------------------------------------------
	// HEADERS: Create different type of headers
	//--------------------------------------------------------

	public void createDataFinalTransfer(String filename, Long fileChecksum) throws IOException {
		//		byte[] filenameBytes = new byte[32];
		//		filenameBytes = utils.Utils.string2byteArray(filename, filenameBytes.length);
		//		byte[] filetypeBytes = new byte[32];
		//		filenameBytes = utils.Utils.string2byteArray(filetype, filetypeBytes.length);
		byte[] fileChecksumBytes = new byte[32];
		fileChecksumBytes = utils.Utils.long2byteArray(fileChecksum, fileChecksumBytes.length);
		//		ByteBuffer bb = ByteBuffer.allocate(filenameBytes.length + filetypeBytes.length + fileChecksumBytes.length);
		//		bb.put(filenameBytes);
		//		bb.put(filetypeBytes);
		//		bb.put(fileChecksumBytes);
		//		this.data = bb.array();
		this.data = fileChecksumBytes;
		this.dataLength = this.data.length;
	}

	public void setHeaderFromBytes(byte[] headerBytes) {
		//Subheader, based on TCP-header
		//[0 			16 			31]
		//[flag 		|amountPackets]
		//[seqNr					  ]
		//[ack	 					  ]
		//[dataLength	|window		  ]		  
		//[checksum		 	  		  ]

		int pointerPrev = 0;
		int pointerNext = flagBytes.length;
		this.flagBytes = Arrays.copyOfRange(headerBytes, pointerPrev, pointerNext);
		this.flag = Flag.values()[utils.Utils.byteArray2Int(this.flagBytes)];

		pointerPrev = pointerNext;
		pointerNext = pointerNext + amountPacketsBytes.length;
		this.amountPacketsBytes = Arrays.copyOfRange(headerBytes, pointerPrev, pointerNext);
		this.amountPackets = utils.Utils.byteArray2Int(this.amountPacketsBytes);

		pointerPrev = pointerNext;
		pointerNext = pointerNext + seqNrBytes.length;
		this.seqNrBytes = Arrays.copyOfRange(headerBytes, pointerPrev, pointerNext);
		this.seqNr = utils.Utils.byteArray2Int(this.seqNrBytes);

		pointerPrev = pointerNext;
		pointerNext = pointerNext + ackBytes.length;
		this.ackBytes = Arrays.copyOfRange(headerBytes, pointerPrev, pointerNext);
		this.acknowlegdment = utils.Utils.byteArray2IntArray(this.ackBytes);

		pointerPrev = pointerNext;
		pointerNext = pointerNext + dataLengthBytes.length;
		this.dataLengthBytes = Arrays.copyOfRange(headerBytes, pointerPrev, pointerNext);
		this.dataLength = utils.Utils.byteArray2Int(this.dataLengthBytes);

		pointerPrev = pointerNext;
		pointerNext = pointerNext + windowBytes.length;
		this.windowBytes = Arrays.copyOfRange(headerBytes, pointerPrev, pointerNext);
		this.window = utils.Utils.byteArray2IntArray(this.windowBytes);

		pointerPrev = pointerNext;
		pointerNext = pointerNext + checksumBytes.length;
		this.checksumBytes = Arrays.copyOfRange(headerBytes, pointerPrev, pointerNext);
		this.checksumData = utils.Utils.byteArray2Long(this.checksumBytes);
	}


	public void setTransferHeader(int amountPackets, int seqNr) throws IOException {
		//Subheader, based on TCP-header
		//[0 			16 			31]
		//[flag 		|amountPackets]
		//[seqNrBytes				  ]
		//[ack	 					  ]
		//[dataLength	|window		  ]		  
		//[checksum		 	  		  ]
		//		byte[] headerTemp = new byte[this.headerLength];
		this.flag = Flag.TRANSFER;
		this.amountPackets = amountPackets;
		this.seqNr = seqNr;
		this.acknowlegdment = new int[1]; 
		this.window = new int[1];
		//		this.dataLength = this.data.length; // See setPacketData()
		createSubHeader();
	}

	public void setSetupHeader(int amountPackets) throws IOException {
		//Subheader, based on TCP-header
		//[0 			16 			31]
		//[flag 		|amountPackets]
		//[seqNrBytes				  ]
		//[ack	 					  ]
		//[dataLength	|window		  ]		  
		//[checksum		 	  		  ]
		//		byte[] headerTemp = new byte[this.headerLength];
		this.flag = Flag.SETUP;
		this.amountPackets = amountPackets;
		this.seqNr = -1;
		this.acknowlegdment = new int[1]; 
		this.window = new int[1];
		//		this.dataLength = 0;
		createSubHeader();
	}

	public void setSetupAckHeader(int amountPackets) throws IOException {
		//Subheader, based on TCP-header
		//[0 			16 			31]
		//[flag 		|amountPackets]
		//[seqNrBytes				  ]
		//[ack	 					  ]
		//[dataLength	|window		  ]		  
		//[checksum		 	  		  ]
		//		byte[] headerTemp = new byte[this.headerLength];
		this.flag = Flag.SETUPACK;
		this.amountPackets = amountPackets;
		this.seqNr = -1;
		this.acknowlegdment = new int[1]; 
		this.window = new int[]{0, 5};
		this.dataLength = 0;
		//		this.checksumData = 5;
		createSubHeader();
	}

	public void setTransferAckHeader(int amountPackets, int[] currentWindow) throws IOException {
		//Subheader, based on TCP-header
		//[0 			16 			31]
		//[flag 		|amountPackets]
		//[seqNrBytes				  ]
		//[ack	 					  ]
		//[dataLength	|window		  ]		  
		//[checksum		 	  		  ]
		this.flag = Flag.ACK;
		this.amountPackets = amountPackets;
		this.seqNr = -1;
		this.acknowlegdment = new int[1]; 
		this.window = new int[]{currentWindow[0], currentWindow[1]};
		this.dataLength = 0;
		//		this.checksumData = 5;
		createSubHeader();
	}
	
	public void setResendHeader(int amountPackets, int[] resendPackages) throws IOException {
		this.flag = Flag.RESEND;
		this.amountPackets = amountPackets;
		this.seqNr = -1;
		this.acknowlegdment = resendPackages; 
		this.window = new int[1];
		this.dataLength = 0;
		createSubHeader();
	}

	public void setTransferFinalHeader(int amountPackets) throws IOException {
		//Subheader, based on TCP-header
		//[0 			16 			31]
		//[flag 		|amountPackets]
		//[seqNrBytes				  ]
		//[ack	 					  ]
		//[dataLength	|window		  ]		  
		//[checksum		 	  		  ]
		this.flag = Flag.TRANSFERFINAL;
		this.amountPackets = amountPackets;
		this.seqNr = -1;
		this.acknowlegdment = new int[1]; 
		this.window = new int[1];
		this.dataLength = 0;
		//			this.checksumData = 5;
		createSubHeader();
	}


	//--------------------------------------------------------
	// Methods: Create header
	//--------------------------------------------------------
	public void createSubHeader() throws IOException {
		// Info: int seqNr [32 bytes], acknowlegdment [32 bytes], window [16 bytes], dataLength[16 bytes]
		this.flagBytes = utils.Utils.int2ByteArray(this.flag.ordinal(), this.flagBytes.length);
		this.amountPacketsBytes = utils.Utils.int2ByteArray(this.amountPackets, this.amountPacketsBytes.length);
		this.seqNrBytes = utils.Utils.int2ByteArray(this.seqNr, this.seqNrBytes.length);
		this.ackBytes = utils.Utils.intArray2ByteArray(this.acknowlegdment, this.ackBytes.length);
		this.dataLengthBytes = utils.Utils.int2ByteArray(this.dataLength, this.dataLengthBytes.length);
		this.windowBytes = utils.Utils.intArray2ByteArray(this.window, this.windowBytes.length);
		if (dataLength == 0) {
			this.checksumBytes = new byte[32];
		} else {
			this.checksumData = createChecksumPacket();
			this.checksumBytes = utils.Utils.long2byteArray(this.checksumData, checksumBytes.length);
		}

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		outputStream.write(this.flagBytes);
		outputStream.write(this.amountPacketsBytes);
		outputStream.write(this.seqNrBytes);
		outputStream.write(this.ackBytes);
		outputStream.write(this.dataLengthBytes);
		outputStream.write(this.windowBytes);
		outputStream.write(this.checksumBytes);

		this.headerBytes = outputStream.toByteArray();
		//		System.out.println("[FRAMEWORK] Header created of " + headerTemp.length + " bytes/n");

	}

	public long createChecksumPacket() {
		if(this.dataLength != 0) {
			CRC32 crc = new CRC32();
			crc.update(this.data, 0, this.data.length);
			long checksumDataTemp = crc.getValue();
			return checksumDataTemp;           
		} else {
			return 0;
		}
	}

	//--------------------------------------------------------
	// GETTERS/SETTERS
	//--------------------------------------------------------
	public void setPacketData(byte[] data) {
		this.data = data;
		this.dataLength = data.length;
	}

	public byte[] getPacketData() {
		return this.data;
	}

	public byte[] getHeader() {
		return this.headerBytes;
	}




	public long getChecksumPacket() {
		return this.checksumData;
	}

	public void setChecksumPacket(long checksumData) {
		this.checksumData = checksumData;

	}



	public int getAmountPackets() {
		return this.amountPackets;
	}

	public int getHeaderLength() {
		return headerLength;
	}


	public void setHeaderLength(int headerLength) {
		this.headerLength = headerLength;
	}

	public void setSeqNr(int seqNr) {
		this.seqNr = seqNr;
	}
	public int getSeqNr() {
		//		byte[] subHeaderBytes = getSubHeaderBytes();
		//		byte[] seqNrBytes = int2byteArray(seqNr, 32);
		//		byte[] seqNrBytes = Arrays.copyOfRange(subHeaderBytes, 0, 32);
		//		return ByteBuffer.wrap(seqNrBytes).getInt();
		return this.seqNr;

	}

	public byte[] getSubHeaderBytes() {
		return Arrays.copyOfRange(packetsSingle.getData(), 0, this.headerLength);
	}

	public Flag getFlag() {
		return flag;
	}

	public void setFlag(Flag flag) {
		this.flag = flag;
	}

	public byte[] getAsRegularUDPData() throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		outputStream.write(this.headerBytes);
		outputStream.write(this.data);		
		return outputStream.toByteArray();
	}

	public int[] getWindow() {
		return this.window;
	}

	public int getDataLength() {
		return this.dataLength;
	}
	
	public int[] getAcknowlegdment() {
		return this.acknowlegdment;
	}

}
