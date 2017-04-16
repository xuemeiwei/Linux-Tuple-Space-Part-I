
/*
 * This class stores the info of matching hostAddress, port and tuple.
 * It will be set once tuple is found.
 * To avoid conflicts since all the hosts can set it, its set() is implemented by using synchronized.
 * Thus only one host can set it at one time.
 */
public class SharedInfo {
	boolean flag = false; // mark whether one of the hosts has the tuple;
	String hostAddress = null;//which host is chosen to remove the tuple;
	String port = null;//corresponding port;
	String tuples = "";
	public synchronized void set(String hostAddress, String port, String tuples) {
		flag = true;
		this.hostAddress = hostAddress;
		this.port = port;
		this.tuples = tuples;
	}
}
