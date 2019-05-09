package senderState;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class SenderMachine extends TimerTask{

	private static int RTT = 10000;
	
	private SenderState fastRecoveryState;
	private SenderState slowStartState;
	private SenderState congestionAvoidanceState;
	
	private SenderState senderState;
	
	private Timer timer;		// Timer.cancel();
	private float cwnd;
	private int ssthresh;
	private int dupAckCount;
	private int base;
	private int ackPacketNumber;
	private int lastSentPacket;
	
	
	//TODO:Complete uncompleted classes
	public void retransmitMissingSegment() {
		
	}
	
	public void transmitNewSegments() {
		
	}
	
	public SenderMachine() {
		this.fastRecoveryState = new FastRecoveryState(this);
		this.slowStartState = new SlowStartState(this);
		this.congestionAvoidanceState = new CongestionAvoidanceState(this);
		this.timer = new Timer();
		this.cwnd = 1;
		this.ssthresh = 8;
		this.dupAckCount = 0;
		this.base = 0;
		this.senderState = slowStartState;
		this.ackPacketNumber = 0;
		this.transmitNewSegments();
	}
	
	public void setSenderState(SenderState senderState) {
		this.senderState = senderState;
	}
	
	public void timeOut() {
		
	}
	
	public void newAck() {
		
	}
	
	public void dupAck() {
		
	}
	
	public void threeDupAck() {
		
	}
	
	public void ssthreshExceed() {
		
	}

	public int getAckPacketNumber() {
		return ackPacketNumber;
	}

	public void updateTimer() {
		timer.cancel();
		timer.schedule(this, new Date().getTime() + (2 * RTT));
	}

	public int getLastSentPacket() {
		return lastSentPacket;
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

	@Override
	public void run() {
		this.timeOut();
	}
}
