package eng.jAtcSim.frmPacks.shared;

import eng.jAtcSim.lib.Simulation;
import eng.jAtcSim.lib.stats.Statistics;
import eng.jAtcSim.startup.LayoutManager;

import javax.swing.*;
import java.awt.*;

public class StatsPanel extends JPanel {

  Statistics stats;

  JLabel lblEmpty = new JLabel();

  JLabel lblElapsed = new JLabel("Seconds elapsed:");
  JLabel lvlElapsed = new JLabel("???");

  JLabel lblBusyDuration = new JLabel("Recalc duration:");
  JLabel lvlBusyDuration = new JLabel("???");

  JLabel lblFinished = new JLabel("Served planes:");
  JLabel ldlFinished = new JLabel("0");
  JLabel lalFinished = new JLabel("0");
  JLabel lvlFinished = new JLabel("0");

  JLabel lblCurrentInGame = new JLabel("Current planes:");
  JLabel ldlCurrentInGame = new JLabel("0");
  JLabel lalCurrentInGame = new JLabel("0");
  JLabel lvlCurrentInGame = new JLabel("0");

  JLabel lblCurrentApp = new JLabel("Served planes:");
  JLabel ldlCurrentApp = new JLabel("0");
  JLabel lalCurrentApp = new JLabel("0");
  JLabel lvlCurrentApp = new JLabel("0");

  public StatsPanel() {
    initComponents();
  }

  public void init(Simulation sim) {
    this.stats = sim.getStats();
    sim.getSecondElapsedEvent().add((a) -> update());
  }

  private void initComponents() {
    this.setPreferredSize(
        new Dimension(200, 200)
    );
    this.setBackground(
        new Color(50, 50, 50)
    );
    JLabel lbl = new JLabel("Stats:");
    lbl.setForeground(Color.white);
    this.add(lbl);


    JPanel pnlStats = LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.left, 0,
        LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.bottom, 0, lblElapsed, lvlElapsed),
        LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.bottom, 0, lblBusyDuration, lvlBusyDuration),
        LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.bottom, 0, lblFinished, ldlFinished, lalFinished, lvlFinished),
        LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.bottom, 0, lblCurrentInGame, ldlCurrentInGame, lalCurrentInGame, lvlCurrentInGame),
        LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.bottom, 0, lblCurrentApp, ldlCurrentApp, lalCurrentApp, lvlCurrentApp)
    );


//    JPanel pnlStats = LayoutManager.createGridPanel(1, 4, 0,
//        lblElapsed, lblEmpty, lblEmpty, lvlElapsed);
//        lblBusyDuration, lblEmpty, lblEmpty, lvlBusyDuration,
//        lblFinished, ldlFinished, lalFinished, lvlFinished,
//        lblCurrentInGame, ldlCurrentInGame, lalCurrentInGame, lvlCurrentInGame,
//        lblCurrentApp, ldlCurrentApp, lalCurrentApp, lvlCurrentApp
//    );
    this.add(pnlStats);
  }

  private void update() {

    setLblText(stats.secondsElapsed.get(), "%d", lvlElapsed);
    setLblText(stats.durationOfSecondElapse.get(), "%.3f", lvlBusyDuration);

    setLblText(stats.finishedDepartures.get(), "%d" ,ldlFinished);
    setLblText(stats.finishedArrivals.get(), "%d" ,lalFinished );
    setLblText(stats.finishedArrivals.get() + stats.finishedDepartures.get(), "%d" ,lvlFinished );

    

  }

  private void setLblText(Object value, String format, JLabel target) {
    String s = String.format(format, value);
    target.setText(s);
  }
}
