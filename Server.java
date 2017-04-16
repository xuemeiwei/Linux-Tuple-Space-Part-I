import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.*;
import java.math.BigInteger;
 
public class Server extends Thread {
	
   private ServerSocket serverSocket;
   private String hostName;
   private String netsPath;
   private String tuplesPath;
   
   public Server(ServerSocket serverSocket, String hostName, String netsPath, String tuplesPath) throws IOException {
      this.serverSocket = serverSocket;
      this.hostName = hostName;
      this.netsPath = netsPath;
      this.tuplesPath = tuplesPath;
   }
 
   public void run() {
      while(true) {
         try {
            System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
            System.out.print("linda>");
            Socket server = serverSocket.accept();
            System.out.println("Just connected to " + server.getRemoteSocketAddress());
            
            DataInputStream in = new DataInputStream(server.getInputStream());
            String str = in.readUTF();
            
            String matched = ""; // store the result match: If "", no match else "match" equals the matched tuple
            if(str.startsWith("add")) {// The command is "add"
                FileWriter fw = new FileWriter(netsPath);
        		BufferedWriter bw = new BufferedWriter(fw);
        		System.out.println("Hosts info server received is:" + str.substring(3));
        		bw.write(str.substring(3));
        		bw.write("\n");
        		bw.close();
            }
            
            if(str.startsWith("out")) { //The command is "out"
                FileWriter fw = new FileWriter(tuplesPath, true);
        		BufferedWriter bw = new BufferedWriter(fw);
        		System.out.println("The tuple to be stored on this machine is: [" + str.substring(3).trim() + "]");
        		bw.write(str.substring(3));
        		bw.write("\n");
        		bw.close();
        		fw.close();
            }
            
            if(str.startsWith("ine")) { //The command is "in exact match"
            	// Read the tuples to see whether there is a match
            	FileReader fr = new FileReader(tuplesPath);
		    	BufferedReader br = new BufferedReader(fr);
		    	String strTowrite = "";
		    	String tmp = null;
		    	boolean found = false;
		    	while((tmp = br.readLine()) != null) {
		    		if(!isEqual(tmp, str.substring(3)) || found) {
		    			strTowrite += tmp + "\n";
		    		}else{
		    			found = true;
		    			System.out.println("Tuple is found on this machine: " + hostName);
		    			matched = "Tuple is found on this machine: " + hostName;
		    		}
		    	}
		    	br.close();
		    	if(!found) {
		    		System.out.println("Can't be removed because tuple is not found on this machine: " + hostName);
		    		matched = "Tuple is not found on machine: " + hostName;
		    	}
                FileWriter fw = new FileWriter(tuplesPath);
        		BufferedWriter bw = new BufferedWriter(fw);
        		System.out.println("The remaining tuples are: \n" + strTowrite);
        		bw.write(strTowrite);
        		bw.close();
        		fw.close();
            }
            
            if(str.startsWith("rde")) { //The command is "read exact match"
            	// Read the tuples to see whether there is a match
            	FileReader fr = new FileReader(tuplesPath);
		    	BufferedReader br = new BufferedReader(fr);
		    	String tmp = null;
		    	boolean found = false;
		    	while((tmp = br.readLine()) != null) {
		    		if(isEqual(tmp, str.substring(3))) {
		    			found = true;
		    			System.out.println("Tuple is found on this machine: " + hostName);
		    			matched = "Tuple on this machine: " + hostName;
		    		}
		    	}
		    	br.close();
		    	if(!found) {
		    		System.out.println("Can't be read because tuple is not found on this machine: " + hostName);
		    		matched = "Tuple not on machine: " + hostName;
		    	}
            }
            
            if(str.startsWith("bro")) { //The command is "broadcast"
            	// Read the tuples to see whether there is a match
            	FileReader fr = new FileReader(tuplesPath);
		    	BufferedReader br = new BufferedReader(fr);
		    	String tmp = null;
		    	while((tmp = br.readLine()) != null) {
		    		if(isMatch(tmp, str.substring(3))) {
		    			matched = tmp;
		    			break;
		    		}
		    	}
		    	br.close();
		    	if(matched.equals("")) {
		    		System.out.println("Tuple is not found on this machine: " + hostName);
		    	}
            }
            
            DataOutputStream out = new DataOutputStream(server.getOutputStream());
            if(!str.startsWith("add") && !str.startsWith("out")) {
            	out.writeUTF(matched);
            }else{
            	out.writeUTF("Thank you for connecting to " + server.getLocalSocketAddress() + "\nGoodbye!");
            }
            server.close();
         }catch(IOException e){
            e.printStackTrace();
            break;
         }
         
      }
   }
   /*
    * Check whether two strings are the same by using hashing value
    */
   boolean isEqual(String str1, String str2) {
		try {
			MessageDigest md =  MessageDigest.getInstance("MD5");
			md.update(str1.getBytes());
			byte[] bArr= md.digest();
			BigInteger number1 = new BigInteger(1, bArr);
			
			md.update(str2.getBytes());
			bArr= md.digest();
			BigInteger number2 = new BigInteger(1, bArr);
			
			return number1.equals(number2);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return false;
	}
   
   /*
    * Check whether current string matches the pattern
    */
   boolean isMatch(String str1, String str2) {
		String[] arr1 = str1.split(" ");
		String[] arr2 = str2.split(" ");
		if(arr1.length != arr2.length) {
			return false;
		}
		for(int i = 0; i < arr1.length; ++i) {
			if(!arr2[i].contains("?")) {
				if(isInteger(arr2[i]) || isFloat(arr2[i])) {
					if(!arr1[i].equals(arr2[i])) {
						return false;
					}
				}else{
					if(!arr1[i].equals(arr2[i])) {
						return false;
					}
				}
			}else{
				if(arr2[i].contains("int")) {
					if(!isInteger(arr1[i])) {
						return false;
					}
				}else if(arr2[i].contains("float")) {
					if(!isFloat(arr1[i])) {
						return false;
					}
				}else{
					if(isInteger(arr1[i]) || isFloat(arr1[i])) {
						return false;
					}
				}
			}
		}
		return true;
	}
   
   /*
    * Called by isMatch to check whether a string is Integer
    */
   
  boolean isInteger(String data) {
	   try{
		   Integer.parseInt(data);
	   }catch(NumberFormatException e) {
		   return false;
	   }catch(NullPointerException e) {
		   return false;
	   }
	   return true;
  }
  /*
   * Called by isMatch to check whether a string is float
   */
  boolean isFloat(String data) {
	   return data.contains(".");
  }
}
