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
public class Protein1 implements SubUnitListener, ProteinI {

    final double KBT = 4.114;
    final static int GSCALE = 16;
    private Filament _f1;
    double _t = -1;
    private LinkedList<SubUnit> _available = new LinkedList<>();
    /*
    0:No connection
    1:Connected to filament 1
    2:Connected to filament 2
     */
    private int _end1Connection = 0;
    private double _thermalFluctions, _k1, _kon1;
    private static Random _rand = new Random();

    private static GammaDistribution GammaDists[] = new GammaDistribution[140 * GSCALE];
    private SubUnit _end1Attached = null;
    private boolean _detachEnd1 = false;
    private boolean _end1CanAttach = true;
    double bt = -1, _nextAttachTime;

    public Protein1(Filament f1, double k1, double kon1,
            double thermalFluctions) {
        _f1 = f1;

        _k1 = k1;

        _kon1 = kon1;

        _thermalFluctions = thermalFluctions;

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

        _end1Connection = 0;
        _end1Attached.removeListener(this);
        _end1Attached._decorated = false;
        _end1Attached = null;
        _detachEnd1 = false;
        
        

    }

    @Override
    public boolean update(double t) {

        if (_end1Connection == 0) {
            if (!_detachEnd1 && _f1._subunits.size() > 0) {
                _available.clear();

                for (SubUnit s : _f1._subunits) {
                    if (s._t >= Filament._STARTTIME && !s._decorated&&s._record) {
                        _available.add(s);
                    }
                }
                int n = _available.size();
                if (_rand.nextDouble() < 1 - Math.exp(-n * _kon1)) {
                    _end1Connection = 1;
                    int index = (int) (Math.random() * (n));
                    //index=n;

                    _end1Attached = _available.get(index);
                    _end1Attached._decorated = true;
                    _end1Attached.addListener(this);

                    _t = _end1Attached._t;

                }

            }
        } else {
            _detachEnd1 |= _rand.nextDouble() < _k1;

        }

        if (_detachEnd1) {
            detach(t);
        }

        if (_end1Connection > 0 && bt == -1) {
            bt = t;
        } else {
            bt = -1;
        }
        return _end1Connection > 0 || _t == -1;
    }

    @Override
    public void remove(double t, SubUnit su) {
        if (t > 0) {
            _t=su._t;
            _detachEnd1 = true;
        } else {
            _end1Attached.removeListener(this);
            reset();
            
        }
    }

    @Override
    public double getTime() {
        return _t;
    }
 
    @Override
    public void reset() {
        _t = -1;
        _end1Connection = 0;
        _detachEnd1 = false;
        _end1Attached = null;
    }

    @Override
    public double getDetachedTime() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void severAlert(double t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void react(double t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
