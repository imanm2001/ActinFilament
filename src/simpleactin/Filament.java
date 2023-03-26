/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simpleactin;

import java.awt.Point;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;
//import static simpleactin.Filament._STARTTIME;

/**
 *
 * @author sm2983 All times are in seconds
 */
public class Filament implements SubUnitListener, DecorationListener {

    private final int __DISCARD = 1, __POINTED = 2, __RANDOM5050 = 4, __RANDOMLD = 8, __DISCARDCONF = 0;
    /**
     * Run simulation for the case that at least two Cofilin is needed to sever
     * a filament
     */
    private final boolean twoCofilin = true;
    /**
     * Store residency time of the capping proteins
     */
    boolean storeCapped = MainJFrame.ACP1P;

    /**
     *
     */
    // public static double _TIME = 0.1,
    /**
     * StartTime for recording data
     */
    public static int _STARTTIME = 1;
    /**
     * Time that the filament was capped
     */
    private double _capOnTime = -1;
    /**
     * Time that th
     */
    private double _initTime = 0, _pIT = 0;
    private double _sideOn, _sideOff;
    /**
     * Reactions' enum
     */
    private final int __ATP = 1, __ADPPI = 2, __ADP = 4, __COFILIN = 8, __SRV2 = 16, __CAP = 1024, __OTHERS = 256;

    /**
     * A list of subunits
     */
    public LinkedList<SubUnit> _subunits;
    /**
     * reaction rates for ATP->ADP-PI and ADP-PI->ADP
     */
    double _atpR, _adppiR, _adppicoR, _depolySRV2;

    int totalADF, chunksize, distance, totalSRV;
    boolean _coffilinwithindist;
    /**
     * True if the capping protein is tagged
     */
    boolean cappistagged = false;

    /**
     * Store polymerization and depolymerization rates for ATP/ADP states
     */
    PolymerizationRate _barbed, _pointed;
    /**
     * Store per subunit reaction rates
     */
    ReactionRate _coflin, _SRV2, _Cap;

    /**
     * Recorde the residency time of the tagged protein
     */
    public LifeTimeRecorder _ltr = null;

    //double lst = -1;
    /**
     * Is filament capped
     */
    boolean _capped = false;
    /**
     * List of severing events
     */
    LinkedList<SeverStatus> _severingList = new LinkedList<>();
    /**
     * List of subunits that have changed status during the past time step
     */
    LinkedList<Point> _changed = new LinkedList<>();
    /**
     * Reaction for capping the filament
     */
    DecorationReaction _capReaction = null;
    /**
     * is the filament original or a clone
     */
    boolean isClone;
    /**
     * The next filament's ID
     */
    private static int _ID = 0;

    /**
     * ID of the filament
     */
    public int ID;
    //PrintStream _ps = null;
/*
    
     */

    /**
     * Create a new filament
     */
    public Filament() {
        //      _b = _p = 0
        isClone = false;
        ID = _ID;
        _ID++;
        _subunits = new LinkedList<>();

    }

    /**
     * This function updates the following reaction rates
     *
     * @param barbed De/Polymerization rates for the barbed end
     * @param pointed De/Polymerization rates for the pointed end
     * @param cofilin Cofilin reaction rates
     * @param SRV2
     * @param cap Capping reaction rates
     * @param patpR ATP hydrolysis reaction rate
     * @param padppir PI release reaction rate
     * @param padpico PI cooperative release reaction rate a
     * @param depolySRV2
     * @param pdistance
     * @param pchunksize
     * @param sideOn Binding rate for the side-binding proteins
     * @param sideOff Unbinding rate for the side-binding proteins
     */
    public void setParams(PolymerizationRate barbed, PolymerizationRate pointed,
            ReactionRate cofilin, ReactionRate SRV2, ReactionRate cap, double patpR, double padppir, double padpico,
            double depolySRV2, int pdistance, int pchunksize, double sideOn, double sideOff) {
        _atpR = patpR;
        _adppiR = padppir;
        _adppicoR = padpico;
        distance = pdistance;
        chunksize = pchunksize;
        _barbed = barbed;
        _pointed = pointed;
        _SRV2 = SRV2;
        _coflin = cofilin;
        _depolySRV2 = depolySRV2;
        _Cap = cap;
        // _STARTTIME = 1;
        _sideOn = sideOn;
        _sideOff = sideOff;
    }

    /**
     *
     * @return age of the filament which is the average age of its subunits
     */
    public double age() {
        double t = 0;
        for (SubUnit su : _subunits) {
            t += su._t;
        }
        t /= (double) _subunits.size();
        return t;
    }

    /**
     *
     * @param t is the step size
     */
    public void update(double t) {
        /*
        if(_ps==null){
            try {
            _ps = new PrintStream(String.format("C:\\Users\\sm2983\\Documents\\Projects\\Fimbin\\HeatMaps\\res2\\reviews\\filament_length\\FL_%03d.txt", ID));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        }*/
        //update state of all of the subunits
        updateSubunits(t);
        //de/polymerization for the pointed end
        dynamic(_subunits, _pointed, false, t);
        //if the filament is not capped call de/polymerization for the barbed end
        if (!_capped) {

            dynamic(_subunits, _barbed, true, t);
            //as long as the length of filament is greater than 2, call reaction for capping the the barbed end
            if (_subunits.size() >= 3) {
                SubUnit barbedEnd = _subunits.getLast();
                boolean b1 = (!barbedEnd._decorated) && Math.random() < _Cap.onRates[0];
                if (b1) {
                    double s = barbedEnd._state == __COFILIN ? 0.1 : 1;
                    barbedEnd._decorationReaction.add(new DecorationReaction(_Cap.onRates[0] * s, _Cap.offRate, __CAP, this));
                }
            }

        }

        double padpoff = _pointed.ADPoff;

        _pointed.ADPoff = padpoff;

        totalADF = 0;
        totalSRV = 0;
        int totalADF2 = 0;
        //count total number of subunits decorated with ADF/Cofilin
        for (int i = 0; i < _subunits.size(); i++) {
            SubUnit su = _subunits.get(i);
            if (su._state == __COFILIN) {
                totalADF++;
                if (i >= _subunits.size() - distance) {
                    totalADF2++;
                }
            }
            if ((su._state & __SRV2) == __SRV2 && i < distance) {
                totalSRV++;
            }
        }
        //Cooperative capping othe filament
        if (_capReaction != null) {
            if (totalADF2 > 0) {
                _capReaction.offrate = _Cap.offRate * 10;
            } else {
                _capReaction.offrate = _Cap.offRate;
            }
        }
        //if the length of the filament is less than two subunits, dissasemble everything

        if (_subunits.size() < 2) {

            capoff(t);
            _pIT = _initTime;
            _initTime = t;
            for (SubUnit su : _subunits) {
                su.remove(t);
            }
            _subunits.clear();

            if (!isClone) {
                while (_subunits.size() < 5) {
                    dynamic(_subunits, _barbed, true, t);
                    dynamic(_subunits, _pointed, false, t);
                    for (SubUnit su : _subunits) {
                        su._record = false;
                    }
                }
            }

        }
        //if the filament is decorated with at least subunit, triger the cooperative binding for all of the subunits
        _coffilinwithindist = totalADF2 > 0;
    }

    /**
     * sever the filament and keep one or both fragments
     *
     * @param t time
     * @param start cofilinated subunit
     */
    private int sever(double t, int start) {
        // keep one end
        if ((__DISCARDCONF & __DISCARD) > 0) {

            //Keep pointed end
            boolean keepPointed = false;
            if ((__DISCARDCONF & __POINTED) > 0) {
                keepPointed = true;
            } else if ((__DISCARDCONF & __RANDOM5050) > 0) {
                keepPointed = Math.random() < 0.5;

                if (keepPointed && start == 0 || !keepPointed && start == _subunits.size()) {
                    keepPointed = !keepPointed;
                }
            } else if ((__DISCARDCONF & __RANDOMLD) > 0) {

                keepPointed = Math.random() < start / (double) _subunits.size();
            }
            int st = 0, ed = 0;
            if (keepPointed) {
                ed = start;
            } else {
                st = start;
                ed = _subunits.size();
            }
            return severAndDiscard(t, st, ed);
        } else {
            ///keep both ends
            Filament f = new Filament();
            int ret = splits(start, f);
            if (f._subunits.size() > 3) {
                MainJFrame._filaments.add(f);
            }
            return ret;
        }
    }

    /**
     * sever the filament and keep one or both fragments
     *
     * @param t time
     * @param start discard start location
     * @param end discard end location
     */
    private int severAndDiscard(double t, int start, int end) {
        int size = _subunits.size();

        if (_capped && end < size - 1) {
            boolean sc = storeCapped;
            capoff(t);
            storeCapped = sc;

        }
        int adf = 0;
        size = _subunits.size() - end;
        for (int i = 0; i < start; i++) {
            if (_subunits.getFirst()._state == __COFILIN) {
                adf++;
            }
            
            _subunits.getFirst().remove(t);
            _subunits.removeFirst();
        }
        if (size > _subunits.size()) {
            System.out.println("" + size + "\t" + _subunits.size() + "\t" + start + "\t" + end);
            throw new RuntimeException("SIZE");
        }
        for (int i = 0; i < size; i++) {
            if (_subunits.getLast()._state == __COFILIN) {
                adf++;
            }
           
            _subunits.getLast().remove(t);
            _subunits.removeLast();
        }

        return adf;
    }
    /**
     * Add an severing event to the queue
     *
     * @param index the subunit index
     * @param type type of the severing event
     */
    private boolean addSevering(int index, int type) {
        int i = 0;

        while (i < _severingList.size()) {
            SeverStatus v = _severingList.get(i);
            if (v.location == index) {
                i = -1;
                break;
            } else if (v.location > index) {
                break;
            } else {
                i++;
            }
        }
        if (i > -1) {
            _severingList.add(i, new SeverStatus(index, type));
        }
        return i > -1;
    }

    /**
     * Between all of the severed fragments, choose one
     * @param offset
     * @return return the selected fragment
     */
    public int selectAChuck(int offset) {
        _severingList.add(new SeverStatus(_subunits.size() + offset, -1));

        int size = _severingList.size();
        double len = _subunits.size();
        double r = Math.random();
        double prop;
        int i = -1;
        do {
            i++;
            prop = Math.round(1000 * (_severingList.get(i).location - offset) / len) / 1000.0;
        } while (prop < r && i < size - 1);
        

        return i;

    }

    /**
     * update all of the subunits status after hydolysis, PI release, or side protein binding
     * @param time
     */
    public void updateSubunits(double time) {
        double adfr;
        
        _severingList.clear();
        _changed.clear();

        double adppi = totalADF == 0 ? _adppiR : _adppicoR;

        for (int i = 0; i < _subunits.size(); i++) {
            SubUnit su = _subunits.get(i);
            switch (su._state) {
                case __ATP:

                    if (Math.random() < _atpR) {
                        _changed.add(new Point(i, __ADPPI));
                    }
                    break;
                case __ADPPI:
                    if (Math.random() < adppi) {
                _changed.add(new Point(i, __ADP));
                    }
                    break;
                case __ADP:
                    adfr = _coflin.onRates[0];
                    if (totalADF > 0) {

                        if (i > 0 && _subunits.get(i - 1)._state == __COFILIN) {
                            adfr = _coflin.onRates[1];
                        }
                        if (i < _subunits.size() - 1 && _subunits.get(i + 1)._state == __COFILIN) {
                            adfr = _coflin.onRates[1];
                        }
                    }

                    if (Math.random() < adfr) {
                        _changed.add(new Point(i, __COFILIN));
                    }
                    break;
                case __COFILIN:

                    if (Math.random() < _coflin.offRate) {
                        _changed.add(new Point(i, __ADP));

                    }
                    boolean hasNeighbour = false;
                    int j = i;
                    
                    if (j > 0 && j < _subunits.size() - 1) {
                        hasNeighbour = twoCofilin && (_subunits.get(j - 1)._state != __COFILIN) && _subunits.get(j + 1)._state == __COFILIN;
                        hasNeighbour |= !twoCofilin && (_subunits.get(j - 1)._state != __COFILIN);
                    }

                    if (hasNeighbour && Math.random() < _coflin.reactRate) {
                        addSevering(Math.min(j, _subunits.size() - 1), 0);

                    }
                    if (j > 0 && j < _subunits.size() - 1) {
                        hasNeighbour = twoCofilin && _subunits.get(j - 1)._state == __COFILIN && _subunits.get(j + 1)._state != __COFILIN;
                        hasNeighbour |= !twoCofilin && (_subunits.get(j + 1)._state != __COFILIN);
                    }

                    if (hasNeighbour && Math.random() < _coflin.reactRate / 4) {
                        addSevering(Math.min(j, _subunits.size() - 1), 0);

                    }
                    break;
            }
           
            if (su._decorated) {
                su._undecorated |= su._decoratedOffrate > 0 && Math.random() < su._decoratedOffrate;

            } else {
                if (Math.random() < _sideOn) {

                    su._decorationReaction.add(new DecorationReaction(_sideOn, _sideOff, __OTHERS, this));
                }
            }
        }

        for (Point p : _changed) {
            _subunits.get(p.x)._state = p.y;
        }

    }

    private int dynamic(LinkedList<SubUnit> _subunits, PolymerizationRate rates, boolean barbed, double t) {
        double c = 1;
        if (!_subunits.isEmpty()) {
            if ((barbed && (_subunits.getLast()._state & __COFILIN) > 0) || (!barbed && (_subunits.getFirst()._state & __COFILIN) > 0)) {
                c = 0;
            }
        }
        boolean addATP = Math.random() < rates.ATPon * c;
        boolean addADP = Math.random() < rates.ADPon * c;
        int ret = 0;

        double poff = 0;
        if (!_subunits.isEmpty()) {
            if (barbed) {
                poff = _subunits.getLast()._state == __ATP ? rates.ATPoff : rates.ADPoff;
            } else {
                poff = _subunits.getFirst()._state == __ATP ? rates.ATPoff : rates.ADPoff;
            }
        }
        boolean remove = !_subunits.isEmpty() && Math.random() < poff;

        if (remove && (addADP || addATP)) {
            double denum = poff, onnum = 0;
            if (addATP) {
                denum += rates.ATPon;
                onnum += rates.ATPon;
            }
            if (addADP) {
                denum += rates.ADPon;
                onnum += rates.ADPon;
            }
            if (Math.random() < poff / (denum)) {
                addADP = addATP = false;
            } else {
                remove = false;
            }
        }
        if (remove) {
            if (barbed) {
                _subunits.getLast().remove(t);
                _subunits.removeLast();
            } else {
                _subunits.getFirst().remove(t);
                _subunits.removeFirst();
            }
            ret = -1;
        }

        int index = barbed ? _subunits.size() : 0;
        
        if (addATP && addADP) {
            addATP = Math.random() < rates.ATPon / (rates.ATPon + rates.ADPon);
            addADP = !addATP;
        }
        SubUnit newSu = null;
        if (addADP) {
            newSu = new SubUnit(__ADP, barbed, t, this, this);
            _subunits.add(index, newSu);
            ret++;
        }
        if (addATP) {
            newSu = new SubUnit(__ATP, barbed, t, this, this);
            _subunits.add(index, newSu);
            ret++;
        }

        return ret;
    }

    /**
     * remove a subunit
     * @param t time 
     * @param su subunit to be removed
     */
    @Override
    public void remove(double t, SubUnit su) {

        if (t >= 0 && _ltr != null && !storeCapped && su._record) {
            _ltr.addTime(t, su._t);
        }
        
        if (_subunits.getLast() == su) {
            capoff(t);
        }
    }
    /**
     * Cap the filament
     * @param t time 
     */
    private void capon(double t) {

        if (storeCapped) {
            cappistagged = Math.random() < 1;
            if (cappistagged) {
                if (_capOnTime == -1) {
                    _capOnTime = t;
                }
            }
           
        }
        _capped = true;

    }
    /**
     * Uncap the filament
     * @param t time 
     */
    private void capoff(double t) {
   
        _capReaction = null;
        if (_capped && _ltr != null && storeCapped && _capOnTime != -1 && Math.random() < 0.05) {
            _ltr.addTime(t, _capOnTime);

        }
        if (_capped && _subunits.size() > 0) {
            SubUnit last = _subunits.getLast();
            if (last._decoratedTag == __CAP) {
                last.resetReaction();
            } else {
                throw new RuntimeException("" + last._decoratedTag);
            }
        }
        cappistagged = false;
        _capOnTime = -1;
        _capped = false;

    }

    /**
     * Decorate all of the subunits after binding a protein to them
     * @param t
     */
    public void decorateSubunits(double t) {
        for (SubUnit su : _subunits) {
            su.decorate(t);
        }
    }

    /**
     * Call back after a reaction is triggered
     * @param su subunit
     * @param t time
     * @param tag protein type
     * @param data generic data
     */
    @Override
    public void reactionCallBack(SubUnit su, double t, int tag, Object data) {
        switch (tag) {
            case __COFILIN:
                su._state = __COFILIN;
                break;
            case -__COFILIN:
                su._state = __ADP;
                break;
            case __CAP:
                for (DecorationReaction dr : su._decorationReaction) {
                    if (dr.tag == __CAP) {
                        _capReaction = dr;
                        break;
                    }
                }
                capon(t);
                break;
            case -__CAP:
                capoff(t);
                break;
            case __OTHERS:
                break;
            case -__OTHERS:
                break;

        }

    }

    /**
     * Split a filament into two fragments
     * @param loc new barbed end location
     * @param f new fragment
     * @return number of cofilinated subunits in the new filament
     */
    public int splits(int loc, Filament f) {
        int ret = 0;
        f.setParams(_barbed, _pointed, _coflin, _SRV2, _Cap, _atpR, _adppiR, _adppicoR, _depolySRV2, distance, chunksize, _sideOn, _sideOff);
        f.isClone = true;
        f._ltr = _ltr;
        f.storeCapped = storeCapped;

        for (int i = 0; i < loc && _subunits.size() > 0; i++) {
            SubUnit su = _subunits.pop();
            f._subunits.addLast(su);
            if (su._state == __COFILIN) {
                ret++;
            }
            su._filament = f;
            su.removeListener(this);
            su.addListener(f);
        }
        return ret;
    }

    /** sever the filament
     *
     * @param t time
     * @return
     */
    public boolean severFilament(double t) {
        if (_severingList.size() > 0) {

         
            int offset = 0;
         
            for (int i = 0; i < _severingList.size(); i++) {
                sever(t, _severingList.get(i).location - offset);
                offset += _severingList.get(i).location;
            }
            _severingList.clear();
        }
        return isClone && _subunits.size() < 2;

    }
}
// Information regarding the severing event
class SeverStatus {

    int location;
    int type;

    public SeverStatus(int l, int t) {
        location = l;
        type = t;
    }
}
