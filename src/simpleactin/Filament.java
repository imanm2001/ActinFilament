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

/**
 *
 * @author sm2983
 */
public class Filament implements SubUnitListener {

    final int __ATP = 1, __ADPPI = 2, __ADP = 4, __COFILIN = 8, __SRV2 = 16;
    int i = 0;
    public LinkedList<SubUnit> _subunits = new LinkedList<>();
    double _atpR, _adppiR, _adppicoR, _depolySRV2;
    int totalADF, chunksize, distance, totalSRV;
    boolean _coffilinwithindist;
    PolymerizationRate _barbed, _pointed;
    ReactionRate _coflin, _SRV2;
    private int _b, _p;
    public LinkedList<Double> _lifeTimes = null;
    double lst = -1;

    public Filament() {
        _b = _p = 0;
    }

    public void setParams(PolymerizationRate barbed, PolymerizationRate pointed,
            ReactionRate cofilin, ReactionRate SRV2, double patpR, double padppir, double padpico,
            double depolySRV2, int pdistance, int pchunksize) {
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
    }

    public void update(double t) {

        _b += dynamic(_subunits, _barbed, true, t);
        double padpoff = _pointed.ADPoff;
        if (totalSRV > 0&&_pointed.ADPoff>0) {
            _pointed.ADPoff = _depolySRV2;
        }
        _p += dynamic(_subunits, _pointed, false, t);
        _pointed.ADPoff = padpoff;
        int sever = -1;

        sever = updateSubunits(0, _subunits.size());

        if (sever != -1) {
            sever(_subunits, sever, t);
            lst = t;
            _p -= sever;
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
        _coffilinwithindist = totalADF2 > 1;
    }

    private static int sever(LinkedList<SubUnit> _subunits, int c, double t) {
        int adf = 0;
        for (int i = 0; i <= c; i++) {
            if (_subunits.getFirst()._state == 3) {
                adf++;
            }
            _subunits.getFirst().remove(t);
            _subunits.removeFirst();
        }
        return adf;
    }

    public int updateSubunits(int start, int end) {
        double adfr;
        int sever = -1;
        LinkedList<Point> changed = new LinkedList<>();
        //ThreadLocalRandom tlr=ThreadLocalRandom.current();
        double adppi = totalADF == 0 ? _adppiR : _adppicoR;

        if (totalSRV > 0) {
            adppi *= 1000000;
        }
        for (int i = start; i < end && (_coflin.onRates[0] > 0 || _SRV2.onRates[0] > 0); i++) {
            SubUnit su = _subunits.get(i);

            if (su._state < __COFILIN && Math.random() < _SRV2.onRates[0]) {
                //   su._state = 4;
                changed.add(new Point(i, (su._state | __SRV2)));
            } else if ((su._state & __SRV2) == __SRV2) {
                boolean off = Math.random() < _SRV2.offRate,
                        severing = _coffilinwithindist && i < distance && Math.random() < _SRV2.reactRate;
                if (off && severing) {
                    off = Math.random() < _SRV2.offRate / (_SRV2.offRate + _SRV2.reactRate);
                    severing = !off;
                }
                if (off) {
                    //  su._state = 2;
                    changed.add(new Point(i, su._state & ~__SRV2));
                }
                if (severing) {
                    if(chunksize>0){
                    sever = Math.max(Math.min(_subunits.size() - 1, chunksize), sever);
                    }else{
                        sever = Math.max(Math.min(_subunits.size() - 1, i), sever);
                    }
                    // sever = Math.max(i, sever);

                }
            }

            switch (su._state) {
                case __ATP:

                    if (Math.random() < _atpR) {
                        // su._state = 2;
                        changed.add(new Point(i, totalSRV == 0 ? __ADPPI : __ADP));
                        //changed.add(new Point(i, __ADPPI));
                    }
                    break;
                case __ADPPI:
                    if (Math.random() < adppi) {

                        //   su._state = 2;
                        changed.add(new Point(i, __ADP));
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
                        //     adfr = _coflin.onRates[1];
                    }

                    if (Math.random() < adfr) {

                        changed.add(new Point(i, __COFILIN));
                    }
                    break;
                case __COFILIN:
                    if (Math.random() < _coflin.offRate) {

                        //   changed.add(i);
                        changed.add(new Point(i, __ADP));

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
                        sever = Math.max(sever, j);

                    }
                    break;
            }
        }
        for (Point p : changed) {
            _subunits.get(p.x)._state = p.y;
        }

        return sever;
    }

    private int dynamic(LinkedList<SubUnit> _subunits, PolymerizationRate rates, boolean barbed, double t) {
        boolean addATP = Math.random() < rates.ATPon;
        boolean addADP = Math.random() < rates.ADPon;
        int ret = 0;

        double poff = _subunits.isEmpty() ? 0 : _subunits.getLast()._state == 0 ? rates.ATPoff : rates.ADPoff;

        if (!_subunits.isEmpty() && Math.random() < poff) {
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
        if (addADP) {
            _subunits.add(index, new SubUnit(__ADP, barbed, t, this));
            ret++;
        }
        if (addATP) {
            _subunits.add(index, new SubUnit(__ATP, barbed, t, this));
            ret++;
        }
        return ret;
    }

    @Override
    public void remove(double t, SubUnit su) {

        if (_lifeTimes != null) {

            _lifeTimes.add(t);

        }
    }
}
