package serverTCP;

import sun.tools.tree.InlineNewInstanceExpression;

import java.net.*;
import java.nio.channels.DatagramChannel;

/**
 * Created with IntelliJ IDEA.
 * User: joaosimoes
 * Date: 10/25/13
 * Time: 12:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class Backup {

    public DatagramSocket sock;
    public DatagramPacket pack;

    public InetAddress myHost;
    public int myPort;

    public InetAddress otherHost;
    public int otherPort;

    public Backup(String myHost, int myPort, String otherHost, int otherPort) {
        this.myPort = myPort;
        this.otherPort = otherPort;
        try {
            this.myHost = InetAddress.getByName(myHost);
            sock = new DatagramSocket();
        } catch (UnknownHostException uhe) {
            System.out.println(uhe);
        } catch (SocketException se) {
            System.out.println(se);
        }
    }

}
