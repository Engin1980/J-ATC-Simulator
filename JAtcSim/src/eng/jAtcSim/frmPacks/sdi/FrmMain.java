package eng.jAtcSim.frmPacks.sdi;

import eng.jAtcSim.frmPacks.shared.FlightListPanel;
import eng.jAtcSim.frmPacks.shared.SwingRadarPanel;
import eng.jAtcSim.lib.speaking.formatting.LongFormatter;
import eng.jAtcSim.radarBase.BehaviorSettings;
import sun.java2d.SurfaceDataProxy;

import javax.swing.*;
import java.awt.*;

public class FrmMain extends JFrame {

  private Pack parent;
  private int refreshRate;
  private int refreshRateCounter;
  private JPanel pnlContent;
  private JPanel pnlBottom;
  private JPanel pnlLeft;

  public FrmMain(){
    initComponents();
  }

  private void initComponents() {

    this.setTitle("SDI");

    // bottom
    pnlBottom = new JPanel();
    pnlBottom.setName("pnlBottom");
    pnlBottom.setBackground(Color.yellow);
    JButton btn = new JButton("Nazdar");
    pnlBottom.add(btn);


    // content (radar) panel
    pnlContent = new JPanel();
    pnlContent.setName("pnlContent");
    pnlContent.setLayout(new BorderLayout());
    pnlContent.setBackground(Color.white);
    Dimension prefferedSize = new Dimension(1032, 607);
    pnlContent.setPreferredSize(prefferedSize);
    pnlContent.setBackground(Color.BLUE);

    // left panel
    pnlLeft = new JPanel();
    pnlLeft.setName("pnlLeft");
    pnlLeft.setBackground(Color.orange);
    pnlLeft.setLayout(new BorderLayout());


    // flight list panel - left

    // content pane
    BorderLayout layout = new BorderLayout();
    this.getContentPane().setLayout(layout);
    //this.getContentPane().add(pnlTop, BorderLayout.PAGE_START);
    //this.getContentPane().add(pnlBottom, BorderLayout.PAGE_END);
    this.getContentPane().add(pnlContent, BorderLayout.CENTER);
    this.getContentPane().add(pnlLeft, BorderLayout.LINE_START);

    pack();

  }

  void init(Pack pack) {

//    JButton btn = new JButton("Dudlajda");
//    pnlContent.add(btn );

//    JButton btn = new JButton("Dudlajda");
//    pnlContent.add(btn );

    this.parent = pack;
    this.refreshRate = parent.getDisplaySettings().refreshRate;
    this.refreshRateCounter = 0;

    // behavior settings for this radar
    BehaviorSettings behSett = new BehaviorSettings(true, new LongFormatter(), 10);


    // Radar to center panel
    SwingRadarPanel pnlSRP = new SwingRadarPanel();
    pnlSRP.init(
        this.parent.getSim().getActiveAirport().getRadarRange(),
        this.parent.getSim(), this.parent.getArea(),
        this.parent.getDisplaySettings(), behSett
    );
    this.pnlContent.add(pnlSRP);

    // Flight list to left panel
    FlightListPanel flightListPanel = new FlightListPanel();
    //flightListPanel.setPreferredSize(new Dimension(200, 200));
    flightListPanel.init(this.parent.getSim());
    pnlLeft.add(flightListPanel);

    // pack();

    this.parent.getSim().getSecondElapsedEvent().add(o -> printGuiTree());

    printGuiTree();
  }

  private void printGuiTree() {
    System.out.println(" * * * ");
    printJComponent(this.getContentPane(), 0);
  }

  private void printJComponent(Container component, int index) {
    for (int i = 0; i < index; i++) {
      System.out.print(" ");
    }
    String layoutName;
    if (component.getLayout() != null)
      layoutName = component.getLayout().getClass().getSimpleName();
    else
      layoutName = "N/A";
    System.out.println(
        String.format("%s -- %s : %d x %d :: %d x %d using %s - visible? %s",
            component.getClass().getName(),
            component.getName(),
            component.getX(),
            component.getY(),
            component.getWidth(),
            component.getHeight(),
            layoutName,
            Boolean.toString(component.isVisible())
            ));
    for (Component item : component.getComponents()) {
      if (item instanceof Container)
        printJComponent((Container) item, index+1);
    }
  }
}
