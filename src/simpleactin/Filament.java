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
public class Filament implements SubUnitListener {

    ProteinI _prot;
    boolean storeCapped = MainJFrame.ACP1P;

    public static double _TIME = 0.1, _STARTTIME = 1;
    double _capOnTime = -1;
    double _initTime = 0;
    double _sideOn, _sideOff;
    final int __ATP = 1, __ADPPI = 2, __ADP = 4, __COFILIN = 8, __SRV2 = 16;

    public LinkedList<SubUnit> _subunits = new LinkedList<>();
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

    public Filament() {
        //      _b = _p = 0;

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

    public void update(double t) {
        int size = _subunits.size();
        updateSubunits(0, _subunits.size(), t);
        dynamic(_subunits, _pointed, false, t);
        if (!capped) {
            //_b += dynamic(_subunits, _barbed, true, t);
            dynamic(_subunits, _barbed, true, t);
            if (_subunits.size() >= 3) {
                boolean b1 = (!_subunits.getLast()._decorated) && Math.random() < _Cap.onRates[0];
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

            }

        } else if (Math.random() < _Cap.offRate) {
            capoff(t);

        }

        double padpoff = _pointed.ADPoff;
        /*
        if (totalSRV > 0 && _pointed.ADPoff > 0) {
            _pointed.ADPoff = _depolySRV2;
        }*/
        //_p += dynamic(_subunits, _pointed, false, t);

        _pointed.ADPoff = padpoff;

        if (_severingList.size() > 0) {

            int select = selectAChuck(0);
            select = _severingList.size() - 1;
            //select=(int)(Math.random()*_severingList.size());
            //select = Math.random() < 0.5 ? 0 : 1;
            //select = 0;

            int sp = select > 0 ? _severingList.get(select - 1).location : 0;
            int ep = _severingList.get(select).location;
            if (sp >= 0) {
                sever(t, sp, ep);

                // System.out.println("INIT>>" + capped + "  " + _subunits.size());
            }

        }

        totalADF = 0;
        totalSRV = 0;
        int totalADF2 = 0;
        for (int i = 0; i < _subunits.size(); i++) {
            SubUnit su = _subunits.get(i);
            if (su._state == __COFILIN) {
                totalADF++;
                if (i < 1000) {
                    totalADF2++;
                }
            }
            if ((su._state & __SRV2) == __SRV2 && i < distance) {
                totalSRV++;
            }
        }

        if (_subunits.size() < 4) {
            boolean sc = storeCapped;
            //    storeCapped = false;
            capoff(t);
            for (SubUnit su : _subunits) {
                su.remove(t);
            }
            _subunits.clear();
            _STARTTIME = t + 1;
            while (_subunits.size() < 4) {
                dynamic(_subunits, _barbed, true, t);
                dynamic(_subunits, _pointed, false, t);
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
    private int sever(double t, int start, int end) {
        int size = _subunits.size();

        if (capped && end < size) {
            boolean sc = storeCapped;
            storeCapped = false;
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

        if (_prot != null) {
            _prot.severAlert(t);
        }
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
            prop = Math.round(1000 * (_severingList.get(i).location - offset) / len) / 1000;
        } while (prop < r && i < size - 1);
        /*
        System.out.print("r="+r+"\t i="+i+"\t");
        for(int j=0;j<_severingList.size();j++){
            System.out.print("\t"+(_severingList.get(j)/len));
        }
        System.out.println("");*/

        return i;

    }

    public void updateSubunits(int start, int end, double time) {
        double adfr;
        //int sever = -1;
        _severingList.clear();
        _changed.clear();

        //ThreadLocalRandom tlr=ThreadLocalRandom.current();
        double adppi = totalADF == 0 ? _adppiR : _adppicoR;

        for (int i = start; i < end; i++) {
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
                        /*
                        if (i > 0 && _subunits.get(i - 1)._state == __COFILIN) {
                            adfr = _coflin.onRates[1];
                        }
                        if (i < _subunits.size() - 1 && _subunits.get(i + 1)._state == __COFILIN) {
                            adfr = _coflin.onRates[1];
                        }*/
                        adfr = _coflin.onRates[1];
                    }

                    if (Math.random() < adfr) {

                        _changed.add(new Point(i, __COFILIN));
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
                    if (j > 0) {
                        hasNeighbour |= _subunits.get(j - 1)._state == __COFILIN;
                    }
                    if (j < _subunits.size() - 1) {
                        hasNeighbour |= _subunits.get(j + 1)._state == __COFILIN;
                    }
                    if (hasNeighbour && Math.random() < _coflin.reactRate) {
                        //****sever = Math.max(sever, j);
                        addSevering(Math.min(j, _subunits.size()), 0);

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
        }

        for (Point p : _changed) {
            _subunits.get(p.x)._state = p.y;
        }

    }

    private int dynamic(LinkedList<SubUnit> _subunits, PolymerizationRate rates, boolean barbed, double t) {
        boolean addATP = Math.random() < rates.ATPon;
        boolean addADP = Math.random() < rates.ADPon;
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
            newSu = new SubUnit(__ADP, barbed, t, this);
            _subunits.add(index, newSu);
            ret++;
        }
        if (addATP) {
            newSu = new SubUnit(__ATP, barbed, t, this);
            _subunits.add(index, newSu);
            ret++;
        }

        return ret;
    }

    @Override
    public void remove(double t, SubUnit su) {

        if (t != -1 && _ltr != null && !storeCapped && su._record) {
            _ltr.addTime(t, su._t);
        }
        if (capped && _subunits.size() < 1) {
            //     capoff(t);
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

        if (capped && _ltr != null && storeCapped && _capOnTime != -1) {
            _ltr.addTime(t, _capOnTime);

        }
        cappistagged = false;
        _capOnTime = -1;
        capped = false;

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
