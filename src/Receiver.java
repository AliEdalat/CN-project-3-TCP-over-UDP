import TCPServer.TCPServerSocket;
import TCPServer.TCPServerSocketImpl;
import TCPSocket.TCPSocket;

public class Receiver {
    public static void main(String[] args) throws Exception {
        TCPServerSocket tcpServerSocket = new TCPServerSocketImpl(1237);
        TCPSocket tcpSocket = tcpServerSocket.accept();
        tcpSocket.receive("output.txt");
        tcpSocket.close();
        tcpServerSocket.close();
    }
}
