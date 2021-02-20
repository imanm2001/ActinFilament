/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simpleactin;

import java.util.LinkedList;
import java.util.Random;
import org.apache.commons.math3.distribution.GammaDistribution;
import static simpleactin.Filament._STARTTIME;
import static simpleactin.Filament._TIME;

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
    private double[] _offRates = new double[2];
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
    public double decoration = 0.01;
    private LifeTimeRecorder _ltr = null;

    public Protein(Filament f1, double k1, double kon1, double k2, double kon2, LifeTimeRecorder ltr) {
        _f1 = f1;
        _k1 = k1;
        _kon1 = kon1;
        _k2 = k2;
        _kon2 = kon2;
        _ratio = _kon1 / (_kon1 + _kon2);
        _ltr = ltr;
        resetOffrates();
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
                if (s._decorationTime == -1) {
                    throw new RuntimeException("ERROR");
                }
                s.removeListener(this);
                _end1Attached.remove(s);
                _ltr.addTime(t, s._decorationTime);
            }
            s._decorated = false;
            s._decoratedOffrateIndex = -1;
            s._decorationTime = -1;

        }
        _detachEnd1.clear();
    }

    public void resetOffrates() {
        _offRates[0] = _k1;
        _offRates[1] = _k2;
    }

    public static double getFrames(double t1, double t2) {
        return (Math.floor(t1 * 10) - Math.floor(t2 * 10)) / 10.0;
    }

    @Override
    public boolean update(double t) {

        if (_f1._subunits.size() > 0) {

            for (SubUnit s : _f1._subunits) {

                if (!s._decorated) {
                    boolean b1 = Math.random() < _kon1;
                    boolean b2 = Math.random() < _kon2;
                    //b1 = s._record;
                    //b2 = false;
                    if (b1 && b2) {
                        if (Math.random() < _ratio) {
                            b2 = false;
                        } else {
                            b1 = false;
                        }
                    }
                    if (b1 || b2) {
                        s._decorated = true;

                        if (b1) {
                            s.addListener(this);
                            if (Math.random() < decoration) {

                                if (!_end1Attached.contains(s)) {
                                    _end1Attached.add(s);
                                    s._decorationTime = t;
                                } else {
                                    System.out.println("ERROR");
                                }

                            }
                            s._decoratedOffrateIndex = 0;

                        } else {
                            s._decoratedOffrateIndex = 1;
                        }

                    }
                } else if (Math.random() < _offRates[s._decoratedOffrateIndex]) {
                    _detachEnd1.add(s);
                }
            }

        }

        if (_detachEnd1.size() > 0) {
            detach(t);
        }

        return true;
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

        if (t > -1) {
            if (!_detachEnd1.contains(su)) {
                _detachEnd1.add(su);
            }
        } else {
            su.removeListener(this);
            _end1Attached.remove(su);

            if (_end1Attached.size() == 0) {
                if (_t > Filament._STARTTIME && _detached != -1 && _f1._subunits.size() < 3) {
                    _forceDetach = true;
                } else {
                    reset();
                }

            }

        }

    }

    @Override
    public double getTime() {
        return _t;
    }

    @Override
    public void reset() {
        _t = -1;
        _detached = -1;
        _forceDetach = false;
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
    public void severAlert(double t) {

        /*if (_end1Attached.size() == 0) {

            if (_detached > -1 && _t > _STARTTIME) {
                _forceDetach = true;
            } else {
                reset();
            }

        }*/
    }
}
