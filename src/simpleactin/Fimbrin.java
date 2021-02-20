/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simpleactin;

import java.util.LinkedList;
import java.util.Random;
import org.apache.commons.math3.distribution.GammaDistribution;

/*
 *
 * @author sm2983
 */
public class Fimbrin implements SubUnitListener, ProteinI {

    final static int _ABDAssociated = 1, _OtherProteinsAssociated = 2, _Dissociated = 4;
    final static double KBT = 4.114;
    final static double GSCALE = 20;
    private Filament _f1, _f2;
    /*
    0:No connection
    1:Connected to filament 1
    2:Connected to filament 2
     */
    double _t1 = -1, _t2 = -1;
    int _end1FilamentIndex = -1;
    int _end2FilamentIndex = -1;
    public static double _thermalFluctions, _k1off, _k2off, _k1on, _k2on, _k3off, _k3on;
    public static double _offrates[] = new double[2];
    public static double _ratio1, _ratio2, _UCA;
    private static GammaDistribution GammaDists[] = new GammaDistribution[(int) (140 * GSCALE)];
    int state = 0;
    private SubUnit _end1Attached, _end2Attached;
    private LifeTimeRecorder _ltr = null;
    private LinkedList<Updates> _updates;

    public Fimbrin(Filament f1, Filament f2,
            double thermalFluctions, WaitingTime waitingTime, LifeTimeRecorder ltr) {
        _f1 = f1;
        _f2 = f2;
        _thermalFluctions = thermalFluctions;
        _end1Attached = _end2Attached = null;
        _ltr = ltr;
        _updates = new LinkedList<>();
//        _p1=new Protein(f1, _k1off, _k1on, _k3off, _k3on);
    }

    public static double getOffRate(double t) {
        int index = Math.min(GammaDists.length - 1, (int) (t * GSCALE));
        double factor = _k2off * Math.exp((GammaDists[index].sample() / KBT));
        //double factor = _k2off * Math.exp((t * _UCA + 0 * GammaDists[0].sample() / KBT));
        //double factor = _k2off * (1 + t * _UCA);
        //double factor = _k2off + t * _UCA;
        return 1 - Math.exp(-factor);

    }

    public static void setDists(double Sh, double ShA, double Sc, double ScA,
            double k1off, double k2off, double k3off, double k1on, double k2on, double k3on) {
        _k1off = k1off;
        _k2off = k2off;
        _k3off = k3off;

        _offrates[0] = _k1off;
        _offrates[1] = _k3off;

        _k1on = k1on;
        _k2on = k2on;
        _k3on = k3on;
        _ratio1 = _k1on / (_k1on + _k3on);
        _ratio2 = _k2on / (_k2on + _k3on);
        _UCA = ScA;

        for (int j = 0; j < GammaDists.length; j++) {
            //int i = j / GSCALE < 5 ? j : 5 * GSCALE;
            double i = j / GSCALE;
            GammaDists[j] = new GammaDistribution(Sh + ShA * i, Sc + ScA * i);
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

    private int associate(double on1, double on2, double ratio) {
        int ret = 0;
        boolean b1 = Math.random() < on1;
        boolean b2 = Math.random() < on2;
        if (b1 && b2) {
            b1 = Math.random() < ratio;
            b2 = !b1;
        }
        if (b1) {
            ret = _ABDAssociated;
        } else if (b2) {
            ret = _OtherProteinsAssociated;
        }

        return ret;
    }

    private void addUpdate(int filamentIndex, int state, SubUnit su) {
        if (!contained(su)) {
            _updates.add(new Updates(filamentIndex, state, su));
        }
    }

    private void updateSubunit(SubUnit su, Filament f, int filamentIndex, double t) {
        if (!su._decorated) {
            if (state == 0) {
                int r = associate(_k1on, _k3on, _ratio1);
                if (r == _ABDAssociated) {
                    addUpdate(filamentIndex, r, su);
                } else if (r == _OtherProteinsAssociated) {
                    su._decorated = true;
                    su._decoratedOffrateIndex = 1;
                }

            } else if (state == 1 && filamentIndex != _end1FilamentIndex) {
                int r = associate(_k2on, _k3on, _ratio2);
                if (r == _ABDAssociated) {
                    addUpdate(filamentIndex, r, su);
                    su._decoratedOffrateIndex = -2;
                } else if (r == _OtherProteinsAssociated) {
                    su._decorated = true;
                    su._decoratedOffrateIndex = 1;
                }

            } else {
                if (Math.random() < _k3on) {
                    su._decorated = true;
                    su._decoratedOffrateIndex = 1;
                }
            }

        } else {
            if (state == 2 && (su == _end1Attached || su == _end2Attached)) {
                double offrate1 = _offrates[0];
                double offrate2 = getOffRate(t - _t2);

                boolean b1 = Math.random() < offrate1;
                boolean b2 = Math.random() < offrate2;

                if (b1 && b2) {
                    double ratio = offrate1 / (offrate1 + offrate2);
                    b1 = Math.random() < ratio;
                    b2 = !b1;
                }
                if (b1) {
                    addUpdate(filamentIndex, _Dissociated, su);

                }
                if (b2) {
                    //     addUpdate(filamentIndex, _Dissociated, su);

                    addUpdate(filamentIndex, _Dissociated, _end1Attached);

                    addUpdate(filamentIndex, _Dissociated, _end2Attached);

                }
            } else {
                double offrate = _offrates[su._decoratedOffrateIndex];
                if (Math.random() < offrate) {
                    if (su == _end1Attached || su == _end2Attached) {
                        addUpdate(filamentIndex, _Dissociated, su);
                    } else {
                        su._decorated = false;
                        su._decoratedOffrateIndex = -1;
                    }
                }

            }
        }
    }

    private void attachToFilament(Filament f, int filamentIndex, double t) {
        boolean found = false;
        for (int i = 0; i < f._subunits.size(); i++) {
            SubUnit su = f._subunits.get(i);
            updateSubunit(su, f, filamentIndex, t);
            found |= (su == _end1Attached);
            found |= (su == _end2Attached);
            found |= contained(su);
        }
        for (Updates u : _updates) {
            if (u.newState == _Dissociated) {
                SubUnit su = u.su;
                found |= (su == _end1Attached);
                found |= (su == _end2Attached);
                found |= contained(su);
            }
        }

        if (state == 2 && !found) {
            Filament f1 = _end1FilamentIndex == 0 ? _f1 : _f2;
            Filament f2 = _end2FilamentIndex == 0 ? _f1 : _f2;

            throw new RuntimeException(_end1Attached + "\t" + _end2Attached + "\t"
                    + (contained(_end1Attached) || f1._subunits.contains(_end1Attached))
                    + "\t" + (contained(_end2Attached) || f2._subunits.contains(_end2Attached))
                    + "\t" + (_end1FilamentIndex + "\t" + _end2FilamentIndex));

        }
    }

    @Override
    public boolean update(double t
    ) {

        attachToFilament(_f1, 0, t);
        attachToFilament(_f2, 1, t);
        applyUpdates(t);
        if (_end1Attached != null && _end1Attached == _end2Attached) {
            throw new RuntimeException("REP");
        }
        _updates.clear();
        return true;
    }

    private void applyUpdates(double t) {
        int newState = state;
        for (int i = 0; i < _updates.size();) {
            Updates update = _updates.get(i);

            if (update.newState == _Dissociated) {
                if (state == 0 && _t1 == -1) {
                    throw new RuntimeException("ERROR");
                }
                if (_end1Attached == update.su) {
                    _end1Attached = null;
                    _end1FilamentIndex = -1;
                    /*if (state == 1) {
                        _end1Attached = null;
                        _end1FilamentIndex = -1;

                    } else if (state == 2) {
                        _end1Attached = _end2Attached;
                        _end2Attached = null;
                        _t2 = -1;
                        _end1FilamentIndex = _end2FilamentIndex;
                        _end2FilamentIndex = -1;

                    }*/
                } else if (update.su == _end2Attached) {
                    _end2Attached = null;
                    _end2FilamentIndex = -1;
                    /*
                    if (state == 2) {
                        _end2Attached = null;
                        _end2FilamentIndex = -1;
                        _t2 = -1;

                    } else {
                        throw new RuntimeException("Error");
                    }*/

                } else {
                    System.out.println("" + _end1Attached + "\t" + _end2Attached + "\t" + update.su);
                    throw new RuntimeException("REP");
                }
                newState--;
                update.su.removeListener(this);
                update.su._decorated = false;
                update.su._decoratedOffrateIndex = -1;
                _updates.remove(i);
            } else {
                i++;
            }
        }

        if (_updates.size() > 0) {
            int N = (int) Math.floor(_updates.size() * Math.random());
            Updates update = _updates.get(N);
            if (state == 0) {
                update.su._decoratedOffrateIndex = 0;
                newState++;
                _end1Attached = update.su;
                _end1FilamentIndex = update.filamentIndex;

            } else if (state == 1) {
                update.su._decoratedOffrateIndex = 0;
                newState++;
                update.su._decorated = true;
                _end2Attached = update.su;
                _end2FilamentIndex = update.filamentIndex;
            }
            update.su._decorated = true;
            update.su.addListener(this);
        }

        if (_end1Attached == null && _end2Attached != null) {
            if (newState != 1) {
                throw new RuntimeException("Err");
            }
            _end1Attached = _end2Attached;
            _end2Attached = null;
            _end1FilamentIndex = _end2FilamentIndex;
            _end2FilamentIndex = -1;
        }
        if (_end1Attached != null) {
            if (_end2Attached != null) {
                if (newState != 2) {
                    throw new RuntimeException("Err");
                }
                if (_t2 == -1) {
                    _t2 = t;
                }
            } else {
                _t2 = -1;
                if (newState != 1) {
                    throw new RuntimeException("Err");
                }
                if (_t1 == -1) {
                    _t1 = t;
                }
                _end2FilamentIndex = -1;
            }
        } else if (_end2Attached == null) {
            if (newState != 0) {
                throw new RuntimeException("ERROR");
            }
            _end1FilamentIndex = _end2FilamentIndex = -1;
        }
        if (_t1 == -1 && newState > 0) {
            _t1 = t;
        }
        if (state > 0 && newState == 0 && _t1 != -1) {
            _ltr.addTime(t, _t1);
            _t1 = -1;
        }
        state = newState;
        if (state < 0 || state > 2) {
            throw new RuntimeException("ERR:" + state);
        }
        if (state == 2 && (_end1Attached == null || _end2Attached == null)) {
            throw new RuntimeException("ERR:" + state);
        }
    }

    @Override
    public void remove(double t, SubUnit su) {
        if ((su == _end1Attached || su == _end2Attached) && !contained(su)) {

            addUpdate(-1, _Dissociated, su);
        }
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
        return _t1;
    }

    @Override
    public void reset() {
        _t1 = -1;
        _t2 = -1;
        state = 0;
        _end1Attached = _end2Attached = null;
        _end1FilamentIndex = _end2FilamentIndex = -1;
        _updates.clear();
        /*        _end1Connection = 0;
        _end2Connection = 0;
        _end1Attached = null;
        _end2Attached = null;*/
    }

    @Override
    public double getDetachedTime() {
        return -1;
    }

    @Override
    public void severAlert(double t) {

    }

    private boolean contained(SubUnit su) {
        boolean ret = false;
        for (Updates up : _updates) {
            if (up.su == su) {
                ret = true;
                break;
            }
        }
        return ret;

    }
}

class Updates {

    int filamentIndex;
    SubUnit su;
    int newState;

    public Updates(int ind, int state, SubUnit su) {
        filamentIndex = ind;
        newState = state;
        this.su = su;
    }
}
/*class ABDs {

    SubUnit _abd1, _abd2;
    double _t1, _t2;
    boolean _tagged=false;
    public ABDs(SubUnit abd1, SubUnit abd2) {
        _abd1 = abd1;
        _abd2 = abd2;
    }
}
 */
