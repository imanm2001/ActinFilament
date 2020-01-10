    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simpleactin;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author sm2983
 */
public class SimpleActin {

    final static double PC = 0.1;

    public static int dynamic(LinkedList<SubUnit> _subunits, double PCton,
            double PCdon, double PCtoff, double PCdoff, boolean barbed, double t,
            AtomicBoolean ab, PrintStream ps) {
        boolean addATP = Math.random() < PCton;
        boolean addADP = Math.random() < PCdon;
        int ret = 0;
        double poff = _subunits.isEmpty() ? 0 : _subunits.getLast()._state == 0 ? PCtoff : PCdoff;
        if (!_subunits.isEmpty()&& Math.random() < poff) {
            if (barbed) {
                ab.set(_subunits.getLast()._state == 3);
                //_subunits.getLast().remove(t);
                _subunits.removeLast();
            } else {
                ab.set(_subunits.getFirst()._state == 3);
                //_subunits.getFirst().remove(t);
                _subunits.removeFirst();
            }
            ret = -1;
        }

        int index = barbed ? _subunits.size() : 0;
        /*   
        if(barbed&&addATP){
            System.out.println(""+t);
        }*/
        if (addATP && addADP) {
            if (Math.random() < 0.5) {
                _subunits.add(index, new SubUnit(2, barbed, t, ps));
                _subunits.add(index, new SubUnit(0, barbed, t, ps));
            } else {
                _subunits.add(index, new SubUnit(0, barbed, t, ps));
                _subunits.add(index, new SubUnit(2, barbed, t, ps));
            }
            ret += 2;
        } else if (addADP) {
            _subunits.add(index, new SubUnit(2, barbed, t, ps));
            ret++;
        } else if (addATP) {
            _subunits.add(index, new SubUnit(0, barbed, t, ps));
            ret++;
        }
        return ret;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException {
        // TODO code application logic hered
        final double ATP = 1, ADP = 0.0, ADF = 40;
        final double bton = 11.6 * ATP, bdon = 3.8 * ADP, btoff = 1.4 , bdoff = 7.2 ;
        final double pton = 1.3 * ATP, pdon = 0.16 * ADP, ptoff = 0.8 , pdoff = 0.27 ;
        final double atpR = 0.35, adppi = 0.0019, adppico = 0.035*ADF, adf = 0.0085*ADF, adfco = 0.075 * ADF, adfoff = 0.005, sev = 0.012, SVR2 = 43, SVR2B = 11/50.0, SVR2UB = 0.45;
        final double maxR = Math.max(SVR2,bton);
        final double dt = PC / maxR;
        final double raise = 0.00275;
        final double totalTime = 10000;
        final int distance = 10, chunksize = 1;

        PrintStream ps = new PrintStream("C:\\Users\\sm2983\\Documents\\Projects\\Fimbin\\Sims\\ATP" + ATP + "_ADF" + (ADF*0+1) + ".txt");
        String fn="C:\\Users\\sm2983\\Documents\\Projects\\Fimbin\\Sims\\LTATP" + ATP + "_ADF" + (ADF*0+1) + ".txt";
        System.out.println(""+fn);
        PrintStream ltps = new PrintStream(fn);
        for (int ii = 0; ii < 1; ii++) {
            System.out.println(":::" + ii);
            int totalADF = 0,totalSRV=0;
            double adppir = adppi, adfr = adf;
            double t = 0;
            LinkedList<SubUnit> _subunits = new LinkedList<>();
            int n = 0, b = 0, p = 0;
            AtomicBoolean ab = new AtomicBoolean(false);
            while (t < totalTime) {

                b += dynamic(_subunits, PC * bton / maxR, PC * bdon / maxR, PC * btoff / maxR, PC * bdoff / maxR, true, t, ab, ltps);
                
                p += dynamic(_subunits, PC * pton / maxR, PC * pdon / maxR, PC * ptoff / maxR, PC * pdoff / maxR, false, t, ab, ltps);
               
                int sever = -1;
                boolean coffilinwithindist =totalADF>-1;
                /*
                for (int i = 0; i < Math.min(_subunits.size(), distance); i++) {
                    if (_subunits.get(i)._state == 3) {
                        coffilinwithindist = true;
                        break;
                    }
                }*/
                //for (SubUnit su : _subunits) {
               
                for (int i = 0; i < _subunits.size(); i++) {
                    SubUnit su = _subunits.get(i);
               
                    if (su._state < 3 && Math.random() < PC * SVR2B / maxR) {
                        su._state = 4;
                    } else if (su._state == 4) {
                        if (Math.random() < PC * SVR2UB / maxR) {
                            su._state = 2;
                        } else if (coffilinwithindist && i < distance && Math.random() < PC * SVR2 / maxR) {
                            sever = Math.max(Math.min(_subunits.size()-1, chunksize), sever);
                        }
                    }
                    switch (su._state) {
                        case 0:

                            if (Math.random() < PC * atpR / maxR) {
                                su._state = 2;
                            }
                            break;
                        case 1:
                            if (Math.random() < PC * adppir / maxR) {

                                su._state = 2;
                            }
                            break;
                        case 2:
                            adfr = adf;
                            if (totalADF > 0) {

                                if (i > 0 && _subunits.get(i - 1)._state == 3) {
                                    adfr = adfco;
                                } else if (i < _subunits.size() - 1 && _subunits.get(i + 1)._state == 3) {
                                    adfr = adfco;
                                }
                                // adfr = adfco;
                            }

                            if (Math.random() < PC * adfr / maxR) {
                                su._state = 3;
                               
                            }
                            break;
                        case 3:
                            if (Math.random() < PC * adfoff / maxR) {
                                su._state = 2;
                                
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
                            if (hasNeighbour && Math.random() < PC * sev / maxR) {
                                sever = Math.max(sever, j);
                                break;
                            }

                            break;
                    }

                }
                if (sever != -1) {
                   // _subunits.get(sever).remove(t);
                    sever(_subunits, sever,t);
                    
                    p -= sever;
                }
                if ((n++) % 1000 == 0) {
                    ps.println("" + t + "\t" + b * raise + "\t" + (p) * raise + "\t" + _subunits.size() * raise + "\t" + b / totalTime + "\t" + p / totalTime);
                    //            System.out.print("\033[2K"); 
                    System.out.println("" + ii + "::" + t+"\t"+totalADF+"\t"+totalSRV+"\t"+_subunits.size());
                }
                if(n%10000==0){
                    System.gc();
                }
                
                t += dt;
                totalADF=0;
                totalSRV=0;
                for(SubUnit su:_subunits){
                    if(su._state==3){
                        totalADF++;
                    }
                    if(su._state==4){
                        totalSRV++;
                    }
                }
             
                if (totalADF == 0) {
                    adppir = adppi;
                } else {
                    adppir = adppico;
                }

            }

            //ps.println("" + t + "\t" + b * raise + "\t" + (p) * raise + "\t" + _subunits.size() * raise + "\t" + b / totalTime + "\t" + p / totalTime);
            System.out.println(_subunits.size() * raise + "\t" + _subunits.size() / t);
        }
        /*
        int bb = 0, pp = 0;
        for (SubUnit su : _subunits) {
            if (su._barbed) {
                bb++;
            } else {
                pp++;
            }
        }
         */

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

}
