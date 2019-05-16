package TCPSocket;

import datagram.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class TCPSocketBinded extends TCPSocket {

    private int BUFFERSIZE = 30;
    private static int RTT = 10000;
    private String ip;
    private int port;
    private EnhancedDatagramSocket datagramSocket;
    private int expectedSeqNum;
    private String[] deliveringData;
    private Boolean[] delivered;
    private BufferedWriter writer;
    private int timeOutNum;
    private int remainBuf;

    public TCPSocketBinded(EnhancedDatagramSocket datagramSocket, int port) throws Exception {
        super(datagramSocket.getLocalAddress().getHostAddress(), datagramSocket.getLocalPort());
        this.datagramSocket = datagramSocket;
        this.datagramSocket.setSoTimeout(10000);
        this.timeOutNum = 0;
        this.port = port;
        this.ip = "127.0.0.1";
        datagramSocket.setSoTimeout(RTT);
        this.deliveringData = new String[this.BUFFERSIZE];
        this.delivered = new Boolean[this.BUFFERSIZE];
        resetDeliverd();
        String outputFileName = "output.txt";
        this.writer = new BufferedWriter(new FileWriter(outputFileName, true));
        this.writer.write("");
        this.remainBuf = this.BUFFERSIZE;
    }

    @Override
    public void send(String pathToFile) throws Exception {

    }

    private boolean isExpectedSeqNum(int sequenceNumber){
        return sequenceNumber == this.expectedSeqNum;
    }

    private boolean inWindowSegment(int sequenceNumber){
        return sequenceNumber >= this.expectedSeqNum && sequenceNumber < this.expectedSeqNum + this.BUFFERSIZE;
    }

    @Override
    public void receive(String pathToFile) throws Exception {
        while (true){
            byte[] data = new byte[datagramSocket.getPayloadLimitInBytes()];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            try {
                datagramSocket.receive(packet);
            } catch (SocketTimeoutException socketTimeoutException){
                this.timeOutNum++;
                if (timeOutNum >= 3)
                    break;
                continue;
            }
            Segment segment = new Segment(packet.getData());
            int segmentSequenceNumber = segment.getSequenceNumber();
            if (inWindowSegment(segmentSequenceNumber)) {
                String cleanedByteStream = cleanByteStream(segment.getDataBytes());
                this.deliveringData[segmentSequenceNumber - this.expectedSeqNum] = cleanedByteStream;
                this.delivered[segmentSequenceNumber - this.expectedSeqNum] = true;
                this.remainBuf--;
                if (isExpectedSeqNum(segmentSequenceNumber)) {
                    int deliveredCount = this.deliverData();
                    this.shiftData(deliveredCount);
                }
            }
            Segment ackSegment = new Segment(false, true, datagramSocket.getLocalPort(),
                    this.port, 0, this.expectedSeqNum, this.remainBuf);
            datagramSocket.send(new DatagramPacket(ackSegment.getBytes(), ackSegment.getBytes().length,
                    InetAddress.getByName(this.ip), segment.getSourcePort()));
        }
    }

    @Override
    public void close() throws Exception {
        writer.close();
        datagramSocket.close();
    }

    @Override
    public long getSSThreshold() {
        return 0;
    }

    @Override
    public long getWindowSize() {
        return 0;
    }

    private void resetDeliverd(){
        for(int i = 0; i < this.BUFFERSIZE; i++)
            delivered[i] = false;
    }

    private int deliverData() throws IOException {
        int i = 0;
        for (i = 0; i < this.BUFFERSIZE; i++) {
            if (delivered[i]){
                writer.append(deliveringData[i]);
                delivered[i] = false;
                this.expectedSeqNum++;
                this.remainBuf++;
            }
            else break;
        }
        return i;
    }

    private void shiftData(int deliveredCount){
        for (int i = 0; i < this.BUFFERSIZE; i++) {
            if (delivered[i]){
                delivered[i] = false;
                deliveringData[i - deliveredCount] = deliveringData[i];
                delivered[i - deliveredCount] = true;
            }
        }
    }

    private String cleanByteStream(byte[] byteStream){
        StringBuilder bytes = new StringBuilder();
        for (int i = 0; i < byteStream.length; i++) {
            if (byteStream[i] == '\0') {
                break;
            }
            bytes.append((char)byteStream[i]);
        }
        return bytes.toString();
    }
}
