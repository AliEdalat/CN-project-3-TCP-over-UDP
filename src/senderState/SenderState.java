package senderState;

public interface SenderState {
	void timeOut();
	void threeDupAck();
	void newAck();
	void dupAck();
	void ssthreshExceed();
}
