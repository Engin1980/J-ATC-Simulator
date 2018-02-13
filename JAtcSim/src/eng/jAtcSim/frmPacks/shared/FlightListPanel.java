package eng.jAtcSim.frmPacks.shared;

import eng.jAtcSim.lib.Simulation;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.exceptions.ENotSupportedException;
import eng.jAtcSim.lib.global.ReadOnlyList;
import eng.jAtcSim.startup.LayoutManager;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class FlightListPanel extends JPanel {

  private static List<Airplane.AirplaneInfo> plns;
  private Simulation sim;
  private JScrollPane pnlScroll;
  private JPanel pnlContent;

  public void init(Simulation sim) {
    this.sim = sim;

    pnlContent = LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.left, 4);
    pnlContent.setName("FlightListPanel_ContentPanel");
    pnlScroll = new JScrollPane(pnlContent);
    pnlScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    pnlScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

    this.setLayout(new BorderLayout());
    this.add(pnlScroll);

    pnlContent.setBackground(new Color(50,50,50));

    this.sim.getSecondElapsedEvent().add(o -> updateList());
  }

  private void updateList() {
    // init pri prvnim volani
    if (plns == null) {
      plns = new LinkedList();
    }

    // znovunaplneni, kdyz nesedi pocet
    ReadOnlyList<Airplane.AirplaneInfo> pi = sim.getPlaneInfos();
    if (plns.size() != pi.size()) {
      plns.clear();
      for (Airplane.AirplaneInfo ai : pi) {
        plns.add(ai);
      }
    }

    pnlContent.removeAll();
    FlightStripPanel.resetIndex();
    for (Airplane.AirplaneInfo pln : plns) {
      JPanel pnlItem = createFlightStrip(pln);
      pnlItem.setName("FlightStrip_"+pln.callsignS());
      pnlContent.add(pnlItem);
    }

    this.revalidate();
  }

  private JPanel createFlightStrip(Airplane.AirplaneInfo ai) {
    JPanel ret = new FlightStripPanel(ai);
    return ret;
  }

}

class FlightStripPanel extends JPanel {
  public static final int WIDTH = 200;
  private static final int HEIGHT = 50;

  private static final int A = 75; // adjust colors
  private static final int B = 125; // adjust colors

  private static int index = 0;

  private static final Color TEXT_COLOR = new Color(230, 230, 230);

  private static final Color TWR_EVEN = new Color(0, 0, A);
  private static final Color TWR_ODD = new Color(0, 0, B);
  private static final Color CTR_EVEN = new Color(0, A, A);
  private static final Color CTR_ODD = new Color(0, B, B);
  private static final Color APP_EVEN = new Color(0, A, 0);
  private static final Color APP_ODD = new Color(0, B, 0);
  private static final Color AIRPROX = new Color(155, 0, 0);

  private static String FONT_NAME = "Consolas"; // "Consolas"; // "Cambria" // "Calibri"; //"PxPlus IMG VGA9";
  private static int FONT_SIZE = 12;
  private static final Font NORMAL_FONT = new Font(FONT_NAME, 0, FONT_SIZE);
  private static final Font BOLD_FONT = new Font(FONT_NAME, Font.BOLD, FONT_SIZE);

  public static void resetIndex(){
    index = 0;
  }

  public FlightStripPanel(Airplane.AirplaneInfo ai) {

    this.setLayout(new BorderLayout());

    Dimension dim = new Dimension(WIDTH, HEIGHT);
    this.setPreferredSize(dim);
    this.setMinimumSize(dim);
    this.setMaximumSize(dim);

    Color color = FlightStripPanel.getColor(ai);
    this.setBackground(color);
    this.setForeground(TEXT_COLOR);

    fillContent(ai);
  }

  private void fillContent(Airplane.AirplaneInfo ai) {
    Component[] cmps = new Component[6];
    JLabel lbl;

    lbl = new JLabel(ai.callsignS());
    lbl.setName("lblCallsign");
    lbl.setFont(BOLD_FONT);
    lbl.setForeground(TEXT_COLOR);
    cmps[0] = lbl;

    lbl = new JLabel(ai.planeType() + " (" + ai.typeCategory() + ")");
    lbl.setName("lblPlaneType");
    lbl.setFont(NORMAL_FONT);
    lbl.setForeground(TEXT_COLOR);
    cmps[2] = lbl;

    lbl = new JLabel(ai.sqwkS());
    lbl.setName("lblSquawk");
    lbl.setFont(BOLD_FONT);
    lbl.setForeground(TEXT_COLOR);
    cmps[4] = lbl;

    lbl = new JLabel(ai.departureArrivalChar() + " " + ai.routeNameOrFix());
    lbl.setName("lblRoute");
    lbl.setFont(NORMAL_FONT);
    lbl.setForeground(TEXT_COLOR);
    cmps[1] = lbl;

    lbl = new JLabel(ai.altitudeSFixed() + " " + ai.climbDescendChar() + " " + ai.targetAltitudeSFixed());
    lbl.setName("lblAltitude");
    lbl.setFont(NORMAL_FONT);
    lbl.setForeground(TEXT_COLOR);
    cmps[3] = lbl;

    lbl = new JLabel(ai.headingSLong() + "° // " + ai.speedSLong());
    lbl.setName("lblHeadingAndSpeed");
    lbl.setFont(NORMAL_FONT);
    lbl.setForeground(TEXT_COLOR);
    cmps[5] = lbl;

    LayoutManager.fillGridPanel(this, 3, 2, 0, cmps);
  }

  private static Color getColor(Airplane.AirplaneInfo ai) {
    Color ret;
    // pozadi
    if (ai.isAirprox()) {
      ret = AIRPROX;
    } else {
      boolean isEven = index++ % 2 == 0;
      switch (ai.responsibleAtcType()) {
        case app:
          ret = isEven ? APP_EVEN : APP_ODD;
          break;
        case twr:
          ret = isEven ? TWR_EVEN : TWR_ODD;
          break;
        case ctr:
          ret = isEven ? CTR_EVEN : CTR_ODD;
          break;
        default:
          throw new ENotSupportedException();
      }
    }
    return ret;
  }
}
