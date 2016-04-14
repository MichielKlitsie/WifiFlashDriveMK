package serverclient;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;

public class UserInputParser implements protocol.Constants {

	//--------------------------------------------------------
	// INSTANCE VARIABLES
	//--------------------------------------------------------
	/** The client. */
	private ClientInputThread client;

	/** The parsed output string. */
	private String parsedOutputString;

	/** The parsed input string. */
	private String parsedInputString;

	private TUI mTUI;

	//--------------------------------------------------------
	// CONSTRUCTOR
	//--------------------------------------------------------
	public UserInputParser(ClientInputThread client) {
		this.client = client;
		this.mTUI = new TUI();
	}

	//--------------------------------------------------------
	// PARSER: COMMAND LINE INPUT
	//--------------------------------------------------------
	public String parseInput(String inputString) throws IOException {
		String stringAfterCommand = "";
		String[] stringParts = inputString.split(DELIMITER);
		String command = stringParts[0].trim().toUpperCase();
		int amountArgs = stringParts.length - 1;
		if (amountArgs > 0) {
			stringAfterCommand = inputString.substring(stringParts[0].length());
		}
		String parsedInputString = "";

		switch (command) {
		//--------------------------------------------------------
		// BASE TUI
		//--------------------------------------------------------
		case "1":
			//			client.print("[TUI]("+ getClass().getName() + ") Option 1 chosen: File availability");
			client.print(client.getHost().getStringAvailableFiles());
			break;
		case "2":
			//			
			client.print(DATAPATHTUI);
			break;	
		case "3":
			client.getHost().getDiscoveryThread().sendDiscoveryBroadcast();
			break;
		case "4":
			//			client.print("[TUI]("+ getClass().getName() + ") Option 2 chosen. Transfer file");
			client.print(TRANSFERTUI);
			client.getHost().getDiscoveryThread().displayDiscoveredAddresses((client.getHost().getDiscoveryThread().getDiscoveredAddresses()));
			break;
			//--------------------------------------------------------
			// File TRANSFER
			//--------------------------------------------------------
		case "SETUP":
			if (amountArgs == 2) {
				// Check file existence before starting sender thread
				String fullfile = stringParts[1];
				String IPAddressDest = stringParts[2];
				try {
					fullfile = client.getHost().getDataPath() + "/"+ fullfile;
					if (client.getHost().checkFile(fullfile)) {
						// If file exists start a thread based on the information provided
						//
						// TODO: correct the IPAddressDest
						//
						client.getHost().createFileSenderThread(fullfile, IPAddressDest);	
					} else {
						client.print("[TRANSFER]("+ getClass().getName() + ") File cannot be found. Try again.\n");	
					}


				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				client.print("[TRANSFER]("+ getClass().getName() + ") Please use format described.\n");
			}
			break;
		case "DATAPATH":
			if (amountArgs == 1) {
				String dataPath = stringParts[1].trim();
				if (dataPath.equals("..")) {
					client.getHost().setDataPath(new File(client.getHost().getDataPath()).getParent());
				} else if (dataPath.equals("LS")){
					File file = new File(client.getHost().getDataPath());
					String[] directories = file.list(new FilenameFilter() {
						  @Override
						  public boolean accept(File current, String name) {
						    return new File(current, name).isDirectory();
						  }
						});
					client.print(Arrays.toString(directories));
					
				} else if (client.getHost().checkFile(client.getHost().getDataPath()+"/"+dataPath)) {
					client.getHost().setDataPath(client.getHost().getDataPath()+"/"+dataPath);	
					client.print(client.getHost().getStringAvailableFiles());
				} else {
					client.print("[TRANSFER]("+ getClass().getName() + ") Folder cannot be found. Try again.\n");	
				}
			} else if (amountArgs == 2) {
				String subcommand = stringParts[1].trim();
				if (subcommand.equals("MK")) {
					String mkDataPath = stringParts[2].trim();
					boolean succes = new File(client.getHost().getDataPath()+"/"+mkDataPath).mkdirs();
					if (!succes) {
						client.print("[TRANSFER]("+ getClass().getName() + ") Directory creation failed.\n");
					}
				} else {
					client.print("[TRANSFER]("+ getClass().getName() + ") Please use format described.\n");
				}
			} else {
				client.print("[TRANSFER]("+ getClass().getName() + ") Please use format described.\n");
			}
			break;
		case "STATE":
			client.getHost().showStateThreads();
			break;
		case "EXIT":
			client.getHost().shutdown();
			break;
		default:
			client.print("[CLIENT] "+ getClass().getName() + "Input not recognised.\n");
		}
		return command;
	}
}
