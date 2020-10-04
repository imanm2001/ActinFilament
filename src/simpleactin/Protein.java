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
public class Protein extends ProteinI implements SubUnitListener {

    final double KBT = 4.114;
    final static int GSCALE = 16;
    private Filament _f1;
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
    double bt = -1, _waitingTime, _nextAttachTime;

    public Protein(Filament f1, double k1, double kon1,
             double thermalFluctions, double waitingTime) {
        _f1 = f1;
        
        _k1 = k1;
        
        _kon1 = kon1;
        
        _thermalFluctions = thermalFluctions;
        _waitingTime = waitingTime;

    }

    public static void setDists(double Sh, double ShA, double Sc, double ScA) {
        for (int j = 0; j < GammaDists.length; j++) {
            int i=j/GSCALE<5?j:5*GSCALE;
            GammaDists[j] = new GammaDistribution(Sh + ShA * i / (double) GSCALE, Sc + ScA * i / (double) GSCALE);
        }
    }

    private final SubUnit getSubUnit(int i) {
        return _f1._subunits.get(i) ;
    }

    private void detach( double t) {
        
            _end1Connection = 0;
            _end1Attached.removeListener(this);
            _end1Attached = null;
            _detachEnd1 = false;
        
    }
@Override
    public boolean update(double t) {
        if (_end1Connection == 0) {

            int n1 = _f1._subunits.size();

            for (int i = 0; i < n1 ; i++) {
                if (_end1Connection == 0 && _end1CanAttach && _rand.nextDouble() < _kon1 * 2) {
                    _end1Connection = i < n1 ? 1 : 2;
                    _end1Attached = _f1._subunits.get(i);
                    _end1Attached.addListener(this);

                }
                
            }
            if (_end1Connection  > 0 && _t < 0) {
                _t = t;
            }
         
        } else {
           _detachEnd1 = _rand.nextDouble() < _k1;
            int n = _f1._subunits.size();
            

    
        if (_detachEnd1) {
            detach(t);
        }
        }
       
        
        if (_end1Connection > 0 && bt == -1) {
            bt = t;
        } else {
            bt = -1;
        }
        return _end1Connection > 0;
    }

    @Override
    public void remove(double t, SubUnit su) {

        if (su == _end1Attached) {
            _detachEnd1 = true;
        } 
    }
}
