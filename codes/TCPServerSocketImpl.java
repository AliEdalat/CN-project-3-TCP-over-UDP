import java.net.DatagramPacket;

public class TCPServerSocketImpl extends TCPServerSocket {
	private EnhancedDatagramSocket datagramSocket = null;
	
    public TCPServerSocketImpl(int port) throws Exception {
        super(port);
        datagramSocket = new EnhancedDatagramSocket(port);
    }

    @Override
    public TCPSocket accept() throws Exception {
    	byte[] data = new byte[datagramSocket.getPayloadLimitInBytes()];
    	DatagramPacket p = new DatagramPacket(data, data.length);
        while (true) {
        	System.out.println('r');
			datagramSocket.receive(p);
			System.out.println(new Segment(p.getData()).toString());
		}
    }

    @Override
    public void close() throws Exception {
        throw new RuntimeException("Not implemented!");
    }
}
