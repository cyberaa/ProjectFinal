package serverRMI;

import java.util.*;
import java.sql.*;
/**
 * Created with IntelliJ IDEA.
 * User: joaosimoes
 * Date: 10/17/13
 * Time: 3:46 PM
 * To change this template use File | Settings | File Templates.
 */

public class ConnectionPool implements Runnable {
    int initialConnections = 5;
    Vector connectionsAvailable = new Vector();
    Vector connectionsUsed = new Vector();

    String connectionUrl ;
    String userName;
    String userPassword ;


    //TODO: Detectar no check connection se a connection está activa. Se não, elimina-a e cria uma nova.

    public ConnectionPool(String url, String userName, String userPass) throws SQLException {
        this.connectionUrl = url;
        this.userName = userName;
        this.userPassword = userPass;
        for (int count = 0; count < initialConnections; count++) {
            connectionsAvailable.addElement(getConnection());
        }
    }


    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(connectionUrl, userName, userPassword);
    }


    public synchronized void releaseConnection(Connection con) throws SQLException{
        //System.out.println("Connections Available: " + connectionsAvailable.size()+" | Connections Used: " + connectionsUsed.size());
        connectionsUsed.remove(con);
        //System.out.println("Connections Available: " + connectionsAvailable.size()+" | Connections Used: " + connectionsUsed.size());
        connectionsAvailable.add(con);
        //System.out.println("Connections Available: " + connectionsAvailable.size()+" | Connections Used: " + connectionsUsed.size());
    }

    public synchronized void closeConnection(Connection con) throws SQLException {
        connectionsUsed.remove(con);
        con.close();
    }

    public synchronized Connection connectionCheck() throws SQLException {
        Connection newConnection = null;
        if (connectionsAvailable.size() == 0) {
            // creating a new Connection
            newConnection = getConnection();
            // adding Connection to used list
            connectionsUsed.addElement(newConnection);
        } else {

            newConnection = (Connection) connectionsAvailable.lastElement();

            connectionsAvailable.removeElement(newConnection);

            connectionsUsed.addElement(newConnection);
        }
        return newConnection;
    }

    public int availableCount() {
        return connectionsAvailable.size();
    }

    public void run() {
        try {
            while (true) {
                synchronized (this) {
                    while (connectionsAvailable.size() > initialConnections) {
                        Connection connection = (Connection) connectionsAvailable
                                .lastElement();
                        connectionsAvailable.removeElement(connection);

                        connection.close();
                    }
                }
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
