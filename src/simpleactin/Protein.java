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
public class Protein implements SubUnitListener, ProteinI, DecorationListener {

    final int __DECORATE = 64;
    final double KBT = 4.114;
    final static int GSCALE = 16;

    boolean _forceDetach = false;
    private LinkedList<SubUnit> _available = new LinkedList<>();

    /*
    0:No connection
    1:Connected to filament 1
    2:Connected to filament 2
     */
    private double _koff1, _kon1;

    private static GammaDistribution GammaDists[] = new GammaDistribution[140 * GSCALE];
    private LinkedList<SubUnit> _end1Attached = new LinkedList<>(), _detachEnd1 = new LinkedList<>();
    private boolean _end1CanAttach = true;
    double bt = -1, _nextAttachTime;
    public double decoration = 0.01;
    private LifeTimeRecorder _ltr = null;

    public Protein(double koff1, double kon1, LifeTimeRecorder ltr) {

        _koff1 = koff1;
        _kon1 = kon1;

        _ltr = ltr;

    }

    public static void setDists(double Sh, double ShA, double Sc, double ScA) {
        for (int j = 0; j < GammaDists.length; j++) {
            int i = j / GSCALE < 5 ? j : 5 * GSCALE;
            GammaDists[j] = new GammaDistribution(Sh + ShA * i / (double) GSCALE, Sc + ScA * i / (double) GSCALE);
        }
    }

    private void detach(double t) {

        for (SubUnit s : _detachEnd1) {

            if (Math.random() < 0.01) {
                _ltr.addTime(t, s._decorationTime);
                /*
                if (s._decorationTime == -1) {
                    throw new RuntimeException("ERROR");
                }
                
                
                if (s._decorationTime == -1) {
                    throw new RuntimeException();
                }
                 */

            }
            s._decorationTime = -1;
            s._undecorated = true;
            s.removeListener(this);

            _end1Attached.remove(s);
        }

        _detachEnd1.clear();
    }

    public static double getFrames(double t1, double t2) {
        return (Math.floor(t1 * 10) - Math.floor(t2 * 10)) / 10.0;
    }

    @Override
    public boolean update(double t) {
        for (Filament f : MainJFrame._filaments) {
            if (f._subunits.size() > 0) {
                for (SubUnit s : f._subunits) {
                    if (!s._decorated) {

                        boolean b1 = Math.random() < _kon1;
                        if (_end1Attached.contains(s)) {
                            throw new RuntimeException("AGAIN" + s._id + "\t" + _end1Attached.get(_end1Attached.indexOf(s))._id);
                        }
                        if (b1) {
                            if (decorationExists(s)) {
                                throw new RuntimeException();
                            }
                            s._decorationReaction.add(new DecorationReaction(_kon1, _koff1, __DECORATE, this));
                        }
                    }
                    /*else {
                        if (!(s == s._filament._subunits.getLast() && s._decoratedTag == 1024)) {
                            throw new RuntimeException();
                        }
                    }*/
                }

            }
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
        su._undecorated = true;
        if (t >= 0) {
            if (!_detachEnd1.contains(su)) {
                _detachEnd1.add(su);
            }
        } else {

            _end1Attached.remove(su);
            /*
            if (_end1Attached.size() == 0) {
                if (_t > Filament._STARTTIME && _detached != -1 && _f1._subunits.size() < 3) {
                    _forceDetach = true;
                } else {
                    reset();
                }

            }*/

        }
        su.removeListener(this);
    }

    @Override
    public double getTime() {
        return 0;
    }

    @Override
    public void reset() {

        _forceDetach = false;
        _detachEnd1.clear();

        for (SubUnit s : _end1Attached) {
            s._decorationReaction.clear();
            s.removeListener(this);
        }
        _end1Attached.clear();
    }

    @Override
    public double getDetachedTime() {
        return -1;
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

    @Override
    public void reactionCallBack(SubUnit su, double t, int tag, Object data) {

        if (tag == __DECORATE) {

            su.addListener(this);

            if (!_end1Attached.contains(su)) {

                _end1Attached.add(su);

            } else {
                throw new RuntimeException("Subunit was already added:" + su._decorated + "\t" + _end1Attached.size() + "\t" + _end1Attached.indexOf(su));
            }

        } else if (tag == -__DECORATE) {
            {

                if (!_detachEnd1.contains(su)) {
                    _detachEnd1.add(su);

                } else if (!_end1Attached.contains(su)) {
                    throw new RuntimeException();
                } else {
                    _end1Attached.remove(su);
                    su.removeListener(this);
                }
            }
        }
    }

    private boolean decorationExists(SubUnit su) {
        for (DecorationReaction dr : su._decorationReaction) {
            if (dr.callback == this) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void react(double t) {
        if (_detachEnd1.size() > 0) {
            detach(t);
        }
    }
}
