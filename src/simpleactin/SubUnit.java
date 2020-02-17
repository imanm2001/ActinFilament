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
    private LinkedList<SubUnitListener> _listerns=new LinkedList<>();

    public SubUnit(int state, boolean barbed, double t,SubUnitListener list) {
        _state = state;
        _barbed = barbed;
        _t = t;
        _listerns.add(list);
    }
public void addListener(SubUnitListener sl){
    _listerns.add(sl);
}
    public void remove(double t) {

        if (_t > 000 && t - _t >= 0.0) {
            double dt=Math.ceil((t-_t)*10-Math.random())/10;
            for(SubUnitListener sl:_listerns){
                sl.remove(dt);
            }
            /*
            MainJFrame.lifeTimes.add(dt);
            _ps.println(t - _t);
            _ps.flush();*/
  
        }
    }
}
