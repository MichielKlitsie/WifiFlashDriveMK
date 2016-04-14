package protocol;

public interface Constants {

	public static final String DISCOVER_REQUEST = "DISCOVER_REQUEST";
	public static final String DISCOVER_RESPONSE = "DISCOVER_RESPONSE";
	public static final String DEFAULTADDRESS = "255.255.255.255";

	public static final int DISCOVERYPORT = 8888;
	public static final int TRANSFERPORT = 8003;

	public static final int MAXDATASIZEPACKET = 1024; //bytes
	public static final int WINDOWSIZE = 5; //packets

	public static final int ACKTIMEOUT = 120000; //milliseconds
	public static final int BROADCASTTIMEOUT = 5000; //milliseconds
	public static final int NUMBYTESLP = 4;

	// Delimiters
	public static final String DELIMITER = " ";
	public static final String ARG_DELIMITER = ";";

	// TUI strings
	public static final String STARTTUI = "[START-SCREEN] Press number to choose option.\n"
			+ "1) Show available files in datafolder.\n"
			+ "2) Change path of datafolder.\n"
			+ "3) Search available destinations (using broadcast).\n"
			+ "4) Transfer a file.\n\n";

	public static final String TRANSFERTUI = "[TRANSFER-TUI] Please enter the following:\n"
			+ "SETUP <filename> <destination IP>"; 
	// Choose a destination (IP-address and port).\n"
	//				+ "3) Start transfer.\n";
	
	public static final String DATAPATHTUI = "[DATAPATH-TUI] Please enter the following:\n"
			+ "DATAPATH <relative datapath> \nOR\nDATAPATH <..> to go to parent folder\nOR\nDATAPATH <LS> to show subdirectories.\n"; 
}
