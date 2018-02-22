package eng.jAtcSim.frmPacks.shared;

import eng.jAtcSim.AppSettings;
import eng.jAtcSim.XmlLoadHelper;
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

  public void init(Simulation sim, AppSettings appSettings) {
    this.sim = sim;
    FlightStripPanel.setStripSettings(
        XmlLoadHelper.loadStripSettings(appSettings.resFolder + "stripSettings.xml"));

    pnlContent = LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.left, 4);
    pnlContent.setName("FlightListPanel_ContentPanel");
    pnlScroll = new JScrollPane(pnlContent);
    pnlScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    pnlScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

    this.setLayout(new BorderLayout());
    this.add(pnlScroll);

    pnlContent.setBackground(new Color(50, 50, 50));

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
      pnlItem.setName("FlightStrip_" + pln.callsignS());
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

  private static FlightStripSettings stripSettings;
  private static int index = 0;
  private static Font normalFont;
  private static Font boldFont;

  public FlightStripPanel(Airplane.AirplaneInfo ai) {

    this.setLayout(new BorderLayout());

    Dimension dim = stripSettings.size;
    this.setPreferredSize(dim);
    this.setMinimumSize(dim);
    this.setMaximumSize(dim);

    Color color = FlightStripPanel.getColor(ai);
    this.setBackground(color);
    this.setForeground(stripSettings.textColor);

    fillContent(ai);
  }

  public static void setStripSettings(FlightStripSettings stripSettings) {
    FlightStripPanel.stripSettings = stripSettings;

    normalFont = new Font(stripSettings.font.getName(), 0, stripSettings.font.getSize());
    boldFont = new Font(stripSettings.font.getName(), Font.BOLD, stripSettings.font.getSize());
  }

  public static void resetIndex() {
    index = 0;
  }

  private static Color getColor(Airplane.AirplaneInfo ai) {
    Color ret;
    // pozadi
    if (ai.isAirprox()) {
      ret = stripSettings.airprox;
    } else {
      boolean isEven = index++ % 2 == 0;
      switch (ai.responsibleAtcType()) {
        case app:
          ret = isEven ? stripSettings.app.even : stripSettings.app.odd;
          break;
        case twr:
          ret = isEven ? stripSettings.twr.even : stripSettings.twr.odd;
          break;
        case ctr:
          ret = isEven ? stripSettings.ctr.even : stripSettings.ctr.odd;
          break;
        default:
          throw new ENotSupportedException();
      }
    }
    return ret;
  }

  private void fillContent(Airplane.AirplaneInfo ai) {
    Component[] cmps = new Component[6];
    JLabel lbl;

    lbl = new JLabel(ai.callsignS());
    lbl.setName("lblCallsign");
    lbl.setFont(boldFont);
    lbl.setForeground(stripSettings.textColor);
    cmps[0] = lbl;

    lbl = new JLabel(ai.planeType() + " (" + ai.typeCategory() + ")");
    lbl.setName("lblPlaneType");
    lbl.setFont(normalFont);
    lbl.setForeground(stripSettings.textColor);
    cmps[2] = lbl;

    lbl = new JLabel(ai.sqwkS());
    lbl.setName("lblSquawk");
    lbl.setFont(boldFont);
    lbl.setForeground(stripSettings.textColor);
    cmps[4] = lbl;

    lbl = new JLabel(ai.departureArrivalChar() + " " + ai.routeNameOrFix());
    lbl.setName("lblRoute");
    lbl.setFont(normalFont);
    lbl.setForeground(stripSettings.textColor);
    cmps[1] = lbl;

    lbl = new JLabel(ai.altitudeSFixed() + " " + ai.climbDescendChar() + " " + ai.targetAltitudeSFixed());
    lbl.setName("lblAltitude");
    lbl.setFont(normalFont);
    lbl.setForeground(stripSettings.textColor);
    cmps[3] = lbl;

    lbl = new JLabel(ai.headingSLong() + "°//" + ai.speedSLong());
    lbl.setName("lblHeadingAndSpeed");
    lbl.setFont(normalFont);
    lbl.setForeground(stripSettings.textColor);
    cmps[5] = lbl;

    LayoutManager.fillGridPanel(this, 3, 2, 0, cmps);
  }
}
