import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

public class TCPSocketImpl extends TCPSocket {
	enum Connection{
		  SYN_SENT,
		  ESTABLISHED
	}
	private EnhancedDatagramSocket datagramSocket = null;
	private Connection connectionState;
	private int port;
	private String ip;
	private int sequenceNumber = 100;
	
    public TCPSocketImpl(String ip, int port) throws Exception {
        super(ip, port);
        datagramSocket = new EnhancedDatagramSocket(1238);
        this.port = port;
        this.ip = ip;
        this.connectionState = null;
        this.handshake();
    }
    
    // TODO: timeout send and res
    private void handshake() throws Exception {
    	if (connectionState != Connection.ESTABLISHED) {
    		byte[] b = new Segment(new byte[0], true, false, 1238, this.port, this.sequenceNumber, 0, 0).getBytes();
            datagramSocket.send(new DatagramPacket(b, b.length, InetAddress.getByName(this.ip), this.port));
            this.connectionState = Connection.SYN_SENT;
            byte[] data = new byte[datagramSocket.getPayloadLimitInBytes()];
        	DatagramPacket p = new DatagramPacket(data, data.length);
        	datagramSocket.receive(p);
        	if (p.getData().length < 16) {
        		throw new Exception();
			}
        	Segment segment = new Segment(p.getData());
        	if (segment.isAck() && segment.getAcknowledgmentNumber() == (this.sequenceNumber + 1)) {
				this.connectionState = Connection.ESTABLISHED;
			} else {
				throw new Exception();
			}
		}
    }

    @Override
    public void send(String pathToFile) throws Exception {
    	// Split file to segments
    	byte[] b = new Segment(pathToFile.getBytes(), true, true, 8, 7, 1, 0, 2).getBytes();
        datagramSocket.send(new DatagramPacket(b, b.length, InetAddress.getByName(this.ip), this.port));
    }

    @Override
    public void receive(String pathToFile) throws Exception {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public void close() throws Exception {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public long getSSThreshold() {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public long getWindowSize() {
        throw new RuntimeException("Not implemented!");
    }
}
