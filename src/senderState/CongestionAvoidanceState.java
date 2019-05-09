package senderState;

public class CongestionAvoidanceState implements SenderState{
	
	private SenderMachine senderMachine;
	
	public CongestionAvoidanceState(SenderMachine senderMachine) {
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
		this.senderMachine.setCwnd(cwnd + (1/cwnd));
		this.senderMachine.setDupAckCount(0);
		this.senderMachine.transmitNewSegments();
		this.senderMachine.setSenderState(this.senderMachine.getCongestionAvoidanceState());
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

	}

}
