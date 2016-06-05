package server;

import lib.RMI;
import lib.RoundAbout;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Bartek on 2016-06-02.
 */
public class RMIServer extends UnicastRemoteObject implements RMI {

    private final static Logger LOGGER = Logger.getGlobal();

    public RMIServer() throws RemoteException {
        super();
    }

//    remote wrapper for simulation(int simsize) method
    @Override
    public RoundAbout runSimulation(int simsize) throws RemoteException {
        LOGGER.info("Start simulation!");
        RoundAbout roundAbout = new RoundAbout();
        roundAbout.simulate(simsize);
        return roundAbout;
    }

    public static void main(String args[]){
//        check if it has been specified at least one port
        if (args.length < 1)
            System.out.println("Please specify ports!");

//        starting servers on specified ports
        for(String port : args) {
            try {
                    Registry reg = LocateRegistry.createRegistry(Integer.parseInt(port));
                    reg.rebind("server", new RMIServer());
                    LOGGER.info("Server started on port: " + port);
            }catch (ExportException e) {
                LOGGER.log(Level.SEVERE, e.getMessage());
            }catch (IllegalArgumentException e) {
                LOGGER.log(Level.SEVERE, e.getMessage());
            }catch (Exception e){
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }
}
