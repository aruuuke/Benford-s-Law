package benfordslaw;
import java.io.*;
import java.net.*;
import java.util.*;
import benfordslaw.AppliedBenfordsLaw;

/*
    this class manages server operations on port 5000 
    the server continuously listens for client connections
    accepts new connections
    starts a thread to handle communication with each connected client
*/
public class BenfordServer {
    public BenfordServer() {
        try (ServerSocket serverSocket = new ServerSocket(5000);) {
            while (true) {
                try {
                    Socket client = serverSocket.accept();
                    new TheClientHandler(client).start();
                } 
                catch (IOException e) {System.out.println("can't establish a server-client connection: " + e.getLocalizedMessage());}
            }
        } 
        catch (IOException e) {System.out.println("can't create a server socket: " + e.getLocalizedMessage());}
    }
}



/*
    this class handles communication with a client in a thread
    allows the server to provide benford's law analysis to multiple connected clients at the same time
*/
class TheClientHandler extends Thread {
    private Socket client;
    private boolean cond = true;
    private List<Integer> numbers = new ArrayList<>();
    private Map<Integer, Double> result_map = new HashMap<>();
    private AppliedBenfordsLaw benfords;

    
    public TheClientHandler(Socket client) {
        this.client = client;
        benfords = new AppliedBenfordsLaw(); // creates an AppliedBenfordsLaw obj used for benfords analysis
    }
    
    
    // manages reading from and writing to the client, handling data and commands.
    @Override
    public synchronized void run() {
        try (ObjectInputStream oin = new ObjectInputStream(client.getInputStream()); // read an obj from client
             ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());) // send an obj to client
        { 
            while (cond) { // continuously process incoming data
                Object obj = oin.readObject(); // read data from client

                switch (obj) { 
                    case File f -> {
                        ReadFile(f); // process file data
                        SendBenfordsData(out); // send analysis results back to client
                    }
                    
                    case String s -> { 
                        if (s.equals("exit".toLowerCase())) {
                            cond = false; // stop if exit command is received.
                        }
                    }
                    
                    default -> {}
                }
            }
        } 
        catch (IOException | ClassNotFoundException e) {System.out.println("can't read create I/O streams to talk to client: " + e.getMessage());}
    }

    
    // reads numeric data from a file and stores it in a list
    private synchronized void ReadFile(File file) {
        //System.out.println("server side file:" + file.getName());
        try (Scanner fin = new Scanner(file)) {
            while (fin.hasNext()) {
                if (fin.hasNextInt()) {
                    int num = fin.nextInt();
                    numbers.add(num); // store int 
                    //System.out.println("server read & stored: " + num);
                }
                else if (fin.hasNextDouble()) {
                    int num = (int) fin.nextDouble();
                    numbers.add(num); // store double
                    //System.out.println("server read & stored: " + num);
                }
                
            }
        } 
        catch (IOException e) {System.out.println("can't read the file: " + e.getMessage());}
    }


    // processes the numeric data using benford's law and sends the results to the client
    private synchronized void SendBenfordsData(ObjectOutputStream out) {
        try {
            if (!numbers.isEmpty()) {
                result_map = benfords.applyBenford(numbers); // perform analysis
               // System.out.println("percent map: " + result_map);
                out.reset();  // reset to avoid caching issues
                out.writeObject(result_map); // send the resilts to the client
                out.flush(); // make sure the results are sent immediately
                boolean fraud = benfords.compareWithBenford(); // determine if data is fraudulent
                out.reset(); 
                out.writeObject("fraudulent: " + fraud); // send fraud status to the client
                out.flush();
            }
        } 
        catch (IOException e) {System.out.println("error in client handling thread: " + e.getMessage());} 
        finally {
            numbers.clear(); // clear data for next file
            benfords.Clear(); // reset analysis tool
        }
    }
}
