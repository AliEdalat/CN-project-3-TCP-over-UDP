package senderState;

public class SlowStartState implements SenderState {
	
	private SenderMachine senderMachine;
	
	public SlowStartState(SenderMachine senderMachine) {
		this.senderMachine = senderMachine;
	}

	@Override
	public void timeOut() {
		float cwnd = this.senderMachine.getCwnd();
		int newSsthresh = Math.max((int)(cwnd/2), 2);
		this.senderMachine.setCwnd(1);
		this.senderMachine.setSsthresh(newSsthresh);
		this.senderMachine.setDupAckCount(0);
		this.senderMachine.retransmitMissingSegment();
		this.senderMachine.setSenderState(this.senderMachine.getSlowStartState());
	}

	@Override
	public void threeDupAck() {
		float cwnd = this.senderMachine.getCwnd();
		int newSsthresh = Math.max((int)(cwnd/2), 2);
		this.senderMachine.setCwnd(newSsthresh + 3);
		this.senderMachine.setSsthresh(newSsthresh);
		this.senderMachine.retransmitMissingSegment();
		this.senderMachine.setSenderState(this.senderMachine.getFastRecoveryState());
	}

	@Override
	public void newAck() {
		float cwnd = this.senderMachine.getCwnd();
		this.senderMachine.setCwnd(1 + cwnd);
		this.senderMachine.setDupAckCount(0);
		this.senderMachine.transmitNewSegments();
		if (cwnd + 1 >= this.senderMachine.getSsthresh()){
			this.senderMachine.ssthreshExceed();
			return;
		}
		this.senderMachine.setSenderState(this.senderMachine.getSlowStartState());
	}

	@Override
	public void dupAck() {
		int dupAckCount = this.senderMachine.getDupAckCount();
		this.senderMachine.setDupAckCount(dupAckCount + 1);
		this.senderMachine.setSenderState(this.senderMachine.getSlowStartState());
		if (dupAckCount + 1 == 3) {
			this.senderMachine.threeDupAck();
		}
	}

	@Override
	public void ssthreshExceed() {
		this.senderMachine.setSenderState(this.senderMachine.getCongestionAvoidanceState());
	}

}
