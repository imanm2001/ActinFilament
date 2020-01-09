/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simpleactin;

import java.io.PrintStream;

/**
 *
 * @author sm2983
 */
public class SubUnit {

    public int _id, _state;
    public boolean _barbed;
    public double _t;
    public PrintStream _ps = null;

    public SubUnit(int state, boolean barbed, double t, PrintStream ps) {
        _state = state;
        _barbed = barbed;
        _t = t;
        _ps = ps;
    }

    public void remove(double t) {
        
        if (_t > 800&&t-_t>=0.0) {
            _ps.println(t - _t);
            _ps.flush();
        }
    }
}
