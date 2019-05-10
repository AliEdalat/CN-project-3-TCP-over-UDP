package senderState;

import datagram.EnhancedDatagramSocket;
import datagram.Segment;

import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Timer;

public class SenderMachine extends TimerTask{

	private static int RTT = 10000;
	
	private SenderState fastRecoveryState;
	private SenderState slowStartState;
	private SenderState congestionAvoidanceState;
	
	private SenderState senderState;
	private String ip = "127.0.0.1";
	private Timer timer;
	private float cwnd;
	private int ssthresh;
	private int dupAckCount;
	private int base;
	private int lastSeqSent;
	private int lastAck;
	private EnhancedDatagramSocket datagramSocket;
	private ArrayList<Segment> segments;
	private ArrayList<Boolean> segmentAcks;

	private class Reciever extends Thread {

		private Boolean isAcked(int segAckNum){
			return segmentAcks.get(segAckNum);
		}

		@Override
		public void run() {
			byte[] data = new byte[datagramSocket.getPayloadLimitInBytes()];
			DatagramPacket p = new DatagramPacket(data, data.length);
			while(true) {
				try {
					datagramSocket.receive(p);
					Segment segment = new Segment(p.getData());
					System.out.println("sender ma rec run " + segment.toString());
					if (segment.isAck() && !isAcked(segment.getAcknowledgmentNumber()) ){
						segmentAcks.set(segment.getAcknowledgmentNumber(), true);
						base += 1;
						lastAck = segment.getAcknowledgmentNumber();
						newAck();
					} else if (segment.isAck() && isAcked(segment.getAcknowledgmentNumber())){
						dupAck();
					}
				} catch (Exception e){
	
				}
			}
		}
	}

	public void retransmitMissingSegment() {
		Segment segment = segments.get(this.lastAck);
		byte[] segmentBytes = segment.getBytes();
		try {
			datagramSocket.send(new DatagramPacket(segmentBytes, segmentBytes.length, InetAddress.getByName(this.ip),
					segment.getDestinationPort()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void transmitNewSegments() {
		System.out.println(base);
		System.out.println(cwnd);
		for (int i = base; i < base+cwnd; i++){
			Segment segment = segments.get(i);
			byte[] segmentBytes = segment.getBytes();
			try {
				datagramSocket.send(new DatagramPacket(segmentBytes, segmentBytes.length, InetAddress.getByName(this.ip),
						segment.getDestinationPort()));
				lastSeqSent = segment.getSequenceNumber();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public SenderMachine(EnhancedDatagramSocket datagramSocket, ArrayList<Segment> segments) {
		this.fastRecoveryState = new FastRecoveryState(this);
		this.slowStartState = new SlowStartState(this);
		this.congestionAvoidanceState = new CongestionAvoidanceState(this);
		this.timer = new Timer();
		this.cwnd = 1;
		this.ssthresh = 3;
		this.dupAckCount = 0;
		this.base = 0;
		this.senderState = slowStartState;
		this.datagramSocket = datagramSocket;
		this.segments = segments;
		segmentAcks = new ArrayList<>();
		for(Segment segment : segments) {
			segmentAcks.add(false);
		}
		Reciever reciever = new Reciever();
		reciever.start();
		this.transmitNewSegments();
		timer.schedule(this, new Date().getTime() + (2 * RTT));
//		reciever.run();
		while(true) {}
	}
	
	public void setSenderState(SenderState senderState) {
		this.senderState = senderState;
	}
	
	public void timeOut() { senderState.timeOut(); }
	
	public void newAck() { System.out.println("newAck"); senderState.newAck(); }
	
	public void dupAck() { senderState.dupAck(); }
	
	public void threeDupAck() { senderState.threeDupAck(); }
	
	public void ssthreshExceed() { senderState.ssthreshExceed(); }

	public void updateTimer() {
		timer.cancel();
		timer.schedule(this, new Date().getTime() + (2 * RTT));
	}

	public float getCwnd() {
		return cwnd;
	}

	public void setCwnd(float cwnd) {
		this.cwnd = cwnd;
	}

	public int getSsthresh() {
		return ssthresh;
	}

	public void setSsthresh(int ssthresh) {
		this.ssthresh = ssthresh;
	}

	public int getDupAckCount() {
		return dupAckCount;
	}

	public void setDupAckCount(int dupAckCount) {
		this.dupAckCount = dupAckCount;
	}

	public SenderState getFastRecoveryState() {
		return fastRecoveryState;
	}
	
	public SenderState getSlowStartState() {
		return slowStartState;
	}
	
	public SenderState getCongestionAvoidanceState() {
		return congestionAvoidanceState;
	}

	public int getBase() { return base; }

	public int getLastAck() { return lastAck; }

	public int getLastSeqSent() { return lastSeqSent; }

	@Override
	public void run() {
		this.timeOut();
	}
}
