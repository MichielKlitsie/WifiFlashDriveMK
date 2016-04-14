package model;

public enum Flag {

	// Decimal     		// Binary
    WAIT(0),    	// 000000
    SETUP(1),  		// 000001
    TRANSFER(2),  // 000010
    CLOSE(4),    	// 000100
    ACK(8),     		// 001000
	SETUPACK(16),
	TRANSFERFINAL(32),
	RESEND(64);
	
    int state;
    Flag(int p) {
    	state = p;
    }
    
    
    
}
