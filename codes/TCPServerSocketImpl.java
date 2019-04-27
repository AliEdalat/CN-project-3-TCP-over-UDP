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
	
    public TCPServerSocketImpl(int port) throws Exception {
        super(port);
        datagramSocket = new EnhancedDatagramSocket(port);
        datagramSocket.setSoTimeout(RTT);
        connectionState = null;
    }

    @Override
    public TCPSocket accept() throws Exception {
    	int pairPort = 0;
    	byte[] data = new byte[datagramSocket.getPayloadLimitInBytes()];
    	DatagramPacket p = new DatagramPacket(data, data.length);
        while (true) {
        	System.out.println('r');
			datagramSocket.receive(p);
			if (p.getData().length < 16) {
				continue;
			}
			Segment segment = new Segment(p.getData());
			System.out.println(segment.toString());
			if (segment.isSyn() && this.connectionState == null) {
				byte[] ack = new Segment(new byte[0], false, true, segment.getDestinationPort(), segment.getSourcePort(),
						this.sequenceNumber, segment.getSequenceNumber()+1, 0).getBytes();
	            datagramSocket.send(new DatagramPacket(ack, ack.length, InetAddress.getByName("127.0.0.1"), segment.getSourcePort()));
	            this.connectionState = Connection.SYN_RESEVED;
			} else if (segment.isAck() && this.connectionState == Connection.SYN_RESEVED
					&& segment.getAcknowledgmentNumber() == this.sequenceNumber+1) {
				this.connectionState = Connection.ESTABLISHED;
				pairPort = segment.getSourcePort();
				break;
			}	
		}
        System.out.println("ali");
        return new TCPSocketImpl("127.0.0.1", pairPort);
    }

    @Override
    public void close() throws Exception {
        throw new RuntimeException("Not implemented!");
    }
}
