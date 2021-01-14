/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simpleactin;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JScrollBar;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.math3.distribution.GammaDistribution;
import static simpleactin.SimpleActin.PC;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;

/**
 *
 * @author sm2983
 */
public class MainJFrame extends javax.swing.JFrame {

    private double _STARTTIME = Filament._STARTTIME;
    private double _originalDataPoints[] = null;
    public static boolean ACTIN = true, ACP1P = false;
    boolean onlyActin = false;
    boolean _running = false, _ended = true, _heatGenerating = false;
    double _erros[];
    double _maxE, _minE, _svX[], _svY[];
    int _xInd, _yInd;
    Filament mr[] = new Filament[8];
    private LinkedList<PointF> _ps1 = null;
    private int _sampNum = 0, _sampNum2 = 0;
    ChiSquareTest _chst = new ChiSquareTest();
    Object _ret[] = null;

    LinkedList<JComponent> _params = new LinkedList<>();
    private LinkedList<LinkedList<PointF>> _histHeat = new LinkedList<>();
    private LinkedList<Double> _lifeTimes = new LinkedList<Double>();
    final int _ADFON = 0, _FIMTDA = 1, _FIMTDS = 2, _FLTD = 3, _USH = 4, _FIMK1ON = 5,
            _USC = 6, _CADF = 7, _CCAP = 8, _CON = 9, _COFF = 10, _MFS = 11, _DISTANCE = 12,
            _CHUNK = 13, _K_SRV2 = 14, _FIMK2ON = 15, _USHA = 16, _USCA = 17,
            _THERMALFLUC = 18, _ADFOFF = 19, _CADP = 20, _CATP = 21, _ADPBON = 22,
            _ADFCOON = 23, _ATPBON = 24, _ADPPON = 25, _ATPPON = 26, _ADPBOFF = 27,
            _SRV2ON = 28, _ATPBOFF = 29, _ADPPOFF = 30, _SRV2OFF = 31, _ATPPOFF = 32,
            _K_ATP = 33, _K_ADPPI = 34, _K_ADPPIC = 35, _FIMK1OFF = 36, _FIMK2OFF = 37,
            _COFF_SEVERING_RATE = 38, _NFILAMENTS = 39;
    final String PATH = "C:\\Users\\sm2983\\Documents\\Projects\\Fimbin\\";

    final String _histFilenames[] = new String[]{"out_actin_java.txt", "out_acp_java.txt",
        "out_myo_java.txt", "out_adb2_java.txt", "out_fim_java.txt"};
    final String _datasetFilenames[] = new String[]{"actin_lt_mike_raw.txt", "acp_lt_mike_raw.txt",
        "myo_lt_mike_raw.txt", "adb2_lt_xiaobai_raw.txt", "fim_lt_mike_raw.txt"};

    /**
     * Creates new form MainJFrame
     */
    public MainJFrame() {
        initComponents();
        Component coms[] = jPanel1.getComponents();
        int k = 0;

        for (int i = 0; i < coms.length; i++) {
            String name = "";
            if ((name = coms[i].getAccessibleContext().getAccessibleName()) != null && name.startsWith(" ")) {
                name = name.substring(1);
                xParComboBox.addItem(name);
                yParComboBox.addItem(name);
                if (name.indexOf('[') > -1) {
                    name = "c" + name.substring(1, name.length() - 1);
                }
                name = name.replaceAll(" ", "_").toUpperCase();
                System.out.print(" _" + name + "=" + (k++) + ",");
                _params.add((JComponent) coms[i]);
            }
        }
        System.out.println("");
        System.out.println(">>>>" + atpTextField.getAccessibleContext().getAccessibleName());
        LinkedList<Double> datapoints = null;
        try {
            if (!onlyActin) {
                /*_ps1 = Utils.getPoints("C:\\Users\\sm2983\\Documents\\Projects\\Fimbin\\out_fim_java.txt");
                datapoints = Utils.loadVector("C:\\Users\\sm2983\\Documents\\Projects\\Fimbin\\fim_lt_mike_raw.txt", ",");*/
 /*
                _ps1 = Utils.getPoints("C:\\Users\\sm2983\\Documents\\Projects\\Fimbin\\out_myo_java.txt");
                datapoints = Utils.loadVector("C:\\Users\\sm2983\\Documents\\Projects\\Fimbin\\myo_lt_mike_raw.txt", ",");
                 */
                _ps1 = Utils.getPoints("C:\\Users\\sm2983\\Documents\\Projects\\Fimbin\\out_adb2_java.txt");
                datapoints = Utils.loadVector("C:\\Users\\sm2983\\Documents\\Projects\\Fimbin\\adb2_lt_xiaobai_raw.txt", ",");

                _sampNum = datapoints.size();
            } else {
                if (!ACP1P) {
                    _ps1 = Utils.getPoints("C:\\Users\\sm2983\\Documents\\Projects\\Fimbin\\out_actin_java.txt");
                    datapoints = Utils.loadVector("C:\\Users\\sm2983\\Documents\\Projects\\Fimbin\\actin_lt_mike_raw.txt", ",");
                } else {

                    _ps1 = Utils.getPoints("C:\\Users\\sm2983\\Documents\\Projects\\Fimbin\\out_acp_java.txt");
                    datapoints = Utils.loadVector("C:\\Users\\sm2983\\Documents\\Projects\\Fimbin\\acp_lt_mike_raw.txt", ",");
                }
                _sampNum = datapoints.size();

            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        _originalDataPoints = Utils.listToArray(datapoints);

        for (int i = 0; i < 8; i++) {
            mr[i] = new Filament();
        }
        _chst = new ChiSquareTest();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        imageBox1 = new intensity.ImageBox();
        imageBox2 = new intensity.ImageBox();
        jPanel1 = new javax.swing.JPanel();
        adpponTextField = new javax.swing.JTextField();
        atpponTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        atpTextField = new javax.swing.JTextField();
        adpTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        atpbonTextField = new javax.swing.JTextField();
        adpbonTextField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        adpboffTextField = new javax.swing.JTextField();
        atpboffTextField = new javax.swing.JTextField();
        atppoffTextField = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        adppoffTextField = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        adfonTextField = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        srv2onTextField = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        srv2offTextField = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        ADFSlider = new javax.swing.JSlider();
        DistanceSlider = new javax.swing.JSlider();
        jLabel16 = new javax.swing.JLabel();
        SRV2Slider = new javax.swing.JSlider();
        ADFLabel = new javax.swing.JLabel();
        DistanceLabel = new javax.swing.JLabel();
        SRV2Label = new javax.swing.JLabel();
        ChunkLabel = new javax.swing.JLabel();
        ChunkSlider = new javax.swing.JSlider();
        jLabel21 = new javax.swing.JLabel();
        adfcoonTextField = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        adfoffTextField = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        svrrTextField = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        katpTextField = new javax.swing.JTextField();
        kadppiTextField = new javax.swing.JTextField();
        jLabel26 = new javax.swing.JLabel();
        kadppicTextField = new javax.swing.JTextField();
        jLabel27 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jProgressBar1 = new javax.swing.JProgressBar();
        jLabel28 = new javax.swing.JLabel();
        totalTimeTextField = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        FLTDTextField = new javax.swing.JTextField();
        FimTDSTextField = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        Fimk1onTextField = new javax.swing.JTextField();
        Fimk1offTextField = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        Fimk2offTextField = new javax.swing.JTextField();
        UShTextField = new javax.swing.JTextField();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        UScTextField = new javax.swing.JTextField();
        uScALabel = new javax.swing.JLabel();
        UScASlider = new javax.swing.JSlider();
        jLabel33 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        UShASlider = new javax.swing.JSlider();
        uShALabel = new javax.swing.JLabel();
        ThFlucLabel = new javax.swing.JLabel();
        ThFluSlider = new javax.swing.JSlider();
        jLabel34 = new javax.swing.JLabel();
        Fimk2onTextField = new javax.swing.JTextField();
        jLabel35 = new javax.swing.JLabel();
        FimTDATextField = new javax.swing.JTextField();
        jLabel49 = new javax.swing.JLabel();
        ConTextField = new javax.swing.JTextField();
        jLabel50 = new javax.swing.JLabel();
        jLabel51 = new javax.swing.JLabel();
        CoffTextField = new javax.swing.JTextField();
        CCapTextField = new javax.swing.JTextField();
        jLabel52 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        chiLabel = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        sampNLabel = new javax.swing.JLabel();
        jLabel47 = new javax.swing.JLabel();
        jLabel48 = new javax.swing.JLabel();
        DistLabel = new javax.swing.JLabel();
        KSLabel = new javax.swing.JLabel();
        MFSTextField = new javax.swing.JTextField();
        jLabel53 = new javax.swing.JLabel();
        jLabel54 = new javax.swing.JLabel();
        ProteinComboBox = new javax.swing.JComboBox<>();
        NFilamentsTextField = new javax.swing.JTextField();
        jLabel55 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        upLimitSlider = new javax.swing.JSlider();
        upperLimitLabel = new javax.swing.JLabel();
        sampZLabel = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        xParComboBox = new javax.swing.JComboBox<>();
        yParComboBox = new javax.swing.JComboBox<>();
        xvTextField = new javax.swing.JTextField();
        yvTextField = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        errorMethComboBox = new javax.swing.JComboBox<>();
        jLabel41 = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        xVLabel = new javax.swing.JLabel();
        yVLabel = new javax.swing.JLabel();
        jLabel45 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        mxVLabel = new javax.swing.JLabel();
        jLabel43 = new javax.swing.JLabel();
        myVLabel = new javax.swing.JLabel();
        jLabel46 = new javax.swing.JLabel();
        jLabel44 = new javax.swing.JLabel();
        eVLabel = new javax.swing.JLabel();
        lowerLimitSlider = new javax.swing.JSlider();
        lowerLimitLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout imageBox1Layout = new javax.swing.GroupLayout(imageBox1);
        imageBox1.setLayout(imageBox1Layout);
        imageBox1Layout.setHorizontalGroup(
            imageBox1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 761, Short.MAX_VALUE)
        );
        imageBox1Layout.setVerticalGroup(
            imageBox1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 307, Short.MAX_VALUE)
        );

        imageBox2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        imageBox2.setPreferredSize(new java.awt.Dimension(200, 200));
        imageBox2.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                imageBox2MouseMoved(evt);
            }
        });

        javax.swing.GroupLayout imageBox2Layout = new javax.swing.GroupLayout(imageBox2);
        imageBox2.setLayout(imageBox2Layout);
        imageBox2Layout.setHorizontalGroup(
            imageBox2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 198, Short.MAX_VALUE)
        );
        imageBox2Layout.setVerticalGroup(
            imageBox2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 198, Short.MAX_VALUE)
        );

        adpponTextField.setText("0.16");

        atpponTextField.setText("1.3");
        atpponTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                atpponTextFieldActionPerformed(evt);
            }
        });

        jLabel1.setText("[ATP]:");

        atpTextField.setText("23");
        atpTextField.setToolTipText("");
        atpTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                atpTextFieldActionPerformed(evt);
            }
        });

        adpTextField.setText("2.6");
        adpTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adpTextFieldActionPerformed(evt);
            }
        });

        jLabel2.setText("[ADP]:");

        jLabel3.setText("ATPbOn:");

        atpbonTextField.setText("11.6");

        adpbonTextField.setText("3.8");

        jLabel5.setText("ATPpOn:");

        jLabel4.setText("ADPbOn:");

        jLabel6.setText("ADPpOn:");

        jLabel7.setText("ATPbOff:");

        jLabel8.setText("ADPbOff:");

        adpboffTextField.setText("7.6");
        adpboffTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adpboffTextFieldActionPerformed(evt);
            }
        });

        atpboffTextField.setText("4.4");
        atpboffTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                atpboffTextFieldActionPerformed(evt);
            }
        });

        atppoffTextField.setText("4");
        atppoffTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                atppoffTextFieldActionPerformed(evt);
            }
        });

        jLabel9.setText("ATPpOff:");

        jLabel10.setText("ADPpOff:");

        adppoffTextField.setText("7.6");
        adppoffTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adppoffTextFieldActionPerformed(evt);
            }
        });

        jLabel11.setText("[ADF]:");

        jLabel12.setText("K_SRV2:");

        adfonTextField.setText("0.0085");

        jLabel13.setText("ADFon:");

        srv2onTextField.setText("0");

        jLabel14.setText("SRV2on:");

        srv2offTextField.setText("0.437");

        jLabel15.setText("SRV2off:");

        ADFSlider.setMaximum(10000);
        ADFSlider.setValue(1070);
        ADFSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                ADFSliderStateChanged(evt);
            }
        });

        DistanceSlider.setMaximum(1000);
        DistanceSlider.setMinimum(1);
        DistanceSlider.setValue(18);
        DistanceSlider.setValueIsAdjusting(true);
        DistanceSlider.setVerifyInputWhenFocusTarget(false);
        DistanceSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                ADFSliderStateChanged(evt);
            }
        });

        jLabel16.setText("Distance:");

        SRV2Slider.setMaximum(1500);
        SRV2Slider.setValue(100);
        SRV2Slider.setValueIsAdjusting(true);
        SRV2Slider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                ADFSliderStateChanged(evt);
            }
        });

        ADFLabel.setText("0");

        DistanceLabel.setText("0");

        SRV2Label.setText("0");

        ChunkLabel.setText("0");

        ChunkSlider.setValue(8);
        ChunkSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                ADFSliderStateChanged(evt);
            }
        });

        jLabel21.setText("Chunk:");

        adfcoonTextField.setText("0.075");

        jLabel22.setText("ADFcoon:");

        adfoffTextField.setText("0.005");

        jLabel23.setText("ADFoff:");

        jLabel24.setText("Severing Rate");

        svrrTextField.setText("0.8");
        svrrTextField.setToolTipText("");

        jLabel25.setText("K_ATP:");

        katpTextField.setText("4");
        katpTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                katpTextFieldActionPerformed(evt);
            }
        });

        kadppiTextField.setText("0.0019");

        jLabel26.setText("K_ADPPI:");

        kadppicTextField.setText("0.035");

        jLabel27.setText("K_ADPPIC:");

        jButton1.setText("Run");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel28.setText("totalTime:");

        totalTimeTextField.setText("10");

        jLabel17.setText("FLTD:");

        FLTDTextField.setText("0.6");

        FimTDSTextField.setText("0.12");

        jLabel18.setText("FimTDS:");

        jLabel19.setText("k1on:");

        Fimk1onTextField.setText("11");

        Fimk1offTextField.setText("0.082");

        jLabel20.setText("k1off:");

        jLabel29.setText("k2off:");

        Fimk2offTextField.setText("8.2e-4");

        UShTextField.setText("0.001");
        UShTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UShTextFieldActionPerformed(evt);
            }
        });

        jLabel30.setText("USh:");

        jLabel31.setText("USc:");

        UScTextField.setText("1");

        uScALabel.setText("0");

        UScASlider.setMaximum(200);
        UScASlider.setMinimum(1);
        UScASlider.setValue(69);
        UScASlider.setValueIsAdjusting(true);
        UScASlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                ADFSliderStateChanged(evt);
            }
        });

        jLabel33.setText("UScA:");

        jLabel32.setText("UShA:");

        UShASlider.setMaximum(200);
        UShASlider.setValue(110);
        UShASlider.setValueIsAdjusting(true);
        UShASlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                ADFSliderStateChanged(evt);
            }
        });

        uShALabel.setText("0");

        ThFlucLabel.setText("0");

        ThFluSlider.setMinimum(1);
        ThFluSlider.setValue(0);
        ThFluSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                ADFSliderStateChanged(evt);
            }
        });

        jLabel34.setText("ThFluc:");

        Fimk2onTextField.setText("17");

        jLabel35.setText("k2on:");

        FimTDATextField.setText("0.32");

        jLabel49.setText("FimTDA:");

        ConTextField.setText("7");

        jLabel50.setText("Con:");

        jLabel51.setText("Coff:");

        CoffTextField.setText("0.004");

        CCapTextField.setText("0.8");

        jLabel52.setText("[Cap]:");

        jLabel36.setText("ChiSqT:");

        chiLabel.setText("0");

        jLabel37.setText("Sam#");

        sampNLabel.setText("0");

        jLabel47.setText("KST:");

        jLabel48.setText("Dist:");

        DistLabel.setText("0");

        KSLabel.setText("0");

        MFSTextField.setText("0");

        jLabel53.setText("MFS:");

        jLabel54.setText("Protein:");

        ProteinComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Actin", "ACP1", "Myo1", "ADP2", "Fimbrin" }));

        NFilamentsTextField.setText("1");

        jLabel55.setText("#Filaments:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(adfonTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel17)
                                            .addComponent(jLabel49))
                                        .addGap(23, 23, 23)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(FimTDATextField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 39, Short.MAX_VALUE)
                                                .addComponent(jLabel18)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(FimTDSTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(FLTDTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jLabel30)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(UShTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGap(18, 18, 18)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel19)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(Fimk1onTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                                .addGap(5, 5, 5)
                                                .addComponent(jLabel31)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(UScTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel32))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGap(14, 14, 14)
                                                .addComponent(jLabel11)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(ADFSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(ADFLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(jLabel16)
                                                    .addComponent(jLabel52, javax.swing.GroupLayout.Alignment.TRAILING))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                                        .addComponent(CCapTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGap(18, 18, 18)
                                                        .addComponent(jLabel50)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(ConTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                        .addComponent(jLabel51)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(CoffTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jLabel53)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(MFSTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                                        .addComponent(DistanceSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(DistanceLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                                        .addGap(12, 12, 12)))
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGap(28, 28, 28)
                                                .addComponent(jLabel12))
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGap(32, 32, 32)
                                                .addComponent(jLabel21)))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(ChunkSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(SRV2Slider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(ChunkLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(SRV2Label, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                            .addComponent(jLabel35)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(Fimk2onTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                            .addGap(23, 23, 23)
                                            .addComponent(UShASlider, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(uShALabel, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(jLabel33)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(UScASlider, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(uScALabel, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                .addGroup(jPanel1Layout.createSequentialGroup()
                                                    .addGap(6, 6, 6)
                                                    .addComponent(jLabel37)
                                                    .addGap(18, 18, 18)
                                                    .addComponent(sampNLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                                    .addComponent(jLabel36)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                    .addComponent(chiLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(jPanel1Layout.createSequentialGroup()
                                                    .addComponent(jLabel48)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(DistLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGroup(jPanel1Layout.createSequentialGroup()
                                                    .addComponent(jLabel47)
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                    .addComponent(KSLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))))))))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(43, 43, 43)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel34)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(ThFluSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(ThFlucLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE))))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel54)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(ProteinComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(67, 67, 67))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel23)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(adfoffTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel2)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(adpTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel1)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(atpTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGap(21, 21, 21)
                                                .addComponent(jLabel4)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(adpbonTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                                .addGap(18, 18, 18)
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                                        .addComponent(jLabel22)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(adfcoonTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                                        .addComponent(jLabel3)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(atpbonTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                                        .addGap(19, 19, 19)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel6)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(adpponTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel5)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(atpponTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(19, 19, 19)
                                        .addComponent(jLabel8)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(adpboffTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel14)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(srv2onTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel7)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(atpboffTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                                .addGap(19, 19, 19)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel10)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(adppoffTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel15)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(srv2offTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel9)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(atppoffTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel25)
                                            .addComponent(jLabel26))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(katpTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(kadppiTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel27)
                                        .addGap(18, 18, 18)
                                        .addComponent(kadppicTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel20)
                                .addGap(18, 18, 18)
                                .addComponent(Fimk1offTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(26, 26, 26)
                                .addComponent(jLabel29)
                                .addGap(18, 18, 18)
                                .addComponent(Fimk2offTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel24)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(svrrTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel28)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(totalTimeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(23, 23, 23)
                                .addComponent(jLabel55)
                                .addGap(18, 18, 18)
                                .addComponent(NFilamentsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(atpTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(adpTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(atpbonTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(adpponTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel10)
                        .addComponent(adppoffTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel26))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(atpboffTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5)
                            .addComponent(atpponTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9)
                            .addComponent(atppoffTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(katpTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel25)
                            .addComponent(svrrTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel24))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel8)
                                .addComponent(adpboffTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(totalTimeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel28))
                            .addComponent(kadppiTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel4)
                        .addComponent(adpbonTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel54)
                            .addComponent(ProteinComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(7, 7, 7)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel55)
                            .addComponent(NFilamentsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addComponent(jLabel34, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGap(18, 18, Short.MAX_VALUE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(ThFlucLabel)
                                    .addComponent(ThFluSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(9, 9, 9)))
                        .addContainerGap(46, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel13)
                                .addComponent(adfonTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel22)
                                .addComponent(adfcoonTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel23)
                                .addComponent(adfoffTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel14)
                                .addComponent(srv2onTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(srv2offTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel15))
                            .addComponent(jLabel27)
                            .addComponent(kadppicTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(ADFLabel)
                                    .addComponent(ADFSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel11))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel16)
                                    .addComponent(DistanceSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(DistanceLabel)
                                        .addComponent(jLabel21)))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel53)
                                        .addComponent(MFSTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel52)
                                        .addComponent(CCapTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel50)
                                        .addComponent(ConTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel51)
                                        .addComponent(CoffTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel12)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(SRV2Slider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(SRV2Label))
                                        .addGap(12, 12, 12)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(ChunkSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(jLabel20)
                                                    .addComponent(Fimk1offTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(jLabel29)
                                                    .addComponent(Fimk2offTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(jLabel35)
                                                    .addComponent(Fimk2onTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                            .addComponent(ChunkLabel))))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel36)
                            .addComponent(chiLabel)
                            .addComponent(jLabel47)
                            .addComponent(KSLabel))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(41, 41, 41)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(0, 0, Short.MAX_VALUE)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(UScASlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(uScALabel)))
                                    .addComponent(jLabel32, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(UShTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel30))))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(uShALabel)
                                            .addComponent(jLabel33))
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jLabel49)
                                                        .addComponent(FimTDATextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(FimTDSTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                    .addGap(10, 10, 10)
                                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jLabel17)
                                                        .addComponent(FLTDTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(jLabel19)
                                                    .addComponent(Fimk1onTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addComponent(jLabel37)
                                                    .addComponent(sampNLabel)
                                                    .addComponent(jLabel48)
                                                    .addComponent(DistLabel))
                                                .addGroup(jPanel1Layout.createSequentialGroup()
                                                    .addGap(30, 30, 30)
                                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(UScTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jLabel31))))
                                            .addContainerGap()))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(UShASlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addContainerGap())))))))
        );

        adpponTextField.getAccessibleContext().setAccessibleName(" ADPpOn");
        atpponTextField.getAccessibleContext().setAccessibleName(" ATPpOn");
        atpTextField.getAccessibleContext().setAccessibleName(" [ATP]");
        adpTextField.getAccessibleContext().setAccessibleName(" [ADP]");
        atpbonTextField.getAccessibleContext().setAccessibleName(" ATPbOn");
        adpbonTextField.getAccessibleContext().setAccessibleName(" ADPbOn");
        adpboffTextField.getAccessibleContext().setAccessibleName(" ADPbOff");
        atpboffTextField.getAccessibleContext().setAccessibleName(" ATPbOff");
        atppoffTextField.getAccessibleContext().setAccessibleName(" ATPpOff");
        adppoffTextField.getAccessibleContext().setAccessibleName(" ADPpOff");
        adfonTextField.getAccessibleContext().setAccessibleName(" ADFon");
        srv2onTextField.getAccessibleContext().setAccessibleName(" SRV2on");
        srv2offTextField.getAccessibleContext().setAccessibleName(" SRV2off");
        ADFSlider.getAccessibleContext().setAccessibleName(" [ADF]");
        DistanceSlider.getAccessibleContext().setAccessibleName(" Distance");
        SRV2Slider.getAccessibleContext().setAccessibleName(" K_SRV2");
        ChunkSlider.getAccessibleContext().setAccessibleName(" Chunk");
        adfcoonTextField.getAccessibleContext().setAccessibleName(" ADFcoon");
        adfoffTextField.getAccessibleContext().setAccessibleName(" ADFoff");
        svrrTextField.getAccessibleContext().setAccessibleName(" Coff Severing Rate");
        katpTextField.getAccessibleContext().setAccessibleName(" K_ATP");
        kadppiTextField.getAccessibleContext().setAccessibleName(" K_ADPPI");
        kadppicTextField.getAccessibleContext().setAccessibleName(" K_ADPPIC");
        FLTDTextField.getAccessibleContext().setAccessibleName(" FLTD");
        FimTDSTextField.getAccessibleContext().setAccessibleName(" FimTDS");
        Fimk1onTextField.getAccessibleContext().setAccessibleName(" fimk1on");
        Fimk1offTextField.getAccessibleContext().setAccessibleName(" fimk1off");
        Fimk2offTextField.getAccessibleContext().setAccessibleName(" fimk2off");
        Fimk2offTextField.getAccessibleContext().setAccessibleDescription("");
        UShTextField.getAccessibleContext().setAccessibleName(" USh");
        UScTextField.getAccessibleContext().setAccessibleName(" USc");
        UScASlider.getAccessibleContext().setAccessibleName(" UScA");
        UShASlider.getAccessibleContext().setAccessibleName(" UShA");
        ThFluSlider.getAccessibleContext().setAccessibleName(" ThermalFluc");
        Fimk2onTextField.getAccessibleContext().setAccessibleName(" fimk2on");
        FimTDATextField.getAccessibleContext().setAccessibleName(" FimTDA");
        ConTextField.getAccessibleContext().setAccessibleName(" Con");
        CoffTextField.getAccessibleContext().setAccessibleName(" Coff");
        CCapTextField.getAccessibleContext().setAccessibleName(" CCap");
        MFSTextField.getAccessibleContext().setAccessibleName(" MFS");
        NFilamentsTextField.getAccessibleContext().setAccessibleName(" nFilaments");

        jCheckBox1.setSelected(true);
        jCheckBox1.setText("Actin");

        upLimitSlider.setMaximum(60);
        upLimitSlider.setValue(17);
        upLimitSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                upLimitSliderStateChanged(evt);
            }
        });

        upperLimitLabel.setText("0");

        jLabel38.setText("Sam0");

        jLabel39.setText("X:");

        jLabel40.setText("Y:");

        xvTextField.setText("2;2;3");

        yvTextField.setText("2;2;3");

        jButton2.setText("Run");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        errorMethComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Dist", "ChiSqr", "KS", "MSD", " " }));

        jLabel41.setText("Error:");

        jLabel42.setText("X:");

        xVLabel.setText("0");
        xVLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        yVLabel.setText("0");
        yVLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel45.setText("Y:");

        jButton3.setText("Export");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        mxVLabel.setText("0");
        mxVLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel43.setText("m:");

        myVLabel.setText("0");
        myVLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel46.setText("m:");

        jLabel44.setText("e:");

        eVLabel.setText("0");
        eVLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        lowerLimitSlider.setMaximum(60);
        lowerLimitSlider.setValue(3);
        lowerLimitSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                upLimitSliderStateChanged(evt);
            }
        });

        lowerLimitLabel.setText("0");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(imageBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(upLimitSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 362, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(upperLimitLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lowerLimitSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 362, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lowerLimitLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jLabel45)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(yVLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel43)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(mxVLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel46)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(myVLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jButton3)
                                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jLabel44)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(eVLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(imageBox2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel39)
                                            .addComponent(jLabel40))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(yParComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(yvTextField, javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(xvTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(jLabel42)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(xVLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addGap(1, 1, 1)
                                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel41)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(errorMethComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addGap(17, 17, 17)
                                        .addComponent(xParComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap())))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jCheckBox1)
                        .addGap(458, 458, 458)
                        .addComponent(jLabel38)
                        .addGap(18, 18, 18)
                        .addComponent(sampZLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 19, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(imageBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lowerLimitSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lowerLimitLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(upLimitSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(upperLimitLabel)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(imageBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel42)
                                    .addComponent(xVLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jButton3)
                                    .addComponent(jLabel45)
                                    .addComponent(yVLabel)
                                    .addComponent(jLabel46)
                                    .addComponent(myVLabel)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel43)
                                    .addComponent(mxVLabel)
                                    .addComponent(jLabel44)
                                    .addComponent(eVLabel))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel39)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(xParComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(yParComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel40)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(xvTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(yvTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton2)
                            .addComponent(errorMethComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel41))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel38)
                        .addComponent(sampZLabel)))
                .addGap(267, 267, 267))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void atpponTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_atpponTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_atpponTextFieldActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        loadDataPoints();
        runFromUI();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void ADFSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_ADFSliderStateChanged
        // TODO add your handling code here:
        if (!_heatGenerating) {
            _running = false;
            runFromUI();
        }
    }//GEN-LAST:event_ADFSliderStateChanged

    private void upLimitSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_upLimitSliderStateChanged
        // TODO add your handling code here:
        upperLimitLabel.setText("" + upLimitSlider.getValue() / 10.0);
        lowerLimitLabel.setText("" + lowerLimitSlider.getValue() / 10.0);
        if (_ret != null) {
            updateHisto((LinkedList< PointF>) _ret[0]);
            LinkedList<PointF> points = (LinkedList<PointF>) _ret[0];
            chiLabel.setText(String.format("%10.2e", calChi(points, _lifeTimes.size())));
            KSLabel.setText(String.format("%10.2e", calKS(_lifeTimes)));
            DistLabel.setText(String.format("%10.2e", calDist(points)));
        }
    }//GEN-LAST:event_upLimitSliderStateChanged
    private void updateErrorHeat() {
        _maxE = _minE = _erros[0];
        int w = imageBox2.getWidth(), h = imageBox2.getHeight();
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) bi.getGraphics();
        g.setColor(Color.yellow);
        g.fillRect(0, 0, w, h);
        int xminInd = 0;
        int yminInd = 0;

        double xs = (_svX[2] - _svX[0]) / _svX[1];
        double ys = (_svY[2] - _svY[0]) / _svY[1];

        for (int i = 1; i < _erros.length; i++) {
            _maxE = Math.max(_erros[i], _maxE);
            if (_erros[i] > 0 && _erros[i] < _minE) {
                _minE = _erros[i];
                xminInd = i % (int) _svX[1];
                yminInd = i / (int) _svY[1];
            }
        }
        mxVLabel.setText("" + (_svX[0] + xminInd * xs));
        myVLabel.setText("" + (_svY[0] + yminInd * ys));
        eVLabel.setText(String.format("%.2E", _minE));
        System.out.println(":::" + _maxE + "\t" + _minE);
        if (_maxE != _minE) {
            double lmin = Math.log(_minE);
            double a = 255.0 / (Math.log(_maxE) - lmin);
            double dw = w / _svX[1];
            double dh = h / _svY[1];
            for (int i = 0; i < _erros.length; i++) {
                if (_erros[i] > 0) {
                    int c = (int) ((Math.log(_erros[i]) - lmin) * a);
                    System.out.println("::>><<" + c);
                    g.setColor(new Color(c, c, c));
                    int x = i % (int) _svX[1], y = i / (int) _svY[1];
                    g.fillRect((int) (x * dw), (int) (y * dh), (int) (dw), (int) (dh));

                }
            }
            imageBox2.setImage(bi);
            imageBox2.repaint();
        }

    }

    private double[] getlims(String s) {
        double ret[] = new double[3];
        StringTokenizer st = new StringTokenizer(s.replaceAll(";", ":"), ":");
        for (int i = 0; i < 3; i++) {
            ret[i] = Float.parseFloat(st.nextToken());
        }
        if (s.indexOf(":") > -1) {
            ret[1] = (ret[2] - ret[0]) / ret[1];
            ret[1] = Math.round(ret[1]);
        }
        return ret;
    }

    private void setValue(int ind, double v) {
        JComponent jc = _params.get(ind);
        if (jc instanceof JTextField) {
            ((JTextField) jc).setText("" + v);
        } else if (jc instanceof JSlider) {
            //   ((JSlider) jc).setValue((int) v);
        }
    }
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        if (!_heatGenerating) {
            _heatGenerating = true;
            _ended = false;

            jButton2.setText("Stop");
            jButton1.setEnabled(false);
            loadDataPoints();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    _xInd = xParComboBox.getSelectedIndex();
                    _yInd = yParComboBox.getSelectedIndex();
                    ACTIN = jCheckBox1.isSelected();
                    final double paramsV[] = new double[_params.size()];
                    /*
        paramsV[_CATP] = getValue(atpTextField);
        paramsV[_CADP] = getValue(adpTextField);
        paramsV[_ATPBON] = getValue(atpbonTextField);
        paramsV[_ATPBOFF] = getValue(atpboffTextField);
        paramsV[_ADPBON] = getValue(adpbonTextField);
        paramsV[_ADPBOFF] = getValue(adpboffTextField);
        paramsV[_ATPPON] = getValue(atpponTextField);
        paramsV[_ATPPOFF] = getValue(atppoffTextField);
        paramsV[_ADPPON] = getValue(adpponTextField);
        paramsV[_ADPPOFF] = getValue(adppoffTextField);
        paramsV[_COFF_SEVERING_RATE] = getValue(svrrTextField);
        paramsV[_K_ATP] = getValue(katpTextField);
        paramsV[_K_ADPPI] = getValue(kadppiTextField);
        paramsV[_K_ADPPIC] = getValue(kadppicTextField);
        paramsV[_ADFON] = getValue(adfonTextField);
        paramsV[_ADFCOON] = getValue(adfcoonTextField);
        paramsV[_ADFOFF] = getValue(adfoffTextField);
        paramsV[_SRV2ON] = getValue(srv2onTextField);
        paramsV[_SRV2OFF] = getValue(srv2offTextField);
        paramsV[_FLTD] = getValue(FLTDTextField);
        paramsV[_FIMTD] = getValue(FimTDTextField);
        paramsV[_USH] = getValue(UShTextField);
        paramsV[_USC] = getValue(UScTextField);
        paramsV[_FIMK1ON]=getValue(Fimk1onTextField);
        paramsV[_FIMK2ON]=getValue(Fimk2onTextField);
        paramsV[_FIMK1OFF]=getValue(Fimk1offTextField);
        paramsV[_FIMK2OFF]=getValue(Fimk2offTextField);
                     */
                    for (int i = 0; i < _params.size(); i++) {
                        if (_params.get(i) instanceof JTextField) {
                            paramsV[i] = getValue((JTextField) _params.get(i));
                        }
                    }
                    paramsV[_K_SRV2] = SRV2Slider.getValue() / 10.0;
                    paramsV[_CADF] = ADFSlider.getValue() / 10.0;
                    paramsV[_USCA] = UScASlider.getValue() / 100.0;
                    paramsV[_USHA] = UShASlider.getValue() / 100.0;
                    paramsV[_THERMALFLUC] = ThFluSlider.getValue() / 50.0;
                    paramsV[_DISTANCE] = DistanceSlider.getValue();
                    paramsV[_CHUNK] = ChunkSlider.getValue();

                    _svX = getlims(xvTextField.getText());
                    _svY = getlims(yvTextField.getText());
                    double xs = (_svX[2] - _svX[0]) / _svX[1];
                    double ys = (_svY[2] - _svY[0]) / _svY[1];

                    double tt = getValue(totalTimeTextField);
                    int totalRuns = (int) (_svY[1] * _svX[1]);
                    _erros = new double[totalRuns];

                    int distanceErr = errorMethComboBox.getSelectedIndex();
                    System.out.println("METHOD:::" + distanceErr);
                    _histHeat.clear();
                    for (int j = 0; j < _svY[1] && _heatGenerating; j++) {
                        paramsV[_yInd] = _svY[0] + j * ys;
                        setValue(_yInd, paramsV[_yInd]);
                        for (int i = 0; i < _svX[1] && _heatGenerating; i++) {
                            paramsV[_xInd] = _svX[0] + i * xs;
                            setValue(_xInd, paramsV[_xInd]);

                            final double atpbonr = paramsV[_ATPBON] * paramsV[_CATP];
                            final double atpponr = paramsV[_ATPPON] * paramsV[_CATP];

                            final double adpbonr = paramsV[_ADPBON] * paramsV[_CADP];
                            final double adpponr = paramsV[_ADPPON] * paramsV[_CADP];

                            final double adfonr = paramsV[_ADFON] * paramsV[_CADF];
                            final double adfcoonr = paramsV[_ADFCOON] * paramsV[_CADF];

                            final double capOn = paramsV[_CCAP] * paramsV[_CON];
                            final double capOff = paramsV[_COFF];
                            System.out.println("" + adfonr);
                            final double maxRate = Utils.max(atpbonr, adpbonr, atpponr, adpponr,
                                    paramsV[_SRV2ON], paramsV[_K_SRV2], 0.96, paramsV[_K_SRV2],
                                    adfonr, adfcoonr, capOn, capOff, paramsV[_FIMK1ON],
                                    paramsV[_FIMK2ON], paramsV[_FIMK1OFF], paramsV[_FIMK2OFF]), dt = PC / maxRate;

                            final double fimk1on = paramsV[_FIMK1ON] * dt,
                                    fimk2on = paramsV[_FIMK2ON] * dt,
                                    fimk1off = paramsV[_FIMK1OFF] * dt,
                                    fimk2off = paramsV[_FIMK2OFF] * dt;
                            final int distance = (int) paramsV[_DISTANCE];
                            final int chunk = (int) paramsV[_CHUNK];
                            _running = true;
                            final int type = ProteinComboBox.getSelectedIndex();
                            try {

                                PolymerizationRate barbed = new PolymerizationRate(atpbonr * dt, paramsV[_ATPBOFF] * dt, adpbonr * dt, paramsV[_ADPBOFF] * dt);
                                PolymerizationRate pointed = new PolymerizationRate(atpponr * dt, paramsV[_ATPPOFF] * dt, adpponr * dt, paramsV[_ADPPOFF] * dt);

                                ReactionRate cofilin = new ReactionRate(paramsV[_COFF_SEVERING_RATE] * dt, paramsV[_ADFOFF] * dt, adfonr * dt, adfcoonr * dt);
                                ReactionRate srv2 = new ReactionRate(paramsV[_K_SRV2] * dt, paramsV[_SRV2OFF] * dt, paramsV[_SRV2ON] * dt);
                                ReactionRate cap = new ReactionRate(0, capOff * dt, capOn * dt);

                                LinkedList<Double> lt = simulate(barbed, pointed, cofilin, srv2, cap, paramsV[_K_ATP] * dt,
                                        paramsV[_K_ADPPI] * dt, paramsV[_K_ADPPIC] * dt, distance,
                                        chunk, (int) paramsV[_MFS], type, paramsV[_FLTD],
                                        paramsV[_FIMTDS], paramsV[_FIMTDA], fimk1on, fimk2on,
                                        fimk1off, fimk2off, paramsV[_USH],
                                        paramsV[_USHA], paramsV[_USC], paramsV[_USCA],
                                        paramsV[_THERMALFLUC], dt, tt, (int) paramsV[_NFILAMENTS], true);
                                Object[] ret = Utils.generateHist(_lifeTimes, 0.25, 6, 0.1);

                                LinkedList<PointF> points = (LinkedList<PointF>) ret[0];
                                _histHeat.add(points);
                                double error = 0;
                                switch (distanceErr) {
                                    case 0:

                                        error = calDist(points);
                                        break;
                                    case 1:
                                        error = 1 - calChi(points, _lifeTimes.size());
                                        break;
                                    case 2:
                                        error = 1 - calKS(lt);
                                        break;
                                    case 3:
                                        error = calMSD(points);
                                        break;
                                }
                                _erros[i + j * (int) _svX[1]] = error;
                                System.out.println("ERR::" + (1 - error));
                                updateErrorHeat();

                                //  jProgressBar2.setValue((runid * 100) / totalRuns);
                            } catch (Exception ex) {
                                _running = false;
                                ex.printStackTrace();
                            }
                        }
                    }

                    _heatGenerating = false;
                    _ended = true;
                    _running = false;
                    jButton2.setText("Run");
                    jButton2.setEnabled(true);
                    jButton1.setEnabled(true);
                    // jProgressBar2.setValue(0);
                }
            }).start();
        } else {
            jButton2.setText("Wait...");
            jButton2.setEnabled(false);
            _heatGenerating = false;
            _running = false;

        }


    }//GEN-LAST:event_jButton2ActionPerformed

    private void imageBox2MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imageBox2MouseMoved
        // TODO add your handling code here:
        if (_erros != null) {
            int dw = (int) (imageBox2.getWidth() / _svX[1]);
            int dh = (int) (imageBox2.getHeight() / _svY[1]);
            Point p = evt.getPoint();
            int x = p.x / dw, y = p.y / dh;
            int index = (int) (x + y * _svX[1]);
            if (!_heatGenerating && index < _histHeat.size()) {
                updateHisto(_histHeat.get(index));
            }
            double xs = (_svX[2] - _svX[0]) / _svX[1];
            double ys = (_svY[2] - _svY[0]) / _svY[1];

            xVLabel.setText("" + (_svX[0] + xs * x));
            yVLabel.setText("" + (_svY[0] + ys * y));

        }
    }//GEN-LAST:event_imageBox2MouseMoved

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        JFileChooser jfc = new JFileChooser();
        FileFilter imageFilter = new FileNameExtensionFilter(
                "Image files", ImageIO.getReaderFileSuffixes());
        jfc.addChoosableFileFilter(imageFilter);
        jfc.setAcceptAllFileFilterUsed(false);
        Properties props = Utils.loadProperties();
        String dir = props.getProperty("ParametersPath");
        if (dir != null) {
            jfc.setCurrentDirectory(new File(dir));

        } else {
            jfc.setCurrentDirectory(new File("."));
        }

        if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String fn = jfc.getSelectedFile().getPath();
            props.setProperty("ParametersPath", fn);
            Utils.storeProperties();
            try {
                PrintStream ps = new PrintStream(fn);
                ps.println("xInd: " + xParComboBox.getItemAt(_xInd));
                ps.println("yInd: " + xParComboBox.getItemAt(_yInd));
                //--------------
                ps.println("" + _svX[0] + "\t" + _svX[1] + "\t" + _svX[2]);
                ps.println("" + _svY[0] + "\t" + _svY[1] + "\t" + _svY[2]);
                //--------------
                for (int j = 0; j < _svY[1]; j++) {

                    for (int i = 0; i < _svX[1]; i++) {
                        System.out.print("" + _erros[i + j * (int) _svX[1]]);
                        if (i < _svX[1] - 1) {
                            System.out.print("\t");
                        }
                    }
                    if (j < _svY[1] - 1) {
                        System.out.println("");
                    }
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void UShTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UShTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_UShTextFieldActionPerformed

    private void adpTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adpTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_adpTextFieldActionPerformed

    private void atpTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_atpTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_atpTextFieldActionPerformed

    private void atppoffTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_atppoffTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_atppoffTextFieldActionPerformed

    private void katpTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_katpTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_katpTextFieldActionPerformed

    private void adppoffTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adppoffTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_adppoffTextFieldActionPerformed

    private void adpboffTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adpboffTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_adpboffTextFieldActionPerformed

    private void atpboffTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_atpboffTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_atpboffTextFieldActionPerformed

    public double calDist(LinkedList<PointF> points) {
        double ret = 0;
        int num = upLimitSlider.getValue();
        for (int i = lowerLimitSlider.getValue(); i < num; i++) {
            ret += Math.abs(points.get(i).y - _ps1.get(i).y);
        }
        return ret;
    }

    public double calMSD(LinkedList<PointF> points) {
        double ret = 0;
        int num = upLimitSlider.getValue();
        for (int i = lowerLimitSlider.getValue(); i < num; i++) {
            ret += Math.pow(points.get(i).y - _ps1.get(i).y, 2.0);
        }
        return ret;
    }

    public double calKS(LinkedList<Double> points) {
        double ret = 0;
        if (points.size() > 10) {

            LinkedList<Double> p = new LinkedList<>(), p2 = new LinkedList<>();
            double lim = upLimitSlider.getValue() / 10.0;
            double lowerLim = lowerLimitSlider.getValue() / 10.0;
            for (double d : points) {
                if (d < lim && d >= lowerLim) {
                    p.add(d);
                }
            }
            for (double d : _originalDataPoints) {
                if (d < lim && d >= lowerLim) {
                    p2.add(d);
                }
            }
            double[] lts = Utils.listToArray(p);
            double[] lts2 = Utils.listToArray(p2);

            KolmogorovSmirnovTest kstest = new KolmogorovSmirnovTest();
            if (lts.length > 10 && lts2.length > 10) {

                ret = kstest.kolmogorovSmirnovTest(lts, lts2, false);

            }
        }
        return ret;

    }

    public double calChi(LinkedList<PointF> points, int numS) {
        double expectedT[] = new double[_ps1.size()];
        long observedT[] = new long[_ps1.size()];
        int j = 0;
        double sc1 = 0, sc2 = 0;
        int num = upLimitSlider.getValue();
        for (int i = 0; i < 60; i++) {
            sc1 += points.get(i).y;
            sc2 += _ps1.get(i).y;
        }
        for (int i = lowerLimitSlider.getValue(); i < num; i++) {
            PointF pf = points.get(i), pf2 = _ps1.get(i);
            if (pf.y > 0.005 && pf2.y > 0.005) {
                expectedT[j] = pf.y * numS / sc1;
                observedT[j] = (long) (pf2.y * numS / sc2);
                j++;
            }
        }
        double d = 0;
        if (j > 1) {
            double expected[] = new double[j];
            long observed[] = new long[j];
            System.arraycopy(expectedT, 0, expected, 0, j);
            System.arraycopy(observedT, 0, observed, 0, j);
            d = _chst.chiSquareTest(expected, observed);
        }
        return d;
    }

    public LinkedList<Double> simulate(PolymerizationRate barbed, PolymerizationRate pointed,
            ReactionRate cofilin, ReactionRate SRV2, ReactionRate cap, double atp, double adppi,
            double adppico, int distance, int chunksize, int mfs, final int proteinType,
            final double filamentTimeDiff, final double fimbrinTimeDiffS, final double fimbrinTimeDiffA,
            final double fimk1on, final double fimk2on, final double fimk1off,
            final double fimk2off, final double uSh, final double uShA,
            final double uSc, final double uScA, final double thermalFluc,
            final double dt, double totalTime, int nFilaments, boolean updatePlot) throws Exception {


        /*
        final double ATP = 1, ADP = 0.0, ADF = 0;
        final double bton = 11.6 * ATP, bdon = 3.8 * ADP, btoff = 1.4 , bdoff = 7.2 ;
        final double pton = 1.3 * ATP, pdon = 0.16 * ADP, ptoff = 0.8 , pdoff = 0.27 ;
        final double atpR = 0.35, adppi = 0.0019, adppico = 0.035*ADF, adf = 0.0085*ADF, adfco = 0.075 * ADF, adfoff = 0.005, sev = 0.012, SVR2 = 43, SVR2B = 0*11/50.0, SVR2UB = 0.45;
        final int distance = 10, chunksize = 1;
         */
        //LinkedList<SubUnit> _subunits = new LinkedList<>();

        /*double bton = btonr * ATP, pton = ptonr * ATP, bdon = btonr * ADP, pdon = bdonr * ADP, SRV2B = SRV2Br * SVR2C;
        adfon *= ADF;
        adfcoon *= ADF;
        System.out.println(">>>" + SRV2B + "   " + ADF);
        final double maxR = Math.max(SRV2, bton);*/
        final double raise = 0.00275;
        _ended = false;
        _ret = null;
        System.out.println(barbed);
        System.out.println(pointed);
        System.out.println(cofilin);
        System.out.println(SRV2);
        System.out.println(atp + "\t" + adppi + "\t" + adppico + "\t" + chunksize + "\t" + distance);
        PrintStream lenps = new PrintStream("C:\\Users\\sm2983\\Documents\\Projects\\Fimbin\\Sims\\LT_myo.txt");
        String fn = "C:\\Users\\sm2983\\Documents\\Projects\\Fimbin\\Sims\\LT_actin_fast.txt";
        PrintStream ltps = new PrintStream(fn);

        final int totalRuns = 1000000;
        LinkedList<Double> lifeTimes = new LinkedList<>();
        int ii = 0;
        Fimbrin.setDists(uSh, uShA, uSc, uScA, fimk1off, fimk2off, fimk1on, fimk2on);
        long t1 = System.currentTimeMillis();
        boolean chiz = true;
        //int maxSamples = _sampNum;
        int maxSamples = Math.min(1000, _sampNum);
        //GammaDistribution protDelay = new GammaDistribution(fimbrinTimeDiffS, fimbrinTimeDiffA);
        WaitingTime wt = new WaitingTime() {
            @Override
            public double getTime() {
                //return protDelay.sample(); //To change body of generated methods, choose Tools | Templates.
                return fimbrinTimeDiffS * Math.random();
            }
        };
        double totalSingleBounded = 0;
        double totalMultiSingleBounded = 0;
        for (ii = 0; ii < totalRuns && _running && lifeTimes.size() < maxSamples; ii++) {
            /*     
            if (ii > 0) {
                System.out.println(">>" + (t2 - t1) / ii);
            }
             */
            int totalADF = 0, totalSRV = 0;
            double t = 0;

            int n = 0, b = 0, p = 0;
            Filament f1 = new Filament(), f2 = new Filament();
            if (onlyActin) {
                f1._lifeTimes = lifeTimes;
            }
            //Fimbrin fim = new Fimbrin(f1, f2, fimk1off, fimk2off, fimk1on, fimk2on, thermalFluc, fimbrinTimeDiff);
            ProteinI prot = null;

            if (proteinType == 4) {
                prot = new Fimbrin(f1, f2, thermalFluc, wt);
            } else {

                prot = new Protein(f1, fimk1off, fimk1on, fimk2off, fimk2on);
            }

            f1.setParams(barbed, pointed, cofilin, SRV2, cap, atp, adppi, adppico, 0.96 * dt, distance, chunksize, mfs);
            f2.setParams(barbed, pointed, cofilin, SRV2, cap, atp, adppi, adppico, 0.96 * dt, distance, chunksize, mfs);
            f1._prot = prot;
            double st2 = Math.random() * filamentTimeDiff, stFim = wt.getTime();
            boolean init2 = false, initFim = false;
            //while ((fim._t == -1 || t - fim._t < totalTime) && _running) {

            while (t < totalTime && _running) {

                if ((n++) % 4000 == 0) {
                    //ps.println("" + t + "\t" + b * raise + "\t" + (p) * raise + "\t" + _subunits.size() * raise + "\t" + b / totalTime + "\t" + p / totalTime);
                    //            System.out.print("\033[2K"); 
                    //  System.out.println("" + t + "\t" + f1._subunits.size());
                    int p1 = (int) (100 * (ii * totalTime + t) / (double) (totalTime * totalRuns));
                    int p2 = (int) (100 * (lifeTimes.size()) / (double) (maxSamples));
                    jProgressBar1.setValue(Math.max(p1, p2));
                    sampNLabel.setText("" + lifeTimes.size());
                    if (updatePlot) {
                        Object[] ret = Utils.generateHist(lifeTimes, 0.25, 6, 0.1);
                        if (!_heatGenerating) {
                            LinkedList<PointF> points = (LinkedList<PointF>) ret[0];
                            double chst = calChi(points, lifeTimes.size());
                            if (chst > 0) {
                                chiz = true;
                            }
                            if (chiz && chst == 0) {
                                sampZLabel.setText("" + lifeTimes.size());
                                chiz = false;

                            }
                            DistLabel.setText(String.format("%10.2e", calDist(points)));
                            KSLabel.setText(String.format("%10.2e", calKS(lifeTimes)));
                            chiLabel.setText(String.format("%10.2e", chst));
                        }

                        updateHisto((LinkedList<PointF>) ret[0]);

                    }

                    // lifeTimes.add(f1._subunits.size() * raise);
                    /*
                    if (f1._subunits.size() > 0) {
                        ltps.println(f1._subunits.size());
                    }*/
                }
                if (n % 10000 == 0) {
                    System.gc();
                }
                f1.update(t);
                if (f1._taggedOnTime > Filament._STARTTIME) {
                    int tags = f1.isTagged();
                    if (tags == 1) {
                        totalSingleBounded += dt;
                    } else if (tags > 1) {
                        totalMultiSingleBounded += dt;
                    }
                }
                if (!onlyActin) {
                    if (t > st2 && fimk2on > 0) {
                        if (!init2) {
                            st2 = t;
                            init2 = true;
                        }
                        f2.update(t - st2);

                    }

                    if (t >= 0) {
                        if (!initFim) {
                            stFim = t;
                            initFim = true;
                        }
                        double pt = -1;
                        if (!prot.update(t)) {
                            if ((pt = prot.getTime()) > Filament._STARTTIME) {
                                double dtt = (Math.ceil(prot.getDetachedTime() * 10) - Math.ceil(pt * 10)) / 10;

                                if (dtt >= 0.0) {
                                    lifeTimes.add(dtt);
                                }
                            }

                            prot.reset();
                        }
                    }
                }
                t += dt;

            }
            if (onlyActin) {
                lenps.println((f1._subunits.size() * raise * 1000));

            }

            //  System.out.println("NUM:"+_subunits.size());
            //ps.println("" + t + "\t" + b * raise + "\t" + (p) * raise + "\t" + _subunits.size() * raise + "\t" + b / totalTime + "\t" + p / totalTime);
            //    System.out.println(_subunits.size() * raise + "\t" + _subunits.size() / t);
            // System.out.println("" + (System.currentTimeMillis() - t1));
        }
        /*
        for (double lt : lifeTimes) {
            ltps.println(lt);
        }
         */
        if (updatePlot) {
            _ret = Utils.generateHist(lifeTimes, 0.25, 6, 0.1);
            _lifeTimes.clear();
            _lifeTimes.addAll(lifeTimes);
            //_sampNum2 = lifeTimes.size();
            System.out.println("Total Number of samples:" + lifeTimes.size());
            //  System.out.println("" + tn / (double) ii);
            _ended = true;
            jButton1.setText("Run");
            jButton1.setEnabled(true);
            for (Double ooo : lifeTimes) {
                ltps.println(ooo);
            }
            ltps.flush();
        }
        lenps.flush();
        lenps.close();
        jProgressBar1.setValue(0);
        _running = false;
        System.out.println("" + totalSingleBounded + "\t" + totalMultiSingleBounded);
        return lifeTimes;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainJFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel ADFLabel;
    private javax.swing.JSlider ADFSlider;
    private javax.swing.JTextField CCapTextField;
    private javax.swing.JLabel ChunkLabel;
    private javax.swing.JSlider ChunkSlider;
    private javax.swing.JTextField CoffTextField;
    private javax.swing.JTextField ConTextField;
    private javax.swing.JLabel DistLabel;
    private javax.swing.JLabel DistanceLabel;
    private javax.swing.JSlider DistanceSlider;
    private javax.swing.JTextField FLTDTextField;
    private javax.swing.JTextField FimTDATextField;
    private javax.swing.JTextField FimTDSTextField;
    private javax.swing.JTextField Fimk1offTextField;
    private javax.swing.JTextField Fimk1onTextField;
    private javax.swing.JTextField Fimk2offTextField;
    private javax.swing.JTextField Fimk2onTextField;
    private javax.swing.JLabel KSLabel;
    private javax.swing.JTextField MFSTextField;
    private javax.swing.JTextField NFilamentsTextField;
    private javax.swing.JComboBox<String> ProteinComboBox;
    private javax.swing.JLabel SRV2Label;
    private javax.swing.JSlider SRV2Slider;
    private javax.swing.JSlider ThFluSlider;
    private javax.swing.JLabel ThFlucLabel;
    private javax.swing.JSlider UScASlider;
    private javax.swing.JTextField UScTextField;
    private javax.swing.JSlider UShASlider;
    private javax.swing.JTextField UShTextField;
    private javax.swing.JTextField adfcoonTextField;
    private javax.swing.JTextField adfoffTextField;
    private javax.swing.JTextField adfonTextField;
    private javax.swing.JTextField adpTextField;
    private javax.swing.JTextField adpboffTextField;
    private javax.swing.JTextField adpbonTextField;
    private javax.swing.JTextField adppoffTextField;
    private javax.swing.JTextField adpponTextField;
    private javax.swing.JTextField atpTextField;
    private javax.swing.JTextField atpboffTextField;
    private javax.swing.JTextField atpbonTextField;
    private javax.swing.JTextField atppoffTextField;
    private javax.swing.JTextField atpponTextField;
    private javax.swing.JLabel chiLabel;
    private javax.swing.JLabel eVLabel;
    private javax.swing.JComboBox<String> errorMethComboBox;
    private intensity.ImageBox imageBox1;
    private intensity.ImageBox imageBox2;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JTextField kadppiTextField;
    private javax.swing.JTextField kadppicTextField;
    private javax.swing.JTextField katpTextField;
    private javax.swing.JLabel lowerLimitLabel;
    private javax.swing.JSlider lowerLimitSlider;
    private javax.swing.JLabel mxVLabel;
    private javax.swing.JLabel myVLabel;
    private javax.swing.JLabel sampNLabel;
    private javax.swing.JLabel sampZLabel;
    private javax.swing.JTextField srv2offTextField;
    private javax.swing.JTextField srv2onTextField;
    private javax.swing.JTextField svrrTextField;
    private javax.swing.JTextField totalTimeTextField;
    private javax.swing.JLabel uScALabel;
    private javax.swing.JLabel uShALabel;
    private javax.swing.JSlider upLimitSlider;
    private javax.swing.JLabel upperLimitLabel;
    private javax.swing.JComboBox<String> xParComboBox;
    private javax.swing.JLabel xVLabel;
    private javax.swing.JTextField xvTextField;
    private javax.swing.JComboBox<String> yParComboBox;
    private javax.swing.JLabel yVLabel;
    private javax.swing.JTextField yvTextField;
    // End of variables declaration//GEN-END:variables

    public double getValue(JTextField tf) {
        return Double.parseDouble(tf.getText());
    }

    private void runFromUI() {
        if (!_running) {
            while (!_ended) {

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MainJFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            ACTIN = jCheckBox1.isSelected();
            final double ATP = getValue(atpTextField), ADP = getValue(adpTextField),
                    atpbon = getValue(atpbonTextField), atpboff = getValue(atpboffTextField), adpbon = getValue(adpbonTextField),//5
                    adpboff = getValue(adpboffTextField), atppon = getValue(atpponTextField), atppoff = getValue(atppoffTextField),
                    adppon = getValue(adpponTextField), adppoff = getValue(adppoffTextField),//5
                    cofsvrr = getValue(svrrTextField), atp = getValue(katpTextField), adppi = getValue(kadppiTextField), adppic = getValue(kadppicTextField),
                    adfon = getValue(adfonTextField), adfcoon = getValue(adfcoonTextField), adfoff = getValue(adfoffTextField),
                    srv2on = getValue(srv2onTextField), srv2off = getValue(srv2offTextField),
                    ADF = ADFSlider.getValue() / 10.0, K_SRV2 = SRV2Slider.getValue() / 10.0,
                    FLTD = getValue(FLTDTextField), fimTDS = getValue(FimTDSTextField),
                    fimTDA = getValue(FimTDATextField),
                    uSc = getValue(UScTextField), uSh = getValue(UShTextField), uScA = UScASlider.getValue() / 100.0,
                    uShA = UShASlider.getValue() / 100.0, thermalFluc = ThFluSlider.getValue() / 50.0,
                    CCap = getValue(CCapTextField), Con = getValue(ConTextField), Coff = getValue(CoffTextField);

            final double atpbonr = atpbon * ATP;
            final double atpponr = atppon * ATP;
            final int msf = (int) getValue(MFSTextField);

            final double adpbonr = adpbon * ADP;
            final double adpponr = adppon * ADP;

            final double adfonr = adfon * ADF;
            final double caponr = CCap * Con;
            System.out.println("" + adfonr);
            final double adfcoonr = adfcoon * ADF;

            double fimk1onr = getValue(Fimk1onTextField),
                    fimk2onr = getValue(Fimk2onTextField),
                    fimk1offr = getValue(Fimk1offTextField),
                    fimk2offr = getValue(Fimk2offTextField);

            final double maxRate = Utils.max(atpbonr, adpbonr, atpponr, adpponr,
                    srv2on, K_SRV2, 0.96, srv2on, adfonr, adfcoonr, caponr, Coff,
                    fimk1onr, fimk2onr, fimk1offr, fimk2offr), dt = PC / maxRate;
            final double fimk1on = fimk1onr * dt,
                    fimk2on = fimk2onr * dt,
                    fimk1off = fimk1offr * dt,
                    fimk2off = fimk2offr * dt;
            final int distance = DistanceSlider.getValue(), chunk = ChunkSlider.getValue();
            ADFLabel.setText("" + ADF);
            SRV2Label.setText("" + K_SRV2);
            DistanceLabel.setText("" + distance);
            ChunkLabel.setText("" + chunk);
            uShALabel.setText("" + uShA);
            uScALabel.setText("" + uScA);
            _running = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        PolymerizationRate barbed = new PolymerizationRate(atpbonr * dt, atpboff * dt, adpbonr * dt, adpboff * dt);
                        PolymerizationRate pointed = new PolymerizationRate(atpponr * dt, atppoff * dt, adpponr * dt, adppoff * dt);

                        ReactionRate cofilin = new ReactionRate(cofsvrr * dt, adfoff * dt, adfonr * dt, adfcoonr * dt);
                        ReactionRate srv2 = new ReactionRate(K_SRV2 * dt, srv2off * dt, srv2on * dt);
                        ReactionRate cap = new ReactionRate(0, Coff * dt, caponr * dt);

                        simulate(barbed, pointed, cofilin, srv2, cap, atp * dt,
                                adppi * dt, adppic * dt, distance, chunk, msf, ProteinComboBox.getSelectedIndex(), FLTD,
                                fimTDS, fimTDA, fimk1on, fimk2on, fimk1off, fimk2off, uSh, uShA,
                                uSc, uScA, thermalFluc, dt, getValue(totalTimeTextField),
                                (int) getValue(NFilamentsTextField), true);
                    } catch (Exception ex) {
                        _running = false;
                        ex.printStackTrace();
                    }
                }
            }).start();
            jButton1.setText("Stop");
        } else {
            _running = false;
            jButton1.setText("Wait");
            jButton1.setEnabled(false);

            while (!_ended) {

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MainJFrame.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
            jButton1.setEnabled(true);

        }

    }

    private void updateHisto(LinkedList<PointF> points) {

        BufferedImage img = null;
        Graphics2D g = null;
        int w = imageBox1.getWidth(), h = imageBox1.getHeight();
        double dw = (w - 50) / 10.3;

        img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        g = (Graphics2D) img.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        h -= 40;
        //drawBackground(g,h,w,imgSx, sf);

        g.setColor(Color.blue);

        g.drawLine(50, 0, 50, h);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2));
        //double max = (Double) ret[1];

        // g.drawLine((int) (dw * 0.7) + 50, 0, (int) (dw * 0.7) + 50, h);
        Utils.drawPoints(g, h, dw, 40, points, Color.red);
        Utils.drawPoints(g, h, dw, 40, _ps1, Color.blue);
        /* 
        g.drawLine(50+(int)(0.6*dw), 0, 50+(int)(0.6*dw), 400);
        g.drawLine(50+(int)(0.7*dw), 0, 50+(int)(0.7*dw), 400);
        g.drawLine(50+(int)(6*dw), 0, 50+(int)(6*dw), 400);
        for(int i=0;i<10;i++){
            System.out.println(""+points.get(i+3).x+"\t"+_ps1.get(i).x);
        }*/
        g.setColor(new Color(100, 100, 100, 100));
        g.fillRect((int) (upLimitSlider.getValue() * (6 * dw) / 60.0) + 50, 0, w, h);
        g.fillRect(50, 0, (int) (lowerLimitSlider.getValue() * (6 * dw) / 60.0), h);
        g.drawString("Life Time (s)", (w - g.getFontMetrics().stringWidth("Life Time (s)")) / 2, h + 20);
        g.translate(30, (h - 50 + g.getFontMetrics().stringWidth("Frequency")) / 2);
        g.rotate(-Math.PI / 2);

        g.drawString("Frequency", 0, 00);
        imageBox1.setImage(img);

    }

    public void loadDataPoints() {
        LinkedList<Double> datapoints = null;
        try {
            int index = ProteinComboBox.getSelectedIndex();
            _ps1 = Utils.getPoints(PATH + _histFilenames[index]);
            datapoints = Utils.loadVector(PATH + _datasetFilenames[index], ",");
            switch (index) {
                case 0:
                    onlyActin = true;
                    ACP1P = false;
                    break;
                case 1:
                    onlyActin = true;
                    ACP1P = true;
                    break;
                default:
                    onlyActin = false;
                    ACP1P = false;
                    break;
            }

            _sampNum = datapoints.size();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        _originalDataPoints = Utils.listToArray(datapoints);
    }
}
