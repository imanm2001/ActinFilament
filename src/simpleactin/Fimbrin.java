/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simpleactin;

import java.util.Random;

/**
 *
 * @author sm2983
 */
public class Fimbrin {

    public boolean end1, end2;
    public double tension, tensionChanges, thermalFluctions;
    public double k1, k2, kon, maxRate;
    private static Random _rand = new Random();
    private double t=-1;

    public boolean update() {
        if (end1 && end2) {
            double factor = 1 - Math.exp(-(tension + 1 + _rand.nextGaussian() * thermalFluctions));
            double k1p = factor * k1;

            if (_rand.nextDouble() < k1p) {
                end1 = false;
            } 
            if (_rand.nextDouble() < k1p) {
                end2 = false;
            }
        } else {
            boolean detach=_rand.nextDouble()<k2,attach=_rand.nextDouble()<kon;
            if(end1){
                end1=!detach;
                end2=attach;
            }else{
                end2=!detach;
                end1=attach;
            }
            
        }
        return end1 | end2;
    }
}
