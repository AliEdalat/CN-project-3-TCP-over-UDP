package state;

public class FastRecoveryState implements SenderState{
	
	private SenderMachine senderMachine;
	
	public FastRecoveryState(SenderMachine senderMachine) {
		this.senderMachine = senderMachine;
	}

	@Override
	public void timeOut() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void threeDupAck() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void newAck() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dupAck() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ssthreshExceed() {
		// TODO Auto-generated method stub
		
	}

}