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
    public double _t, _connectedTime = -1;
    public int _decoratedOffrateIndex=-1;
            ;
    private LinkedList<SubUnitListener> _listerns = new LinkedList<>();
    boolean _record = false;
    int _connectionStatus = 0;
    public boolean _decorated=false;

    public SubUnit(int state, boolean barbed, double t, SubUnitListener list) {
        _state = state;
        _barbed = barbed;
        _t = t;
        if (list != null) {
            _listerns.add(list);
        }
        _record = Math.random() < 0.01;
        //  _record = true;
    }

    public void addListener(SubUnitListener sl) {
        _listerns.add(sl);
    }

    public void removeListener(SubUnitListener sl) {
        _listerns.remove(sl);
    }

    public void remove(double t) {

        if (MainJFrame.ACTIN ) {
//            double dt = Math.ceil((t - _t) * 10 + Math.random() - 1) / 10;
            
            //if (dt >= 0.0) {
                for(int i=0;i<_listerns.size();){
                    SubUnitListener sl=_listerns.get(i);
                    int size=_listerns.size();
                    sl.remove(t, this);
                    if(_listerns.size()==size){
                        i++;
                    }
                }
            //}
            //_t = t;
            /*
            MainJFrame.lifeTimes.add(dt);
            _ps.println(t - _t);
            _ps.flush();*/

        }
    }
}
