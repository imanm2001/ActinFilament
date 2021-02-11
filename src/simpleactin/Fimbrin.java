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
public class Fimbrin implements SubUnitListener, ProteinI {

    
    final static double KBT = 4.114;
    final static int GSCALE = 16;
    private Filament _f1, _f2;
    /*
    0:No connection
    1:Connected to filament 1
    2:Connected to filament 2
     */
    double _t = -1, _detached = -1;
    boolean _forceDetach1 = false,_forceDetach2 = false;
    private LinkedList<SubUnit> _end1Attached = new LinkedList<>(), _detachEnd1 = new LinkedList<>();
    
    public static double _thermalFluctions, _k1off, _k2off, _k1on, _k2on,_k3off,_k3on;
    public Protein _p1,_p2;
    private static Random _rand = new Random();

    private static GammaDistribution GammaDists[] = new GammaDistribution[140 * GSCALE];
    
    
    private boolean _end1CanAttach = true, _end2CanAttach = true;
    double bt = -1, _nextAttachTime;
    WaitingTime _wt;
    public double decoration=0.1;
    

    public Fimbrin(Filament f1, Filament f2,
            double thermalFluctions, WaitingTime waitingTime) {
        _f1 = f1;
        _f2 = f2;
        _thermalFluctions = thermalFluctions;
        _wt = waitingTime;
        _p1=new Protein(f1, _k1off, _k1on, _k3off, _k3on);
    }

    public static double getOffRate(double t) {
        double factor = _k1off * Math.exp((GammaDists[(int) ((t) * 10 * GSCALE)].sample() / KBT));

        return 1 - Math.exp(-factor);

    }

    public static void setDists(double Sh, double ShA, double Sc, double ScA,
            double k1off, double k2off,double k3off, double k1on, double k2on, double k3on) {
        _k1off = k1off;
        _k2off = k2off;
        _k3off = k3off;
        _k1on = k1on;
        _k2on = k2on;
        _k3on = k3on;
        for (int j = 0; j < GammaDists.length; j++) {
            int i = j / GSCALE < 5 ? j : 5 * GSCALE;
            GammaDists[j] = new GammaDistribution(Sh + ShA * i / (double) GSCALE, Sc + ScA * i / (double) GSCALE);
        }
    }

    private final SubUnit getSubUnit(int i, int n1) {
        return i < n1 ? _f1._subunits.get(i) : _f2._subunits.get(i - n1);
    }

    private void detach(boolean end1, double t) {
        /*
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
            _nextAttachTime = _wt.getTime() + t;
        }
        */
    }

    @Override
    public boolean update(double t) {
        
        return false;
    }

    @Override
    public void remove(double t, SubUnit su) {
        /*
        if (t == -1) {
            if (su == _end1Attached) {
                _end1Attached = _end2Attached;
                _end1Connection = _end2Connection;
            }
            _end2Attached = null;
            _end2Connection = 0;
            if(_end1Attached==_end2Attached){
                reset();
            }

        } else {
            if (su == _end1Attached) {
                _detachEnd1 = true;
            } else if (su == _end2Attached) {
                _detachEnd2 = true;
            } else {
                throw new RuntimeException("Detachment from not-attached-subunit?!?");
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
        bt = -1;
/*        _end1Connection = 0;
        _end2Connection = 0;
        _end1Attached = null;
        _end2Attached = null;*/
    }

    @Override
    public double getDetachedTime() {
        return _detached;
    }

    @Override
    public void severAlert(double t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
