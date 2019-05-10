package recieverState;

import datagram.Segment;
import java.util.TimerTask;

public class RecieverMachine{

	private int expectedSeqNum;
	private Segment sendPacket;

	public RecieverMachine() {
		this.expectedSeqNum = 1;
//		this.sendPacket = new Segment();
	}

	private boolean hasSeqNumber(Segment segment){
		return segment.getSequenceNumber() == this.expectedSeqNum;
	}

	public void recievePacket(Segment segment){
		if (hasSeqNumber(segment)){
			String data = segment.getBytes().toString();
			// Not null
			//TODO:Deliver data
			//TODO:Make packet
			this.expectedSeqNum++;
		}
		//TODO:Send packet
	}
}

