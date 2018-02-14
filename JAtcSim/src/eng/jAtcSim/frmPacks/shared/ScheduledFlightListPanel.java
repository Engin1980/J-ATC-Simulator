package eng.jAtcSim.frmPacks.shared;

import eng.jAtcSim.lib.Simulation;
import eng.jAtcSim.lib.traffic.Movement;
import eng.jAtcSim.startup.LayoutManager;

import javax.swing.*;
import java.awt.*;

public class ScheduledFlightListPanel extends JPanel {

  private JPanel pnlContent;
  private JScrollPane pnlScroll;
  private Simulation sim;


  public void init(Simulation sim) {
    this.sim = sim;

    pnlContent = LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.left, 4);
    pnlContent.setName("ScheduledFlightListPanel_ContentPanel");
    pnlScroll = new JScrollPane(pnlContent);
    pnlScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    pnlScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

    this.setLayout(new BorderLayout());
    this.add(pnlScroll);

    pnlContent.setBackground(new Color(50, 50, 50));

    this.sim.getSecondElapsedEvent().add(o -> updateList());
  }

  private void updateList() {
    Movement[] movements =
        this.sim.getScheduledMovements();

    pnlContent.removeAll();
    ScheduledFlightStripPanel.resetIndex();
    for (Movement mvm : movements) {
      JPanel pnlItem = createMovementStrip(mvm);
      pnlItem.setName("MovementStrip_" + mvm.getCallsign().toString());
      pnlContent.add(pnlItem);
    }

    this.revalidate();
  }

  private JPanel createMovementStrip(Movement mvm) {
    JPanel ret = new ScheduledFlightStripPanel(mvm);
    return ret;
  }

}

class ScheduledFlightStripPanel extends JPanel {
  private static final int WIDTH = 200;
  private static final int HEIGHT = 40;

  private static final int A = 75; // adjust colors
  private static final int B = 125; // adjust colors
  private static final Color TEXT_COLOR = new Color(230, 230, 230);
  private static final Color DEPARTURE_EVEN = new Color(0, 0, A);
  private static final Color DEPARTURE_ODD = new Color(0, 0, B);
  private static final Color ARRIVAL_EVEN = new Color(0, A, A);
  private static final Color ARRIVAL_ODD = new Color(0, B, B);
  private static int index = 0;
  private static String FONT_NAME = "Consolas"; // "Consolas"; // "Cambria" // "Calibri"; //"PxPlus IMG VGA9";
  private static int FONT_SIZE = 12;
  private static final Font NORMAL_FONT = new Font(FONT_NAME, 0, FONT_SIZE);
  private static final Font BOLD_FONT = new Font(FONT_NAME, Font.BOLD, FONT_SIZE);

  public ScheduledFlightStripPanel(Movement mvm) {

    this.setLayout(new BorderLayout());

    Dimension dim = new Dimension(WIDTH, HEIGHT);
    this.setPreferredSize(dim);
    this.setMinimumSize(dim);
    this.setMaximumSize(dim);

    Color color = ScheduledFlightStripPanel.getColor(mvm);
    this.setBackground(color);
    this.setForeground(TEXT_COLOR);

    fillContent(mvm, TEXT_COLOR, color);
  }

  public static void resetIndex() {
    index = 0;
  }

  private static Color getColor(Movement mvm) {
    Color ret;
    // pozadi
    boolean isEven = index++ % 2 == 0;
    if (mvm.isDeparture()) {
      ret = isEven ? DEPARTURE_EVEN : DEPARTURE_ODD;
    } else {
      ret = isEven ? ARRIVAL_EVEN : ARRIVAL_ODD;
    }
    return ret;
  }

  private static final Dimension CALLSIGN_DIMENSION = new Dimension(75,15);
  private static final Dimension FLAG_DIMENSION = new Dimension(25, 15);
  private static final Dimension TIME_DIMENSION = new Dimension(75,15);
  private static final Dimension DELAY_DIMENSION = FLAG_DIMENSION;

  private void fillContent(Movement movement, Color frColor, Color bgColor) {
    JLabel lblCallsign = new JLabel(movement.getCallsign().toString());
    setLabelFixedSize(lblCallsign, CALLSIGN_DIMENSION);

    JLabel lblDepartureArrival = new JLabel(movement.isDeparture() ? "DEP" : "ARR");
    setLabelFixedSize(lblDepartureArrival,FLAG_DIMENSION);
    JLabel lblIfrVfr = new JLabel(movement.isIfr() ? "IFR" : "VFR");
    setLabelFixedSize(lblIfrVfr,FLAG_DIMENSION);
    JLabel lblTime = new JLabel(movement.getInitTime().toTimeString());
    setLabelFixedSize(lblTime,TIME_DIMENSION);
    JLabel lblDelay = new JLabel(Integer.toString(movement.getDelayInMinutes()));
    setLabelFixedSize(lblDelay,DELAY_DIMENSION);


    JPanel firstLine = LayoutManager.createFlowPanel(
        LayoutManager.eVerticalAlign.middle, 0, lblCallsign, lblDepartureArrival, lblIfrVfr);
    JPanel secondLine = LayoutManager.createFlowPanel(
        LayoutManager.eVerticalAlign.middle, 0, lblTime, lblDelay);
    JPanel pnl = LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.left, 0, firstLine, secondLine );

    adjustComponentStyle(bgColor, frColor, NORMAL_FONT,
      firstLine, secondLine);

    adjustComponentStyle(bgColor, frColor, NORMAL_FONT,
        lblCallsign,lblDepartureArrival,lblIfrVfr,lblTime,lblDelay);

    lblCallsign.setFont(BOLD_FONT);

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

  private void adjustComponentStyle(Color bgColor, Color frColor, Font font, JComponent ... components ){
    for (JComponent component : components) {
      component.setForeground(frColor);
      component.setBackground(bgColor);
      component.setFont(font);
    }
  }
}