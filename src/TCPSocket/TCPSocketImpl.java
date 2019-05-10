package TCPSocket;

import datagram.*;
import senderState.SenderMachine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
	private static int segmentDataSize = 1392;
	
    public TCPSocketImpl(String ip, int port) throws Exception {
        super(ip, port);
         try {
        	 Random rand = new Random();
        	 this.myPort = 1238+rand.nextInt(50);
//        	this.myPort = this.freePort();
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

    private int freePort() throws IOException {
		ServerSocket s = new ServerSocket(0);
		int port = s.getLocalPort();
		s.close();
		return port & 0xffff;
	}

	private void sendSYN() throws IOException {
		byte[] b = new Segment(new byte[0], true, false, this.myPort, this.port, this.sequenceNumber, 0, 0).getBytes();
		datagramSocket.send(new DatagramPacket(b, b.length, InetAddress.getByName(this.ip), this.port));
		this.connectionState = Connection.SYN_SENT;
	}

	private void sendACK(Segment segment) throws IOException {
		byte[] ack = new Segment(new byte[0], false, true, this.myPort, this.port, segment.getAcknowledgmentNumber(),
				segment.getSequenceNumber()+1, 0).getBytes();
		datagramSocket.send(new DatagramPacket(ack, ack.length, InetAddress.getByName(this.ip), this.port));
		this.connectionState = Connection.ESTABLISHED;
	}

	private void closeUDP() throws Exception {
		datagramSocket.close();
		this.connectionState = null;
		throw new Exception("connection is failed.");
	}

	// TODO: retry send ack
    private void handshake() throws Exception {
    	int synIterate = 3;
    	int synAckIterate = 30;
    	boolean retry = false;
    	if (connectionState != Connection.ESTABLISHED) {
    		this.sendSYN();
            byte[] data = new byte[datagramSocket.getPayloadLimitInBytes()];
        	DatagramPacket p = new DatagramPacket(data, data.length);
        	while(true) {
        		if (this.connectionState == Connection.SYN_SENT  && retry){
        			synIterate--;
        			if (synIterate == 0){
						this.closeUDP();
					}
				}
        		try {
					datagramSocket.receive(p);
					retry = false;
					if (this.connectionState == Connection.SYN_SENT) {
						synAckIterate--;
						if (synAckIterate == 0) {
							this.closeUDP();
						}
					}
				}catch(Exception e){
					this.sendSYN();
					retry = true;
					continue;
				}
				if (p.getData().length < 16) {
					continue;
				}
				Segment segment = new Segment(p.getData());
				if (segment.isAck() && segment.isSyn() && segment.getAcknowledgmentNumber() == (this.sequenceNumber + 1)) {
					this.sendACK(segment);
					return;
				} else {
					continue;
				}
			}
		}
    }

    @Override
    public void send(String pathToFile) throws Exception {
		ArrayList<Segment> segments = new ArrayList<>();
		File currentDirFile = new File("src/" + pathToFile);
		char[] segmentData = new char[segmentDataSize];
		BufferedReader br = new BufferedReader(new FileReader(currentDirFile));
		int seqNum = 0;
    	while(br.read(segmentData) != -1) {
//	    	System.out.println(segmentData);
	    	byte[] data = new String(segmentData).getBytes();
	    	Segment segment= new Segment(data, false, false, this.myPort, this.port, sequenceNumber++, 0, 2);
	    	segments.add(segment);
    	}
		SenderMachine senderMachine = new SenderMachine(this.datagramSocket , segments);
    }

    @Override
    public void receive(String pathToFile) throws Exception {
//    	byte[] data = new byte[datagramSocket.getPayloadLimitInBytes()];
//		DatagramPacket p = new DatagramPacket(data, data.length);
//		datagramSocket.receive(p);
//		Segment segment = new Segment(p.getData());
//		System.out.println(segment.toString());
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
