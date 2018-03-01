package eng.jAtcSim.frmPacks.sdi;

import eng.jAtcSim.frmPacks.shared.*;
import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.lib.speaking.formatting.LongFormatter;
import eng.jAtcSim.radarBase.BehaviorSettings;
import eng.jAtcSim.radarBase.Radar;
import eng.jAtcSim.startup.LayoutManager;

import javax.swing.*;
import java.awt.*;

public class FrmMain extends JFrame {

  private Pack parent;
  private JPanel pnlContent;
  private JPanel pnlBottom;
  private JPanel pnlLeft;
  private JPanel pnlTop;
  private JPanel pnlRight;
  private SwingRadarPanel srpRadar;
  private FlightListPanel flightListPanel;

  public FrmMain() {
    initComponents();
  }

  private void initComponents() {

    this.setPreferredSize(new Dimension(1000, 600));
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setTitle("SDI");

    Color bgColor = new Color(50, 50, 50);

    // top
    pnlTop = buildTopPanel();
    pnlTop.setBackground(bgColor);

    // bottom
    pnlBottom = new JPanel();
    pnlBottom.setName("pnlBottom");
    pnlBottom.setBackground(bgColor);


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
    pnlRight.setPreferredSize(new Dimension(200, 200));

    // content pane

    LayoutManager.fillBorderedPanel(
        this.getContentPane(),
        pnlTop, pnlBottom, pnlLeft, pnlRight, pnlContent);

    pack();

  }

  private JPanel buildTopPanel() {

    JButton btnStrips = new JButton("Strips");
    adjustJComponentColors(btnStrips);
    btnStrips.addActionListener(o -> {
      boolean isVis = pnlLeft.isVisible();
      isVis = !isVis;
      pnlLeft.setVisible(isVis);
    });

    JButton btnMovs = new JButton("Movs & Stats");
    adjustJComponentColors(btnMovs);
    btnMovs.addActionListener(o -> {
      boolean isVis = pnlRight.isVisible();
      isVis = !isVis;
      pnlRight.setVisible(isVis);
    });

    JButton btnPause = new JButton("Pause");
    adjustJComponentColors(btnPause);
    btnPause.addActionListener(o -> {
      if (parent.getSim().isRunning()) {
        parent.getSim().stop();
        btnPause.setText("Resume");
      } else {
        parent.getSim().start();
        btnPause.setText("Pause");
      }
    });

    JButton btnView = new JButton("Add view");
    adjustJComponentColors(btnView);
    btnView.addActionListener(o -> {
      FrmView f = new FrmView();
      f.init(this.parent);
      f.setVisible(true);
    });

    JPanel ret = LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.middle, 4,
        btnStrips, btnMovs, btnPause, btnView);
    ret.setName("pnlTop");
    return ret;
  }

  private void adjustJComponentColors(JComponent component) {
    component.setBackground(new Color(50, 50, 50));
    component.setForeground(new Color(200, 200, 200));
  }

  void init(Pack pack) {

    this.parent = pack;

    // radar
    BehaviorSettings behSett = new BehaviorSettings(true, new LongFormatter(), 10);
    this.srpRadar = new SwingRadarPanel();
    this.srpRadar.init(
        this.parent.getSim().getActiveAirport().getInitialPosition(),
        this.parent.getSim(), this.parent.getArea(),
        this.parent.getDisplaySettings(), behSett
    );
    this.pnlContent.add(srpRadar);

    // Left panel
    this.flightListPanel = new FlightListPanel();
    this.flightListPanel.init(this.parent.getSim(), parent.getAppSettings());
    pnlLeft.add(flightListPanel, BorderLayout.CENTER);
    CommandButtonsPanel pnlButtons = new CommandButtonsPanel();
    pnlLeft.add(pnlButtons, BorderLayout.PAGE_END);

    // Right panel
    ScheduledFlightListPanel scheduledPanel = new ScheduledFlightListPanel();
    scheduledPanel.init(this.parent.getSim(), parent.getAppSettings());
    pnlRight.add(scheduledPanel, BorderLayout.CENTER);
    StatsPanel statsPanel = new StatsPanel();
    statsPanel.init(this.parent.getSim());
    pnlRight.add(statsPanel, BorderLayout.PAGE_END);

    //this.parent.getSim().getSecondElapsedEvent().add(o -> printGuiTree());
    //printGuiTree();

    srpRadar.getRadar().getSelectedAirplaneChangedEvent().add((sender, callsign) -> {
      flightListPanel.setSelectedCallsign((Callsign) callsign);
      pnlButtons.setPlane((Callsign)callsign);
    });
    flightListPanel.getSelectedCallsignChangedEvent().add((sender, callsign) -> {
      srpRadar.getRadar().setSelectedCallsign((Callsign) callsign);
      pnlButtons.setPlane((Callsign)callsign);
    });

    pnlButtons.getGeneratedEvent().add(s -> srpRadar.addCommandTextToLine((String) s));
    pnlButtons.getSendEvent().add(() -> srpRadar.sendCommand());
    pnlButtons.getEraseEvent().add(() -> srpRadar.eraseCommand());
  }

  private void printGuiTree() {
    System.out.println(" * * * ");
    printGuiTreeJComponent(this.getContentPane(), 0);
  }

  private void printGuiTreeJComponent(Container component, int index) {
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
        printGuiTreeJComponent((Container) item, index + 1);
    }
  }
}
