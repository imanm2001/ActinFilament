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

    int i = 0;
    public LinkedList<SubUnit> _subunits = new LinkedList<>();
    double _atpR, _adppiR, _adppicoR;
    int totalADF, chunksize, distance, totalSRV;
    boolean _coffilinwithindist;
    PolymerizationRate _barbed, _pointed;
    ReactionRate _coflin, _SRV2;
    private int _b, _p;
    LinkedList<Double> _lifeTimes = new LinkedList<>();
    
    public Filament() {
        _b = _p = 0;
    }

    public void setParams(PolymerizationRate barbed, PolymerizationRate pointed,
            ReactionRate cofilin, ReactionRate SRV2, double patpR, double padppir, double padpico,
            int pdistance, int pchunksize) {
        _atpR = patpR;
        _adppiR = padppir;
        _adppicoR = padpico;
        distance = pdistance;
        chunksize = pchunksize;
        _barbed = barbed;
        _pointed = pointed;
        _SRV2 = SRV2;
        _coflin = cofilin;
        _lifeTimes.clear();
    }

    public void update(double t) {
        _b += dynamic(_subunits, _barbed, true, t);
        _p += dynamic(_subunits, _pointed, false, t);

        int sever = -1;

        //int ssize = _subunits.size() / numT;
        sever = updateSubunits(0, _subunits.size());

        if (sever != -1) {
            // _subunits.get(sever).remove(t);
            sever(_subunits, sever, t);
            _p -= sever;
        }
        totalADF = 0;
        totalSRV = 0;
        for (SubUnit su : _subunits) {
            if (su._state == 3) {
                totalADF++;
            }
            if (su._state == 4) {
                totalSRV++;
            }
        }
        _coffilinwithindist = totalADF > 0;
    }

    private static int sever(LinkedList<SubUnit> _subunits, int c, double t) {
        int adf = 0;

        for (int i = 0; i < c; i++) {
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
        double adppi=totalADF==0?_adppiR:_adppicoR;
        
        for (int i = start; i < end && (_coflin.onRates[0] > 0 || _SRV2.onRates[0] > 0); i++) {
            SubUnit su = _subunits.get(i);

            if (su._state < 3 && Math.random() < _SRV2.onRates[0]) {
                //   su._state = 4;
                changed.add(new Point(i, 4));
            } else if (su._state == 4) {
                if (Math.random() < _SRV2.offRate) {
                    //  su._state = 2;
                    changed.add(new Point(i, 2));
                } else if (_coffilinwithindist && i < distance && Math.random() < _SRV2.reactRate) {
                    sever = Math.max(Math.min(_subunits.size() - 1, chunksize), sever);
                }
            }
            switch (su._state) {
                case 0:

                    if (Math.random() < _atpR) {
                        // su._state = 2;
                        changed.add(new Point(i, 1));
                    }
                    break;
                case 1:
                    if (Math.random() < adppi) {

                        //   su._state = 2;
                        changed.add(new Point(i, 2));
                    }
                    break;
                case 2:
                    adfr = _coflin.onRates[0];
                    if (totalADF > 0) {

                        if (i > 0 && _subunits.get(i - 1)._state == 3) {
                            adfr = _coflin.onRates[0];
                        } else if (i < _subunits.size() - 1 && _subunits.get(i + 1)._state == 3) {
                            adfr = _coflin.onRates[1];
                        }
                        // adfr = adfco;
                    }

                    if (Math.random() < adfr) {
                        su._state = 3;

                        changed.add(new Point(i, 3));
                    }
                    break;
                case 3:
                    if (Math.random() < _coflin.offRate) {
                        su._state = 2;
                        //   changed.add(i);
                        changed.add(new Point(i, 2));

                    }
                    boolean hasNeighbour = false;
                    //int j = _subunits.size() - 1 - i;
                    int j = i;
                    if (j > 0) {
                        hasNeighbour |= _subunits.get(j - 1)._state == 3;
                    }
                    if (j < _subunits.size() - 1) {
                        hasNeighbour |= _subunits.get(j + 1)._state == 3;
                    }
                    if (hasNeighbour && Math.random() < _coflin.reactRate) {
                        sever = Math.max(sever, j);
                        break;
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
        boolean addATP = Math.random() < rates.ATPoff;
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
            if (Math.random() < 0.5) {
                _subunits.add(index, new SubUnit(2, barbed, t, this));
                _subunits.add(index, new SubUnit(0, barbed, t, this));
            } else {
                _subunits.add(index, new SubUnit(0, barbed, t, this));
                _subunits.add(index, new SubUnit(2, barbed, t, this));
            }
            ret += 2;
        } else if (addADP) {
            _subunits.add(index, new SubUnit(2, barbed, t, this));
            ret++;
        } else if (addATP) {
            _subunits.add(index, new SubUnit(0, barbed, t, this));
            ret++;
        }
        return ret;
    }

    @Override
    public void remove(double t) {
        
    }
}
