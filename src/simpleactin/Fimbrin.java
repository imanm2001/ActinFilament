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
public class Fimbrin implements SubUnitListener, ProteinI, DecorationListener {

    final static int _ABDAssociated = 1, _OtherProteinsAssociated = 2, _Dissociated = 4, _SEVERED = 8, _OTHERFIMBRINS = 16;
    final static double KBT = 4.114;
    final static double GSCALE = 20;

    /*
    0:No connection
    1:Connected to filament 1
    2:Connected to filament 2
     */
    double _t1 = -1, _t2 = -1;

    public double _thermalFluctions, _k1off, _k2off, _k1on, _k2on;

    public double _UCA;
    private static GammaDistribution GammaDists[] = new GammaDistribution[(int) (140 * GSCALE)];
    int state = 0;
    private SubUnit _end1Attached, _end2Attached;
    private LifeTimeRecorder _ltr = null;
    private LinkedList<Updates> _updates;
    private LinkedList<ABDs> _fimbrins = new LinkedList<>();
    int _numFoundSU = 0;

    public Fimbrin(double thermalFluctions, WaitingTime waitingTime, LifeTimeRecorder ltr) {

        _thermalFluctions = thermalFluctions;
        _end1Attached = _end2Attached = null;
        _ltr = ltr;
        _updates = new LinkedList<>();
//        _p1=new Protein(f1, _k1off, _k1on, _k3off, _k3on);
    }

    public double getOffRate(double t) {
        int index = Math.min(GammaDists.length - 1, (int) (t * GSCALE));
        //double factor = _k2off * Math.exp((GammaDists[index].sample() / KBT));
        //double factor = _k2off * Math.exp((t * _UCA + 0 * GammaDists[0].sample() / KBT));
        double factor = _k2off * (1 + t * _UCA);
        //double factor = _k2off + t * _UCA;
        return 1 - Math.exp(-factor);

    }

    public void setDists(double Sh, double ShA, double Sc, double ScA,
            double k1off, double k2off, double k1on, double k2on) {
        _k1off = k1off;
        _k2off = k2off;

        _k1on = k1on;
        _k2on = k2on;

        _UCA = ScA;

        for (int j = 0; j < GammaDists.length; j++) {
            //int i = j / GSCALE < 5 ? j : 5 * GSCALE;
            double i = j / GSCALE;
            GammaDists[j] = new GammaDistribution(Sh + ShA * i, Sc + ScA * i);
        }
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

    private void addUpdate(int state, SubUnit su, double t) {
        if (!contained(su)) {
            _updates.add(new Updates(state, su, t));
        } else {
//            throw new RuntimeException();
        }
    }

    private void attachToFilament(Filament f, double t) {
        boolean found = false;
        double onrate = -1;
        double offrate = -1;
        if (t > Filament._STARTTIME) {
            if (state == 0) {
                onrate = _k1on;
                offrate = -1;
            } else if (state == 1) {
                if (f.ID != _end1Attached._filament.ID) {
                    onrate = _k2on;
                } else {
                    onrate = -1;
                }
                offrate = -1;
            }
        }

        for (int i = 0; i < f._subunits.size(); i++) {
            SubUnit su = f._subunits.get(i);
            if (!su._decorated) {
                /*boolean b = onrate > 0 && Math.random() < onrate;
                if (state == 0) {
                    if (b) {
                        if (state == 0) {
                            //            su._decorationReaction.add(new DecorationReaction(_k1on, _k1off, _ABDAssociated, this));
                        } else if (state == 1) {
                            // su._decorationReaction.add(new DecorationReaction(onrate, 0, _ABDAssociated, this));
                        }
                    }
                }*/

                if (Math.random() < _k1on) {
                    if (state == 0 && su._record) {
                        su._decorationReaction.add(new DecorationReaction(_k1on, 0, _ABDAssociated, this));
                    } else {
                        su._decorationReaction.add(new DecorationReaction(_k1on, _k1off, _OTHERFIMBRINS, this));
                    }
                }
                if (state == 1 && _end1Attached._filament.ID != f.ID && Math.random() < _k2on) {
                    su._decorationReaction.add(new DecorationReaction(_k2on, 0, _ABDAssociated, this));
                }

                //updateSubunit(su, f, filamentIndex, t);
            }

        }

    }

    @Override
    public boolean update(double t) {
        _numFoundSU = 0;
        for (Filament f : MainJFrame._filaments) {
            attachToFilament(f, t);
        }

        if (state == 2) {
            if (contained(_end1Attached)) {
                _numFoundSU++;
            }
            if (contained(_end2Attached)) {
                _numFoundSU++;
            }
            Filament f1 = _end1Attached._filament;
            Filament f2 = _end2Attached._filament;
            if (f1._subunits.contains(_end1Attached)) {
                _numFoundSU++;
            }
            if (f2._subunits.contains(_end2Attached)) {
                _numFoundSU++;
            }
            if (_numFoundSU != 2) {

                throw new RuntimeException(_end1Attached + "\t" + _end2Attached + "\t"
                        + (contained(_end1Attached) + "," + f1._subunits.contains(_end1Attached))
                        + "\t" + (contained(_end2Attached) + "," + f2._subunits.contains(_end2Attached))
                        + "\t" + _numFoundSU);

            }
            if (Math.random() < getOffRate(t - _t2)) {
                addUpdate(_Dissociated, _end1Attached, t);
            }
            if (Math.random() < getOffRate(t - _t2)) {
                addUpdate(_Dissociated, _end2Attached, t);
            }
        } else if (state == 1 && Math.random() < _k1off) {
            addUpdate(_Dissociated, _end1Attached, t);

        }

        return true;
    }

    /*private void applyUpdates(double t) {
        int newState = state;
        boolean severed = false;
        for (int i = 0; i < _updates.size();) {
            Updates update = _updates.get(i);

            if (update.newState == _Dissociated || update.newState == _SEVERED) {
                if (state == 0 && _t1 == -1) {
                    throw new RuntimeException("ERROR");
                }
                if (_end1Attached == update.su) {
                    if (state == 1) {

                        _end1FilamentIndex = -1;
                        severed |= update.newState == _SEVERED;
                        _end1Attached = null;

                    } else if (state == 2) {
                        _end1Attached = _end2Attached;
                        _end2Attached = null;

                        _end1FilamentIndex = _end2FilamentIndex;
                        _end2FilamentIndex = -1;

                    }
                } else if (update.su == _end2Attached) {
                    if (state == 2) {
                        _end2Attached = null;
                        _end2FilamentIndex = -1;
                    } else {
                        throw new RuntimeException("Error");
                    }

                } else {
                    System.out.println("" + _end1Attached + "\t" + _end2Attached + "\t" + update.su);
                    throw new RuntimeException("REP");
                }
                newState--;
                update.su.removeListener(this);
                update.su._decorated = false;
                update.su._decoratedOffrate = 0;
                update.su._decorationTime = -1;

                _updates.remove(i);
            } else {
                i++;
            }
        }

        if (_updates.size() > 0) {
            int N = (int) Math.floor(_updates.size() * Math.random());
            Updates update = _updates.get(N);

            if (state == 0) {
                
//              update.su._decoratedOffrateIndex = 0;
                newState++;
                _end1Attached = update.su;
                _end1FilamentIndex = update.filamentIndex;
                _t1 = update._t;
                if (_end1Attached == null) {
                    throw new RuntimeException();
                }
            } else if (state == 1) {
//                update.su._decoratedOffrateIndex = 0;
                newState++;
                _end2Attached = update.su;
                _end2FilamentIndex = update.filamentIndex;
                if (_end1FilamentIndex == _end2FilamentIndex) {
                    System.out.flush();
                    System.err.flush();
                    throw new RuntimeException();

                }
            }
            update.su.addListener(this);
            for (int i = 0; i < _updates.size(); i++) {
                if (i != N) {
                    Updates up = _updates.get(i);
                    up.su.removeReaction(this, up.su._oldDecorationReaction);
                    if (up.su._oldDecorationReaction.size() > 0) {
                        up.su.peakAReaction(t, up.su._oldDecorationReaction);
                    } else {
                        up.su.resetReaction();
                    }
                }
            }
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

                _end2FilamentIndex = -1;
            }

        } else if (_end2Attached == null) {
            if (newState != 0) {
                throw new RuntimeException("ERROR");
            }
            _end1FilamentIndex = _end2FilamentIndex = -1;
        }
        if (_t1 == -1 && newState > 0) {
            _t1 = _end1Attached._t;
        }
        if (state > 0 && newState == 0) {
            if (_t1 != -1 && !severed) {
                _ltr.addTime(t, _t1);
            }

            _t1 = -1;
            _end1FilamentIndex = _end2FilamentIndex = -1;
        }

        state = newState;

        if (state < 0 || state > 2) {
            throw new RuntimeException("ERR:" + state);
        }
        if (state == 2 && (_end1Attached == null || _end2Attached == null)) {
            throw new RuntimeException("ERR:" + state);
        }
        if (state == 2 && (_end1FilamentIndex == _end2FilamentIndex)) {
            Filament f1 = _end1FilamentIndex == 0 ? _f1 : _f2;
            Filament f2 = _end2FilamentIndex == 0 ? _f1 : _f2;
            System.out.flush();
            throw new RuntimeException("ERR:" + _end1Attached + "," + _end2Attached + ":" + _end1FilamentIndex + "\t" + _end2FilamentIndex + "\t" + f1._subunits.contains(_end1Attached)
                    + "\t" + f2._subunits.contains(_end2Attached));
        }

    }*/
    private void applyUpdates(double t) {
        int newState = state;
        boolean severed = false;
        boolean dissociated = false;
        double dissociateTime = 0;
        for (int i = 0; i < _updates.size();) {

            Updates update = _updates.get(i);

            if (update.newState != _ABDAssociated) {
                if (update.su == _end1Attached || update.su == _end2Attached) {
                    newState--;
                    if (newState == 0) {
                        _ltr.addTime(t, _t1);
                        System.out.println("" + t + "\t" + _t1 + "\t" + update.su._t);
                    }

                    if (update.newState == _Dissociated) {

                        //recored = true;
                    }
                    if (update.su == _end1Attached) {

                        _end1Attached = null;

                    }
                    if (update.su == _end2Attached) {
                        _end2Attached = null;

                    }
                    update.su._decorationTime = -1;
                }

                _updates.remove(i);

            } else {
                i++;
            }
        }

        if (_updates.size() > 0) {

            int N = (int) Math.floor(_updates.size() * Math.random());
            Updates update = _updates.get(N);
            update.su.addListener(this);
            if (state == 0) {

                if (_end1Attached == null) {
                    newState++;
                    _end1Attached = update.su;
                    _t1 = update._t;
                } else {
                    throw new RuntimeException();
                }

                //_end1Attached._decorationTime = t;
                /*if (_end1Attached == null) {
                    throw new RuntimeException();
                }*/
            } else if (state == 1) {
                newState++;
                _end2Attached = update.su;
                _t2 = t;
            }

            for (int i = 0; i < _updates.size() * 0; i++) {
                Updates up = _updates.get(i);
                if (up.newState != _ABDAssociated) {
                    throw new RuntimeException();
                }
                up.su._decorationTime = t;
//                _fimbrins.add(up.su);

                /*
                if (i != N) {

                    up.su.removeReaction(this, up.su._oldDecorationReaction);
                    if (up.su._oldDecorationReaction.size() > 0) {
                        up.su.peakAReaction(t, up.su._oldDecorationReaction);
                    } else {
                        up.su.resetReaction();
                    }
                }*/
            }
        }

        if (!severed && dissociated) {

//            _ltr.addTime(t, _end1Attached._t);
        }
        if (newState != state) {


            /* if (state > 0 && newState == 0) {

                _end1Attached._decorationTime = -1;
                _end1Attached = null;
                _t1 = -1;
                _end1FilamentIndex = _end2FilamentIndex = -1;
            }*/
            if (_end1Attached == null && _end2Attached != null) {
                _end1Attached = _end2Attached;

                _end2Attached = null;

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
                        throw new RuntimeException("Err:" + newState + "\t" + state);
                    }

                }

            } else if (_end2Attached == null) {

                if (newState != 0) {
                    throw new RuntimeException("ERROR");
                }
                if (_end1Attached != null || _end2Attached != null) {
                    throw new RuntimeException("Err");
                }

                _t1 = _t2 = -1;
            }

            state = newState;
        }

        if (state < 0 || state > 2) {
            throw new RuntimeException("ERR:" + state);
        }
        if (state == 2 && (_end1Attached == null || _end2Attached == null)) {
            throw new RuntimeException("ERR:" + state);
        }
        if (state == 2 && (_end1Attached._filament.ID == _end2Attached._filament.ID)) {
            Filament f1 = _end1Attached._filament;
            Filament f2 = _end2Attached._filament;
            System.out.flush();
            throw new RuntimeException("ERR:" + _end1Attached + "," + _end2Attached + ":" + f1._subunits.contains(_end1Attached)
                    + "\t" + f2._subunits.contains(_end2Attached));
        }

    }

    @Override
    public void remove(double t, SubUnit su) {

        su._undecorated = true;
        if (t >= 0) {

            if (!contained(su)) {
                addUpdate(_Dissociated, su, t);

                //_ltr.addTime(t, su._t);
            } else {
                throw new RuntimeException();
            }
        } else {
            //  System.out.println(":::SEVEER::");
            addUpdate(_SEVERED, su, t);

        }
        /*
                removeFromUpdates(su);
                su.removeListener(this);
                su._decorationTime = -1;
                
                if (state == 1) {
                    _end1Attached = null;
                    _end1FilamentIndex = -1;
                    _t1 = -1;
                    state = 0;
                } else if (state == 2) {
                    if (su == _end1Attached) {
                        _end1Attached = _end2Attached;
                        _end1FilamentIndex = _end2FilamentIndex;
                        _end2Attached = null;
                        _end2FilamentIndex = -1;
                    } else {
                        _end2Attached = null;
                        _end2FilamentIndex = -1;
                    }
                    state = 1;
                    _t2 = -1;
                }*/

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
        if (_end1Attached != null) {
            _end1Attached._decorationReaction.clear();
        }
        if (_end2Attached != null) {
            _end2Attached._decorationReaction.clear();
        }
        _end1Attached = _end2Attached = null;

        _updates.clear();
        _fimbrins.clear();
        
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
    public void severAlert(double t
    ) {

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

    @Override
    public void reactionCallBack(SubUnit su, double t, int tag) {
        if (su._filament == null) {
            throw new RuntimeException();
        }
        int filamentIndex = su._filament.ID;

        if (!su._filament._subunits.contains(su)) {
            throw new RuntimeException();
        }

        switch (tag) {
            case _ABDAssociated:
                if (state == 0 || state == 1) {
                    addUpdate(_ABDAssociated, su, t);

                }
                su._decorated = true;
                break;

            case _OTHERFIMBRINS:
                su._decorationTime = t;
                su._decorated = true;
                break;
            case _OtherProteinsAssociated:
                su._decorationTime = t;
                break;
            case -1:
//                addUpdate(su._filament == _f1 ? 0 : 1, _Dissociated, su);
//                break;

            case -2:
                throw new RuntimeException();

        }

    }

    @Override
    public void react(double t
    ) {
        applyUpdates(t);
        if (_end1Attached != null && _end1Attached == _end2Attached) {
            throw new RuntimeException("REP");
        }
        _updates.clear();

    }

    private void removeFromUpdates(SubUnit su) {
        for (int i = 0; i < _updates.size();) {
            if (_updates.get(i).su == su) {
                _updates.remove(i);
            } else {
                i++;

            }
        }
    }

    private Filament getFilamentByIndex(int ID) {
        for (Filament f : MainJFrame._filaments) {
            if (f.ID == ID) {
                return f;
            }
        }
        return null;
    }
}

class Updates {

    SubUnit su;
    int newState;
    double _t;

    public Updates(int state, SubUnit su, double t) {

        newState = state;
        this.su = su;
        _t = t;
    }
}

class ABDs {

    SubUnit _abd1, _abd2;
    double _t1, _t2;
    boolean _tagged = false;

    public ABDs(SubUnit abd1, SubUnit abd2) {
        _abd1 = abd1;
        _abd2 = abd2;
    }
}
