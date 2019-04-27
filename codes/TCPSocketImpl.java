import java.io.BufferedReader;
import java.io.FileReader;
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
	private int myPort;
	private String ip;
	private int sequenceNumber = 100;
	private static int RTT = 10000;
	private static int segmentDataSize = 1464;
	
    public TCPSocketImpl(String ip, int port) throws Exception {
        super(ip, port);
        try {
        	Random rand = new Random();
        	this.myPort = 1238+rand.nextInt(50);
        	datagramSocket = new EnhancedDatagramSocket(this.myPort);
        } catch (Exception e) {
        	Random rand = new Random();
        	this.myPort = 1238+rand.nextInt(50)+rand.nextInt(30);
        	datagramSocket = new EnhancedDatagramSocket(this.myPort);
		}
        this.port = port;
        this.ip = ip;
        this.connectionState = null;
        datagramSocket.setSoTimeout(RTT);
        this.handshake();
    }
    
    private void handshake() throws Exception {
    	if (connectionState != Connection.ESTABLISHED) {
    		byte[] b = new Segment(new byte[0], true, false, this.myPort, this.port, this.sequenceNumber, 0, 0).getBytes();
            datagramSocket.send(new DatagramPacket(b, b.length, InetAddress.getByName(this.ip), this.port));
            this.connectionState = Connection.SYN_SENT;
            byte[] data = new byte[datagramSocket.getPayloadLimitInBytes()];
        	DatagramPacket p = new DatagramPacket(data, data.length);
        	datagramSocket.receive(p);
        	if (p.getData().length < 16) {
        		this.connectionState = null;
        		datagramSocket.close();
        		throw new Exception("connection is failed.");
			}
        	Segment segment = new Segment(p.getData());
        	if (segment.isAck() && segment.getAcknowledgmentNumber() == (this.sequenceNumber + 1)) {
				this.connectionState = Connection.ESTABLISHED;
				byte[] ack = new Segment(new byte[0], false, true, this.myPort, this.port, segment.getAcknowledgmentNumber(),
						segment.getSequenceNumber()+1, 0).getBytes();
	            datagramSocket.send(new DatagramPacket(ack, ack.length, InetAddress.getByName(this.ip), this.port));
			} else {
				this.connectionState = null;
				datagramSocket.close();
				throw new Exception("connection is failed.");
			}
		}
    }

    @Override
    public void send(String pathToFile) throws Exception {
    	// Split file to segments
    	char[] segmentData = new char[segmentDataSize];
    	BufferedReader br = new BufferedReader(new FileReader(pathToFile));
    	br.read(segmentData);
    	byte[] data = new String(segmentData).getBytes();
    	byte[] b = new Segment(data, false, false, this.myPort, this.port, 1, 0, 2).getBytes();
        datagramSocket.send(new DatagramPacket(b, b.length, InetAddress.getByName(this.ip), this.port));
    }

    @Override
    public void receive(String pathToFile) throws Exception {
    	byte[] data = new byte[datagramSocket.getPayloadLimitInBytes()];
    	DatagramPacket p = new DatagramPacket(data, data.length);
    	datagramSocket.receive(p);
    	Segment segment = new Segment(p.getData());
		System.out.println(segment.toString());
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
