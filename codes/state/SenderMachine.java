package state;

import java.util.Timer;
import java.util.TimerTask;

public class SenderMachine extends TimerTask{
	
	private SenderState fastRecoveryState;
	private SenderState slowStartState;
	private SenderState congestionAvoidanceState;
	
	private SenderState senderState;
	
	private Timer timer;		// Timer.cancel();
	private int cwnd;
	private int ssthresh;
	private int dupAckCount;
	
	
	
	private void retransmitMissingSegment() {
		
	}
	
	private void transmitNewSegment() {
		
	}
	
	public SenderMachine() {
		this.fastRecoveryState = new FastRecoveryState(this);
		this.slowStartState = new SlowStartState(this);
		this.congestionAvoidanceState = new CongestionAvoidanceState(this);
		this.timer = new Timer();
		this.senderState = slowStartState;
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
