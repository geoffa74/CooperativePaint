package classes;

import java.io.IOException;
import java.net.Inet4Address;

import javax.swing.JOptionPane;

public class CooperativePaintApp {

    static Server server;
    static Client client;
    
    public static void main(String args[]) {
	try {
	    Object[] options = {"Host","Connect","Host/Connect"};
	    int choice = JOptionPane.showOptionDialog(null, "Choose", "Host/Connect", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
	    switch(choice) {
	    case 0:
		server = new Server();
		server.start();
		break;
	    case 1:
		String address = JOptionPane.showInputDialog(null, "Enter Address");
		client = new Client(address);
		client.setVisible(true);
		break;
	    case 2:
		server = new Server();
		server.start();
		client = new Client(Inet4Address.getLocalHost().getHostAddress());
		client.setVisible(true);
		break;
	    }
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
}
