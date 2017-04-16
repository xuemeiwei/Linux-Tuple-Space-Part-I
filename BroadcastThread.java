import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/*
 * Implement the broadcast request that client will send to server by using thread
 */
public class BroadcastThread extends Thread{
	public SharedInfo sharedInfo;
//	boolean flag = false;
	private String hostAddress = null;
	private String port = null;
	String tuples;
	private String hostname;
	BroadcastThread(SharedInfo sharedInfo, String hostAddress, String port, String tuples, String hostname) {
		this.sharedInfo = sharedInfo;
		this.hostAddress = hostAddress;
		this.port = port;
		this.tuples = tuples;
		this.hostname = hostname;
	}
	
	public void run(){
		try {
			// check whether the tuple is found, if not send request to server to continue searching
			while(sharedInfo.tuples.equals("")) {
				Socket message = new Socket(hostAddress, Integer.valueOf(port));
				tuples = "bro" + tuples;
				
				DataOutputStream out = new DataOutputStream(message.getOutputStream());
				out.writeUTF(tuples);
				InputStream inFromServer = message.getInputStream();
				DataInputStream in = new DataInputStream(inFromServer);
				String cmd = in.readUTF();
				if(cmd.equals("")) {
					System.out.println("Nothing Matched on: " + hostname);
				}else{
					System.out.println("Matched on: " + hostname);
				}
				
				// If found, sharedInfo will be set and store the corresponding hostAddress, port and tuple
				// Since the "set" method is synchronized, only one host can set it at one time;
				if(!cmd.equals("")) {
					sharedInfo.set(hostAddress, port, cmd);
				}
				message.close();
				Thread.sleep(1000);
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
