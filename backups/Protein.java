/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simpleactin;

import java.util.LinkedList;
import java.util.Random;
import org.apache.commons.math3.distribution.GammaDistribution;

/**
 *
 * @author sm2983
 */
public class Protein implements SubUnitListener, ProteinI {

    final double KBT = 4.114;
    final static int GSCALE = 16;
    private Filament _f1;
    double _t = -1, _detached = -1;
    boolean _forceDetach = false;
    private LinkedList<SubUnit> _available = new LinkedList<>();
    /*
    0:No connection
    1:Connected to filament 1
    2:Connected to filament 2
     */
    private double _k1, _kon1;
    private double _k2, _kon2;
    private double _ratio;

    private static GammaDistribution GammaDists[] = new GammaDistribution[140 * GSCALE];
    private LinkedList<SubUnit> _end1Attached = new LinkedList<>(), _detachEnd1 = new LinkedList<>();
    private boolean _end1CanAttach = true;
    double bt = -1, _nextAttachTime;

    public Protein(Filament f1, double k1, double kon1, double k2, double kon2) {
        _f1 = f1;

        _k1 = k1;
        _kon1 = kon1;

        _k2 = k2;
        _kon2 = kon2;
        _ratio = _kon1 / (_kon1 + _kon2);
    }

    public static void setDists(double Sh, double ShA, double Sc, double ScA) {
        for (int j = 0; j < GammaDists.length; j++) {
            int i = j / GSCALE < 5 ? j : 5 * GSCALE;
            GammaDists[j] = new GammaDistribution(Sh + ShA * i / (double) GSCALE, Sc + ScA * i / (double) GSCALE);
        }
    }

    private final SubUnit getSubUnit(int i) {
        return _f1._subunits.get(i);
    }

    private void detach(double t) {
        for (SubUnit s : _detachEnd1) {
            if (_end1Attached.contains(s)) {
                s.removeListener(this);
                _end1Attached.remove(s);
            }
            s._decorated = false;
            s._decoratedOffrate = -1;

        }
        _detachEnd1.clear();
    }

    @Override
    public boolean update(double t) {

        /*
        if (_f1._subunits.size() > 2) {

            for (SubUnit s : _f1._subunits) {

                if (!s._decorated) {
                    boolean b1 = Math.random() < _kon1;
                    boolean b2 = Math.random() < _kon2;
                    if (s._record) {
                        b1 = true;
                        b2 = false;
                    }
                    if (b1 && b2) {
                        if (Math.random() < _ratio) {
                            b2 = false;
                        } else {
                            b1 = false;
                        }
                    }
                    if (b1 || b2) {
                        s._decorated = true;
                        s.addListener(this);
                        if (b1) {
                            if (Math.random() < 1 && _end1Attached.size() <= 400) {
                                _detached = -1;
                                
                                    _t =_t==-1?s._t:Math.min(_t,s._t);
                                
                                if(!_end1Attached.contains(s)){
                                _end1Attached.add(s);
                                }else{
                                    System.out.println("ERROR");
                                }

                            }
                            s._decoratedOffrate = _k1;

                        } else {
                            s._decoratedOffrate = _k2;
                        }

                    }
                } else if (s._decorated && Math.random() < s._decoratedOffrate) {
                    _detachEnd1.add(s);
                }
            }

        } else {
            _t = -1;
            _detached = -1;
        }
        
        if (_detachEnd1.size() > 0) {
            detach(t);
        }*/
        if (_t == -1 && _f1.isTagged() > 0) {
            _t = t;
        }
        if (_t != -1 && _detached == -1 && (_f1.isTagged() == 0)) {
            _detached = t;

        }

        boolean waitingTime = _detached > 0 && (Math.floor(t * 10) - Math.floor(_detached * 10)) / 10.0 < 0.1;
        if (waitingTime && _f1.isTagged() > 0) {
            _detached = -1;
        }
        boolean attached = _detached == -1 || waitingTime;
        if (_f1._subunits.size() < 2) {
            if (_t > -1) {
                attached = false;
                if (_detached == -1) {
                    _detached = t;
                }
            }

        }
        if (!attached && _t < Filament._STARTTIME) {
            _t = _detached = -1;
            attached = true;
        }
        attached|=_forceDetach;
        /*
        if (_t > -1 && _end1Attached.size() != _f1.isTagged()) {
            for (SubUnit s : _end1Attached) {
                if (!_f1._subunits.contains(s)) {
                    System.out.println("ERROOR" + _end1Attached.size() + "\t" + _f1.isTagged());
                }
            }

        }*/
 /*
        if (!attached) {
            _t += t - _detached;
        }*/
        return attached;
        /*boolean decorated = false;
        for (SubUnit s : _f1._subunits) {
            if (s._t > Filament._STARTTIME && s._record) {
                decorated = true;

                _t = _t == -1 ? s._t : Math.min(_t, s._t);
                break;
            }
        }

        if (decorated) {

            _detached = -1;
        } else if (_t > 0 && _detached
                < 0) {
            _detached = t;
        }
        return _t == -1 || _detached
                == -1 || (Math.ceil(t
                        * 10) - Math.ceil(_detached * 10)) / 10 < 0.2;*/
    }

    @Override
    public void remove(double t, SubUnit su) {
        /*if (t == -1 && _t != -1 && _f1.isTagged() == 0) {
            _t = _detached = -1;
        }*/
 /*
        if (t > -1000) {
            if (!_detachEnd1.contains(su)) {
                _detachEnd1.add(su);
            }
        } else {
            su.removeListener(this);
            _end1Attached.remove(su);
            if (_end1Attached.size() == 0) {
                _t = -1;
                _detached = -1;

            }

        }
         */
    }

    @Override
    public double getTime() {
        return _t;
    }

    @Override
    public void reset() {
        _t = -1;
        _detached = -1;
        _forceDetach=false;
        _detachEnd1.clear();
        for (SubUnit s : _end1Attached) {
            s.removeListener(this);
        }
        _end1Attached.clear();
    }

    @Override
    public double getDetachedTime() {
        return _detached;
    }

    @Override
    public void severAlert() {

        if (_t != -1 && _f1.isTagged() == 0) {
                if(_detached!=-1){
                 _forceDetach=true;   
                }else{
                _t = -1;
                _detached = -1;
                }
            

        }
    }
}
