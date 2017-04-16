import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.*;
import java.math.BigInteger;
import java.util.Scanner;

public class P1 {
	static String hostName;
	static String hostAddress;
	static String port;
	
	@SuppressWarnings("static-access")
	P1(String hostName, String hostAddress, String port) {
		this.hostName = hostName;
		this.hostAddress = hostAddress;
		this.port = port;
	}
	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		hostName = args[0];
	    Scanner in = new Scanner(System.in);
		/*----Find available port and display it on the screen*/
	    ServerSocket serverSocket =  new ServerSocket(0); 
	    
	    InetAddress addr = InetAddress.getLocalHost();
	    hostAddress = String.valueOf(addr.getHostAddress());
		port = String.valueOf(serverSocket.getLocalPort());
		System.out.println(hostAddress + " at port number: " + port);
		
		hostName = args[0];
		
		System.out.println("Please add this host and port on another machine using:\nadd (" 
		+ hostName + " ," + hostAddress + ", " + port +")");
		
		/*************Create the directories for nets and tuples*****************/
	    String dirPath = "/tmp/xwei1/linda/" + hostName;
	    File file = new File(dirPath);
	    file.mkdirs();
	    
	    String netsPath = dirPath+ "/nets.txt";
	    file = new File(netsPath);
	    file.createNewFile();
	    
	    String tuplesPath = dirPath+ "/tuples.txt";
	    file = new File(tuplesPath);
	    file.createNewFile();
	    
	    /****************Start Server******************/
	    try{
	    	Thread t = new Server(serverSocket, hostName, netsPath, tuplesPath);
  	         t.start();
  	    }catch(IOException e){
  	         e.printStackTrace();
  	    }
	    
	    System.out.print("linda>");
	    String command = in.nextLine();
	    command.trim();
	    int hostNumber = 0;
	    
	    while(true) {
	    	
	    	/*************************If the command is "add"******************/
	    	if(command.startsWith("add")) {
	    		//Get the number of hosts from the "add" command
	    		String[] hostInfo = command.split("\\(");
		    	hostNumber = hostInfo.length;
		    	
		    	// The first row is hostNumber and second row is the Master Host;
		    	writeMasterInfo(netsPath, hostNumber);
		    	
		    	// Write the other hosts in current machine's nets file
		    	String allHostInfo = hostName + " " + hostAddress + " " + port + " " + "\n";
		    	FileWriter fw = new FileWriter(netsPath, true);
	    		BufferedWriter bw = new BufferedWriter(fw);
		    	for(int i = 1; i < hostInfo.length; i = i + 1) {
		    		String eachHost = hostInfo[i].trim();
		    		eachHost = eachHost.substring(0, eachHost.length() - 1);
		    		String[] info = eachHost.split(",");
		    		String otherHostName = info[0].trim();
		    		String otherHostIP = info[1].trim();
		    		String otherHostPortNumber = info[2].trim();	    		
		    		bw.write(otherHostName + " ");
		    		bw.write(otherHostIP + " ");
		    		bw.write(otherHostPortNumber + " ");
		    		bw.write("\n");
		    		allHostInfo += otherHostName + " " + otherHostIP + " " + otherHostPortNumber + " " + "\n";
		    	}
		    	bw.close();
		    	fw.close();
		    	
		    	allHostInfo = hostNumber + " hosts" + "\n" + allHostInfo;
		    	System.out.println("all the hosts info is:\n" + allHostInfo);
		    	
		    	// Let all the other hosts add hosts info by sending add request to all the other hosts.
		    	FileReader fr = new FileReader(netsPath);
		    	BufferedReader br = new BufferedReader(fr);
		    	br.readLine();//The first line is number of hosts info
		    	String tmp = br.readLine();// The second line is the current machine info;
		    	
		    	while( (tmp = br.readLine()) != null) {
		    		String[] strs = tmp.split(" ");
		    		String otherHostIP = strs[1];
		    		String otherHostPortNumber = strs[2];
		    		Client client = new Client();
		    		client.add(otherHostIP, otherHostPortNumber, allHostInfo);
		    	}
				br.close();
				System.out.print("linda>");
				
		    }else if(command.startsWith("out")) {
		    	hostNumber = getHostNumber(netsPath);// The number of hosts info is stored at the first line
		    	int start = command.indexOf('(');
		    	int end = command.indexOf(')');
		    	String strToOut = command.substring(start + 1, end);
		    	strToOut = preprocess(strToOut);
		    	System.out.println("The tuple to put in upper space is: [ " + strToOut.trim() +" ]");
		    	
		    	//Get the host to store the tuple by hashing and send the "out tuple" request to corresponding host
		    	String[] targetInfo = exactMatch(strToOut, hostNumber, netsPath);
		    	System.out.println("The tuple will be stored on " + targetInfo[0]);
	    		Client client = new Client();
	    		client.out(targetInfo[1], targetInfo[2], strToOut);
	    		System.out.print("linda>");
		    }else if(command.startsWith("in")) {
		    	hostNumber = getHostNumber(netsPath);// The number of hosts info is stored at the first line
		    	
		    	//get the string to search
		    	int start = command.indexOf('(');
		    	int end = command.indexOf(')');
		    	String strToIn = command.substring(start + 1, end);
		    	strToIn = preprocess(strToIn);
		    	
		    	//If exact match
		    	if(!strToIn.contains("?")) {
		    		//Get the host to store the tuple by hashing and send the "out tuple" request to corresponding host
		    		String[] targetInfo = exactMatch(strToIn, hostNumber, netsPath);
		    		Client client = new Client();
		    		while(!client.rde(targetInfo[1], targetInfo[2], strToIn)) {
		    			
		    		}
		    		client.ine(targetInfo[1], targetInfo[2], strToIn);
		    		System.out.print("linda>");
		    	}else{
		    		/* Create n threads to broadcast the message to all the hosts. 
		    		 * If not found, current host will block, waiting for available tuple at all the hosts.
		    		 * If found, random host will be chosen and send back the host machine to current host
		    		 * Then current machine will delete the tuple
		    		 */
		    		Thread[] broadcastThread = new BroadcastThread[hostNumber];
		    		SharedInfo sharedInfo = new SharedInfo();
		    		broadCast(netsPath, hostNumber, broadcastThread, sharedInfo, strToIn);
		    		
	    			Client client = new Client();
	    			System.out.println("The tuple to be deleted is: [" + sharedInfo.tuples + "]");
		    		System.out.println("The tuple will be removed from: " + sharedInfo.hostAddress + ". Port number is: " + sharedInfo.port);
		    		client.ine(sharedInfo.hostAddress, sharedInfo.port, sharedInfo.tuples);// Remove the tuple using exact match command
		    		System.out.print("linda>");
		    	}
		    	
		    }else if(command.startsWith("rd")) {
		    	hostNumber = getHostNumber(netsPath);// The number of hosts info is stored at the first line
		    	
		    	//get the string to read
		    	int start = command.indexOf('(');
		    	int end = command.indexOf(')');
		    	String strToRd = command.substring(start + 1, end);
		    	strToRd = preprocess(strToRd);
		    	
		    	if(!strToRd.contains("?")) {
		    		//Get the host to store the tuple by hashing and send the "out tuple" request to corresponding host
		    		String[] targetInfo = exactMatch(strToRd, hostNumber, netsPath);
		    		Client client = new Client();
		    		while(!client.rde(targetInfo[1], targetInfo[2], strToRd)) {
		    			
		    		}
		    		System.out.print("linda>");
		    	}else{
		    		/* Create n threads to broadcast the message to all the hosts. 
		    		 * If not found, current host will block, waiting for available tuple at all the hosts.
		    		 * If found, random host will be chosen and send back the host machine to current host
		    		 * Then current machine will display the corresponding info
		    		 */
		    		Thread[] broadcastThread = new BroadcastThread[hostNumber];
		    		SharedInfo sharedInfo = new SharedInfo();
		    		broadCast(netsPath, hostNumber, broadcastThread, sharedInfo, strToRd);
		    		System.out.println("The tuple to be read is: [" + sharedInfo.tuples + "]");
		    		System.out.println("The tuple will be read from: " + sharedInfo.hostAddress + ". Port number is: " + sharedInfo.port);
		    		System.out.print("linda>");
		    	}
		    	
		    }else{
		    	System.out.println("Command Wrong! Must start with add, out, in or rd. Please input again.");
		    }
		    command = in.nextLine();
		    command.trim();
	    }
	    
	}
	/*
	 * write the master info to the nets file
	 */
	public static void writeMasterInfo(String netsPath, int hostNumber) throws IOException{
		FileWriter fw = new FileWriter(netsPath);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(hostNumber + " hosts:" + "\n" + hostName + " " + hostAddress + " " + port + " " + "\n");
		bw.close();
	}
	/*
	 *  Get the corresponding hostId by using hashing and mod
	 */
	public static int md5(String str, int hostNumber) {
		int hostId = 0;
		try {
			MessageDigest md =  MessageDigest.getInstance("MD5");
			md.update(str.getBytes());
			byte[] bArr= md.digest();
			BigInteger number = new BigInteger(1, bArr);
			
			hostId = number.intValue() % hostNumber;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return hostId;
	}
	/*
	 *  Preprocess the command line
	 */
	public static String preprocess(String str){
		String res = "";
		String[] tmp = str.split(",");
		for(String item: tmp) {
			res += item.trim() + " ";
		}
		return res;
	}
	/*
	 *  Broadcast the tuple to all hosts
	 */
	public static void broadCast(String netsPath, int hostNumber, Thread[] broadcastThread, SharedInfo sharedInfo, String strToIn) throws IOException, InterruptedException {
		FileReader fr = new FileReader(netsPath);
    	BufferedReader br = new BufferedReader(fr);
    	br.readLine();
		for(int i = 0; i < hostNumber; ++i) {
			String tmp = br.readLine();
			String[] strs = tmp.split(" ");
			String otheHostname = strs[0];
    		String otherHostIP = strs[1];
    		String otherHostPortNumber = strs[2];
    		broadcastThread[i] = new BroadcastThread(sharedInfo, otherHostIP, otherHostPortNumber, strToIn, otheHostname);// Create n threads
		}
		br.close();
		// start all the threads
		for(int i = 0; i < hostNumber; ++i) {
			broadcastThread[i].start();
		}
		//If not found, block current host
		while(sharedInfo.tuples.equals("")) {
			Thread.sleep(2000);
		}
	}
	
	/*
	 * Get number of hosts from the nets file
	 */
	public static int getHostNumber(String netsPath) throws IOException {
		FileReader fr = new FileReader(netsPath);
    	@SuppressWarnings("resource")
		BufferedReader br = new BufferedReader(fr);
    	String[] hostNumberInfo = br.readLine().split(" ");
    	return Integer.valueOf(hostNumberInfo[0]);
	}
	
	/*
	 * Get the corresponding host info by hashing and mod
	 */
	
	public static String[] exactMatch(String strToIn, int hostNumber, String netsPath) throws IOException{
		int hostId = md5(strToIn, hostNumber);
		if(hostId == -1) {
    		hostId += hostNumber;
    	}
		int cnt = 0;
		FileReader fr = new FileReader(netsPath);
    	BufferedReader br = new BufferedReader(fr);
		
    	br.readLine();
    	String tmp = br.readLine();
    	while( cnt++ != hostId) {
    		tmp = br.readLine();
    	}
    	br.close();
    	String[] targetInfo = tmp.split(" ");
		return targetInfo;
	}
}
