package classes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Client extends JFrame {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    Socket socket;
    Display display;
    ClientReader clientReader;
    ClientWriter clientWriter;
    BufferedImage image;
    Graphics imageGraphics;
    String dataToServer;
    private final int port = 60000;
    private final int width = 800;
    private final int height = 800;
    
    public Client(String address) throws UnknownHostException, IOException {
	dataToServer = "";
	socket = new Socket(address,port);
	initialize();
	clientReader = new ClientReader();
	clientReader.start();
	clientWriter = new ClientWriter();
	clientWriter.start();
	//implement image read
	
	
	setTitle("CooperativePaint");
	setSize(width, height);
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	setLayout(new BorderLayout());
	setResizable(false);

	display = new Display();
	add(display, BorderLayout.CENTER);
    }
    
    private void initialize() {
	try {
	    System.out.println("Starting Initialization");
	    image = ImageIO.read(socket.getInputStream());
	    System.out.println("Finished Initializing");
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
    
    public void updateDisplay(String data) {
	display.updateDisplay(data);
    }
    
    public synchronized String getDataToServer() {
	return dataToServer;
    }
    
    public void clearDataToServer() {
	dataToServer = "";
    }
    
    public synchronized void addToDataToServer(String data) {
	dataToServer += data;
    }
    
    private class Display extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int mouseX;
	int mouseY;
	int pmouseX;
	int pmouseY;
	
	public Display() {
	    
	    mouseX = 0;
	    mouseY = 0;
	    imageGraphics = image.getGraphics();
	    addMouseMotionListener(new MouseMotionListener() {
		
		@Override
		public void mouseDragged(MouseEvent e0) {
		    pmouseX = mouseX;
		    pmouseY = mouseY;
		    mouseX = e0.getX();
		    mouseY = e0.getY();
		    if(getDataToServer().equals("")) {
			addToDataToServer("l " + pmouseX + " " + pmouseY + " " + mouseX + " " + mouseY);
		    }else{
			addToDataToServer(" l " + pmouseX + " " + pmouseY + " " + mouseX + " " + mouseY);
		    }
		}

		@Override
		public void mouseMoved(MouseEvent e1) {
		    pmouseX = mouseX;
		    pmouseY = mouseY;
		    mouseX = e1.getX();
		    mouseY = e1.getY();
		}
		
	    });
	}
	
	public void paint(Graphics g) {
	    g.drawImage(image, 0, 0, null);
	}
	
	public void updateDisplay(String data) {
	    String[] commands = data.split(" ");
	    for(int i = 0; i < commands.length; i += 5) {
		imageGraphics.setColor(Color.BLACK);
		switch(commands[i]) {
		case "l":
		    imageGraphics.drawLine(Integer.parseInt(commands[i+1]),
				Integer.parseInt(commands[i+2]),
				Integer.parseInt(commands[i+3]), 
				Integer.parseInt(commands[i+4]));
		    break;
		case "p":
		    image.setRGB(Integer.parseInt(commands[i+1]), 
			    Integer.parseInt(commands[i+2]),
			    Integer.parseInt(commands[i+3]));    
		}
		Client.this.repaint();
	    }
	}
    }
    
    private class ClientWriter extends Thread {
	
	public void run() {
	    while(!socket.isClosed()) {
		if(!getDataToServer().equals("")) {
		    try {
			DataOutputStream output = new DataOutputStream(socket.getOutputStream());
			output.writeBytes(getDataToServer() + "\n");
			clearDataToServer();
		    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		}
	    }
	}
	
    }
    
    private class ClientReader extends Thread {
		
	public void run() {
	    while(!socket.isClosed()) {
		try {
		    DataInputStream input = new DataInputStream(socket.getInputStream());
		    String dataFromServer = "";
		    byte data;
		    while((data = input.readByte()) != 10) {
       		    	dataFromServer += (char) data;
		    }
		    if(!dataFromServer.equals("")) updateDisplay(dataFromServer);
		} catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }
	}
    }
}
