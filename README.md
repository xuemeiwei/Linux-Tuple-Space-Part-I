# Linux Tuple Space, Part I

This assignment is to implement the distributed model using java. In a distributed environment, Linda provides a conceptually "global" tuple space (TS) which remote processes can sccess the matched tuples in TS by atomix operations (in, rd, inp, rdp, out, eval). "out" will put tuple in specified tuple by hashing the tuple and get the corresponding host. Both "in" and "rd" operations have exact match and variable match. Exact match will go to the sepecified host and variable match will broadcast the message. Check all the hosts and only operate on one host. The difference between "in" and "rd" is that "in" will remove the tuple while "rd" will not.

## Tasks
1. Find avaiable port on current machine;
2. Add other hosts;
3. Implement the features of "in", "rd", and "out".

## How to run the program

## Main Structure

1. P1 is the main program which will call Server, Broadcast, Client.

2. Server.java is implemented using thread which is always running listening to the specified port;

3. Client.java is only called when there is a request from current host.

4. Broadcast.java is thread called by P1 to broadcast the messages from current host to all the hosts. Each request is implemented using a thread.

5. SharedInfo.java is the message shared by all the broadcast thread. When a tuple is found on one host, it will set the SharedInfo and all the hosts will know it's found and all the threads will complete. If no tuple is found, all the threads are running all the time and the main program is waiting until available tuple.

## Code Desciption
### 1. P1 contains:
1) Find available port on current machine;
2) Create files to store host info and tuples info;
3) Start the server;
4) Accept input from the keyboard and call responding cliend request;

### 2. Server contains:
1) Property: serverSocket, hostName, netsPath, tuplesPath
2) Method: run() including connects with client, receive message, do corresponding operations, send message to client

### 3. Client contains (only have methods):
1) add(): add request
2) out(): out request
3) ine(): exact in request
4) rde(): exact rd request

### 4. Broadcast:
1) Properties: sharedInfo, hostAddress, port, tuples, hostname;
2) Method: run() which sends request to server

### 5. SharedInfo contains:
1) Property: flag, hostAddress, port, tuples
2) Method: set()
