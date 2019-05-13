package senderState;

public class SlowStartState implements SenderState {
	
	private SenderMachine senderMachine;
	
	public SlowStartState(SenderMachine senderMachine) {
		this.senderMachine = senderMachine;
	}

	@Override
	public void timeOut() {
		System.out.println("SlowStartState-timeOut");
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
		System.out.println("SlowStartState-threeDupAck");
		float cwnd = this.senderMachine.getCwnd();
		int newSsthresh = Math.max((int)(cwnd/2), 2);
		this.senderMachine.setCwnd(newSsthresh + 3);
		this.senderMachine.setSsthresh(newSsthresh);
		this.senderMachine.retransmitMissingSegment();
		this.senderMachine.updateTimer();
		this.senderMachine.setSenderState(this.senderMachine.getFastRecoveryState());
	}

	@Override
	public void newAck() {
		System.out.println("SlowStartState-newAck");
		float cwnd = this.senderMachine.getCwnd();
		this.senderMachine.setCwnd(1 + cwnd);
		this.senderMachine.setDupAckCount(0);
		this.senderMachine.transmitNewSegments();
		System.out.println("after trans in slow start");
		this.senderMachine.updateTimer();
		System.out.println("hhhhh>>>>>>>>>>>>>>>" + String.valueOf(cwnd));
		if (cwnd + 1 >= this.senderMachine.getSsthresh()){
			System.out.println("ssthresh ex");
			this.senderMachine.ssthreshExceed();
			return;
		}
		this.senderMachine.setSenderState(this.senderMachine.getSlowStartState());
	}

	@Override
	public void dupAck() {
		System.out.println("SlowStartState-dupAck");
		int dupAckCount = this.senderMachine.getDupAckCount();
		this.senderMachine.setDupAckCount(dupAckCount + 1);
		this.senderMachine.updateTimer();
		this.senderMachine.setSenderState(this.senderMachine.getSlowStartState());
		if (dupAckCount + 1 == 3) {
			this.senderMachine.threeDupAck();
		}
	}

	@Override
	public void ssthreshExceed() {
		System.out.println("SlowStartState-ssthreshExceed");
		this.senderMachine.setSenderState(this.senderMachine.getCongestionAvoidanceState());
	}

}
