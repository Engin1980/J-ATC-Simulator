package eng.jAtcSim.frmPacks.sdi;

import eng.jAtcSim.frmPacks.shared.FlightListPanel;
import eng.jAtcSim.frmPacks.shared.ScheduledFlightListPanel;
import eng.jAtcSim.frmPacks.shared.SwingRadarPanel;
import eng.jAtcSim.lib.speaking.formatting.LongFormatter;
import eng.jAtcSim.radarBase.BehaviorSettings;
import eng.jAtcSim.startup.LayoutManager;

import javax.swing.*;
import java.awt.*;

public class FrmMain extends JFrame {

  private Pack parent;
  private int refreshRate;
  private int refreshRateCounter;
  private JPanel pnlContent;
  private JPanel pnlBottom;
  private JPanel pnlLeft;
  private JPanel pnlTop;
  private JPanel pnlRight;

  public FrmMain(){
    initComponents();
  }

  private void initComponents() {

    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setTitle("SDI");

    Color bgColor = new Color(50,50,50);

    // top
    pnlTop = buildTopPanel();
    pnlTop.setBackground(bgColor);

    // bottom
    pnlBottom = new JPanel();
    pnlBottom.setName("pnlBottom");
    pnlBottom.setBackground(bgColor);
    JButton btn = new JButton("Nazdar");
    pnlBottom.add(btn);


    // content (radar) panel
    pnlContent = new JPanel();
    pnlContent.setName("pnlContent");
    pnlContent.setLayout(new BorderLayout());
    pnlContent.setBackground(bgColor);

    // left panel
    pnlLeft = new JPanel();
    pnlLeft.setName("pnlLeft");
    pnlLeft.setBackground(bgColor);
    pnlLeft.setLayout(new BorderLayout());
    pnlLeft.setPreferredSize(new Dimension(200, 200));

    // right panel
    pnlRight = new JPanel();
    pnlRight.setName("pnlRight");
    pnlRight.setBackground(bgColor);
    pnlRight.setLayout(new BorderLayout());

    // content pane
    BorderLayout layout = new BorderLayout();
    this.getContentPane().setLayout(layout);
    this.getContentPane().add(pnlTop, BorderLayout.PAGE_START);
    this.getContentPane().add(pnlBottom, BorderLayout.PAGE_END);
    this.getContentPane().add(pnlContent, BorderLayout.CENTER);
    this.getContentPane().add(pnlLeft, BorderLayout.LINE_START);
    this.getContentPane().add(pnlRight, BorderLayout.LINE_END);

    pack();

  }

  private JPanel buildTopPanel() {

    JButton btnStrips = new JButton("Strips");
    adjustJComponentColors(btnStrips);
    btnStrips.addActionListener(o -> {
      boolean isVis = pnlLeft.isVisible();
      isVis = ! isVis;
      pnlLeft.setVisible(isVis);
    });

    JButton btnPause = new JButton("Pause");
    adjustJComponentColors(btnPause);
    btnPause.addActionListener(o -> {
      if (parent.getSim().isRunning()) {
        parent.getSim().stop();
        btnPause.setText("Resume");
      }
      else {
        parent.getSim().start();
        btnPause.setText("Pause");
      }
    });

    JPanel ret = LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.middle, 4,
        btnStrips, btnPause );
    ret.setName("pnlTop");
    return ret;
  }

  private void adjustJComponentColors(JComponent component){
    component.setBackground(new Color(50,50,50));
    component.setForeground(new Color(200,200,200));
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
    flightListPanel.init(this.parent.getSim());
    pnlLeft.add(flightListPanel);

    // Scheduled flights to right panel
    ScheduledFlightListPanel scheduledPanel = new ScheduledFlightListPanel();
    scheduledPanel.init(this.parent.getSim());
    pnlRight.add(scheduledPanel);

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
