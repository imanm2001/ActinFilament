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

    public static int ID = 0;
    public int _id, _state;
    public boolean _barbed;
    public double _t = -1, _decorationTime = -1;
    public double _decoratedOffrate = 0;
    public LinkedList<DecorationReaction> _decorationReaction = new LinkedList<>();
    public LinkedList<DecorationReaction> _oldDecorationReaction = new LinkedList<>();
    private LinkedList<SubUnitListener> _listerns = new LinkedList<>();
    boolean _record = false;
    int _connectionStatus = 0;
    public boolean _decorated = false;
    public boolean _undecorated = false;
    private DecorationListener _dl;
    public int _decoratedTag = 0;
    public Filament _filament;
    private DecorationReaction _theChosenOne = null;

    public SubUnit(int state, boolean barbed, double t, SubUnitListener list, Filament fl) {
        _state = state;
        _barbed = barbed;
        _filament = fl;
        _t = t;

        if (list != null) {
            _listerns.add(list);
        }
        _record = Math.random() < 0.01;
        //  _record = true;
        _id = ID;
        ID++;
    }

    public void addListener(SubUnitListener sl) {
        _listerns.add(sl);
    }

    public void removeListener(SubUnitListener sl) {
        if (_listerns.contains(sl)) {
            while (_listerns.contains(sl)) {
                _listerns.remove(sl);
            }
        } else {
            //  throw new RuntimeException();
        }
    }

    public void remove(double t) {

        if (MainJFrame.ACTIN) {
//            double dt = Math.ceil((t - _t) * 10 + Math.random() - 1) / 10;

            //if (dt >= 0.0) {
            for (int i = 0; i < _listerns.size();) {
                SubUnitListener sl = _listerns.get(i);
                int size = _listerns.size();
                sl.remove(t, this);
                if (_listerns.size() == size) {
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

    void forceReaction(DecorationReaction dr, double t) {
        _decoratedOffrate = dr.offrate;
        _dl = dr.callback;
        _decoratedTag = dr.tag;
        _decorated = true;
        _decorationTime = t;
        if (dr.callback != null) {
            dr.callback.reactionCallBack(this, t, dr.tag, dr._data);
        }
        _theChosenOne = dr;
    }

    void resetReaction() {
        _theChosenOne = null;
        _decoratedOffrate = -1;
        _dl = null;
        _decoratedTag = 0;
        _decorated = false;
    }

    void peakAReaction(double t, LinkedList<DecorationReaction> list) {

        double denum = 0;

        for (int i = 0; i < list.size(); i++) {
            DecorationReaction dr = list.get(i);
            denum += dr.onrate;
        }
        double r = Math.random() * denum;
        double s = 0;
        int i = 0;
        for (; i < list.size(); i++) {
            s = s + (list.get(i).onrate);
            if (r < s) {
                break;
            }
        }

        DecorationReaction dr = list.get(i);
        forceReaction(dr, t);

    }

    void decorate(double t) {
        if (_undecorated) {
            if (_dl != null) {
                _dl.reactionCallBack(this, t, -_decoratedTag, _theChosenOne._data);
            }

            resetReaction();;

        } else if (_decorationReaction.size() > 0) {

            if (_decorated) {
                throw new RuntimeException("DECORATED");
            }
            peakAReaction(t, _decorationReaction);
            _oldDecorationReaction.clear();
            _oldDecorationReaction.addAll(_decorationReaction);

        }
        _decorationReaction.clear();
    }

    void removeReaction(DecorationListener dl, LinkedList<DecorationReaction> reactionList) {
        LinkedList<DecorationReaction> indx = new LinkedList<>();
        for (int i = 0; i < reactionList.size(); i++) {
            DecorationReaction dr = reactionList.get(i);
            if (dr.callback != dl) {
                indx.add(dr);
            }
        }
        reactionList.clear();
        reactionList.addAll(indx);
        /*
        for (DecorationReaction dr : reactionList) {
            reactionList.remove(dr);
        }*/
    }

}

class DecorationReaction {

    double onrate;
    double offrate;
    int tag;
    DecorationListener callback;
    Filament _filament;
    Object _data;

    public DecorationReaction(double onr, double offr, int tag, DecorationListener dl, Object data) {
        this.onrate = onr;
        this.offrate = offr;
        this.tag = tag;
        callback = dl;
        _data = data;
    }

    public DecorationReaction(double onr, double offr, int tag, DecorationListener dl) {
        this(onr, offr, tag, dl, null);
    }
}

interface DecorationListener {

    void reactionCallBack(SubUnit su, double t, int tag, Object data);
}
