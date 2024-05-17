package benfordslaw;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import benfordslaw.AppliedBenfordsLaw;

// this class creates a jpanel that draws a bar chart based on frequency data
class BenfordChart extends JPanel {
    private HashMap<Integer, Double> map; // stores frequency data for the chart

    public BenfordChart() {
        this.setPreferredSize(new Dimension(600, 400)); 
        setVisible(true);
    }

    public void UpdateChart(HashMap<Integer, Double> new_map) {
        map = new_map; // update the chart with new frequency
        repaint(); // redraw on update
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (map != null) { // make sure there's data to draw
            drawBarChart(g); // draw the bar chart
        }
    }

    // draws individual bars for each digit's frequency
    private void drawBarChart(Graphics g) {
        int x = 30; // initial x-coord for bars
        for (Integer digit : map.keySet()) {
            double freq = map.get(digit);
            int y = (int) (getHeight() * (freq / 100)); // find height of the curr bar using the digit's freq
            g.setColor(Color.GRAY);
            g.fillRect(x, getHeight() - y, 20, y); // start_y = panel_height - bar_height, end_y = panel_height = start_y + bar_height 
            g.setColor(Color.BLACK);
            g.drawString(digit.toString() + "=" + String.format("%.0f", freq) + "%", x, 400);
            x += 65; // increment x-coord for the next bar, creates space btw the bars
        }
    }
}

// this class manages client gui for interaction with the benford server
class ClientGUI extends JFrame implements ActionListener {
    private Client client; // a client obj for server communication
    private HashMap<Integer, Double> freqs_map = new HashMap(); // stores analysis results
    private File file; // pass a file to the client obj's method so that it can send it to the server
    private boolean cond = true; // use to continuosly listen for incoming data from server
    private String text = "";
    private JButton load_button;
    private JButton send_button;
    private JTextArea results_text_area;
    private JPanel top_panel;
    private JPanel center_panel;
    private JPanel bottom_panel;
    private BenfordChart chart_panel;
    
    
    public ClientGUI() {
        super("Benford's Law");
        SetFrame(); 
        SetClient();
        setVisible(true);
    }

    
    // initializes a client obj & starts a thread to listen for incoming server data
    private void SetClient() {
        this.client = new Client();

        new Thread() {
            @Override
            public void run() {
                GetAndDisplayResults();
            }
        }.start();
    }
    
    
    // continuously retrieves & displays results from the server
    private void GetAndDisplayResults() {
        while (cond) {
            try {
                Object obj = client.ReadObject(); // read an obj sent from the server
                //System.out.println("read obj:" + obj);
                switch (obj) {
                    case HashMap map ->
                        freqs_map = map; // stores a hashmap w results
                    case String str -> 
                        text = str; // stores a fraud msg
                    default -> { // if neither of those, empty both. 
                        freqs_map = null;
                        text = "";
                    }
                }
                SwingUtilities.invokeLater(new Runnable() { // using SwingUtilities for the gui updates thread 
                    @Override
                    public void run() {
                        chart_panel.UpdateChart(freqs_map); // display the chart w results
                        results_text_area.setText(file.getName() + " is "+ text); // display the fraud msg
                    }
                });
            } 
            catch (IOException | ClassNotFoundException e) {
                System.out.println("can't read from server: " + e.getMessage());
                break;  // exit the loop on error
            }
        }
    }

    
    // set up the frame's properties & layouts
    private void SetFrame() {
        setTitle("Benford's Law Client");
        setSize(600, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        SetUpPanels();
        add(top_panel, BorderLayout.NORTH);
        add(center_panel, BorderLayout.CENTER);
        add(bottom_panel, BorderLayout.SOUTH);

        // notifies the server of the client exit
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cond = false; // stop the server listening thread
                client.NotifyServerOfExit(); // tell the client to close its socket & I/O streams
                System.exit(0);  // make sure we exit after notifications
            }
        });
    }
    
    
    // returns a file that a user opened or null if something went wrong
    private File FileSelect() {
        JFileChooser file_chooser = new JFileChooser();
        file_chooser.setCurrentDirectory(new File(System.getProperty("user.home"))); // start dir
        file_chooser.setFileSelectionMode(JFileChooser.FILES_ONLY); // upload only files
        file_chooser.setAcceptAllFileFilterUsed(false); // only 1 file at a time
        int action = file_chooser.showOpenDialog(this);
        return (action == JFileChooser.APPROVE_OPTION) ? file_chooser.getSelectedFile() : null; 
    }

    
    // implement action events for load & send buttons
    @Override
    public void actionPerformed(ActionEvent e) {
        if ((JButton) e.getSource() == load_button) {
            //System.out.println("got file:" + file.getName());
            file = FileSelect(); // assign to the file a user opened 
        } 
        else if ((JButton) e.getSource() == send_button) {
            if (file != null) {
                //System.out.println("sending file:" + file.getName());
                client.SendFileDataToServer(file); // send file to the server
            }
        }
    }

    
    // set up every panel's properties & layouts
    private void SetUpPanels() {
        top_panel = new JPanel();
        top_panel.setLayout(new GridLayout(1, 2));
        load_button = new JButton("Upload a file");
        send_button = new JButton("Send to analyze");
        // add this class's action listener to the load file button to get a file from user
        load_button.addActionListener(this);
        // add this class's action listener to the send button to send a file to the server
        send_button.addActionListener(this);

        // buttons at the top panel
        top_panel.add(load_button);
        top_panel.add(send_button);

        // benford bar chart at the center panel
        center_panel = new JPanel();
        chart_panel = new BenfordChart();
        center_panel.add(chart_panel);
        
        // benford fraud message at the bottom panel
        results_text_area = new JTextArea();
        bottom_panel = new JPanel();
        bottom_panel.add(results_text_area);
    }
}


// this class is used to handle client side operations
class Client {
    private Socket client_socket; 
    private Scanner fin;
    private ObjectInputStream obin;
    private ObjectOutputStream obout;
    
    public Client() {
        try {
            client_socket = new Socket("localhost", 5000); // port 5000, localhost (my machine)
            obout = new ObjectOutputStream(client_socket.getOutputStream()); // sends an obj to the server
            obin = new ObjectInputStream(client_socket.getInputStream()); // reads an obj from the server
        } 
        catch (IOException e) {System.out.println("Failed to set up client socket: " + e.getMessage());}
    }
    

    // reads from the server & return it 
    protected Object ReadObject() throws IOException, ClassNotFoundException {
        Object obj = obin.readObject();
        return obj;
    }
    

    // sends a file to the server
    protected void SendFileDataToServer(File file) {
        try {
            if (file != null) {
                obout.reset(); 
                obout.writeObject(file);
                obout.flush();
            }
        } 
        catch (IOException e) {System.out.println("Error sending data to server: " + e.getMessage());}
    }
    
    
    // closes client's socket & I/O streams
    private final void Close() {
        try {
            client_socket.close();
            obout.close();
            obin.close();
        } 
        catch (IOException e) {System.out.println("can't close client stuff: " + e.getMessage());}
    }
    
    
    // tell the server im exiting & close the resoruces
    protected void NotifyServerOfExit() {
        try {
            obout.writeChars("exit");
            obout.flush();
            Close();
        } 
        catch (IOException e) {System.out.println("error w sending exit to server &| closing" + e.getLocalizedMessage());}
    }
}




// a main method allows us to run this file's program when the server is running
public class BenfordClient {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ClientGUI();
            }
        });
    }
}
