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

    protected static Authenticate auth = null;

    protected static Notifications nots;

    protected static Object savedCmd = null;

    protected static String serverAddress_1;
    protected static String serverAddress_2;

	protected static String current;
	protected static String other;

    protected static int serverPort1;
    protected static int serverPort2;

	protected static int currentPort;
	protected static int otherPort;
	protected static int currentPortNot;
	protected static int otherPortNot;

    protected static int chooseAuth;
    protected static int chooseMenu;

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
	    if(s == null)
	    {
		    System.out.println("Could not connect to any server.");
		    return;
	    }

        chooseAuth = -1;
        chooseMenu = -1;

	    while(true)
	    {
		    try {
                if(auth == null) {
                    authAndReg();
                }
                else {
                    writeObject(auth);
                }
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
        int report;
        String username;
        String password;
        Object returnCommand;

        do {
	        if(reconnect)
		        throw new IOException();

            if(chooseAuth == -1) {
                chooseAuth = registerMenu();
            }

            report = -3;
            switch(chooseAuth) {
                case 1:
                    Authenticate auth;
                    if(savedCmd == null) {
                        auth = mAuthenticate();
                        Client.auth = auth;
                    }
                    else {
                        auth = Client.auth;
                    }
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
                    Register reg;
                    if (savedCmd == null) {
                        reg = mRegister();
                    }
                    else {
                        reg = (Register) savedCmd;
                    }
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
                    chooseAuth = 1;
                    break;
                default:
                    System.out.println("Fizeste merda.");
            }
            savedCmd = null;
            chooseAuth = -1;
        } while(report != 0);
    }

    public static Authenticate mAuthenticate() {
        String username, password;
        System.out.println(delimiter);
        System.out.print("Username: ");
        username = scString.nextLine();
        System.out.print("Password: ");
        password = scString.nextLine();
        Authenticate auth = new Authenticate(username,password);
        return auth;
    }

    public static Register mRegister() {
        String username, password;
        System.out.println(delimiter);
        System.out.print("Username: ");
        username = scString.nextLine();
        System.out.print("Password: ");
        password = scString.nextLine();
        Register reg = new Register(username,password, "");
        return reg;
    }

    public static int registerMenu() {
        System.out.println("\t \t IDEA BROKER - WE DON'T NEED GUI TO BE THE BEST\n");

        System.out.println("1 - Login");
        System.out.println("2 - Register\n");

        System.out.print("Option: ");

        chooseAuth = scInt.nextInt();

        return chooseAuth;
    }

    protected static boolean reconnectUserToServer()
    {
	    int tries=0, max=3;

	    //Check which server we are/were connected to.
	    if(current == null)
	    {
		    current = serverAddress_1;
		    currentPort = serverPort1;
		    currentPortNot = server1_not_port;
		    other = serverAddress_2;
		    otherPort = serverPort2;
		    otherPortNot = server2_not_port;
	    }

	    //Try to reconnect with the current server.
	    System.out.println("Establishing connection to "+current+":"+currentPort+" and to "+current+":"+currentPortNot);
	    while(tries < max)
	    {
		    try {
			    if(nots != null)
				    nots.shutdown = true;

			    //Notifications thread.
			    Socket aux = new Socket(current, currentPortNot);
			    System.out.println("Notifications socket established");
			    nots = new Notifications(aux);

			    System.out.println("Notifications thread started");

			    //Main thread sockets.
                if(s != null)
                    s.close();

			    s = new Socket(current, currentPort);
			    System.out.println("Main socket established");
			    out = new ObjectOutputStream(s.getOutputStream());
			    in = new ObjectInputStream(s.getInputStream());

			    reconnect = false;
			    return true;
		    } catch (Exception e) {
			    System.out.println(e);
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
	    int auxPort1, auxPort2;
	    String auxString;
	    tries = 0;
	    System.out.println("Establishing connection to "+other+":"+otherPort+" and to "+other+":"+otherPortNot);
	    while(tries < max)
	    {
		    try {
			    if(nots != null)
				    nots.shutdown = true;

			    //Notifications thread.
			    Socket aux = new Socket(other, otherPortNot);
                System.out.println("Notifications socket established");
			    nots = new Notifications(aux);

                System.out.println("Notifications thread started");

			    //Main thread sockets.
                if(s != null)
                    s.close();

			    s = new Socket(other, otherPort);
			    System.out.println("Main socket established");
			    out = new ObjectOutputStream(s.getOutputStream());
			    in = new ObjectInputStream(s.getInputStream());

			    //Change current and other.
			    auxString = current;
			    current = other;
			    other = auxString;

			    auxPort1 = currentPort;
			    currentPort = otherPort;
			    otherPort = auxPort1;

			    auxPort2 = currentPortNot;
			    currentPortNot = otherPortNot;
			    otherPortNot = auxPort2;

			    reconnect = false;
			    return true;
		    } catch (Exception e) {
			    System.out.println(e);
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

        Object returnComand;
        int report;

        System.out.println(delimiter);
        System.out.println("\t \t IDEA BROKER - WE DON'T NEED GUI TO BE THE BEST\n");

        do {
	        if(reconnect)
		        throw new IOException();

            if (chooseMenu == -1) {
                chooseMenu = menuOptions();
            }

            switch(chooseMenu) {
                case 1:
                    System.out.println("\nVou criar tópico\n");
                    CreateTopic cTopic;
                    if(savedCmd == null) {
                        cTopic = mCreateTopic();
                        savedCmd = cTopic;
                    }
                    else {
                        System.out.println("Tópico no saveCmd");
                        cTopic = (CreateTopic) savedCmd;
                    }

                    writeObject(cTopic);
                    if(reconnect == true) {
                        continue;
                    }
                    System.out.println("\nPedido enviado\n");

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
                    ViewIdeasTopic ideasTopic;
                    if(savedCmd == null) {
                        ideasTopic = mViewIdeasTopic();
                        savedCmd = ideasTopic;
                    }
                    else {
                        ideasTopic = (ViewIdeasTopic) savedCmd;
                    }
                    writeObject(ideasTopic);
                    if(reconnect == true) {
                        continue;
                    }

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
                    savedCmd = lTopics;
                    writeObject(lTopics);
                    if(reconnect == true) {
                        continue;
                    }

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
                    SubmitIdea sIdea;

                    if(savedCmd == null) {
                        sIdea = mSubmitIdea();
                        savedCmd = sIdea;
                    }
                    else {
                        sIdea = (SubmitIdea) savedCmd;
                    }

                    writeObject(sIdea);
                    if(reconnect == true) {
                        continue;
                    }

                    if (!sIdea.fileAttachName.equals("-")) {
                        try {
                            File fileToSend = new File(sIdea.fileAttachName);
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
                    BuyShares bShares;
                    if(savedCmd == null) {
                        bShares = mBuyShares();
                        savedCmd = bShares;
                    }
                    else {
                        bShares = (BuyShares) savedCmd;
                    }

                    writeObject(bShares);
                    if(reconnect == true) {
                        continue;
                    }


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
                    ViewIdeasNested vIdeasNested;

                    if(savedCmd == null) {
                        vIdeasNested = mViewIdeasNested();
                        savedCmd = vIdeasNested;
                    }
                    else {
                        vIdeasNested = (ViewIdeasNested) savedCmd;
                    }

                    writeObject(vIdeasNested);
                    if(reconnect == true) {
                        continue;
                    }

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


                    if (vIdeasNested.loadAttach == true && report == 0) {
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
                    savedCmd = showHist;

                    writeObject(showHist);
                    if(reconnect == true) {
                        continue;
                    }

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
                    ViewIdeasShares vIdeasShares;

                    if(savedCmd == null) {
                        vIdeasShares = mViewIdeasShares();
                        savedCmd = vIdeasShares;
                    }
                    else {
                        vIdeasShares = (ViewIdeasShares) savedCmd;
                    }

                    writeObject(vIdeasShares);
                    if(reconnect == true) {
                        continue;
                    }

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
                    SetShareValue setValue;

                    if(savedCmd == null) {
                        setValue = mSetShareValue();
                        savedCmd = setValue;
                    }
                    else {
                        setValue = (SetShareValue) savedCmd;
                    }

                    writeObject(setValue);
                    if(reconnect == true) {
                        continue;
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
                        System.out.println("Share value successfully changed.");
                    }
                    break;
                case 10:
                    DeleteIdea del;

                    if(savedCmd == null) {
                        del = mDeleteIdea();
                        savedCmd = del;
                    }
                    else {
                        del = (DeleteIdea) savedCmd;
                    }

                    writeObject(del);
                    if(reconnect == true) {
                        continue;
                    }

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
            savedCmd = null;
            chooseMenu = -1;
        } while(chooseMenu != 11);

    }

    public static int menuOptions() {
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

        chooseMenu = scInt.nextInt();

        return chooseMenu;
    }

    public static CreateTopic mCreateTopic() {
        String name;
        System.out.println(delimiter);
        System.out.print("Insert new topic: ");
        name = scString.nextLine();
        CreateTopic cTopic = new CreateTopic(name);
        return cTopic;
    }

    public static ViewIdeasTopic mViewIdeasTopic() {
        int topic;
        System.out.println(delimiter);
        System.out.println("Insert topic id: ");
        topic = scInt.nextInt();
        ViewIdeasTopic ideasTopic = new ViewIdeasTopic(topic);
        return ideasTopic;
    }

    public static SubmitIdea mSubmitIdea() {
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

        return sIdea;
    }

    public static BuyShares mBuyShares() {
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
        return bShares;
    }

    public static ViewIdeasNested mViewIdeasNested() {
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

        return vIdeasNested;
    }

    public static ViewIdeasShares mViewIdeasShares() {
        int ideaId_shares;
        System.out.print("Idea ID: ");
        ideaId_shares = scInt.nextInt();

        ViewIdeasShares vIdeasShares = new ViewIdeasShares(ideaId_shares);

        return vIdeasShares;
    }

    public static SetShareValue mSetShareValue() {
        int ideaId_Set;
        int newValue;
        System.out.print("Idea ID: ");
        ideaId_Set = scInt.nextInt();

        System.out.print("New Share Value: ");
        newValue = scInt.nextInt();

        SetShareValue setValue = new SetShareValue(ideaId_Set,newValue);

        return setValue;
    }

    public static DeleteIdea mDeleteIdea() {
        int ideaToDelete;
        System.out.print("Idea ID: ");
        ideaToDelete = scInt.nextInt();

        DeleteIdea del = new DeleteIdea(ideaToDelete);

        return del;
    }



}
