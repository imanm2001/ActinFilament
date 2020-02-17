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

    public double onRates[], reactRate,offRate;

    public ReactionRate(double reactRate, double offRate,double... onRates) {
        onRates = new double[onRates.length ];
        for (int i = 0; i < onRates.length; i++) {
            onRates[i] = onRates[i];
        }
        offRate = offRate;
        reactRate=reactRate;
    }
}
