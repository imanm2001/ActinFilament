/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simpleactin;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 *
 * @author sm2983
 */
public class Utils {

    private static Properties props = null;

    public static void storeProperties() {
        if (props != null) {
            String currentDir = System.getProperty("user.dir");
            try {
                FileOutputStream fouts = new FileOutputStream(currentDir + "/props.dat");
                props.store(fouts, "test");
                fouts.flush();
                fouts.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static Properties loadProperties() {
        if (props == null) {
            String currentDir = System.getProperty("user.dir");
            props = new Properties();
            try {
                File file = new File(currentDir + "/props.dat");
                if (file.isFile()) {
                    FileInputStream fins = new FileInputStream(file);
                    props.load(fins);
                    fins.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return props;
    }

    private static int getBin(double value, double min, double step) {
        int ret = 0;
        while ((ret+1) * step + min <= value) {
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
        int tot=0;
        for (double f : vals) {
            maxv = Math.max(maxv, f);
            //int i = (int) ((f - min) / step);
            if(f>=min){
            int i = getBin(f, min, step);
            if (i >= 0 && i < ret.length) {
                tot++;
                ret[i]++;
                max = Math.max(max, ret[i]);
            }
            }
        }
        
        max=tot;
        for (int i = 0; i < bins; i++) {
            points.add(new PointF(min + (i+1.5) * step, ret[i] / max));
        }

        return new Object[]{points, 1.0};

    }

    public static void drawPoints(Graphics2D g, int h, double scaleX, double scaleY, LinkedList<PointF> points, Color c) {
        double s = 0;
        for (int i = 0; i < 30; i++) {
            PointF p = points.get(i);
            s += p.y;
        }
        float dx = (float) (scaleX) * 1;
        scaleY *= 20 / s;
        PointF p = points.getFirst();
        g.setColor(c);
        g.setStroke(new BasicStroke(3));

        int lx = (int) (p.x * dx), ly = h - (int) (p.y * scaleY);
        double maxv = -1;
        for (int i = 1; i < points.size() - 1; i++) {
            p = points.get(i);
            int x = (int) (p.x * dx), y = h - (int) (p.y * scaleY);
            maxv = Math.max(maxv, p.x);
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

    public static LinkedList<PointF> getPoints(String path) {
        LinkedList<PointF> ret = new LinkedList<PointF>();
        try {
            FileInputStream fins = new FileInputStream(path);
            String txt = stream2Str(fins);
            fins.close();
            StringTokenizer st1 = new StringTokenizer(txt, "\r\n");
            while (st1.hasMoreTokens()) {
                StringTokenizer st2 = new StringTokenizer(st1.nextToken(), ",");
                ret.add(new PointF(Math.round(Double.parseDouble(st2.nextToken()) * 100) / 100.0, Double.parseDouble(st2.nextToken())));
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return ret;
    }

    public static double max(double... v) {
        double ret = v[0];
        for (int i = 1; i < v.length; i++) {
            ret = Math.max(v[i], ret);
        }
        return ret;
    }
    public static LinkedList<Double> loadVector(String fn,String delimiter)throws IOException{
        FileInputStream fins=new FileInputStream(fn);
        String s=stream2Str(fins);
        StringTokenizer st=new StringTokenizer(s,delimiter);
        LinkedList<Double> ret=new LinkedList<>();
        while(st.hasMoreTokens()){
            ret.add(Double.parseDouble(st.nextToken().trim()));
        }
        fins.close();
        return ret;
    }

    public static double[] listToArray(LinkedList<Double> datapoints) {
        double[] ret=new double[datapoints.size()];
        for(int i=0;i<ret.length;i++){
            ret[i]=datapoints.get(i);
        }
        return ret;
    }
}
