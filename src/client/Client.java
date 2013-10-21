package client;

import common.tcp.Authenticate;
import common.tcp.CreateTopic;
import common.tcp.Register;
import common.tcp.ViewIdeasTopic;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 * User: joaosimoes
 * Date: 10/19/13
 * Time: 9:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class Client {

    protected static Socket s;

    protected static String serverAddress_1;
    protected static String serverAddress_2;

    protected static int serverPort1;
    protected static int serverPort2;

    protected static ObjectInputStream in;
    protected  static ObjectOutputStream out;

    protected static String delimiter = "\n----------------------------------------------\n";

    public static void main(String args[]) {

        if(args.length != 4)
        {
            System.out.println("Usage: java ServerTCP <server1_address> <server1_port> <server2_address> <server2_port>");
            return;
        }

        serverAddress_1 = args[0];
        serverPort1 = Integer.parseInt(args[1]);

        serverAddress_2 = args[2];
        serverPort2 = Integer.parseInt(args[3]);

        try {
            s = new Socket(serverAddress_1, serverPort1);
        } catch(IOException ioe) {
            System.out.println("Error in socket creation.\n" + ioe);
            return;
        }

        try {
            out = new ObjectOutputStream(s.getOutputStream());
            in = new ObjectInputStream(s.getInputStream());
        } catch (IOException ioe) {

        }

        int choose;
        String username;
        String password;

        Scanner sc = new Scanner(System.in);

        System.out.println("\t \t IDEA BROKER - WE DON'T NEED GUI TO BE THE BEST\n");

        System.out.println("1 - Login");
        System.out.println("2 - Register\n");

        System.out.print("Option: ");

        choose = sc.nextInt();

        boolean success;

        do {
            success = false;
            switch(choose) {
                case 1:
                    System.out.println(delimiter);
                    System.out.print("Username: ");
                    username = sc.next();
                    System.out.print("Password: ");
                    password = sc.next();
                    Authenticate auth = new Authenticate(username,password);
                    writeObject(auth);
                    try {
                        System.out.println("Cheguei");
                        success = in.readBoolean();
                        System.out.println("Passei");
                    } catch (IOException e) {
                        System.out.println("Error reading authentication report from socket");
                    }
                    break;
                case 2:
                    System.out.println(delimiter);
                    System.out.print("Username: ");
                    username = sc.next();
                    System.out.print("Password: ");
                    password = sc.next();
                    Register reg = new Register(username,password, "");
                    writeObject(reg);
                    break;
                default:
                    System.out.println("Fizeste merda.");
            }
        } while(!success);

        System.out.println(delimiter);

        System.out.println("\t \t IDEA BROKER - WE DON'T NEED GUI TO BE THE BEST\n");

        System.out.println("1 - Create topic");
        System.out.println("2 - View topic ideas");
        System.out.println("3 - List topics");
        System.out.println("4 - Submit idea");
        System.out.println("5 - View topic ideas");
        System.out.println("6 - View ideas nested");
        System.out.println("7 - View user transactions history");
        System.out.println("8 - View idea shares");
        System.out.println("9 - Set Share Value");
        System.out.println("10 - Delete Idea\n");

        System.out.print("Option: ");

        choose = sc.nextInt();

        switch(choose) {
            case 1:
                String name;
                System.out.println(delimiter);
                System.out.print("Insert new topic: ");
                name = sc.next();
                CreateTopic cTopic = new CreateTopic(name);
                writeObject(cTopic);
                break;
            case 2:
                int topic;
                System.out.println(delimiter);
                System.out.println("Insert topic id: ");
                topic = sc.nextInt();
                ViewIdeasTopic ideasTopic = new ViewIdeasTopic(topic);
                writeObject(topic);
                break;
            case 3:

        }
    }

    protected static void writeObject(Object obj) {
        try {
            out.writeObject(obj);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(-1);
        }
    }



}
