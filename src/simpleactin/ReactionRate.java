/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simpleactin;

/**
 *
 * @author sm2983
 */
public class ReactionRate {

    public double onRates[], reactRate, offRate;

    public ReactionRate(double reactRate, double offRate, double... onR) {
        this.onRates = new double[onR.length];

        for (int i = 0; i < onR.length; i++) {
            this.onRates[i] = onR[i];
        }
        this.offRate = offRate;
        this.reactRate = reactRate;
    }

    @Override
    public String toString() {
        String ret="(";
        for (double d : onRates) {
            ret+= d + "\t";
        }
        return ret+")"+String.format("%f\t%f", offRate, reactRate);
    }
}
