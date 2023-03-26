/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simpleactin;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import org.apache.commons.math3.distribution.GammaDistribution;

/*
 *
 * @author sm2983
 */
public class Fimbrin implements SubUnitListener, ProteinI, DecorationListener {

    final static int _NewABDAssociated = 1, _Associated = 2, _Dissociated = 4, _SEVERED = 8;
    final static double KBT = 4.114;
    final static double GSCALE = 20;

    /*
    0:No connection
    1:Connected to filament 1
    2:Connected to filament 2
     */
    //double _t1 = -1, _t2 = -1;
    public double _thermalFluctions, _k1off, _k2off, _k1on, _k2on;

    public double _UCA;
    private static GammaDistribution GammaDists[] = new GammaDistribution[(int) (140 * GSCALE)];
   
    private LifeTimeRecorder _ltr = null;
    private LinkedList<Updates> _updates;
    private LinkedList<ABDs> _fimbrins = new LinkedList<>();

    public Fimbrin(double thermalFluctions, WaitingTime waitingTime, LifeTimeRecorder ltr) {

        _thermalFluctions = thermalFluctions;

        _ltr = ltr;
        _updates = new LinkedList<>();
    }

    public double getOffRate(double t) {
        int index = Math.min(GammaDists.length - 1, (int) (t * GSCALE));
        double factor = _k2off * Math.exp((GammaDists[index].sample() / KBT));
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
            double i = j / GSCALE;
            GammaDists[j] = new GammaDistribution(Sh + ShA * i, Sc + ScA * i);
        }
    }

 
    private void addUpdate(int state, SubUnit su, ABDs abd, double t) {
        if (!contained(su)) {
            if (abd != null) {
                _updates.add(new Updates(state, su, abd, t));
            } else {
                throw new RuntimeException(" " + abd);
            }
        }
    }

    private void attachToFilament(Filament f, double t) {
        boolean found = false;
        double onrate = -1;
        double offrate = -1;
        if (t > Filament._STARTTIME) {
            onrate = _k1on;
            offrate = -1;
            
        }

        for (int i = 0; i < f._subunits.size(); i++) {
            SubUnit su = f._subunits.get(i);
            if (!su._decorated) {
               
                if (Math.random() < _k1on) {

                    su._decorationReaction.add(new DecorationReaction(_k1on, 0, _NewABDAssociated, this));

                }

                for (ABDs ab : _fimbrins) {
                    ab.arrange();
                    if (ab._state == 1 && ab._abd1._filament != f && Math.random() < _k2on) {

                        su._decorationReaction.add(new DecorationReaction(_k2on, 0, _Associated, this, ab));
                    }
                }

       
            }

        }

    }

    @Override
    public boolean update(double t) {

        for (Filament f : MainJFrame._filaments) {
            attachToFilament(f, t);
        }
        for (ABDs abd : _fimbrins) {
            abd.arrange();
            int numFound = 0;
            if (abd._state == 2) {
                if (contained(abd._abd1)) {
                    numFound++;
                }
                if (contained(abd._abd2)) {
                    numFound++;
                }
                Filament f1 = abd._abd1._filament;
                Filament f2 = abd._abd2._filament;
                if (f1._subunits.contains(abd._abd1)) {
                    numFound++;
                }
                if (f2._subunits.contains(abd._abd2)) {
                    numFound++;
                }
                if (numFound != 2) {

                    throw new RuntimeException(abd._abd1 + "\t" + abd._abd1 + "\t("
                            + contained(abd._abd1) + "," + abd._abd1._filament._subunits.contains(abd._abd1)
                            + ")\t(" + contained(abd._abd2) + "," + abd._abd2._filament._subunits.contains(abd._abd2)
                            + ")\t" + numFound);

                }
                if (abd._t2 <= 0) {
                    throw new RuntimeException();
                }
                if (Math.random() < getOffRate(t - abd._t2)) {

                    addUpdate(_Dissociated, abd._abd1, abd, t);

                }
                if (Math.random() < getOffRate(t - abd._t2)) {
                    addUpdate(_Dissociated, abd._abd2, abd, t);
                }

            } else if (abd._state == 1 && Math.random() < _k1off) {
                abd.arrange();
                addUpdate(_Dissociated, abd._abd1, abd, t);

            }
        }

        return true;
    }

    private void applyUpdates(double t) {

        boolean severed = false;
        boolean dissociated = false;
        double dissociateTime = 0;
        for (int i = 0; i < _updates.size();) {

            Updates update = _updates.get(i);

            if (update.newState == _Dissociated || update.newState == _SEVERED) {
                if (update.su == update._abd._abd1 || update.su == update._abd._abd2) {
                    if (update._abd._newState == 2) {
                        if (update._abd._crosslinkingSTime == -1) {
                            throw new RuntimeException();
                        }
                        update._abd._crossLinkingTTime += t - update._abd._crosslinkingSTime;
                        update._abd._crosslinkingSTime = -1;
                    }
                    update._abd._newState--;

                    if (update.newState == _Dissociated) {

                        //recored = true;
                    }
                    if (update.su == update._abd._abd1) {

                        update._abd._abd1 = null;

                    }
                    if (update.su == update._abd._abd2) {
                        update._abd._abd2 = null;

                    }
                    update.su._decorationTime = -1;
                }
                update.su.removeListener(this);
                _updates.remove(i);

            } else {
                i++;
            }
        }

        if (_updates.size() > 0) {
            HashMap hmap = splitTheReactionToTheirCorrespondingFimbrins();

            for (Object obj : hmap.keySet()) {
                ABDs abd = (ABDs) obj;
                LinkedList<Updates> updates = (LinkedList<Updates>) hmap.get(obj);
                int N = (int) Math.floor(updates.size() * Math.random());
                Updates update = updates.get(N);
                update.su.addListener(this);
                abd.arrange();

                abd._newState++;

                abd._abd2 = update.su;
                if (abd._newState == 2) {
                    abd._t2 = t;
                    abd._crosslinkingSTime = t;
                } else {

                }

                for (int i = 0; i < updates.size(); i++) {
                    Updates up = updates.get(i);
                    if (up.newState != _Associated) {
                        throw new RuntimeException();
                    }
                    up.su._decorationTime = t;


                    if (i != N) {
                        up.su.removeReaction(this, up.su._oldDecorationReaction);
                        if (up.su._oldDecorationReaction.size() > 0) {
                            up.su.peakAReaction(t, up.su._oldDecorationReaction);
                        } else {
                            up.su.resetReaction();
                        }
                    }
                }

            }
        }
        LinkedList<ABDs> removeList = new LinkedList<>();
        for (ABDs abd : _fimbrins) {
            if (abd._newState != abd._state) {
                abd.arrange();

          
                if (abd._abd1 != null) {
                    if (abd._abd2 != null) {
                        if (abd._newState != 2) {
                            throw new RuntimeException("Err");
                        }

                        if (abd._t2 == -1) {
                            abd._t2 = t;
                            abd._crosslinkingSTime = t;
                        }
                        
                    } else {
                        abd._t2 = -1;
                        if (abd._newState != 1) {
                            throw new RuntimeException("Err:" + abd._newState + "\t" + abd._state);
                        }

                    }

                } else if (abd._abd2 == null) {

                    if (abd._newState != 0) {
                        throw new RuntimeException("ERROR");
                    }
                    if (abd._abd1 != null || abd._abd2 != null) {
                        throw new RuntimeException("Err");
                    }

                    abd._t2 = -1;
                }

                abd._state = abd._newState;
                if (abd._newState == 0) {
                
                    if (Math.random() < 0.05) {
               
                        if (abd._crosslinkingSTime != -1) {
                            abd._crossLinkingTTime += t - abd._crosslinkingSTime;
                            
                        }
                   
                        _ltr.addTime(t, abd._t1);
                    }
                    abd._t1 = -1;
                    removeList.add(abd);
                }
            }

            if (abd._state < 0 || abd._state > 2) {
                throw new RuntimeException("ERR:" + abd._state);
            }
            if (abd._state == 2 && (abd._abd1 == null || abd._abd2 == null)) {
                throw new RuntimeException("ERR:" + abd._state);
            }
            if (abd._state == 2 && (abd._abd1._filament == abd._abd2._filament)) {
                Filament f1 = abd._abd1._filament;
                Filament f2 = abd._abd2._filament;
                System.out.flush();
                throw new RuntimeException("ERR:" + abd._abd1 + "," + abd._abd2 + ":" + f1._subunits.contains(abd._abd1)
                        + "\t" + f2._subunits.contains(abd._abd2));
            }
        }
        _fimbrins.remove(removeList);
    }

    public ABDs getTheCorrespondingFimbrin(SubUnit su) {
        ABDs ret = null;
        for (ABDs abd : _fimbrins) {
            if (abd._abd1 == su || abd._abd2 == su) {
                if (ret == null) {
                    ret = abd;
                } else {
                    throw new RuntimeException();
                }
            }
        }
        return ret;
    }

    @Override
    public void remove(double t, SubUnit su) {

        su._undecorated = true;
        if (t >= 0) {

            if (!contained(su)) {
                addUpdate(_Dissociated, su, getTheCorrespondingFimbrin(su), t);

            } else {
                throw new RuntimeException();
            }
        } else {
      
            addUpdate(_SEVERED, su, getTheCorrespondingFimbrin(su), t);

        }
        
    }

    @Override
    public double getTime() {
        return -1;
    }

    @Override
    public void reset() {
        for (ABDs abd : _fimbrins) {
            if (abd._abd1 != null) {
                abd._abd1._decorationReaction.clear();
                abd._abd1.removeListener(this);
            }
            if (abd._abd2 != null) {
                abd._abd1.removeListener(this);
                abd._abd2._decorationReaction.clear();
            }
        }

        _updates.clear();
        _fimbrins.clear();

        
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
    public void reactionCallBack(SubUnit su, double t, int tag, Object data) {
        if (su._filament == null) {
            throw new RuntimeException();
        }
        int filamentIndex = su._filament.ID;

        if (!su._filament._subunits.contains(su)) {
            throw new RuntimeException();
        }

        switch (tag) {
            case _NewABDAssociated:
                //    addUpdate(_NewABDAssociated, su, t);
                _fimbrins.add(new ABDs(su, null, t));
                su._decorated = true;
                su._decorationTime = t;
                su.addListener(this);
                break;

            case _Associated:
                ABDs abd = (ABDs) data;
                abd.arrange();

                addUpdate(_Associated, su, abd, t);
                su._decorationTime = t;
                su._decorated = true;
                break;

            case -1:

            case -2:
                throw new RuntimeException();

        }

    }

    @Override
    public void react(double t) {
        applyUpdates(t);
        for (ABDs abd : _fimbrins) {
            if (abd._abd1 != null && abd._abd1 == abd._abd2) {
                throw new RuntimeException("REP");
            }
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

    private HashMap splitTheReactionToTheirCorrespondingFimbrins() {
        HashMap<ABDs, LinkedList<Updates>> ret = new HashMap<>();
        for (Updates up : _updates) {
            LinkedList<Updates> updates = null;
            if (!ret.containsKey(up._abd)) {
                updates = new LinkedList<>();
                ret.put(up._abd, updates);
            } else {
                updates = ret.get(up._abd);
            }
            updates.add(up);
        }
        return ret;
    }
}

class Updates {

    ABDs _abd;
    SubUnit su;
    int newState;
    double _t;

    public Updates(int state, SubUnit su, ABDs abd, double t) {

        newState = state;
        this.su = su;
        _t = t;
        _abd = abd;
    }
}

class ABDs {

    double _crosslinkingSTime = -1, _crossLinkingTTime = 0;
    SubUnit _abd1, _abd2;
    double _t1, _t2;
    boolean _tagged = false;
    int _state, _newState = 0;

    public ABDs(SubUnit abd1, SubUnit abd2, double t) {
        _abd1 = abd1;
        _abd2 = abd2;
        _t1 = t;
        _t2 = -1;
        arrange();
        if (abd1 != null && _abd2 != null) {
            _state = 2;
        } else if (_abd1 != null && _abd2 == null) {
            _state = 1;
        } else {
            _state = 0;
        }
        _newState = _state;
    }

    public void arrange() {
        if (_abd1 == null && _abd2 != null) {
            _abd1 = _abd2;
            _abd2 = null;
        }
    }

}
