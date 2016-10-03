package classes;

import java.net.Inet4Address;
import java.net.UnknownHostException;

public class Test extends Thread {
   
   public static void main(String args[]) {
       try {
	System.out.println(Inet4Address.getLocalHost().getHostAddress());
    } catch (UnknownHostException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
    }
   }

}
