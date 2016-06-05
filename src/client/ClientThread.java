package client;

import lib.RMI;
import lib.RoundAbout;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Bartek on 2016-06-03.
 */
public class ClientThread extends Thread {

    private final static Logger LOGGER = Logger.getGlobal();
    private RoundAbout roundAbout;
    private int port;
    private int simsize;

    public ClientThread(int simsize, int port) {
        this.roundAbout = new RoundAbout();
        this.simsize = simsize;
        this.port = port;
    }

    public RoundAbout getRoundAbout() {
        return roundAbout;
    }

    public void run(){
        try {
            Registry reg = LocateRegistry.getRegistry("127.0.0.1", port);
            RMI rmi = (RMI) reg.lookup("server");
            LOGGER.log(Level.INFO, "Connected to server on port: " + port);

            roundAbout = rmi.runSimulation(simsize);

            LOGGER.info("On port: " + port + " number of cars arrived (N)= " + roundAbout.arrival_cnt[0]);
            LOGGER.info("On port: " + port + " number of cars arrived (W)= " + roundAbout.arrival_cnt[1]);
            LOGGER.info("On port: " + port + " number of cars arrived (S)= " + roundAbout.arrival_cnt[2]);
            LOGGER.info("On port: " + port + " number of cars arrived (E)= " + roundAbout.arrival_cnt[3]);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage() + "to port: " + port);
            roundAbout = null;
        }
    }
}
