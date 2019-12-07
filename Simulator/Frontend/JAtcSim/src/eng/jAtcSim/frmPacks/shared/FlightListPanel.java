package eng.jAtcSim.frmPacks.shared;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.events.Event;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.swing.LayoutManager;
import eng.jAtcSim.AppSettings;
import eng.jAtcSim.newLib.Simulation;
import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.airplanes.AirplaneDataFormatter;
import eng.jAtcSim.newLib.airplanes.AirproxType;
import eng.jAtcSim.newLib.airplanes.Callsign;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FlightListPanel extends JPanel {

  private static IList<Airplane.Airplane4Display> plns;
  public eng.eSystem.events.Event<FlightListPanel, Callsign> selectedCallsignChangedEvent = new eng.eSystem.events.Event(this);
  private Simulation sim;
  private JScrollPane pnlScroll;
  private JPanel pnlContent;
  private Callsign selectedCallsign;

  public void init(Simulation sim, AppSettings appSettings) {
    this.sim = sim;
    FlightStripPanel.setStripSettings(appSettings.getLoadedFlightStripSettings());

    pnlContent = LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.left, 4);
    pnlContent.setName("FlightListPanel_ContentPanel");
    pnlScroll = new JScrollPane(pnlContent);
    pnlScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    pnlScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

    this.setLayout(new BorderLayout());
    this.add(pnlScroll);
    this.setDoubleBuffered(true);

    pnlContent.setBackground(new Color(50, 50, 50));

    this.sim.getOnSecondElapsed().add(o -> updateList());
  }

  public Event<FlightListPanel, Callsign> getSelectedCallsignChangedEvent() {
    return selectedCallsignChangedEvent;
  }

  public Callsign getSelectedCallsign() {
    return selectedCallsign;
  }

  public void setSelectedCallsign(Callsign selectedCallsign) {
    Callsign bef = this.selectedCallsign;
    this.selectedCallsign = selectedCallsign;
    if (bef != this.selectedCallsign) {
      selectedCallsignChangedEvent.raise(this.selectedCallsign);
      updateList();
    }
  }

  private void updateList() {
    // init pri prvnim volani
    if (plns == null) {
      plns = new EList<>();
    }

    // znovunaplneni, kdyz nesedi pocet nebo poslední prvek (odebrani a pridani najednou)
    IReadOnlyList<Airplane.Airplane4Display> pi = sim.getPlanesToDisplay();
    if (plns.size() != pi.size() || pi.isEmpty() == false && !pi.getLast().callsign().equals(plns.getLast().callsign())) {
      plns.clear();
      for (Airplane.Airplane4Display ai : pi) {
        plns.add(ai);
      }
    }

    pnlContent.removeAll();
    FlightStripPanel.resetIndex();
    for (Airplane.Airplane4Display pln : plns) {
      FlightStripPanel pnlItem = createFlightStrip(pln);
      pnlItem.setName("FlightStrip_" + pln.callsign());
      pnlContent.add(pnlItem);
      pnlItem.getClickEvent().add((sender, callsign) -> {
        if (this.getSelectedCallsign() == callsign)
          this.setSelectedCallsign(null);
        else
          this.setSelectedCallsign((Callsign) callsign);
      });
    }

    this.revalidate();
    this.repaint();
  }

  private FlightStripPanel createFlightStrip(Airplane.Airplane4Display ai) {
    FlightStripPanel ret = new FlightStripPanel(this, ai);
    return ret;
  }

}

class FlightStripPanel extends JPanel {

  private static FlightStripSettings stripSettings;
  private static int index = 0;
  private static Font normalFont;
  private static Font boldFont;
  private final Event<FlightStripPanel, Callsign> clickEvent = new Event<>(this);
  private Callsign callsign;
  private FlightListPanel parent;

  public static void setStripSettings(FlightStripSettings stripSettings) {
    FlightStripPanel.stripSettings = stripSettings;

    normalFont = new Font(stripSettings.font.getName(), 0, stripSettings.font.getSize());
    boldFont = new Font(stripSettings.font.getName(), Font.BOLD, stripSettings.font.getSize());
  }

  public static void resetIndex() {
    index = 0;
  }

  public FlightStripPanel(FlightListPanel parent, Airplane.Airplane4Display ai) {

    this.parent = parent;
    this.callsign = ai.callsign();

    this.setLayout(new BorderLayout());

    LayoutManager.setFixedSize(this, stripSettings.flightStripSize);

    Color color = this.getColor(ai);
    this.setBackground(color);
    this.setForeground(stripSettings.textColor);

    fillContent(ai);

    this.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        clickEvent.raise(FlightStripPanel.this.callsign);
      }
    });
  }

  public Event<FlightStripPanel, Callsign> getClickEvent() {
    return clickEvent;
  }

  private Color getColor(Airplane.Airplane4Display ai) {
    Color ret;
    // pozadi
    if (ai.getAirprox() == AirproxType.full) {
      ret = stripSettings.airprox;
    } else if (ai.callsign() == parent.getSelectedCallsign()) {
      ret = stripSettings.selected;
    } else {
      boolean isEven = index++ % 2 == 0;
      switch (ai.responsibleAtc().getType()) {
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
          throw new EEnumValueUnsupportedException(ai.responsibleAtc().getType());
      }
    }
    return ret;
  }

  private void fillContent(Airplane.Airplane4Display ai) {
    Component[] cmps = new Component[6];
    JLabel lbl;

    lbl = new JLabel(ai.callsign().toString());
    lbl.setName("lblCallsign");
    lbl.setFont(boldFont);
    lbl.setForeground(stripSettings.textColor);
    cmps[0] = lbl;

    lbl = new JLabel(ai.planeType().name + " (" + ai.planeType().category + ")");
    lbl.setName("lblPlaneType");
    lbl.setFont(normalFont);
    lbl.setForeground(stripSettings.textColor);
    cmps[2] = lbl;

    lbl = new JLabel(AirplaneDataFormatter.formatSqwk(ai.squawk()));
    lbl.setName("lblSquawk");
    lbl.setFont(boldFont);
    lbl.setForeground(stripSettings.textColor);
    cmps[4] = lbl;

    String routeLabel = ai.getAssignedRoute() == null
        ? "(" + ai.entryExitPoint().getName() + ")"
        : ai.getExpectedRunwayThreshold().getName() + "/" + ai.getAssignedRoute().getName();
    lbl = new JLabel(AirplaneDataFormatter.getDepartureArrivalChar(ai.isDeparture()) + " " + routeLabel);
    lbl.setName("lblRoute");
    lbl.setFont(normalFont);
    lbl.setForeground(stripSettings.textColor);
    cmps[1] = lbl;

    lbl = new JLabel(
        AirplaneDataFormatter.formatAltitudeShort(ai.altitude(), true)
            + " " +
            AirplaneDataFormatter.getClimbDescendChar(ai.verticalSpeed())
            + " " +
            AirplaneDataFormatter.formatAltitudeShort(ai.targetAltitude(), true));
    lbl.setName("lblAltitude");
    lbl.setFont(normalFont);
    lbl.setForeground(stripSettings.textColor);
    cmps[3] = lbl;

    lbl = new JLabel(
        AirplaneDataFormatter.formatHeadingLong(ai.heading())
            + " // " +
            AirplaneDataFormatter.formatSpeedLong(ai.ias()));
    lbl.setName("lblHeadingAndSpeed");
    lbl.setFont(normalFont);
    lbl.setForeground(stripSettings.textColor);
    cmps[5] = lbl;

    lbl = new JLabel(getStatus(ai));
    lbl.setName("lblTextStatus");
    lbl.setFont(normalFont);
    lbl.setForeground(stripSettings.textColor);

    JPanel pnlFlightDataPanel = LayoutManager.createGridPanel(3, 2, 0, cmps);
    JPanel pnlTextStatus = LayoutManager.createBorderedPanel(16, 0, 0, 0, lbl);

    Color color = this.getColor(ai);
    pnlFlightDataPanel.setBackground(color);
    pnlFlightDataPanel.setForeground(stripSettings.textColor);

    pnlTextStatus.setBackground(color);
    pnlTextStatus.setForeground(stripSettings.textColor);

    JPanel pnl = LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.left, 0,
        pnlFlightDataPanel, pnlTextStatus);
    pnl.setBackground(color);

    pnl = LayoutManager.createBorderedPanel(stripSettings.stripBorder, pnl);
    pnl.setBackground(color);
    this.add(pnl);
  }

  private String getStatus(Airplane.Airplane4Display ai) {
    return ai.status();
  }


}
