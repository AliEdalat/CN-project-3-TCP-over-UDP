import java.util.Random;

public class TCPSocketBinded extends TCPSocket {

    private int sequenceNumber = 100;
    private static int RTT = 10000;
    private static int segmentDataSize = 1392;
    private String ip;
    private int port;
    private EnhancedDatagramSocket datagramSocket;
    private int myPort;

    public TCPSocketBinded(String ip, int port) throws Exception {
        super(ip, port);
        System.out.println("binded");
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
        datagramSocket.setSoTimeout(RTT);
    }

    @Override
    public void send(String pathToFile) throws Exception {

    }

    @Override
    public void receive(String pathToFile) throws Exception {

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
