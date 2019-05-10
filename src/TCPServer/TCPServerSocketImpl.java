package TCPServer;

import TCPSocket.*;
import datagram.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class TCPServerSocketImpl extends TCPServerSocket {

	enum Connection{
		  SYN_RESEVED,
		  ESTABLISHED
	}
	private EnhancedDatagramSocket datagramSocket = null;
	private int sequenceNumber = 200;
	private Connection connectionState;
	private static int RTT = 10000;
	private int port;
	
    public TCPServerSocketImpl(int port) throws Exception {
        super(port);
        this.port = port;
        datagramSocket = new EnhancedDatagramSocket(port);
        datagramSocket.setSoTimeout(RTT);
        connectionState = null;
    }

    private void sendSynAck(Segment segment) throws IOException {
    	if (segment != null) {
			byte[] ack = new Segment(new byte[0], true, true, segment.getDestinationPort(), segment.getSourcePort(),
					this.sequenceNumber, segment.getSequenceNumber() + 1, 0).getBytes();
			datagramSocket.send(new DatagramPacket(ack, ack.length, InetAddress.getByName("127.0.0.1"), segment.getSourcePort()));
			this.connectionState = Connection.SYN_RESEVED;
		}
	}

    @Override
    public TCPSocket accept() throws Exception {
    	int pairPort = 0;
    	int synAckIterate = 5;
		Segment segment = null;
    	byte[] data = new byte[datagramSocket.getPayloadLimitInBytes()];
    	DatagramPacket p = new DatagramPacket(data, data.length);
        while (true) {
        	try {
				datagramSocket.receive(p);
			}catch (Exception e){
        		if (this.connectionState == Connection.SYN_RESEVED){
					synAckIterate--;
					if (synAckIterate == 0){
						this.connectionState = null;
						continue;
					}
					this.sendSynAck(segment);
				}
			}
			if (p.getData().length < 16) {
				continue;
			}
			segment = new Segment(p.getData());
			System.out.println(segment.toString());
			if (segment.isSyn() && this.connectionState == null) {
				this.sendSynAck(segment);
			} else if (segment.isAck() && this.connectionState == Connection.SYN_RESEVED
					&& segment.getAcknowledgmentNumber() == this.sequenceNumber+1) {
				this.connectionState = Connection.ESTABLISHED;
				pairPort = segment.getSourcePort();
				break;
			}	
		}
        System.out.println(pairPort);
        return new TCPSocketBinded(datagramSocket, pairPort);
    }

    @Override
    public void close() throws Exception {
        throw new RuntimeException("Not implemented!");
    }
}
