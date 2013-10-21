package client;

import common.TopicInfo;
import common.tcp.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
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
    protected static ObjectOutputStream out;

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

        Object returnComand;

        int report;

        do {
            report = -1;
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
                        returnComand = in.readObject();
                        report = (Integer) returnComand;
                    } catch (IOException e) {
                        System.out.println("Error reading authentication report from socket.\n" + e);
                        return;
                    } catch (ClassNotFoundException e) {
                        System.out.println(e);
                        return;
                    }

                    if(report == -1) {
                        System.out.println("Server could not fulfill request.");
                    }
                    else if (report == -2) {
                        System.out.println("Username or password is not correct.");
                    }
                    else {
                        System.out.println("You're logged!");
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
                    try {
                        returnComand = in.readObject();
                        report = (Integer) returnComand;
                    } catch (IOException e) {
                        System.out.println("Error reading authentication report from socket.\n" + e);
                        return;
                    } catch (ClassNotFoundException e) {
                        System.out.println(e);
                        return;
                    }
                    if(report == -1) {
                        System.out.println("Server could not fulfill request.");
                    }
                    else if (report == -2) {
                        System.out.println("Username is already in use.");
                    }
                    else {
                        System.out.println("Account creation successful.");
                    }
                    break;
                default:
                    System.out.println("Fizeste merda.");
            }
        } while(report != 0);

        System.out.println(delimiter);

        System.out.println("\t \t IDEA BROKER - WE DON'T NEED GUI TO BE THE BEST\n");

        do {

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

                    try {
                        returnComand = in.readObject();
                        report = (Integer) returnComand;
                    } catch (IOException e) {
                        System.out.println("Error reading authentication report from socket.\n" + e);
                        return;
                    } catch (ClassNotFoundException e) {
                        System.out.println(e);
                        return;
                    }

                    if(report == -1) {
                        System.out.println("Server could not fulfill request.");
                    }
                    else if (report == -2) {
                        System.out.println("Topic already exists.");
                    }
                    else {
                        System.out.println("Topic created successful!");
                    }
                    break;
                case 2:
                    int topic;
                    System.out.println(delimiter);
                    System.out.println("Insert topic id: ");
                    topic = sc.nextInt();
                    ViewIdeasTopic ideasTopic = new ViewIdeasTopic(topic);
                    writeObject(topic);

                    try {
                        returnComand = in.readObject();
                        if(returnComand instanceof ArrayList<?>) {
                            System.out.println(delimiter);
                            ArrayList<TopicInfo> topics = (ArrayList) returnComand;
                            for (int i=0; i<topics.size(); i++) {
                                System.out.println(topics.get(i));
                            }
                            report = 0;
                        }
                        else {
                            report = (Integer) returnComand;
                        }
                    } catch (IOException e) {
                        System.out.println("Error reading authentication report from socket.\n" + e);
                        return;
                    } catch (ClassNotFoundException e) {
                        System.out.println(e);
                        return;
                    }

                    if(report == -1) {
                        System.out.println("Server could not fulfill request.");
                    }
                    else if (report == -2) {
                        System.out.println("Topic already exists.");
                    }
                    else {
                        System.out.println("Topic created successful!");
                    }

                    break;
                case 3:
                    //TODO: Read topics from socket.
                    System.out.println(delimiter);
                    ListTopics lTopics = new ListTopics();
                    writeObject(lTopics);
                    break;
                case 4:
                    String topicName;
                    int relatedIdea;
                    int nParts;
                    int valueShare;
                    int stance;
                    String text;
                    ArrayList<String> topics = new ArrayList<String>();

                    // Get related topics
                    do {
                        System.out.print("Related Topic: ");
                        topicName = sc.next();
                        if(!topicName.equals("")) {
                            topics.add(topicName);
                        }
                    } while(!topicName.equals(""));

                    //Get related idea
                    System.out.print("Related Idea: ");
                    relatedIdea = sc.nextInt();

                    // Get number of parts
                    System.out.print("Total of shares: ");
                    nParts = sc.nextInt();

                    // Get value of each share
                    System.out.print("Value of each share: ");
                    valueShare = sc.nextInt();

                    //Get stance if exists
                    if (relatedIdea > 0) {
                        System.out.print("Stance: ");
                        stance = sc.nextInt();
                    }
                    else {
                        stance = -2;
                    }

                    // Get idea text
                    System.out.print("Idea: ");
                    text = sc.next();

                    SubmitIdea sIdea = new SubmitIdea(topics,relatedIdea,nParts,valueShare,stance,text);

                    writeObject(sIdea);
                    break;
                case 5:
                    int topicId;
                    System.out.print("Topic ID: ");
                    topicId = sc.nextInt();

                    ViewIdeasTopic vIdeasTopic = new ViewIdeasTopic(topicId);

                    writeObject(vIdeasTopic);
                    break;
                case 6:
                    int ideaId;
                    System.out.print("Idea ID: ");
                    ideaId = sc.nextInt();

                    ViewIdeasNested vIdeasNested = new ViewIdeasNested(ideaId);

                    writeObject(vIdeasNested);
                    break;
                case 7:
                    ShowHistory showHist = new ShowHistory();

                    writeObject(showHist);
                    break;
                case 8:
                    int ideaId_shares;
                    System.out.print("Idea ID: ");
                    ideaId_shares = sc.nextInt();

                    ViewIdeasNested vIdeasShares = new ViewIdeasNested(ideaId_shares);

                    writeObject(vIdeasShares);
                    break;
                case 9:
                    int ideaId_Set;
                    int newValue;
                    System.out.print("Idea ID: ");
                    ideaId_Set = sc.nextInt();

                    System.out.print("New Share Value: ");
                    newValue = sc.nextInt();

                    SetShareValue setValue = new SetShareValue(ideaId_Set,newValue);

                    writeObject(setValue);
                    break;
                case 10:
                    int ideaToDelete;
                    System.out.print("Idea ID: ");
                    ideaToDelete = sc.nextInt();

                    DeleteIdea del = new DeleteIdea(ideaToDelete);

                    writeObject(del);
                    break;
                case 11:
                    System.out.println("Exiting...");
                    break;
            }
            System.out.print(delimiter);
        } while(choose != 11);
    }

    protected static void writeObject(Object obj) {
        try {
            out.writeObject(obj);
        } catch (IOException e) {
            System.out.println("Error writting object.\n" + e);
            System.exit(-1);
        }
    }



}
