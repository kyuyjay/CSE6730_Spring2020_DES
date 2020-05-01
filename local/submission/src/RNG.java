package sim;

import java.util.*;

public class RNG {

    static int count = 0;
    int id;
    Random r;
    double a;
    double b;
    double c;
    double[] p_1 = new double[]{0,0,0.0484,0.0161,0.0323,0.0484,0.0323,0.0484,0.1452,0.1774,0.0968,0.3548,0,0,0,0,0,0,0,0,0};
    double[] p_2 = new double[]{0.1061,0,0,0,0,0,0,0,0,0,0,0,0.1970,0.0455,0.3030,0.0455,0.1515,0.0152,0.1061,0.0152,0.0152};
    double[] p_12 = new double[]{0.3700,0.1200,0.0100,0.0100,0.0100,0.0100,0.0100,0.0100,0.0900,0.0800,0.0100,0.0100,0.0100,0.0400,0.1500,0.0100,0.0100,0.0100,0.0100,0.0100,0.0100};
    double[] p_13 = new double[]{0,0.4900,0.0100,0.0100,0.0100,0.0100,0.0100,0.0100,0.0900,0.0800,0.0100,0.0100,0.0100,0.0400,0.1500,0.0100,0.0100,0.0100,0.0100,0.0100,0.0100};
        
    RNG() {
        id = count;
        count++;
        r = new Random();
        // r = new Random(id);
        // Parameters determined from performing MLE on empirical data
        a = -0.00000184178586;
        b = a + 7.6560728289;
        c = 0.0000001687984;
    }
    
    /**
     * Generates a random variate pulled from a triangular distribution.
     *
     * @return int between a and b with mode c
     */

    int nextTime() {
        double f = (c-a) / (b-a);
        double U = r.nextDouble();
        if (U < f) {
            return (int)Math.round((a + Math.sqrt(U * (b-a) * (c-a))) * 60);
        } else {
            return (int)Math.round((b - Math.sqrt((1-U) * (b-a) * (b-c))) * 60);
        }
    }

    int nextDest(int src) {
        double roll = r.nextDouble();
        int i = 0;
        double curr = 0;
        if (src > 2 && src < 13) {
            curr = p_12[i];
            while (roll > curr) {
                i++;
                curr += p_12[i];
            }
            return i;
        } else if (src >= 13) {
            curr = p_13[i];
            while (roll > curr) {
                i++;
                curr += p_13[i];
            }
            return i;
        } else if (src == 2) {
            curr = p_2[i];
            while (roll > curr) {
                i++;
                curr += p_2[i];
            }
            return i;
        } else {
            curr = p_1[i];
            while (roll > curr) {
                i++;
                curr += p_1[i];
            }
            return i;
        }
    }

}

