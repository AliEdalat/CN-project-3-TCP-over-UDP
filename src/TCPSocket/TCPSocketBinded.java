package TCPSocket;

import datagram.*;

import java.net.DatagramPacket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TCPSocketBinded extends TCPSocket {

    private int sequenceNumber = 100;
    private static int RTT = 10000;
    private static int segmentDataSize = 1392;
    private String ip;
    private int port;
    private EnhancedDatagramSocket datagramSocket;
    private int myPort;

    public TCPSocketBinded(EnhancedDatagramSocket datagramSocket, int port) throws Exception {
        super(datagramSocket.getLocalAddress().getHostAddress(), port);
        System.out.println("binded");
        this.datagramSocket = datagramSocket;
        this.port = port;
        this.ip = ip;
        datagramSocket.setSoTimeout(RTT);
    }

    @Override
    public void send(String pathToFile) throws Exception {

    }

    @Override
    public void receive(String pathToFile) throws Exception {
    	while(true) {
	    	byte[] data = new byte[datagramSocket.getPayloadLimitInBytes()];
	    	DatagramPacket p = new DatagramPacket(data, data.length);
	    	Date date = new Date();
	        String strDateFormat = "hh:mm:ss a";
	        DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
	        String formattedDate= dateFormat.format(date);
	    	System.out.println(formattedDate);
	    	try {
	    		datagramSocket.receive(p);
	    	} catch (Exception e) {
				continue;
			}
	    	Segment segment = new Segment(p.getData());
			System.out.println(new String(segment.getDataBytes()));
    	}
    }

    @Override
    public void close() throws Exception {

    }

    @Override
    public long getSSThreshold() {
        return 0;
    }

    @Override
    public long getWindowSize() {
        return 0;
    }
}
