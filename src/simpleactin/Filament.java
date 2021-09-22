/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simpleactin;

import java.awt.Point;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;
import static simpleactin.Filament._STARTTIME;

/**
 *
 * @author sm2983
 */
public class Filament implements SubUnitListener, DecorationListener {

    private final boolean twoCofilin = true;
    boolean storeCapped = MainJFrame.ACP1P;

    public static double _TIME = 0.1, _STARTTIME = 1;
    double _capOnTime = -1;
    double _initTime = 0;
    double _sideOn, _sideOff;
    final int __ATP = 1, __ADPPI = 2, __ADP = 4, __COFILIN = 8, __SRV2 = 16, __CAP = 1024, __OTHERS = 256;

    public LinkedList<SubUnit> _subunits;
    double _atpR, _adppiR, _adppicoR, _depolySRV2;

    int totalADF, chunksize, distance, totalSRV;
    boolean _coffilinwithindist;
    boolean cappistagged = false;
    PolymerizationRate _barbed, _pointed;

    ReactionRate _coflin, _SRV2, _Cap;
//    private int _b, _p;
    public LifeTimeRecorder _ltr = null;
    double lst = -1;
    boolean capped = false;

    LinkedList<SeverStatus> _severingList = new LinkedList<>();
    LinkedList<Point> _changed = new LinkedList<>();
    DecorationReaction _capReaction = null;
    boolean isClone;
    private static int _ID = 0;
    public int ID;

    public Filament() {
        //      _b = _p = 0
        isClone = false;
        ID = _ID;
        _ID++;
        _subunits = new LinkedList<>();
    }

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
        _STARTTIME = 1;
        _sideOn = sideOn;
        _sideOff = sideOff;
    }

    public double age() {
        double t = 0;
        for (SubUnit su : _subunits) {
            t += su._t;
        }
        t /= (double) _subunits.size();
        return t;
    }

    public void update(double t) {

        updateSubunits(t);
        dynamic(_subunits, _pointed, false, t);
        if (!capped) {
            //_b += dynamic(_subunits, _barbed, true, t);
            dynamic(_subunits, _barbed, true, t);
            if (_subunits.size() >= 3) {
                SubUnit barbedEnd = _subunits.getLast();
                boolean b1 = (!barbedEnd._decorated) && Math.random() < _Cap.onRates[0];
                //boolean b1 =  Math.random() < _Cap.onRates[0];
                if (b1) {
                    double s = barbedEnd._state == __COFILIN ? 0.1 : 1;
                    barbedEnd._decorationReaction.add(new DecorationReaction(_Cap.onRates[0] * s, _Cap.offRate, __CAP, this));
                    //capon(t);
                }
                /*
                boolean b2 = _subunits.getLast()._decorated || (_ltr != null && Math.random() < _sideOn);
                if (b1 && b2) {
                    double ratio = _Cap.onRates[0] / (_Cap.onRates[0] + _sideOn);
                    b1 = Math.random() < ratio;
                    b2 = !b1;
                }
                if (b1) {
                    capon(t);
                }

                _subunits.getLast()._decorated = b2;
                 */
            }

        }

        double padpoff = _pointed.ADPoff;
        /*
        if (totalSRV > 0 && _pointed.ADPoff > 0) {
            _pointed.ADPoff = _depolySRV2;
        }*/
        //_p += dynamic(_subunits, _pointed, false, t);

        _pointed.ADPoff = padpoff;

        totalADF = 0;
        totalSRV = 0;
        int totalADF2 = 0;
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
        if (_capReaction != null) {
            if (totalADF2 > 0) {
                _capReaction.offrate = _Cap.offRate * 10;
            } else {
                _capReaction.offrate = _Cap.offRate;
            }
        }
        if (_subunits.size() < 2) {
            //        boolean sc = storeCapped;
            //          storeCapped = false;
            capoff(t);
//            storeCapped=sc;
            _initTime = t;
            for (SubUnit su : _subunits) {
                su.remove(t);
            }
            _subunits.clear();

            if (!isClone) {
                //_STARTTIME = t + 1;
                while (_subunits.size() < 5) {
                    dynamic(_subunits, _barbed, true, t);
                    dynamic(_subunits, _pointed, false, t);
                    for (SubUnit su : _subunits) {
                        su._record = false;
                    }
                }
            }

        }
        _coffilinwithindist = totalADF2 > 0;
        /*
        if (!_init && _subunits.size() >= _mfs) {
            _init = true;
            for (int i = 0; i < _subunits.size(); i++) {
                _subunits.get(i)._t = t;
            }
       
        }
         */

    }

    /*private void addTime(double offTime, double onTime) {
        double dt = (Math.ceil(_taggedOffTime * 10) - Math.ceil(_taggedOnTime * 10)) / 10;
        if (_taggedOnTime >= _initTime + _STARTTIME && dt <= 6000 && dt >= 0.0) {
            if (_lifeTimes != null) {
                _lifeTimes.add(dt);
            }
        }
    }*/
    private int sever(double t, int start) {
        if (true) {
            // keep one end

            //boolean pointed = false;
            boolean pointed = Math.random() < 0.5;
            if (pointed && start == 0 || !pointed && start == _subunits.size()) {
                pointed = !pointed;
            }
            //boolean pointed = Math.random()<start/(double)_subunits.size();
            int st = 0, ed = 0;
            if (pointed) {
                ed = start;
            } else {
                st = start;
                ed = _subunits.size();
            }
            return severOld(t, st, ed);
        } else {
            /*
            boolean pointed = Math.random()<start/_subunits.size();
            Filament f = new Filament();
            int ret = splits(start, f);
            //if (f._subunits.size() > 3) {
            if(pointed){
                MainJFrame._filaments.add(f);
                MainJFrame._filaments.remove(this);
                for(SubUnit su:_subunits){
                    su.remove(t);
                }
                _subunits.clear();
            }*/
            Filament f = new Filament();
            int ret = splits(start, f);
            if (f._subunits.size() > 3) {
                MainJFrame._filaments.add(f);
            }
            return ret;
        }
    }

    private int severOld(double t, int start, int end) {
        int size = _subunits.size();

        if (capped && end < size - 1) {
            boolean sc = storeCapped;
            //storeCapped = false;
            capoff(t);
            storeCapped = sc;
            /*if (storeCapped) {
                   if (_taggedOffTime > -1 && _taggedOnTime > -1) {
                addTime(_taggedOffTime, _taggedOnTime);
            }
                _taggedOffTime = _taggedOnTime = -1;
            }*/

        }
        int adf = 0;
        size = _subunits.size() - end;
        for (int i = 0; i < start; i++) {
            if (_subunits.getFirst()._state == __COFILIN) {
                adf++;
            }
            //if (_lifeTimes == null) {
            _subunits.getFirst().remove(t);
            //}
            _subunits.removeFirst();
        }
        if (size > _subunits.size()) {
            System.out.println("" + size + "\t" + _subunits.size() + "\t" + start + "\t" + end);
            throw new RuntimeException("SIZEEE");
        }
        for (int i = 0; i < size; i++) {
            if (_subunits.getLast()._state == __COFILIN) {
                adf++;
            }
            //if (_lifeTimes == null) {
            _subunits.getLast().remove(t);
            //}
            _subunits.removeLast();
        }
        /*
        if (_prot != null) {
            _prot.severAlert(t);
        }*/
        return adf;
    }

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
        /*
        System.out.print("r="+r+"\t i="+i+"\t");
        for(int j=0;j<_severingList.size();j++){
            System.out.print("\t"+(_severingList.get(j)/len));
        }
        System.out.println("");*/

        return i;

    }

    public void updateSubunits(double time) {
        double adfr;
        //int sever = -1;
        _severingList.clear();
        _changed.clear();

        //ThreadLocalRandom tlr=ThreadLocalRandom.current();
        double adppi = totalADF == 0 ? _adppiR : _adppicoR;

        for (int i = 0; i < _subunits.size(); i++) {
            SubUnit su = _subunits.get(i);
            switch (su._state) {
                case __ATP:

                    if (Math.random() < _atpR) {
                        // su._state = 2;
                        //_changed.add(new Point(i, totalSRV == 0 ? __ADPPI : __ADP));
                        //   _changed.add(new Point(i, __ADP));
                        _changed.add(new Point(i, __ADPPI));
                    }
                    break;
                case __ADPPI:
                    if (Math.random() < adppi) {

                        //   su._state = 2;
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
                        //adfr = _coflin.onRates[1];
                    }

                    if (Math.random() < adfr) {

                        _changed.add(new Point(i, __COFILIN));
                        //su._decorationReaction.add(new DecorationReaction(adfr, _coflin.offRate, __COFILIN, this));
                    }
                    break;
                case __COFILIN:

                    if (Math.random() < _coflin.offRate) {

                        //   changed.add(i);
                        _changed.add(new Point(i, __ADP));

                    }
                    boolean hasNeighbour = false;
                    //int j = _subunits.size() - 1 - i;
                    int j = i;
                    /*
                    if (j >1) {
                        hasNeighbour |= (_subunits.get(j - 1)._state == __COFILIN && _subunits.get(j - 2)._state != __COFILIN);
                    }*/
                    if (j > 0 && j < _subunits.size() - 1) {
                        //hasNeighbour = (_subunits.get(j-1)._state!= __COFILIN && _subunits.get(j + 1)._state == __COFILIN);
                        hasNeighbour = twoCofilin && (_subunits.get(j - 1)._state != __COFILIN) && _subunits.get(j + 1)._state == __COFILIN;
                        hasNeighbour |= !twoCofilin && (_subunits.get(j - 1)._state != __COFILIN);
                    }

                    if (hasNeighbour && Math.random() < _coflin.reactRate) {
                        //****sever = Math.max(sever, j);
                        addSevering(Math.min(j, _subunits.size() - 1), 0);

                    }
                    if (j > 0 && j < _subunits.size() - 1) {
                        //hasNeighbour = (_subunits.get(j-1)._state!= __COFILIN && _subunits.get(j + 1)._state == __COFILIN);
                        hasNeighbour = twoCofilin && _subunits.get(j - 1)._state == __COFILIN && _subunits.get(j + 1)._state != __COFILIN;
                        hasNeighbour |= !twoCofilin && (_subunits.get(j + 1)._state != __COFILIN);
                    }

                    if (hasNeighbour && Math.random() < _coflin.reactRate / 4) {
                        //****sever = Math.max(sever, j);
                        addSevering(Math.min(j, _subunits.size() - 1), 0);

                    }
                    break;
            }
            /*
            if(su._connectionStatus==0){
                if(Math.random()<Fimbrin._kon1){
                su._connectionStatus=1;
                }
            }else if(su._connectionStatus==1){
                boolean on=Math.random()<Fimbrin._kon2;
                boolean off=Math.random()<Fimbrin._k2;
                if(on&&off){
                    double a=Fimbrin._kon2/(Fimbrin._kon2+Fimbrin._k2);
                    if(Math.random()<a){
                        su._connectionStatus=2;
                    }else{
                        su._connectionStatus=0;
                    }
                }else if(on){
                    su._connectionStatus=2;
                }else if(off){
                    su._connectionStatus=0;
                }
                if(su._connectionStatus==2){
                    su._connectedTime=time;
                }
            }else{
                double offrate=Fimbrin.getOffRate(time-su._connectedTime);
                if(Math.random()<offrate){
                    su._connectedTime=-1;
                    if(Math.random()<offrate){
                        su._connectionStatus=0;
                    }else if(Math.random()<0.5){
                         su._connectionStatus=1;
                    }
                }
            }
             */
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
        /*
        if (barbed && addATP) {
            //  System.out.println(""+t);
            ps.println(t);
        }*/
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

    @Override
    public void remove(double t, SubUnit su) {

        if (t >= 0 && _ltr != null && !storeCapped && su._record) {
            _ltr.addTime(t, su._t);
        }
        if (_subunits.getLast() == su) {
            capoff(t);
        }
    }

    private void capon(double t) {

        if (storeCapped) {
            cappistagged = Math.random() < 1;
            if (cappistagged) {
                if (_capOnTime == -1) {
                    _capOnTime = t;
                }
            }
            /*
            if (cappistagged) {
                taggON(t);
            } else if (pcappistagged) {
                double dt = (Math.ceil(_taggedOffTime * 10) - Math.ceil(_taggedOnTime * 10)) / 10;
                if (_taggedOnTime >= _initTime + _STARTTIME && dt <= 6000 && dt >= 0.0) {
                    if (_lifeTimes != null) {
                        _lifeTimes.add(dt);
                    }
                }
                _taggedOffTime = _taggedOnTime = -1;
            }
             */
        }
        capped = true;

    }

    private void capoff(double t) {
        /*
        double dt = Math.ceil((t - _caponedTime) * 10 + Math.random() - 1) / 10;
        if (_init && capped && storeCapped && _caponedTime > -1 && dt >= 0.3) {
            
            _lifeTimes.add(dt);
            _initTime = t;
            _init = false;
            
        }*/
        _capReaction = null;
        if (capped && _ltr != null && storeCapped && _capOnTime != -1 && Math.random() < 0.05) {
            _ltr.addTime(t, _capOnTime);

        }
        if (capped && _subunits.size() > 0) {
            SubUnit last = _subunits.getLast();
            if (last._decoratedTag == __CAP) {
                last.resetReaction();
            } else {
                throw new RuntimeException("" + last._decoratedTag);
            }
        }
        cappistagged = false;
        _capOnTime = -1;
        capped = false;

    }

    public void decorateSubunits(double t) {
        for (SubUnit su : _subunits) {
            su.decorate(t);
        }
    }

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

    public int splits(int end, Filament f) {
        int ret = 0;
        f.setParams(_barbed, _pointed, _coflin, _SRV2, _Cap, _atpR, _adppiR, _adppicoR, _depolySRV2, distance, chunksize, _sideOn, _sideOff);
        f.isClone = true;
        f._ltr = _ltr;
        f.storeCapped = storeCapped;

        for (int i = 0; i < end && _subunits.size() > 0; i++) {
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

    public boolean severFilament(double t) {
        if (_severingList.size() > 0) {

            //int select = selectAChuck(0);
            //select = _severingList.size() - 1;
            //select=(int)(Math.random()*_severingList.size());
            //select = Math.random() < 0.5 ? 0 : 1;
//            select = 0;

            /*int sp = select > 0 ? _severingList.get(select - 1).location : 0;
            int ep = _severingList.get(select).location;
            if (sp >= 0) {
                sever(t, ep,ep);

                // System.out.println("INIT>>" + capped + "  " + _subunits.size());
            }
             */
            int offset = 0;
            //sever(t, _severingList.get(0).location - offset);

            for (int i = 0; i < _severingList.size(); i++) {
                sever(t, _severingList.get(i).location - offset);
                offset += _severingList.get(i).location;
            }
            _severingList.clear();
        }
        return isClone && _subunits.size() < 2;

    }
}

class SeverStatus {

    int location;
    int type;

    public SeverStatus(int l, int t) {
        location = l;
        type = t;
    }
}
