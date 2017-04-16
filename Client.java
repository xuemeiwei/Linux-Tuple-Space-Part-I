import java.net.*;
import java.io.*;

/*
 * Implement different requests client send to the server
 */
public class Client {
	
	// Add request
	void add(String hostAddress, String portNumber, String allHostInfo) {
		System.out.println("Connecting to " + hostAddress + " on port " + portNumber);
		try {
			Socket client = new Socket(hostAddress, Integer.valueOf(portNumber));
			System.out.println("Just connected to "+ client.getRemoteSocketAddress());
			
			allHostInfo = "add" + allHostInfo;
			DataOutputStream out = new DataOutputStream(client.getOutputStream());
			out.writeUTF(allHostInfo);
			
			InputStream inFromServer = client.getInputStream();
			DataInputStream in = new DataInputStream(inFromServer);
			System.out.println("Server says " + in.readUTF());
			
			client.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Out request
	void out(String hostAddress, String portNumber, String tuples) {
		System.out.println("Connecting to " + hostAddress + " on port " + portNumber);
		try {
			Socket client = new Socket(hostAddress, Integer.valueOf(portNumber));
			System.out.println("Just connected to "+ client.getRemoteSocketAddress());
			
			tuples = "out" + tuples;
			DataOutputStream out = new DataOutputStream(client.getOutputStream());
			out.writeUTF(tuples);
			
			InputStream inFromServer = client.getInputStream();
			DataInputStream in = new DataInputStream(inFromServer);
			System.out.println("Server says " + in.readUTF());
			
			client.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Exact in request
	void ine(String hostAddress, String portNumber, String tuples) {
		System.out.println("Connecting to " + hostAddress + " on port " + portNumber);
		try {
			Socket client = new Socket(hostAddress, Integer.valueOf(portNumber));
			System.out.println("Just connected to "+ client.getRemoteSocketAddress());
			tuples = "ine" + tuples;
			
			DataOutputStream out = new DataOutputStream(client.getOutputStream());
			out.writeUTF(tuples);
			
			InputStream inFromServer = client.getInputStream();
			DataInputStream in = new DataInputStream(inFromServer);
			System.out.println("Server says " + in.readUTF());
			client.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Exact read request
	void rde(String hostAddress, String portNumber, String tuples) {
		System.out.println("Connecting to " + hostAddress + " on port " + portNumber);
		try {
			Socket client = new Socket(hostAddress, Integer.valueOf(portNumber));
			System.out.println("Just connected to "+ client.getRemoteSocketAddress());
			tuples = "rde" + tuples;
			
			DataOutputStream out = new DataOutputStream(client.getOutputStream());
			out.writeUTF(tuples);
			
			InputStream inFromServer = client.getInputStream();
			DataInputStream in = new DataInputStream(inFromServer);
			System.out.print("Server says " + in.readUTF());
			
			client.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
