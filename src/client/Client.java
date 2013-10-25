package client;

import common.*;
import common.tcp.*;
import serverRMI.Transaction;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
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

	protected static long timeout = 500;
	public static boolean reconnect = false;
    protected static Socket s;

    protected static Notifications nots;

    protected static String serverAddress_1;
    protected static String serverAddress_2;

	protected static String current;
	protected static String other;

    protected static int serverPort1;
    protected static int serverPort2;

    protected static int server1_not_port;
    protected static int server2_not_port;

    protected static ObjectInputStream in;
    protected static ObjectOutputStream out;

    protected static Scanner scString = new Scanner(System.in);
    protected static Scanner scInt = new Scanner(System.in);

    protected static String delimiter = "\n----------------------------------------------\n";

    public static void main(String args[]) {

        if(args.length != 6)
        {
            System.out.println("Usage: java ServerTCP <server1_address> <server1_port> <server1_not_port> <server2_address> <server2_port> <server2_not_port>");
            return;
        }

        serverAddress_1 = args[0];
        serverPort1 = Integer.parseInt(args[1]);
        server1_not_port = Integer.parseInt(args[2]);

        serverAddress_2 = args[3];
        serverPort2 = Integer.parseInt(args[4]);
        server2_not_port = Integer.parseInt(args[5]);

        reconnectUserToServer();

	    while(true)
	    {
		    try {
			    authAndReg();
			    execMenu();
		    } catch (IOException io) {
			    reconnectUserToServer();
		    }
	    }

    }

    protected static void writeObject(Object obj) {

	    try {
		    out.writeObject(obj);
		    out.flush();
	    } catch (Exception e) {
		    reconnect = true;
	    }
    }

    protected static void authAndReg() throws IOException {
	    int choose;
        int report;
        String username;
        String password;
        Object returnCommand;

        do {
	        if(reconnect)
		        throw new IOException();

	        System.out.println("\t \t IDEA BROKER - WE DON'T NEED GUI TO BE THE BEST\n");

	        System.out.println("1 - Login");
	        System.out.println("2 - Register\n");

	        System.out.print("Option: ");

	        choose = scInt.nextInt();

            report = -3;
            switch(choose) {
                case 1:
                    System.out.println(delimiter);
                    System.out.print("Username: ");
                    username = scInt.next();
                    System.out.print("Password: ");
                    password = scInt.next();
                    Authenticate auth = new Authenticate(username,password);
                    writeObject(auth);
                    try {
                        returnCommand = in.readObject();
                        report = (Integer) returnCommand;
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
                    username = scInt.next();
                    System.out.print("Password: ");
                    password = scInt.next();
                    Register reg = new Register(username,password, "");
                    writeObject(reg);
                    try {
                        returnCommand = in.readObject();
                        report = (Integer) returnCommand;
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
                    else if (report == 0) {
                        System.out.println("Account creation successful.");
                    }
                    else {
                        System.out.println("Could not connect to server.");
                    }
                    report = 1;
                    choose = 1;
                    break;
                default:
                    System.out.println("Fizeste merda.");
            }
        } while(report != 0 || choose != 1);
    }

    protected static boolean reconnectUserToServer()
    {
	    int tries=0, max=3;

	    //Check which server we are/were connected to.
	    if(current == null)
	    {
		    current = serverAddress_1;
		    other = serverAddress_2;
	    }

	    if (nots != null && nots.isAlive())
		    nots.shutdown = true;

	    //Try to reconnect with the current server.
	    while(tries < max)
	    {
		    try {
			    //Notifications thread.
			    Socket aux = new Socket(current, server1_not_port);
			    nots = new Notifications(aux);

			    System.out.println("Notifications socket established");

			    //Main thread sockets.
			    s.close();

			    s = new Socket(current, serverPort1);
			    System.out.println("Main socket established");
			    out = new ObjectOutputStream(s.getOutputStream());
			    in = new ObjectInputStream(s.getInputStream());

			    reconnect = false;
			    return true;
		    } catch (Exception e) {
			    if(tries++ >= max)
				    break;
			    else
			    {
				    try {
					    Thread.sleep(timeout);
				    } catch (InterruptedException e1) {
					    //Do nothing.
				    }
			    }
		    }
	    }

	    //Try to reconnect with the other server.
	    String auxString;
	    while(tries < max)
	    {
		    try {
			    //Notifications thread.
			    Socket aux = new Socket(other, server2_not_port);
			    nots = new Notifications(aux);

			    System.out.println("Notifications socket established");

			    //Main thread sockets.
			    s.close();

			    s = new Socket(other, serverPort2);
			    System.out.println("Main socket established");
			    out = new ObjectOutputStream(s.getOutputStream());
			    in = new ObjectInputStream(s.getInputStream());

			    //Change current and other.
			    auxString = current;
			    current = other;
			    other = auxString;

			    reconnect = false;
			    return true;
		    } catch (Exception e) {
			    if(tries++ >= max)
				    break;
			    else
			    {
				    try {
					    Thread.sleep(timeout);
				    } catch (InterruptedException e1) {
					    //Do nothing.
				    }
			    }
		    }
	    }

        return false;
    }

    public static void execMenu() throws IOException{

        int choose;
        Object returnComand;
        int report;

        System.out.println(delimiter);
        System.out.println("\t \t IDEA BROKER - WE DON'T NEED GUI TO BE THE BEST\n");

        do {
	        if(reconnect)
		        throw new IOException();

            System.out.println("1 - Create topic");
            System.out.println("2 - View topic ideas");
            System.out.println("3 - List topics");
            System.out.println("4 - Submit idea");
            System.out.println("5 - Buy shares");
            System.out.println("6 - View ideas nested");
            System.out.println("7 - View user transactions history");
            System.out.println("8 - View idea shares");
            System.out.println("9 - Set share value");
            System.out.println("10 - Delete idea\n");

            System.out.print("Option: ");

            choose = scInt.nextInt();

            switch(choose) {
                case 1:
                    String name;
                    System.out.println(delimiter);
                    System.out.print("Insert new topic: ");
                    name = scString.nextLine();
                    CreateTopic cTopic = new CreateTopic(name);
                    writeObject(cTopic);

                    try {
                        returnComand = in.readObject();
                        report = (Integer) returnComand;
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
                    else if (report == -3) {
                        System.out.println("RMI is down.");
                        System.exit(-1);
                    }
                    else {
                        System.out.println("Topic created successful!");
                    }
                    break;
                case 2:
                    int topic;
                    System.out.println(delimiter);
                    System.out.println("Insert topic id: ");
                    topic = scInt.nextInt();
                    ViewIdeasTopic ideasTopic = new ViewIdeasTopic(topic);
                    writeObject(ideasTopic);

                    try {
                        returnComand = in.readObject();

                        if(returnComand instanceof ArrayList<?>) {
                            System.out.println(delimiter);
                            ArrayList<IdeaInfo> ideas = (ArrayList) returnComand;
                            for (int i=0; i<ideas.size(); i++) {
                                System.out.println(ideas.get(i));
                            }
                            report = 0;
                        }
                        else {
                            report = (Integer) returnComand;
                        }
                    } catch (ClassNotFoundException e) {
                        System.out.println(e);
                        return;
                    }

                    if(report == -1) {
                        System.out.println("Server could not fulfill request.");
                    }
                    else if (report == -3) {
                        System.out.println("RMI is down.");
                        System.exit(-1);
                    }
                    break;
                case 3:
                    System.out.println(delimiter);
                    ListTopics lTopics = new ListTopics();
                    writeObject(lTopics);

                    try {
                        returnComand = in.readObject();

                        if(returnComand instanceof ArrayList<?>) {
                            ArrayList<TopicInfo> topics = (ArrayList) returnComand;
                            for (int i=0; i<topics.size(); i++) {
                                System.out.println(topics.get(i));
                            }
                            report = 0;
                        }
                        else {
                            report = (Integer) returnComand;
                        }
                    } catch (ClassNotFoundException e) {
                        System.out.println(e);
                        return;
                    }

                    if(report == -1) {
                        System.out.println("Server could not fulfill request.");
                    }
                    else if (report == -3) {
                        System.out.println("RMI is down.");
                        System.exit(-1);
                    }
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
                        topicName = scString.nextLine();
                        if(!topicName.equals("-1")) {
                            topics.add(topicName);
                        }
                    } while(!topicName.equals("-1"));

                    //Get related idea
                    System.out.print("Related Idea: ");
                    relatedIdea = scInt.nextInt();

                    // Get number of parts
                    System.out.print("Total of shares: ");
                    nParts = scInt.nextInt();

                    // Get value of each share
                    System.out.print("Value of each share: ");
                    valueShare = scInt.nextInt();

                    //Get stance if exists
                    if (relatedIdea > 0) {
                        System.out.print("Stance: ");
                        stance = scInt.nextInt();
                    }
                    else {
                        stance = 0;
                    }

                    // Get idea text
                    System.out.print("Idea: ");
                    text = scString.nextLine();



                    String hasAttach, attach;

                    System.out.println("Do you want attach some file? (y/n): ");
                    hasAttach = scString.nextLine();

                    if(hasAttach.equals("y")) {
                        System.out.print("Filename: ");
                        attach = scString.nextLine();
                    }
                    else {
                        attach = "-";
                    }

                    SubmitIdea sIdea = new SubmitIdea(topics,relatedIdea,nParts,valueShare,stance,text,attach);

                    writeObject(sIdea);

                    if (hasAttach.equals("y")) {
                        try {
                            File fileToSend = new File(attach);
                            int fileLength = (int)fileToSend.length();
                            out.writeObject(fileLength);
                            byte[] fileData = new byte[fileLength];
                            FileInputStream fis = new FileInputStream(fileToSend);
                            BufferedInputStream bis = new BufferedInputStream(fis);
                            bis.read(fileData,0,fileData.length);
                            out.write(fileData,0,fileData.length);
                            out.flush();
                            System.out.println("File successfully sent!");
                        } catch (IOException ioe) {
                            System.out.println("Error sending file.\n" + ioe);
                            return;
                        }
                    }

                    try {
                        returnComand = in.readObject();
                        report = (Integer) returnComand;
                    } catch (ClassNotFoundException e) {
                        System.out.println(e);
                        return;
                    }

                    if(report == -1) {
                        System.out.println("Server could not fulfill request."); //TODO: Se no SQL der merda, gerar esta excepção. VERIFICAR IMPORTANTE
                    }
                    else if (report == -3) {
                        System.out.println("RMI is down.");
                        System.exit(-1);
                    }
                    else {
                        System.out.println("Idea submitted successfully.");
                    }
                    break;
                case 5:
                    int idea_id, share_num, price_per_share, new_price_share;
                    System.out.print("Idea ID: ");
                    idea_id = scInt.nextInt();
                    System.out.print("Number of shares: ");
                    share_num = scInt.nextInt();
                    System.out.print("Buy price: ");
                    price_per_share = scInt.nextInt();
                    System.out.print("New price: ");
                    new_price_share = scInt.nextInt();
                    BuyShares bShares = new BuyShares(idea_id,share_num,price_per_share,new_price_share);

                    writeObject(bShares);


                    try {
                        returnComand = in.readObject();
                        report = (Integer) returnComand;
                    } catch (ClassNotFoundException e) {
                        System.out.println(e);
                        return;
                    }

                    if(report == -1) {
                        System.out.println("Server could not fulfill request.");
                    }
                    else if (report == -3) {
                        System.out.println("RMI is down.");
                        System.exit(-1);
                    }
                    else {
                        System.out.println("Share successfully bought.");
                    }
                    break;
                case 6:
                    int ideaId;
                    String load;
                    boolean loadAttach;

                    System.out.print("Idea ID: ");
                    ideaId = scInt.nextInt();
                    System.out.println("Do you want load attached file? (y/n)");
                    load = scString.nextLine();

                    if(load.equals("y")) {
                        loadAttach = true;
                    }
                    else {
                        loadAttach = false;
                    }

                    ViewIdeasNested vIdeasNested = new ViewIdeasNested(ideaId, loadAttach);

                    writeObject(vIdeasNested);

                    IdeasNestedPack ideasNested = null;

                    try {
                        returnComand = in.readObject();

                        if(returnComand instanceof IdeasNestedPack) {
                            ideasNested = (IdeasNestedPack) returnComand;
                            for (int i=0; i<ideasNested.ideasNested.size(); i++) {
                                System.out.println(ideasNested.ideasNested.get(i));
                            }
                            report = 0;
                        }
                        else {
                            report = (Integer) returnComand;
                        }
                    } catch (ClassNotFoundException e) {
                        System.out.println(e);
                        return;
                    }


                    if (loadAttach == true && report == 0) {
                        try {


                            FileOutputStream fos = new FileOutputStream("downloads/"+"merda.zip");
                            BufferedOutputStream bos = new BufferedOutputStream(fos);

                            bos.write(ideasNested.attachFile, 0 , ideasNested.fileSize); //TODO: Set current
                            bos.flush();
                            bos.close();
                        } catch (IOException ioe) {
                            System.out.println(ioe);
                        }
                    }


                    if(report == -1) {
                        System.out.println("Server could not fulfill request.");
                    }
                    else if (report == -3) {
                        System.out.println("RMI is down.");
                        System.exit(-1);
                    }
                    break;
                case 7:
                    ShowHistory showHist = new ShowHistory();

                    writeObject(showHist);

                    try {
                        returnComand = in.readObject();

                        if(returnComand instanceof ArrayList<?>) {
                            ArrayList<TransactionInfo> transInfo = (ArrayList) returnComand;
                            for (int i=0; i<transInfo.size(); i++) {
                                System.out.println(transInfo.get(i));
                            }
                            report = 0;
                        }
                        else {
                            report = (Integer) returnComand;
                        }
                    } catch (ClassNotFoundException e) {
                        System.out.println(e);
                        return;
                    }

                    if(report == -1) {
                        System.out.println("Server could not fulfill request.");
                    }
                    else if (report == -3) {
                        System.out.println("RMI is down.");
                        System.exit(-1);
                    }
                    break;
                case 8:
                    int ideaId_shares;
                    System.out.print("Idea ID: ");
                    ideaId_shares = scInt.nextInt();

                    ViewIdeasShares vIdeasShares = new ViewIdeasShares(ideaId_shares);

                    writeObject(vIdeasShares);

                    try {
                        returnComand = in.readObject();

                        if(returnComand instanceof ArrayList<?>) {
                            ArrayList<ShareInfo> sharesIdea = (ArrayList) returnComand;
                            for (int i=0; i<sharesIdea.size(); i++) {
                                System.out.println(sharesIdea.get(i));
                            }
                            report = 0;
                        }
                        else {
                            report = (Integer) returnComand;
                        }
                    } catch (ClassNotFoundException e) {
                        System.out.println(e);
                        return;
                    }

                    if(report == -1) {
                        System.out.println("Server could not fulfill request.");
                    }
                    else if (report == -3) {
                        System.out.println("RMI is down.");
                        System.exit(-1);
                    }

                    break;
                case 9:
                    int ideaId_Set;
                    int newValue;
                    System.out.print("Idea ID: ");
                    ideaId_Set = scInt.nextInt();

                    System.out.print("New Share Value: ");
                    newValue = scInt.nextInt();

                    SetShareValue setValue = new SetShareValue(ideaId_Set,newValue);

                    writeObject(setValue);

                    try {

                        returnComand = in.readObject();

                        report = (Integer) returnComand;
                    } catch (ClassNotFoundException e) {
                        System.out.println(e);
                        return;
                    }

                    if(report == -1) {
                        System.out.println("Server could not fulfill request."); //TODO: Se no SQL der merda, gerar esta excepção. VERIFICAR IMPORTANTE
                    }
                    else if (report == -3) {
                        System.out.println("RMI is down.");
                        System.exit(-1);
                    }
                    else {
                        System.out.println("Share value successfully changed.");
                    }
                    break;
                case 10:
                    int ideaToDelete;
                    System.out.print("Idea ID: ");
                    ideaToDelete = scInt.nextInt();

                    DeleteIdea del = new DeleteIdea(ideaToDelete);

                    writeObject(del);

                    try {
                        returnComand = in.readObject();
                        report = (Integer) returnComand;
                    } catch (ClassNotFoundException e) {
                        System.out.println(e);
                        return;
                    }

                    if(report == -1) {
                        System.out.println("Server could not fulfill request.");
                    }
                    else if (report == -2) {
                        System.out.println("You're not idea full owner.");
                    }
                    else if (report == -3) {
                        System.out.println("RMI is down.");
                        System.exit(-1);
                    }
                    else {
                        System.out.println("Idea deleted successfully.");
                    }

                    break;
                case 11:
                        in.close();
                        out.close();
                        s.close();
                        nots.shutdown = true;

                    System.out.println("Exiting...");
                    System.exit(0);
            }
            System.out.print(delimiter);
        } while(choose != 11);

    }
}
