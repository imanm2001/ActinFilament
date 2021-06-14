/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simpleactin;

import com.bulenkov.darcula.DarculaLaf;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicLookAndFeel;

/**
 *
 * @author sm2983
 */
public class SimpleActin {

    final static double PC = 0.05;

    
    public static void testHist() {

    }

    /**
     * @param args the command line arguments
     */
    public static void add(LinkedList<Integer> ls,int index){
         int i = 0;

        while (i < ls.size()) {
            int v = ls.get(i);
            if (v == index) {
                i = -1;
                break;
            } else if (v > index) {
                break;
            } else {
                i++;
            }
        }
        if (i > -1) {
            ls.add(i, index);
        }
    }
    public static void main(String[] args) throws Exception {
        
        LinkedList<Integer> ls = new LinkedList<>();
        for(int i:ls){
            System.out.println(""+i);
        }
        
        
        LinkedList<Double> vals=Utils.loadVector("C:\\Users\\sm2983\\Documents\\Projects\\Fimbin\\adb2_lt_xiaobai_raw.txt", ",");
        Object[] ret=Utils.generateHist(vals, 0.25, 17, 0.1);
        LinkedList<PointF> data=(LinkedList<PointF>)ret[0];
        String txt="";
        for(int i=0;i<data.size();i++){
            txt+=data.get(i).x+","+data.get(i).y+"\r\n";
        }
        
        PrintStream ps=new PrintStream("C:\\Users\\sm2983\\Documents\\Projects\\Fimbin\\out_adb2_java.txt");
        ps.print(txt);
        ps.flush();
        ps.close();
                 
        try {
            // Set cross-platform Java L&F (also called "Metal")
            BasicLookAndFeel darcula = new DarculaLaf();
            UIManager.setLookAndFeel(darcula);

        } catch (Exception e) {
            // handle exception
            e.printStackTrace();
        }
        new MainJFrame().setVisible(true);
        //run();
    }

    
}
