import java.net.*;
import java.io.*;
import java.util.*;

class Proxy extends Thread
{
    ServerSocket serverSocket;
    static String webServer = "128.32.39.121";
    static int webPort = 8000;

    static int  DEFAULT_PORT = webPort; //proxy port
    static int	TO_BROWSER = 0;
    static int	TO_SERVER = 1;
    static String filename = "out.txt";
    private PrintWriter outputFileWriter;

    // The following are a list of server message strings and headers sent and
    // received by the server.  Some of the messages sent indicates errors and reports.
    // Some of the message received indicates server requests. Lots of them are short
    // for speed considerations

    // Starts the server on the port specified by the argument.
    // if it doesn't exist use the Default port
    public static void main(String args[]) throws Exception {
        Proxy server;
        int port = DEFAULT_PORT;

	if( args.length != 4 ) {
	    System.out.println( "Usage: java Proxy port serverhost serverport outputfile" );
	    System.out.println( "\tProxy will listen to http requests coming to 'port', record them, and forward the http requests to serverhost and serverport" );
	    return;
	}

        try {
	    port = Integer.parseInt(args[0]);
	    webServer= args[1];
            webPort= Integer.parseInt(args[2]);
            filename = args[3];

        } catch (Exception e) {
            System.err.println("No port specified or cannot understand argument.");
            System.err.println("Using default port of " + DEFAULT_PORT);
        }

        System.out.println("Starting server on port " + port);

        // try to start the server
        try {
            server = new Proxy(port);
        } catch (Exception e) {
            System.err.println("Error starting server.  Exiting.");
            return;
        }


        System.out.println("Server listening on port " + port);
	System.out.println("Writing output to " + filename );
        server.start();
    }

    public Proxy(int port) throws Exception {
       outputFileWriter = new PrintWriter(new FileWriter(filename, true));
       serverSocket = new ServerSocket(port);
    }

    public void run() {
        try {
            // wait forever for connections
            while (true) {
                Socket clientSocket = serverSocket.accept(); //wait for client to connect, then return the socet back to the client
                // Received a connection, start a new thread (Connection) to
                // communicate with it
                System.err.println("Received a connection from " + clientSocket.getInetAddress());
                Connection toBrowser = new Connection(clientSocket, TO_BROWSER);

		// open a connection to the webserver and start forwarding
                Socket webClientSocket = new Socket(webServer, webPort);

                Connection toServer = new Connection(webClientSocket, TO_SERVER);

                toBrowser.forwardOut = toServer.socketOut;
                toBrowser.forwardIn = toServer.socketIn;

                toServer.forwardOut = toBrowser.socketOut;
		            toServer.forwardIn = toBrowser.socketIn;

                toServer.start();
	            	toBrowser.start();

                System.err.println("Both connections started OK.");
            }
        } catch (Exception e) {
            System.err.println("Server error while listening for connections");
            System.err.println(e);
        }
    }

    // subclass Connection is a thread that handles the I/O
    // between the server and the client applet.
    // it also runs the spider on behalf of the client
    class Connection extends Thread {
        Socket              clientSocket;
        public PrintWriter         socketOut;
        public BufferedReader      socketIn;

        public PrintWriter         forwardOut;
        public BufferedReader      forwardIn;

  	int connectionType;

        // Constructor.  Sets up the I/O streams
        public Connection(Socket clientSocket, int type) {
	   this.connectionType = type;
            try {
                this.clientSocket = clientSocket;

                // obtain the input streams
                OutputStream out = clientSocket.getOutputStream();
                InputStream in = clientSocket.getInputStream();

                // Construct text input/output streams
                socketOut = new PrintWriter(new BufferedOutputStream(out));
                socketIn = new BufferedReader(new InputStreamReader(in));


            } catch (Exception e) {
                System.err.println("Error occured when receving request from client");
                System.err.println(e);
                e.printStackTrace(System.err);

                // inform the client that some unforseen error has
                // occurred
		//clientSocket.close();
            }
        }

        public void run() {
	    int state = 0;
            try {
	        while (true) {
	    	    String in = socketIn.readLine();
                    if (in==null) break;
 		    if (connectionType == TO_BROWSER) {

          outputFileWriter.println(in);
          outputFileWriter.flush();
       	}

		    forwardOut.println(in);
		    forwardOut.flush();

  		}
		clientSocket.close();
    outputFileWriter.println();
    outputFileWriter.flush();
		System.err.println("Closed socket " + connectionType);
		if (connectionType == TO_SERVER) {state = 0;}
            } catch (Exception e) {
            }

        }

    }

}


