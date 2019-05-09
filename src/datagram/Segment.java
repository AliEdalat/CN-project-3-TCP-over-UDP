package datagram;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class Segment {
	private byte[] dataBytes;
	private boolean syn;
	private boolean ack;
	private int sourcePort;
	private int destinationPort;
	private int sequenceNumber;
	private int acknowledgmentNumber;
	private int windowSize;
	
	public Segment(byte[] dataBytes, boolean syn, boolean ack, int sourcePort, int destinationPort,
			int sequenceNumber, int acknowledgmentNumber, int windowSize) {
		this.dataBytes = dataBytes;
		this.syn = syn;
		this.ack = ack;
		this.sourcePort = sourcePort;
		this.destinationPort = destinationPort;
		this.sequenceNumber = sequenceNumber;
		this.acknowledgmentNumber = acknowledgmentNumber;
		this.windowSize = windowSize;
	}
	
	public Segment(byte[] all) {
		this.dataBytes = this.getData(all);
		this.syn = (this.getSyn(all) == 1 ? true : false);
		this.ack = (this.getAck(all) == 1 ? true : false);
		this.sourcePort = this.getSourcePort(all);
		this.destinationPort = this.getDestinationPort(all);
		this.sequenceNumber = this.getSequenceNumber(all);
		this.acknowledgmentNumber = this.getAcknowledgmentNumber(all);
		this.windowSize = this.getWindowSize(all);
	}
	
	public String toString() {
		return "sp: " +String.valueOf(this.sourcePort)
				+ " dp: " +String.valueOf(this.destinationPort)
				+ " sn: " +String.valueOf(this.sequenceNumber)
				+ " an: " +String.valueOf(this.acknowledgmentNumber)
				+ " ack: " +String.valueOf(this.ack)
				+ " syn: " +String.valueOf(this.syn)
				+ " ws: " +String.valueOf(this.windowSize)
				+ " db: " +String.valueOf(this.dataBytes);
	}
	
	public byte[] getBytes() {
		ArrayList<Byte> result = new ArrayList<Byte>();
		ByteBuffer b = ByteBuffer.allocate(2);
		b.putShort((short)this.sourcePort);
		for(byte item :  b.array()) {
			result.add(item);
		}
		ByteBuffer b2 = ByteBuffer.allocate(2);
		b2.putShort((short)this.destinationPort);
		for(byte item :  b2.array()) {
			result.add(item);
		}
		ByteBuffer b3 = ByteBuffer.allocate(4);
		b3.putInt(this.sequenceNumber);
		for(byte item :  b3.array()) {
			result.add(item);
		}
		ByteBuffer b4 = ByteBuffer.allocate(4);
		b4.putInt(this.acknowledgmentNumber);
		for(byte item :  b4.array()) {
			result.add(item);
		}
		result.add((byte) ((this.ack)?1:0));
		result.add((byte) ((this.syn)?1:0));
		ByteBuffer b6 = ByteBuffer.allocate(2);
		b6.putShort((short)this.windowSize);
		for(byte item :  b6.array()) {
			result.add(item);
		}
		for(byte item : this.dataBytes) {
			result.add(item);
		}
		byte[] res = new byte[result.size()];
		for(int i = 0; i < result.size(); i++) {
		    res[i] = result.get(i).byteValue();
		}
		return res;
	}
	
	private short getSourcePort(byte[] ba){
		byte[] temp = {ba[0], ba[1]};
		ByteBuffer bf = ByteBuffer.wrap(temp);
		return bf.getShort();
	}
	
	private short getDestinationPort(byte[] ba){
		byte[] temp = {ba[2], ba[3]};
		ByteBuffer bf = ByteBuffer.wrap(temp);
		return bf.getShort();
	}
	
	private int getSequenceNumber(byte[] ba){
		byte[] temp = {ba[4], ba[5], ba[6], ba[7]};
		ByteBuffer bf = ByteBuffer.wrap(temp);
		return bf.getInt();
	}

	private int getAcknowledgmentNumber(byte[] ba){
		byte[] temp = {ba[8], ba[9], ba[10], ba[11]};
		ByteBuffer bf = ByteBuffer.wrap(temp);
		return bf.getInt();
	}
	
	private int getAck(byte[] ba){
		int i = (byte)ba[12];
		return i;
	}
	
	private int getSyn(byte[] ba){
		int i = (byte)ba[13];
		return i;
	}
	
	private short getWindowSize(byte[] ba){
		byte[] temp = {ba[14], ba[15]};
		ByteBuffer bf = ByteBuffer.wrap(temp);
		return bf.getShort();
	}
	
	private byte[] getData(byte[] ba){
		int dataLength = ba.length - 16;
		byte[] b = new byte[dataLength];
		for(int i=16; i<ba.length; i++)
			b[i-16] = ba[i];
		return b;
	}

	
	public byte[] getDataBytes() {
		return dataBytes;
	}

	public boolean isSyn() {
		return syn;
	}

	public boolean isAck() {
		return ack;
	}

	public int getSourcePort() {
		return sourcePort;
	}

	public int getDestinationPort() {
		return destinationPort;
	}

	public int getSequenceNumber() {
		return sequenceNumber;
	}

	public int getAcknowledgmentNumber() {
		return acknowledgmentNumber;
	}

	public int getWindowSize() {
		return windowSize;
	}

}
