package lib;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by Bartek on 2016-06-02.
 */
public interface RMI extends Remote{

//    declaration of remote method
    RoundAbout runSimulation(int simsize) throws RemoteException ;
}
