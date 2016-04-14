package model;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;

import javax.imageio.ImageIO;

public class FileUDPPackets {

	//--------------------------------------------------------
	// INSTANCE VARIABLES
	//--------------------------------------------------------
	// Limits
	private int maxFileSize = 1000; //mb (total filesize
	private int maxDataPacketSize = 1024;

	private int totalDataLength; 
	private byte[] bufferTotalData;

	//	private int headerLength = 32+32+16+16;
	// File
	File file;
	private String path;
	private String filename;
	private String extension;
	
	private long checksumFile;
	private long checksumData;
	
	private int amountPackets;

	// Packets
	//	private ArrayList<UDPPacketMK> packetsAll;
	HashMap <Integer, UDPPacketMK> packetsAll = new HashMap <Integer, UDPPacketMK>();
	HashMap <Integer, UDPPacketMK> packetsAllUnderConstruction;
	boolean isFinished = false;

	// Source, Destination and port
	private InetAddress IPAddressSrc;// = InetAddress.getByName("localhost");
	private InetAddress IPAddressDst;// = InetAddress.getByName("localhost");
	private int port = 8003;
	

	//--------------------------------------------------------
	// CONSTRUCTOR
	//--------------------------------------------------------
	public FileUDPPackets(String fullfile) throws IOException {
		// Create File object
		this.file = new File(fullfile);
		
		// Create filename parts
		String[] splitFullFile = utils.Utils.splitFullFile(fullfile);
		this.path = splitFullFile[0];
		this.filename = splitFullFile[1];
		this.extension = splitFullFile[2];

		// Check if file exists
		if (file.exists()) {

			// Create filepackets
			createPacketsFromFile(file);

			// Create file checksum
			this.checksumFile = createChecksumFromFile(file);
			this.checksumData = createChecksumFromData();
			System.out.println(" - bufferTotalData length: " + bufferTotalData.length + "\n - Checksum full file: " + this.checksumFile + "\n - Checksum full data: " + this.checksumData + "\n\n");
		} else {
			System.out.println("[FILE]("+ getClass().getName() + ") File does not exist");
		}
	}

	public FileUDPPackets(int amountpackets) {
		// Create Empty object to be filled
		createDummyFileAllPackets(amountpackets);
	}



	//--------------------------------------------------------
	// DECONSTRUCTING: CREATING PACKETS
	//--------------------------------------------------------
	// Image
	public void createPacketsFromFile(File file) throws IOException {
		//TODO: create byte array from file
		byte[] bufferTotalData = utils.Utils.file2byteArray(file);
		this.bufferTotalData = bufferTotalData;
		this.totalDataLength = bufferTotalData.length;

		// How many datapackets?
		
		this.amountPackets = (int) Math.ceil(((double) this.totalDataLength) / ((double) this.maxDataPacketSize));;
		this.packetsAllUnderConstruction = new HashMap <Integer, UDPPacketMK>();

		UDPPacketMK dataTransferPacket = new UDPPacketMK(Flag.TRANSFER, new byte[this.maxDataPacketSize], this.amountPackets, 0);

		int pointerPrev = 0;
		int pointerNext = this.maxDataPacketSize;
		for (int seqNr=0; seqNr < this.amountPackets; seqNr++) {

			// Create a byte array of datapackets
			byte[] bufferDataPacket = Arrays.copyOfRange(bufferTotalData, pointerPrev, pointerNext);
			dataTransferPacket = new UDPPacketMK(Flag.TRANSFER, bufferDataPacket, this.amountPackets, seqNr);

			this.packetsAllUnderConstruction.put(seqNr, dataTransferPacket);

			// Update pointer
			pointerPrev = pointerNext;
			pointerNext = pointerNext + this.maxDataPacketSize;
			
			if (pointerNext > this.totalDataLength) {
				pointerNext = this.totalDataLength;
			}
		}

		System.out.println("[FILE]("+ getClass().getName() + ") Deconstructed file summary: \n - " + filename + "\n - Amount UDP Datagram-packets: " + this.amountPackets + "\n - Original size: " + this.totalDataLength + " bytes\n - All packets size: " + (pointerNext +  (this.amountPackets * dataTransferPacket.getHeaderLength())) + " bytes");
		this.isFinished = true;
		this.packetsAll = this.packetsAllUnderConstruction;
	}

	//--------------------------------------------------------
	// RECONSTRUCTING: CREATING FILE
	//--------------------------------------------------------
	public void createBufferDataFromPackets() throws IOException {
		if (this.isFinished) {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );


			for(Map.Entry<Integer, UDPPacketMK> entry: this.packetsAll.entrySet()) {
				UDPPacketMK packet = entry.getValue();
				outputStream.write(packet.getPacketData());
			}

			this.bufferTotalData = outputStream.toByteArray();

		} else {
			System.out.println("File has not been fully reconstructed");
		}
	}
	

	public void saveFile(String datapath) throws IOException {
		// Create empty file
		File f = new File(datapath +"/"+this.filename);
		f.getParentFile().mkdirs();
		f.createNewFile();
		

		// Write dataBuffer to file
		OutputStream out = null;
		try {
			FileOutputStream fos = new FileOutputStream(f);
			out = new BufferedOutputStream(fos);
			out.write(this.bufferTotalData);
			out.flush();
		} finally {
			if (out != null) out.close();

			System.out.println("[FILE]("+ getClass().getName() + ") File has been saved to: '" + datapath + "/" + this.filename + "'\n");
		}
	}

	//--------------------------------------------------------
	// SENDING PACKETS OF FILE
	//--------------------------------------------------------
	public UDPPacketMK getFilePacket(int seqNr) {
		if (isFinished) {
			return packetsAll.get(seqNr);
		} else {
			return packetsAllUnderConstruction.get(seqNr);
		}
	}

	public void createDummyFileAllPackets(int amountpackets) {
//		System.out.println("[FILE]("+ getClass().getName() + ") Created dummy file of " + amountpackets);
		this.packetsAllUnderConstruction = new HashMap <Integer, UDPPacketMK> (amountpackets);
		this.amountPackets = amountpackets;
		//		this.packetsAllUnderConstruction = new HashMap <Integer, UDPPacketMK>();
		for (int i = 0; i < amountpackets; i++) {
			this.packetsAllUnderConstruction.put(i, null);
		}
	}

	public void addFilePacket(UDPPacketMK packet) {
		int seqNr = packet.getSeqNr();
//		System.out.println("[FILE]("+ getClass().getName() + ") Packet nr " + seqNr + " received (datalength " +packet.getPacketData().length +"). Written to temp file");
		this.packetsAllUnderConstruction.put(seqNr, packet);

		if(!packetsAllUnderConstruction.containsValue(null)){
			// NOTICE ALL PACKETS HAVE ARRIVED.
			System.out.println("[FILE]("+ getClass().getName() + ") No empty packets left..." + "\n");
			this.packetsAll = this.packetsAllUnderConstruction;
			this.isFinished = true;
			try {
				createBufferDataFromPackets();
			} catch (IOException e) {
				System.out.println("[FILE]("+ getClass().getName() + ") File could not be reconstructed.");
				e.printStackTrace();
			}
//			System.out.println("[FILE]("+ getClass().getName() + ") Checksum of arrived file." + "\n");
		}
	}

	public boolean isFinished() {
		return this.isFinished;
	}

	public long createChecksumFromFile(File file) {
		long checksumInputLength = file.length();
		byte[] checksumInputContent = new byte[(int) checksumInputLength];
		CRC32 crc = new CRC32();
		crc.update(checksumInputContent);
		long checksumFile = crc.getValue();

		return checksumFile;                                   
	}
	
	public long createChecksumFromData() {
//		long checksumInputLength = this.bufferTotalData.length;
//		byte[] checksumInputContent = new byte[(int) checksumInputLength];
		CRC32 crc = new CRC32();
		crc.update(this.bufferTotalData, 0, this.bufferTotalData.length);
		long checksumData = crc.getValue();

		return checksumData;                                   
	}


	//--------------------------------------------------------
	// GETTERS AND SETTERS
	//--------------------------------------------------------
	public int getAmountPackets() {
		return amountPackets;
	}

	public String getFilename() {
		return filename;
	}
	
	public String getExtension() {
		return extension;
	}

	public void setFilename(String filename) {
		this.filename = filename;
		this.extension = utils.Utils.createExtension(filename);
	}

	public long getChecksum() {
		return checksumFile;
	}


	public byte[] getBufferTotalData() {
		return bufferTotalData;
	}

}
