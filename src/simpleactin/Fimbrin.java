/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simpleactin;

import java.util.Random;
import org.apache.commons.math3.distribution.GammaDistribution;

/**
 *
 * @author sm2983
 */
public class Fimbrin implements SubUnitListener {

    final double KBT = 4.114;
    private Filament _f1, _f2;
    /*
    0:No connection
    1:Connected to filament 1
    2:Connected to filament 2
     */
    private int _end1Connection = 0, _end2Connection = 0;
    private double _thermalFluctions, _k1, _k2, _kon1, _kon2;
    private static Random _rand = new Random();
    public double _t = -1;
    private static GammaDistribution GammaDists[] = new GammaDistribution[110];
    private SubUnit _end1Attached = null, _end2Attached = null;
    private boolean _detachEnd1 = false, _detachEnd2 = false;
    private boolean _end1CanAttach = true, _end2CanAttach = true;

    public Fimbrin(Filament f1, Filament f2, double k1, double k2, double kon1,
            double kon2, double thermalFluctions) {
        _f1 = f1;
        _f2 = f2;
        _k1 = k1;
        _k2 = k2;
        _kon1 = kon1;
        _kon2 = kon2;
        _thermalFluctions = thermalFluctions;
    }

    public static void setDists(double Sh, double ShA, double Sc, double ScA) {
        for (int i = 0; i < 110; i++) {
            GammaDists[i] = new GammaDistribution(Sh + ShA * i, Sc + ScA * i);
        }
    }

    private final SubUnit getSubUnit(int i, int n1) {
        return i < n1 ? _f1._subunits.get(i) : _f2._subunits.get(i - n1);
    }

    private void detach(boolean end1) {
        if (end1) {
            _end1Connection = 0;
            _end1Attached.removeListener(this);
            _end1Attached = null;
            _detachEnd1 = false;
        } else {
            _end2Connection = 0;
            _end2Attached.removeListener(this);
            _end2Attached = null;
            _detachEnd2 = false;
            _end2CanAttach = false;
        }
    }

    public boolean update(double t) {
        if (_end1Connection + _end2Connection == 0) {

            int n1 = _f1._subunits.size(), n2 = _f2._subunits.size();

            for (int i = 0; i < n1 + n2; i++) {
                if (_end1Connection == 0 && _end1CanAttach && _rand.nextDouble() < _kon1 * 2) {
                    _end1Connection = i < n1 ? 1 : 2;
                    _end1Attached = getSubUnit(i, n1);
                    _end1Attached.addListener(this);

                }
                if (_end2Connection == 0 && _end1Connection == 1 && i >= n1
                        && _end2CanAttach && _rand.nextDouble() < _kon2) {
                    _end2Connection = 2;
                    _end2Attached = _f2._subunits.get(i - n1);
                    _end2Attached.addListener(this);
                }
            }
            if (_end1Connection + _end2Connection > 0 && _t < 0) {
                _t = t;
            }
        } else if (_end1Connection * _end2Connection > 0) {
            double factor =  _k1*Math.exp((GammaDists[(int) ((t-_t) * 10)].sample() / KBT));
            //factor=1;
            
            double k1p =1-Math.exp(-factor) ;

            if (_rand.nextDouble() < k1p) {
                _detachEnd2 = true;
            }
            if (_rand.nextDouble() < k1p) {
                if (_detachEnd2) {
                    _detachEnd1 = true;
                } else {
                    _detachEnd2 = true;
                }
            }
        } else {
            boolean detach = _rand.nextDouble() < _k2, attach = false;
            int n = _end1Connection == 1 || _end2Connection == 1 ? _f2._subunits.size() : _f1._subunits.size();
            int subunit = 0;
            for (; subunit < n && !attach;) {
                attach |= _rand.nextDouble() < _kon2;
                if (!attach) {
                    subunit++;
                }
            }
            if (_end1Connection == 0) {
                throw new RuntimeException("ERROR end1 cannot detach first");
                /*
                if (attach) {
                    _end1Connection = 3 - _end2Connection;
                    _end1Attached = _end1Connection == 1 ? _f1._subunits.get(subunit)
                            : _f2._subunits.get(subunit);
                    _end1Attached.addListener(this);
                }
                if (detach) {
                    _detachEnd2 = true;
                }
                 */
            } else {

                if (attach && _end2CanAttach) {
                    _end2Connection = 3 - _end1Connection;
                    _end2Attached = _end2Connection == 1 ? _f1._subunits.get(subunit)
                            : _f2._subunits.get(subunit);
                    _end2Attached.addListener(this);
                }
                if (detach) {
                    _detachEnd1 = true;
                }

            }
        }
        if (_detachEnd1) {
            detach(true);
        }
        if (_detachEnd2) {
            detach(false);
        }
        if (_end1Connection == 0 && _end2Connection > 0) {
            _end1Connection = _end2Connection;
            _end1Attached = _end2Attached;
            _end1CanAttach = _end2CanAttach;

            _end2Connection = 0;
            _end2Attached = null;

        }
        return _end1Connection + _end2Connection > 0;
    }

    @Override
    public void remove(double t, SubUnit su) {

        if (su == _end1Attached) {
            _detachEnd1 = true;
        } else if (su == _end2Attached) {
            _detachEnd2 = true;
        } else {
            throw new RuntimeException("Detachment from not-attached-subunit?!?");
        }
    }
}
