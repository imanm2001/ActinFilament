/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simpleactin;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 *
 * @author sm2983
 */
public class Utils {

    private static int getBin(double value, double min, double step) {
        int ret = 0;
        while ((ret + 0.5) * step + min < value) {
            ret++;
        }
        return ret;
    }

    public static Object[] generateHist(LinkedList<Double> vals, double min, double maxt, double step) {
        //double step = (maxt - min) / (double) (bins - 1);
        int bins = (int) Math.ceil((maxt - min) / step) + 5;
        LinkedList<PointF> points = new LinkedList<>();
        double ret[] = new double[bins];
        double max = -1;
        double maxv = -1;
        for (double f : vals) {
            maxv = Math.max(maxv, f);
            //int i = (int) ((f - min) / step);
            int i = getBin(f, min, step);
            if (i >= 0) {
                ret[i]++;
                max = Math.max(max, ret[i]);
            }
        }
        for (int i = 0; i < bins; i++) {
            points.add(new PointF(min + i * step, ret[i] / max));
        }

        return new Object[]{points, 1.0};

    }

    public static void drawPoints(Graphics2D g, int h, double scaleX, double scaleY, LinkedList<PointF> points, Color c) {
        double s=0;
        for(PointF p:points){
            s+=p.y;
        }
        float dx = (float) (scaleX) * 1;
        scaleY *= 50/s;
        PointF p = points.getFirst();
        g.setColor(c);
        g.setStroke(new BasicStroke(3));

        int lx = (int) (p.x * dx), ly = h - (int) (p.y * scaleY);
        double maxv=-1;
        for (int i = 1; i < points.size() - 1; i++) {
            p = points.get(i);
            int x = (int) (p.x * dx), y = h - (int) (p.y * scaleY);
            maxv=Math.max(maxv, p.x);
            g.drawLine(lx + 50, ly, x + 50, y);
            lx = x;
            ly = y;

        }
        
    }
     public static String stream2Str(InputStream ins) throws IOException {
        String ret = "";
        byte[] b = new byte[1024];
        int j;
        while ((j = ins.read(b)) > 0) {
            ret += new String(b, 0, j);
        }
        return ret;
    }
     public static LinkedList<PointF> getPoints(String path){
        LinkedList<PointF> ret= new LinkedList<PointF>();
        try {
            FileInputStream fins = new FileInputStream(path);
            String txt = stream2Str(fins);
            fins.close();
            StringTokenizer st1 = new StringTokenizer(txt, "\r\n");
            while (st1.hasMoreTokens()) {
                StringTokenizer st2 = new StringTokenizer(st1.nextToken(), ",");
                ret.add(new PointF(Double.parseDouble(st2.nextToken()),Double.parseDouble(st2.nextToken())));
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return ret;
    }
}
