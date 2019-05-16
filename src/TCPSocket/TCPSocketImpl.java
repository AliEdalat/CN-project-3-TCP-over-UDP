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
	private String file;
	private SenderMachine senderMachine;
	
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
        this.file = "";
        this.connectionState = null;
        datagramSocket.setSoTimeout(RTT);
        this.handshake();
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

    private void handshake() throws Exception {
    	int synIterate = 30;
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
		BufferedReader br = new BufferedReader(new FileReader(currentDirFile));
		int seqNum = 0;
		while (true){
			String line = br.readLine();
			if (line == null)
				break;
			line += "\n";
			file += line;
		}
		for (int i = 0; i < file.length(); i+= segmentDataSize){
			byte[] data = getSendByteArray(i);
			Segment segment = new Segment(data, false, false, this.myPort, this.port, seqNum++,
					0, 2);
			segments.add(segment);
		}
		this.senderMachine = new SenderMachine(this.datagramSocket , this, segments);
		this.senderMachine.run();
    }

    @Override
    public void receive(String pathToFile) throws Exception {
    }

    @Override
    public void close() throws Exception {
        datagramSocket.close();
    }

    @Override
    public long getSSThreshold() {
        return senderMachine.getSsthresh();
    }

    @Override
    public long getWindowSize() {
        return (long) senderMachine.getCwnd();
    }

    private byte[] getSendByteArray(int index){
		int start = index;
		int end = 0;
		if (index > file.length() - segmentDataSize)
			end = file.length()-1;
		else
			end = index + segmentDataSize - 1;
		return file.substring(start, end).getBytes();
	}
}
