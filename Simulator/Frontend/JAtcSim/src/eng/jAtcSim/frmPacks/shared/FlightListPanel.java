//package eng.jAtcSim.frmPacks.shared;
//
//import eng.eSystem.collections.EList;
//import eng.eSystem.collections.IList;
//import eng.eSystem.collections.IReadOnlyList;
//import eng.eSystem.events.Event;
//import eng.eSystem.exceptions.EEnumValueUnsupportedException;
//import eng.eSystem.swing.LayoutManager;
//import eng.jAtcSim.settings.AppSettings;
//import eng.jAtcSim.newLib.airplanes.AirproxType;
//import eng.jAtcSim.newLib.gameSim.IAirplaneInfo;
//import eng.jAtcSim.newLib.gameSim.ISimulation;
//import eng.jAtcSim.newLib.shared.Callsign;
//import eng.jAtcSim.newLib.shared.Format;
//import eng.jAtcSim.settings.FlightStripSettings;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//
//@Deprecated
//public class FlightListPanel extends JPanel {
//
//  private static IList<IAirplaneInfo> plns;
//  public eng.eSystem.events.Event<FlightListPanel, Callsign> selectedCallsignChangedEvent = new eng.eSystem.events.Event(this);
//  private ISimulation sim;
//  private JScrollPane pnlScroll;
//  private JPanel pnlContent;
//  private Callsign selectedCallsign;
//
//  public Callsign getSelectedCallsign() {
//    return selectedCallsign;
//  }
//
//  public void setSelectedCallsign(Callsign selectedCallsign) {
//    Callsign bef = this.selectedCallsign;
//    this.selectedCallsign = selectedCallsign;
//    if (bef != this.selectedCallsign) {
//      selectedCallsignChangedEvent.raise(this.selectedCallsign);
//      updateList();
//    }
//  }
//
//  public Event<FlightListPanel, Callsign> getSelectedCallsignChangedEvent() {
//    return selectedCallsignChangedEvent;
//  }
//
//  public void init(ISimulation sim, AppSettings appSettings) {
//    this.sim = sim;
//    FlightStripPanel.setStripSettings(appSettings.getFlightStripSettings());
//
//    pnlContent = LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.left, 4);
//    pnlContent.setName("FlightListPanel_ContentPanel");
//    pnlScroll = new JScrollPane(pnlContent);
//    pnlScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
//    pnlScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
//
//    this.setLayout(new BorderLayout());
//    this.add(pnlScroll);
//    this.setDoubleBuffered(true);
//
//    pnlContent.setBackground(new Color(50, 50, 50));
//
//    this.sim.registerOnSecondElapsed(s -> updateList());
//  }
//
//  private void updateList() {
//    // init pri prvnim volani
//    if (plns == null) {
//      plns = new EList<>();
//    }
//
//    // znovunaplneni, kdyz nesedi pocet nebo poslední prvek (odebrani a pridani najednou)
//    IReadOnlyList<IAirplaneInfo> pi = sim.getPlanesToDisplay();
//    if (plns.size() != pi.size() || pi.isEmpty() == false && !pi.getLast().callsign().equals(plns.getLast().callsign())) {
//      plns.clear();
//      for (IAirplaneInfo ai : pi) {
//        plns.add(ai);
//      }
//    }
//
//    pnlContent.removeAll();
//    FlightStripPanel.resetIndex();
//    for (IAirplaneInfo pln : plns) {
//      FlightStripPanel pnlItem = createFlightStrip(pln);
//      pnlItem.setName("FlightStrip_" + pln.callsign());
//      pnlContent.add(pnlItem);
//      pnlItem.getClickEvent().add((sender, callsign) -> {
//        if (this.getSelectedCallsign() == callsign)
//          this.setSelectedCallsign(null);
//        else
//          this.setSelectedCallsign((Callsign) callsign);
//      });
//    }
//
//    this.revalidate();
//    this.repaint();
//  }
//
//  private FlightStripPanel createFlightStrip(IAirplaneInfo ai) {
//    FlightStripPanel ret = new FlightStripPanel(this, ai);
//    return ret;
//  }
//
//}
//
//class FlightStripPanel extends JPanel {
//
//  private static FlightStripSettings stripSettings;
//  private static int index = 0;
//  private static Font normalFont;
//  private static Font boldFont;
//
//  public static void setStripSettings(FlightStripSettings stripSettings) {
//    FlightStripPanel.stripSettings = stripSettings;
//
//    normalFont = new Font(stripSettings.font.getName(), 0, stripSettings.font.getSize());
//    boldFont = new Font(stripSettings.font.getName(), Font.BOLD, stripSettings.font.getSize());
//  }
//
//  public static void resetIndex() {
//    index = 0;
//  }
//  private final Event<FlightStripPanel, Callsign> clickEvent = new Event<>(this);
//  private Callsign callsign;
//  private FlightListPanel parent;
//
//  public FlightStripPanel(FlightListPanel parent, IAirplaneInfo ai) {
//
//    this.parent = parent;
//    this.callsign = ai.callsign();
//
//    this.setLayout(new BorderLayout());
//
//    LayoutManager.setFixedSize(this, stripSettings.flightStripSize);
//
//    Color color = this.getColor(ai);
//    this.setBackground(color);
//    this.setForeground(stripSettings.textColor);
//
//    fillContent(ai);
//
//    this.addMouseListener(new MouseAdapter() {
//      @Override
//      public void mouseClicked(MouseEvent e) {
//        clickEvent.raise(FlightStripPanel.this.callsign);
//      }
//    });
//  }
//
//  public Event<FlightStripPanel, Callsign> getClickEvent() {
//    return clickEvent;
//  }
//
//  private Color getColor(IAirplaneInfo ai) {
//    Color ret;
//    // pozadi
//    if (ai.getAirprox() == AirproxType.full) {
//      ret = stripSettings.airprox;
//    } else if (ai.callsign() == parent.getSelectedCallsign()) {
//      ret = stripSettings.selected;
//    } else {
//      boolean isEven = index++ % 2 == 0;
//      if (ai.responsibleAtc() == null) {
//        ret = stripSettings.uncontrolled;
//      } else
//        switch (ai.responsibleAtc().getType()) {
//          case app:
//            ret = isEven ? stripSettings.app.even : stripSettings.app.odd;
//            break;
//          case twr:
//            ret = isEven ? stripSettings.twr.even : stripSettings.twr.odd;
//            break;
//          case ctr:
//            ret = isEven ? stripSettings.ctr.even : stripSettings.ctr.odd;
//            break;
//          default:
//            throw new EEnumValueUnsupportedException(ai.responsibleAtc().getType());
//        }
//    }
//    return ret;
//  }
//
//  private void fillContent(IAirplaneInfo ai) {
//    Component[] cmps = new Component[6];
//    JLabel lbl;
//
//    lbl = new JLabel(ai.callsign().toString());
//    lbl.setName("lblCallsign");
//    lbl.setFont(boldFont);
//    lbl.setForeground(stripSettings.textColor);
//    cmps[0] = lbl;
//
//    lbl = new JLabel(ai.planeType().name + " (" + ai.planeType().category + ")");
//    lbl.setName("lblPlaneType");
//    lbl.setFont(normalFont);
//    lbl.setForeground(stripSettings.textColor);
//    cmps[2] = lbl;
//
//    lbl = new JLabel(Format.formatSqwk(ai.squawk()));
//    lbl.setName("lblSquawk");
//    lbl.setFont(boldFont);
//    lbl.setForeground(stripSettings.textColor);
//    cmps[4] = lbl;
//
//    String routeLabel = ai.getRoutingLabel();
//    lbl = new JLabel(Format.Flight.getDepartureArrivalChar(ai.getArriDep()) + " " + routeLabel);
//    lbl.setName("lblRoute");
//    lbl.setFont(normalFont);
//    lbl.setForeground(stripSettings.textColor);
//    cmps[1] = lbl;
//
//    lbl = new JLabel(
//            Format.Altitude.toFLShort(ai.altitude())
//                    + " " +
//                    Format.VerticalSpeed.getClimbDescendChar(ai.verticalSpeed())
//                    + " " +
//                    Format.Altitude.toFLShort(ai.targetAltitude()));
//    lbl.setName("lblAltitude");
//    lbl.setFont(normalFont);
//    lbl.setForeground(stripSettings.textColor);
//    cmps[3] = lbl;
//
//    lbl = new JLabel(
//            Format.Heading.to(ai.heading())
//                    + " // " +
//                    Format.Speed.toShort(ai.ias()));
//    lbl.setName("lblHeadingAndSpeed");
//    lbl.setFont(normalFont);
//    lbl.setForeground(stripSettings.textColor);
//    cmps[5] = lbl;
//
//    lbl = new JLabel(getStatus(ai));
//    lbl.setName("lblTextStatus");
//    lbl.setFont(normalFont);
//    lbl.setForeground(stripSettings.textColor);
//
//    JPanel pnlFlightDataPanel = LayoutManager.createGridPanel(3, 2, 0, cmps);
//    JPanel pnlTextStatus = LayoutManager.createBorderedPanel(16, 0, 0, 0, lbl);
//
//    Color color = this.getColor(ai);
//    pnlFlightDataPanel.setBackground(color);
//    pnlFlightDataPanel.setForeground(stripSettings.textColor);
//
//    pnlTextStatus.setBackground(color);
//    pnlTextStatus.setForeground(stripSettings.textColor);
//
//    JPanel pnl = LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.left, 0,
//            pnlFlightDataPanel, pnlTextStatus);
//    pnl.setBackground(color);
//
//    pnl = LayoutManager.createBorderedPanel(stripSettings.stripBorder, pnl);
//    pnl.setBackground(color);
//    this.add(pnl);
//  }
//
//  private String getStatus(IAirplaneInfo ai) {
//    return ai.status();
//  }
//
//
//}
