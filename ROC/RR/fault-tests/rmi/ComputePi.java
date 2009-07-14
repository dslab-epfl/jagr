//package client;

import java.rmi.*;
import java.math.*;
//import compute.*;

public class ComputePi {
    public static void main(String args[]) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }
        try {
            String name = "//" + args[0] + "/Compute";
            Compute comp = (Compute) Naming.lookup(name);
            Pi task = new Pi(Integer.parseInt(args[1]));
            BigDecimal pi = (BigDecimal) (comp.executeTask(task));
            System.out.println(pi);
        } catch (Exception e) {
	    System.err.println("ComputePi exception: " + e);
	    System.err.println("ComputePi exception class: " + e.getClass().getName());
            System.err.println("ComputePi exception msg: " + e.getMessage());
            e.printStackTrace();
        }
    }    
}
