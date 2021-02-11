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
public interface ProteinI {
    public double getTime();
    public double getDetachedTime();
    public abstract boolean update(double t);
    public void reset();
    public void severAlert(double t);
    
}
