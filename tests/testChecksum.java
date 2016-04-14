package tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.math.*;
import java.util.Random;
import utils.*;

import org.junit.Before;
import org.junit.Test;

import model.Flag;
import model.UDPPacketMK;

public class testChecksum {

	@Before
	public void setUp() throws Exception {

	}

	@Test
	public void testChecksum() {
		try {
			byte[] data = new byte[1024];
			Random rand = new Random();
			int randomInt = rand.nextInt(100);
			data = utils.Utils.int2ByteArray(randomInt, data.length);
			UDPPacketMK testPacket1 = new UDPPacketMK(Flag.TRANSFER, data, 200, 5);
			UDPPacketMK testPacket2 = new UDPPacketMK(Flag.TRANSFER, data, 200, 5);
			long a = testPacket1.getChecksumPacket();
			long b = testPacket2.getChecksumPacket();
			System.out.println("Checksum 1: " + a);
			System.out.println("Checksum 2: " + b);
			
			byte[] aBytes = new byte[32];
			aBytes = utils.Utils.long2byteArray(a, aBytes.length);
			
			long c = utils.Utils.byteArray2Long(aBytes);
			System.out.println("Checksum after conv:" + c);
			
			long d = testPacket1.createChecksumPacket();
			System.out.println("Checksum after conv:" + d);
			
			int randomInt2 = rand.nextInt(100);
			byte[] data2 = new byte[1024];
			data2 = utils.Utils.int2ByteArray(randomInt2, data.length);
			testPacket2.setPacketData(data2);
			long e = testPacket2.createChecksumPacket();
			System.out.println("Checksum after data change:" + e);
			
			byte[] data3 = data;
//			byte b1 = (byte) 129;
			String s1 = String.format("%8s", Integer.toBinaryString(data3[1] & 0xFF)).replace(' ', '0');
			System.out.println(s1); // 10000001

			data3[1] = (byte) (data3[1] | (1 << 2)); 
			String s2 = String.format("%8s", Integer.toBinaryString(data3[1] & 0xFF)).replace(' ', '0');
			System.out.println(s2); // 00000010
//			data3[1] ^=  1 << bit;
			testPacket1.setPacketData(data3);
			long f = testPacket1.createChecksumPacket();
			System.out.println("Checksum after single bit change:" + f);
			
			data3[1] = (byte) (data3[1] | (1 << 4)); 
			String s3 = String.format("%8s", Integer.toBinaryString(data3[1] & 0xFF)).replace(' ', '0');
			System.out.println(s3); // 00000010
			testPacket1.setPacketData(data3);
			long g = testPacket1.createChecksumPacket();
			System.out.println("Checksum after a two bit change:" + g);
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		fail("Not yet implemented");
	}

}
