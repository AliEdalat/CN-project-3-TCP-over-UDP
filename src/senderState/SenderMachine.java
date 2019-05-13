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

public class SenderMachine {

	private static int RTT = 10000;
	
	private SenderState fastRecoveryState;
	private SenderState slowStartState;
	private SenderState congestionAvoidanceState;
	
	private SenderState senderState;
	private String ip = "127.0.0.1";
//	private static Timer timer = new Timer();
	private TimerHandler timerHandler;
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
	
	private class MyTimerTask  extends TimerTask {
		@Override
		public void run() {
			timeOut();
		}
	}
	
	private class TimerHandler extends Thread {
		private Timer timer = new Timer();
		private MyTimerTask myTimerTask = new MyTimerTask();
		private boolean end = false;
		
		@Override
		public void run() {
			timer.schedule(myTimerTask, new Date().getTime() + (2 * RTT));
			while (!end) {}
			System.out.println("Thread  exiting.");
		}
		
		public void setEnd(boolean end) {
			this.end = end;
		}
//		
//		public void cancel() {
//			timer.cancel();
//		}
//		
//		public void schedule() {
//			timer.schedule(myTimerTask, new Date().getTime() + (2 * RTT));
//		}
	}

	public void retransmitMissingSegment() {
		if (rcvWindowSize >= 1) {
			Segment segment = segments.get(this.base);
			byte[] segmentBytes = segment.getBytes();
			try {
				datagramSocket.send(new DatagramPacket(segmentBytes, segmentBytes.length, InetAddress.getByName(this.ip),
						segment.getDestinationPort()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void transmitNewSegments() {
		System.out.println(base);
		System.out.println(cwnd);
		int remainBuf = rcvWindowSize;
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
		System.out.println("\ntransmit is ended?");
	}
	
	public SenderMachine(EnhancedDatagramSocket datagramSocket, ArrayList<Segment> segments) {
		this.fastRecoveryState = new FastRecoveryState(this);
		this.slowStartState = new SlowStartState(this);
		this.congestionAvoidanceState = new CongestionAvoidanceState(this);
//		this.timer = new Timer();
//		this.myTimerTask = new MyTimerTask();
		this.rcvWindowSize = 1;
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
		this.transmitNewSegments();
//		timer.schedule(myTimerTask, new Date().getTime() + (2 * RTT));
//		this.timerHandler = new TimerHandler();
//		timerHandler.start();
		this.start = new Date().getTime();
		this.runFsm();
//		while (true) {
//			
//		}
	}

	private Boolean isAcked(int segAckNum){
		return segmentAcks.get(segAckNum);
	}

	private void runFsm() {
		byte[] data = new byte[datagramSocket.getPayloadLimitInBytes()];
		DatagramPacket p = new DatagramPacket(data, data.length);
		while(true) {
			System.out.println(">>>>>>>>>>>>>>>>>>> " + String.valueOf(segments.size()));
			for(boolean b : segmentAcks) {
				System.out.println(String.valueOf(b));
			}
			System.out.println(">>>>>>>>>>>>>>>>>>>>>");
			if ((new Date().getTime() - this.start) > (2*RTT)) {
				timeOut();
			}
			try {
				datagramSocket.receive(p);
				Segment segment = new Segment(p.getData());
				System.out.println("sender ma rec run " + segment.toString());
				if (segment.isAck() && !isAcked(segment.getAcknowledgmentNumber() - 1) ){
					segmentAcks.set(segment.getAcknowledgmentNumber() - 1, true);
					numberOfAckSegments = segment.getAcknowledgmentNumber() - this.base;
					base = segment.getAcknowledgmentNumber();
					rcvWindowSize = segment.getWindowSize();
					if (base >= segments.size()){
						break;
					}
					System.out.println("\nbf new");
					newAck();
					System.out.println("\n after new");
				} else if (segment.isAck() && isAcked(segment.getAcknowledgmentNumber())){
					rcvWindowSize = segment.getWindowSize();
					dupAck();
				}
			} catch (Exception e){

			}
		}
	}
	
	public void setSenderState(SenderState senderState) {
		this.senderState = senderState;
	}
	
	public void timeOut() { System.out.println("timeout"); senderState.timeOut(); }
	
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
