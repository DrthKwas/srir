package lib;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Created by Bartek on 2016-06-02.
 */

public class RoundAbout implements Serializable
{
    private final static Logger LOGGER = Logger.getGlobal();

	/* Entrances parameters
	   f - Mean time between vehicle arrivals
	   d - matrix of probabilities that car entering at i will exit at j  */

    private int[] f = new int[DefineConstants.ESIZE];
    private double[][] d = new double[DefineConstants.ESIZE][DefineConstants.ESIZE];
    public static String[] DIRECTIONS = {"N", "W", "S", "E"};

    /* Data stuctures representing the traffic circle:
       circle - current state of traffic circle
       new-circle - next state of traffic circle  */
    private int[] circle = new int[DefineConstants.CSIZE];
    private int[] new_circle = new int[DefineConstants.CSIZE];

	/* Data structures representing the four entrances:
	   OFFSET - each entrance's location (index) in traffic circle
	   arrival - value 1 if car arrived in this time step
	   wait_cnt - number of cars that have had to wait
	   arrival_cnt - total number of cars that have arrived
	   queue - number of cars waiting to enter circle
	   queue_accum - accumulated queue size over all time steps */

    private static int[] OFFSET = {0,4,8,12};
    private int[] arrival = new int[DefineConstants.ESIZE];
    private int[] queue = new int[DefineConstants.ESIZE];
    public int[] wait_cnt = new int[DefineConstants.ESIZE];
    public int[] arrival_cnt = new int[DefineConstants.ESIZE];
    public int[] queue_accum = new int[DefineConstants.ESIZE];

    /*	function initialize_arrays - fills up arrays with starting values */
    public void initialize_arrays()
    {
        int i;
        for (i = 0;i < DefineConstants.CSIZE;++i)
        {
            circle[i] = new_circle[i] = -1;
        }

        for (i = 0;i < DefineConstants.ESIZE;++i)
        {
            arrival[i] = wait_cnt[i] = arrival_cnt[i] = queue[i] = queue_accum[i] = 0;
        }
    }

	/*	function load_default - in case of input file opening failure, initializes simulation parameters with default values  */

    public void load_default()
    {
					 /* N W S E */
        int[] f_default = {3,3,4,2};
						  /* N    W    S    E */
        double[][] d_default =
                {
                        {0.1, 0.2, 0.5, 0.2},
                        {0.2, 0.1, 0.3, 0.4},
                        {0.5, 0.1, 0.1, 0.3},
                        {0.3, 0.4, 0.2, 0.1}
                }; //N
        f = f_default.clone();
        d = d_default.clone();
    }


	/*	function load_data - reads following data from file: type of generator, array f, matrix d  */

    public void load_data()
    {
        String [] fileNames = {"input_f.dat", "input_d.dat"};
        try {
            Scanner scanner = new Scanner(new File(fileNames[0]));
            int i = 0;
            while (scanner.hasNextInt()) {
                f[i++] = scanner.nextInt();
            }

            Stream<String> stream = Files.lines(Paths.get(fileNames[1]));
            d = stream
                    .map((l) -> l.trim().split("\\s+"))
                    .map((sa) -> Stream.of(sa)
                        .mapToDouble(Double::parseDouble)
                        .toArray())
                    .toArray(double[][]::new);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot open file for reading, initializing with default values");
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            load_default();
        }
    }

	/*	function choose_exit - determines on which of the four entrances (N/S/W/E) car will leave the traffic circle
		@arg stream - pointer to the sprng generator initialized in main function
		@arg index - integer value of the index, representing the entrance that the car used to enter the traffic circle
		@return - integer value of the index, representing the entrance that the car will use to leave the traffic circle  */

    enum ExitRoute
    {
        N,
        W,
        S,
        E;

        public int getValue()
        {
            return this.ordinal();
        }
    }

    int choose_exit(int index){
        ExitRoute er;
        Random random = new Random();
        double u =  random.nextDouble();
        if (u < d[index][0]){
            er = ExitRoute.N;
        } else if (u < (d[index][0] + d[index][1])) {
            er = ExitRoute.W;
        } else if (u < (d[index][0] + d[index][1] + d[index][2])) {
            er = ExitRoute.S;
        } else {
            er = ExitRoute.E;
        }
        return OFFSET[er.getValue()];
    }


    /*
        Main program function, performs the simulation process with 1 argument:
            int simsize - number of simulation
    */
    public void simulate(int simsize)
    {
        int i;
        int j;
        int steps;
        double u;

        initialize_arrays();
        load_data();

        Random random = new Random();

        steps = 0;
		/* Main simulation loop */
        while (steps++ < simsize)
        {
			/* 1 step - new cars arrive at entrances */
            for (i = 0;i < DefineConstants.ESIZE;++i)
            {
                u = random.nextDouble();
                if (u <= 1.0 / (double)f[i])
                {
                    arrival[i] = 1;
                    arrival_cnt[i]++;
                }
                else
                {
                    arrival[i] = 0;
                }
            }

			/* 2 step - cars inside circle advance simulataneously */
            for (i = 0;i < DefineConstants.CSIZE;++i)
            {
                j = (i + 1) % 16;
                if (circle[i] == -1 || circle[i] == j)
                {
                    new_circle[j] = -1;
                }
                else
                {
                    new_circle[j] = circle[i];
                }
            }

			/* Replace content of circle with new_circle */
            for (i = 0;i < DefineConstants.CSIZE;++i)
            {
                circle[i] = new_circle[i];
            }

			/*3 step - cars enter circle */
            for (i = 0;i < DefineConstants.ESIZE;++i)
            {
                if (circle[OFFSET[i]] == -1)
                { // There is a space for a car to enter
                    if (queue[i] > 0)
                    { // Car waiting in the queue enters the circle
                        queue[i]--;
                        circle[OFFSET[i]] = choose_exit(i);
                    }
                    else if (arrival[i] > 0)
                    { // Newly arrived car enters the circle
                        arrival[i] = 0;
                        circle[OFFSET[i]] = choose_exit(i);
                    }
                }

                if (arrival[i] > 0)
                { // Newlu arrived car queues up
                    wait_cnt[i]++;
                    queue[i]++;
                }
            }

            for (i = 0;i < DefineConstants.ESIZE;++i)
            {
                queue_accum[i] += queue[i];
            }

        } // End of main loop
    }
}