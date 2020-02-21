/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simpleactin;

import java.io.PrintStream;
import java.util.LinkedList;

/**
 *
 * @author sm2983
 */
public class SubUnit {

    public int _id, _state;
    public boolean _barbed;
    public double _t;
    private LinkedList<SubUnitListener> _listerns = new LinkedList<>();
    boolean _record = false;

    public SubUnit(int state, boolean barbed, double t, SubUnitListener list) {
        _state = state;
        _barbed = barbed;
        _t = t;
        if (list != null) {
            _listerns.add(list);
        }
        _record = Math.random() < 0.1;
    }

    public void addListener(SubUnitListener sl) {
        _listerns.add(sl);
    }

    public void removeListener(SubUnitListener sl) {
        _listerns.remove(sl);
    }

    public void remove(double t) {

        if (_t > 000) {
            double dt = Math.ceil((t - _t) * 10 + Math.random() - 1) / 10;
            if (dt >= 0.3) {
                for (SubUnitListener sl : _listerns) {
                    sl.remove(dt, this);
                }
            }
            /*
            MainJFrame.lifeTimes.add(dt);
            _ps.println(t - _t);
            _ps.flush();*/

        }
    }
}
