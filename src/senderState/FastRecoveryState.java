package senderState;

public class FastRecoveryState implements SenderState{
	
	private SenderMachine senderMachine;
	
	public FastRecoveryState(SenderMachine senderMachine) {
		this.senderMachine = senderMachine;
	}

	@Override
	public void timeOut() {
		System.out.println("FastRecoveryState-timeOut");
		float cwnd = this.senderMachine.getCwnd();
		int newSsthresh = Math.max((int)(cwnd/2), 2);
		this.senderMachine.setCwnd(1);
		this.senderMachine.setSsthresh(newSsthresh);
		this.senderMachine.setDupAckCount(0);
		this.senderMachine.retransmitMissingSegment();
		this.senderMachine.updateTimer();
		this.senderMachine.setSenderState(this.senderMachine.getSlowStartState());
	}

	@Override
	public void threeDupAck() {

	}

	@Override
	public void newAck() {
		System.out.println("FastRecoveryState-newAck");
		if (this.senderMachine.getBase() < this.senderMachine.getLastSeqSent()){
			this.senderMachine.setCwnd(this.senderMachine.getCwnd() -
					this.senderMachine.getNumberOfAckSegments());
			this.senderMachine.retransmitMissingSegment();
			this.senderMachine.updateTimer();
			return;
		}
		this.senderMachine.setCwnd(this.senderMachine.getSsthresh());
		this.senderMachine.setDupAckCount(0);
		this.senderMachine.updateTimer();
		this.senderMachine.setSenderState(this.senderMachine.getCongestionAvoidanceState());
	}

	@Override
	public void dupAck() {
		System.out.println("FastRecoveryState-dupAck");
		this.senderMachine.setCwnd(this.senderMachine.getCwnd() + 1);
		this.senderMachine.transmitNewSegments();
		this.senderMachine.setSenderState(this.senderMachine.getFastRecoveryState());
	}

	@Override
	public void ssthreshExceed() {

	}

}
