package eng.jAtcSim.frmPacks.shared;

import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.AppSettings;
import eng.jAtcSim.XmlLoadHelper;
import eng.jAtcSim.lib.Simulation;
import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.lib.traffic.Movement;
import eng.jAtcSim.shared.LayoutManager;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Paths;

public class ScheduledFlightListPanel extends JPanel {

  private JPanel pnlContent;
  private JScrollPane pnlScroll;
  private Simulation sim;
  private Callsign firstItemCallsign = null;
  private Callsign lastItemCallsign = null;
  private Integer itemCount = null;


  public void init(Simulation sim, AppSettings appSettings) {
    this.sim = sim;
    ScheduledFlightStripPanel.setStripSettings(
        XmlLoadHelper.loadStripSettings(appSettings.stripSettingsFile.toString()));

    pnlContent = LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.left, 4);
    pnlContent.setName("ScheduledFlightListPanel_ContentPanel");
    pnlScroll = new JScrollPane(pnlContent);
    pnlScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    pnlScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

    this.setLayout(new BorderLayout());
    this.add(pnlScroll);
    this.setDoubleBuffered(true);

    pnlContent.setBackground(new Color(50, 50, 50));

    this.sim.getSecondElapsedEvent().add(o -> updateList());
  }

  private void updateList() {
    if (refreshNeeded()) {
      IReadOnlyList<Movement> movements =
          this.sim.getScheduledMovements().orderBy(q -> q.getAppExpectedTime());

      pnlContent.removeAll();
      ScheduledFlightStripPanel.resetIndex();
      for (Movement mvm : movements) {
        JPanel pnlItem = createMovementStrip(mvm);
        pnlItem.setName("MovementStrip_" + mvm.getCallsign().toString());
        pnlContent.add(pnlItem);
      }

      this.itemCount = this.sim.getScheduledMovements().size();
      if (itemCount == 0) {
        this.firstItemCallsign = null;
        this.lastItemCallsign = null;
      } else {
        this.firstItemCallsign = this.sim.getScheduledMovements().getFirst().getCallsign();
        this.lastItemCallsign = this.sim.getScheduledMovements().getLast().getCallsign();
      }

      this.revalidate();
      this.repaint();
    }
  }

  private boolean refreshNeeded() {
    IReadOnlyList<Movement> mvm = this.sim.getScheduledMovements();
    if (itemCount == null || itemCount != mvm.size())
      return true;
    if (itemCount > 0) {
      if (this.firstItemCallsign == null || this.firstItemCallsign.equals(mvm.getFirst().getCallsign()) == false)
        return true;
      if (this.lastItemCallsign == null || this.lastItemCallsign.equals(mvm.getLast().getCallsign()) == false)
        return true;
    }
    return false;
  }

  private JPanel createMovementStrip(Movement mvm) {
    JPanel ret = new ScheduledFlightStripPanel(mvm);
    return ret;
  }

}

class ScheduledFlightStripPanel extends JPanel {
  private static final Dimension CALLSIGN_DIMENSION = new Dimension(75, 15);
  private static final Dimension FLAG_DIMENSION = new Dimension(25, 15);
  private static final Dimension TIME_DIMENSION = new Dimension(75, 15);
  private static final Dimension DELAY_DIMENSION = FLAG_DIMENSION;
  private static FlightStripSettings stripSettings;
  private static int index = 0;
  private static Font normalFont;
  private static Font boldFont;

  public static void setStripSettings(FlightStripSettings stripSettings) {
    ScheduledFlightStripPanel.stripSettings = stripSettings;

    normalFont = new Font(stripSettings.font.getName(), 0, stripSettings.font.getSize());
    boldFont = new Font(stripSettings.font.getName(), Font.BOLD, stripSettings.font.getSize());
  }

  public static void resetIndex() {
    index = 0;
  }

  private static Color getColor(Movement mvm) {
    Color ret;
    // pozadi
    boolean isEven = index++ % 2 == 0;
    if (mvm.isDeparture()) {
      ret = isEven ? stripSettings.twr.even : stripSettings.twr.odd;
    } else {
      ret = isEven ? stripSettings.ctr.even : stripSettings.ctr.odd;
    }
    return ret;
  }

  public ScheduledFlightStripPanel(Movement mvm) {

    this.setLayout(new BorderLayout());

    Dimension dim = stripSettings.size;
    this.setPreferredSize(dim);
    this.setMinimumSize(dim);
    this.setMaximumSize(dim);

    Color color = ScheduledFlightStripPanel.getColor(mvm);
    this.setBackground(color);
    this.setForeground(stripSettings.textColor);

    fillContent(mvm, stripSettings.textColor, color);
  }

  private void fillContent(Movement movement, Color frColor, Color bgColor) {
    JLabel lblCallsign = new JLabel(movement.getCallsign().toString());
    setLabelFixedSize(lblCallsign, CALLSIGN_DIMENSION);

    JLabel lblDepartureArrival = new JLabel(movement.isDeparture() ? "DEP" : "ARR");
    setLabelFixedSize(lblDepartureArrival, FLAG_DIMENSION);

    JLabel lblTypeName = new JLabel(movement.getAirplaneType().name);
    setLabelFixedSize(lblTypeName, CALLSIGN_DIMENSION);

    JLabel lblTime = new JLabel(movement.getAppExpectedTime().toTimeString());
    setLabelFixedSize(lblTime, TIME_DIMENSION);
    JLabel lblDelay = new JLabel(Integer.toString(movement.getDelayInMinutes()));
    setLabelFixedSize(lblDelay, DELAY_DIMENSION);


    JPanel firstLine = LayoutManager.createFlowPanel(
        LayoutManager.eVerticalAlign.middle, 0, lblCallsign, lblTypeName, lblDepartureArrival);
    JPanel secondLine = LayoutManager.createFlowPanel(
        LayoutManager.eVerticalAlign.middle, 0, lblTime, lblDelay);
    JPanel pnl = LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.left, 0, firstLine, secondLine);

    adjustComponentStyle(bgColor, frColor, normalFont,
        firstLine, secondLine);

    adjustComponentStyle(bgColor, frColor, normalFont,
        lblCallsign, lblTypeName, lblDepartureArrival, lblTime, lblDelay);

    lblCallsign.setFont(boldFont);

    pnl.setBackground(bgColor);
    pnl = LayoutManager.createBorderedPanel(4, pnl);
    pnl.setBackground(bgColor);
    this.add(pnl);
  }

  private void setLabelFixedSize(JLabel lbl, Dimension dimension) {
    lbl.setLayout(new BorderLayout());
    lbl.setMinimumSize(dimension);
    lbl.setMaximumSize(dimension);
  }

  private void adjustComponentStyle(Color bgColor, Color frColor, Font font, JComponent... components) {
    for (JComponent component : components) {
      component.setForeground(frColor);
      component.setBackground(bgColor);
      component.setFont(font);
    }
  }
}