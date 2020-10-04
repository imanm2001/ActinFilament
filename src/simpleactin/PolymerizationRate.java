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
public class PolymerizationRate {
    
   public double ATPon,ATPoff,ADPon,ADPoff;

    public PolymerizationRate(double ATPon, double ATPoff, double ADPon, double ADPoff) {
        this.ATPon = ATPon;
        this.ATPoff = ATPoff;
        this.ADPon = ADPon;
        this.ADPoff = ADPoff;
        
    }
   @Override
   public String toString(){
       return String.format("%f\t%f\t%f\t%f", ATPon,ATPoff,ADPon,ADPoff);
   }
}
