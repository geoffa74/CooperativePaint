package classes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

public class Server extends Thread {

    ServerSocket serverSocket;
    List<ThreadedConnection> connections;
    String dataFromClients;
    int numConnections;
    ConnectionHandler connectionHandler;
    private final int port = 60000;
    BufferedImage image;
    Graphics imageGraphics;
    int width = 800;
    int height = 800;

    public Server() throws IOException {
	image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	imageGraphics = image.getGraphics();
	dataFromClients = "";
	serverSocket = new ServerSocket(port);
	numConnections = 0;
	connections = new LinkedList<ThreadedConnection>();
	connectionHandler = new ConnectionHandler();
	connectionHandler.start();
    }
    
    private synchronized void addData(String data) {
	dataFromClients += data;
    }
    
    private synchronized String getData() {
	return dataFromClients;
    }
    
    public void updateImage(String data) {
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
	}
    }
    
    public void run() {

	while(!serverSocket.isClosed()) {
	    if(!getData().equals("")) {
		try {
		    List<ThreadedConnection> clientConnections = getConnections();
		    for(ThreadedConnection connection: clientConnections) {
			if(connection.isInitialized()) {
			    if(connection.getInitializeData().equals("")) {
				DataOutputStream output = new DataOutputStream(connection.getSocket().getOutputStream());
				output.writeBytes(getData() + "\n");
			    }else{
				DataOutputStream output = new DataOutputStream(connection.getSocket().getOutputStream());
				output.writeBytes(connection.getInitializeData());
				connection.clearInitializeData();
			    }
			} else {
			    connection.addInitializeData(getData());
			}
		    }
		    updateImage(getData());
		    clearData();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    }
	}
    }
    
    private synchronized void clearData() {
	dataFromClients = "";
    }

    private synchronized List<ThreadedConnection> getConnections() {
	return connections;
    }
    
    private synchronized void addConnection(Socket client) {
	ThreadedConnection connection = new ThreadedConnection(client);
	connections.add(connection);
    }
 
    class ConnectionHandler extends Thread {
	
	public void run() {
	    while(!serverSocket.isClosed()) {
		try {
		    Socket client = serverSocket.accept();
		    addConnection(client);
		} catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }
	}
    }
    
    class ImageUpdater extends Thread {
	
	public ImageUpdater() {
	    image = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
	    imageGraphics = image.getGraphics();
	}
    }
    
    class ThreadedConnection extends Thread {
	
	Socket client;
	String initializeData;
	boolean isInitialized;
	
	public ThreadedConnection(Socket client) {
	    this.client = client;
	    isInitialized = false;
	    initializeData = "";
	    initialize();
	    start();
	}
	
	private void initialize() {
	    try {
		ImageIO.write(image, "PNG", client.getOutputStream());
		setInitialized(true);
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	
	public boolean isInitialized() {
	    return isInitialized;
	}
	
	public void clearInitializeData() {
	    initializeData = "";
	}
	
	public String getInitializeData() {
	    return initializeData;
	}
	
	public void addInitializeData(String data) {
	    if(initializeData.equals("")) {
		initializeData += data;
	    }else{
		initializeData += " " + data;
	    }
	}
	
	public void setInitialized(boolean isInitialized) {
	    this.isInitialized = isInitialized;
	}
	
	
	public Socket getSocket() {
	    return client;
	}
	
	public void run() {
	    while(!client.isClosed()) {
		try {
		    DataInputStream input = new DataInputStream(client.getInputStream());
		    String dataFromClient = "";
		    byte data;
		    while((data = input.readByte()) != 10) {
			dataFromClient += (char) data;
		    }
		    if(getData().equals("")) {
			addData(dataFromClient);
		    }else{
			addData(" " + dataFromClient);
		    }
		} catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }
	}
	
    }
}


