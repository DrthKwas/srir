package client;

import lib.DefineConstants;
import lib.RoundAbout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Bartek on 2016-06-02.
 */
public class RMIClient {

    private final static Logger LOGGER = Logger.getGlobal();

    public static void main(String args[]){
//        check if it has been specified at least one port
        if(args.length < 1){
            System.out.println("Please specify ports!");
            return;
        }

        int simsize = 100000;

//        Create RMIClient
//        Connect to servers on specified ports
//        Get list of results from servers
        RMIClient client = new RMIClient();
        List<RoundAbout> roundAboutList = client.connectServer(simsize, Arrays.stream(args).mapToInt(Integer::parseInt).toArray());

//        Accumulate results from all servers
        RoundAbout roundAbout = accumulateResults(roundAboutList);
        int numberOfServers = roundAboutList.size();

//        Print accumulated results
        printResults(simsize, roundAbout, numberOfServers);
    }

    private List<RoundAbout> connectServer(int simsize, int [] ports){

//        Prepere lists for Threads and Results
        List<ClientThread> threadList = new ArrayList<>();
        List<RoundAbout> roundAboutList = new ArrayList<>();

//        Run threads on different ports
        for (Integer port : ports) {
            threadList.add(new ClientThread(simsize, port));
            threadList.get(threadList.size()-1).start();
        }
//        Waiting until all threads finish processing
        threadList.parallelStream().forEach(t -> {
            try {
                t.join();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        });

//        Add all results to list
        roundAboutList.addAll(threadList.parallelStream().map(ClientThread::getRoundAbout).collect(Collectors.toList()));

        return roundAboutList;
    }

    private static void printResults(int simsize, RoundAbout roundAbout, int numberOfServer) {

        double probability;
        double queue_avg_length;
        System.out.printf("Algorithm results [executed on %d servers]: \n", numberOfServer);
        System.out.print("----------------------------------------------------- \n");
        for (int i = 0; i < DefineConstants.ESIZE; ++i) {
            probability = 100.0 * (double) roundAbout.wait_cnt[i] / (double) roundAbout.arrival_cnt[i];
            queue_avg_length = (double) roundAbout.queue_accum[i] / (simsize);
            System.out.printf("For entrance: %s \n", RoundAbout.DIRECTIONS[i]);
            System.out.printf("Total number of arrived cars: %d \n", roundAbout.arrival_cnt[i]);
            System.out.printf("Total number of cars that had to wait: %d \n", roundAbout.wait_cnt[i]);
            System.out.printf("Probability of waiting: %.2f %%\n", probability);
            System.out.printf("Average queue length: %f \n", queue_avg_length);
            System.out.print("----------------------------------------------------- \n");
        }
    }

    private static RoundAbout accumulateResults(List<RoundAbout> roundAboutList) {
//        Accumulate results from all servers
        RoundAbout roundAbout = new RoundAbout();
        for (int i = 0; i < roundAboutList.size(); ++i) {
            for(int j = 0; j < DefineConstants.ESIZE; ++j) {
                roundAbout.arrival_cnt[j] += roundAboutList.get(i).arrival_cnt[j];
                roundAbout.wait_cnt[j] += roundAboutList.get(i).wait_cnt[j];
                roundAbout.queue_accum[j] += roundAboutList.get(i).queue_accum[j] / roundAboutList.size();
            }
        }
        return roundAbout;
    }
}
