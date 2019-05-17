package senderState;

import TCPSocket.TCPSocketImpl;
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

public class SenderMachine {

	private static int RTT = 10000;
	
	private SenderState fastRecoveryState;
	private SenderState slowStartState;
	private SenderState congestionAvoidanceState;
	private SenderState senderState;
	private String ip = "127.0.0.1";
	private float cwnd;
	private int ssthresh;
	private int dupAckCount;
	private int base;
	private int lastSeqSent;
	private int numberOfAckSegments;
	private EnhancedDatagramSocket datagramSocket;
	private ArrayList<Segment> segments;
	private ArrayList<Boolean> segmentAcks;
	private long start;
	private int rcvWindowSize;
	private TCPSocketImpl tcpSocket;
	private int remainBuf;

	public void retransmitMissingSegment() {
		Segment segment = segments.get(this.base);
		byte[] segmentBytes = segment.getBytes();
		try {
			datagramSocket.send(new DatagramPacket(segmentBytes, segmentBytes.length, InetAddress.getByName(this.ip),
					segment.getDestinationPort()));
			this.remainBuf--;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void transmitNewSegments() {
		for (int i = base; i < base+cwnd; i++){
			if (remainBuf >= 1 && (segmentAcks.get(i) == false)) {
				Segment segment = segments.get(i);
				byte[] segmentBytes = segment.getBytes();
				try {
					datagramSocket.send(new DatagramPacket(segmentBytes, segmentBytes.length, InetAddress.getByName(this.ip),
							segment.getDestinationPort()));
					lastSeqSent = segment.getSequenceNumber();
					remainBuf -= 1;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public SenderMachine(EnhancedDatagramSocket datagramSocket, TCPSocketImpl tcpSocket, ArrayList<Segment> segments) {
		this.fastRecoveryState = new FastRecoveryState(this);
		this.slowStartState = new SlowStartState(this);
		this.congestionAvoidanceState = new CongestionAvoidanceState(this);
		this.rcvWindowSize = 1;
		this.remainBuf = 1;
		this.cwnd = 1;
		this.ssthresh = 3;
		this.dupAckCount = 0;
		this.base = 0;
		this.senderState = slowStartState;
		this.datagramSocket = datagramSocket;
		this.tcpSocket = tcpSocket;
		this.segments = segments;
		segmentAcks = new ArrayList<>();
		for(Segment segment : segments) {
			segmentAcks.add(false);
		}
	}

	public void run(){
		this.transmitNewSegments();
		this.start = new Date().getTime();
		this.runFsm();
	}

	private Boolean isAcked(int segAckNum){
		return segmentAcks.get(segAckNum);
	}

	private void runFsm() {
		byte[] data = new byte[datagramSocket.getPayloadLimitInBytes()];
		DatagramPacket p = new DatagramPacket(data, data.length);
		while(true) {
			if ((new Date().getTime() - this.start) > (2*RTT)) {
				System.out.println("cwnd: " + this.cwnd);
				System.out.println("seqNum: " + base);
				timeOut();
			}
			try {
				System.out.println("cwnd: " + this.cwnd);
				datagramSocket.receive(p);
				Segment segment = new Segment(p.getData());
				if (segment.isAck() && !isAcked(segment.getAcknowledgmentNumber() - 1) ){
					segmentAcks.set(segment.getAcknowledgmentNumber() - 1, true);
					numberOfAckSegments = segment.getAcknowledgmentNumber() - this.base;
					if (base < segment.getAcknowledgmentNumber())
						base = segment.getAcknowledgmentNumber();
					rcvWindowSize = segment.getWindowSize();
					remainBuf = rcvWindowSize;
					if (base >= segments.size()){
						break;
					}
					System.out.println("seqNum: " + base);
					newAck();
				} else if (segment.isAck() && isAcked(segment.getAcknowledgmentNumber())){
					rcvWindowSize = segment.getWindowSize();
					remainBuf = rcvWindowSize;
					System.out.println("seqNum: " + base);
					dupAck();
				}
			} catch (Exception e){

			}
		}
	}
	
	public void setSenderState(SenderState senderState) {
		this.senderState = senderState;
	}
	
	public void timeOut() { senderState.timeOut(); }
	
	public void newAck() { senderState.newAck(); }
	
	public void dupAck() { senderState.dupAck(); }
	
	public void threeDupAck() { senderState.threeDupAck(); }
	
	public void ssthreshExceed() { senderState.ssthreshExceed(); }

	public void updateTimer() {
		this.start = new Date().getTime();
	}

	public float getCwnd() {
		return cwnd;
	}

	public void setCwnd(float cwnd) {
		this.cwnd = cwnd;
		tcpSocket.onWindowChange();
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

	public int getNumberOfAckSegments() { return numberOfAckSegments; }

	public int getLastSeqSent() { return lastSeqSent; }

}
