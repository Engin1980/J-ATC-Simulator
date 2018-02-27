package eng.jAtcSim.frmPacks.shared;

import eng.jAtcSim.lib.Simulation;
import eng.jAtcSim.lib.stats.Statistics;
import eng.jAtcSim.startup.LayoutManager;

import javax.swing.*;
import java.awt.*;

public class StatsPanel extends JPanel {

  private static Color bgColor = new Color(50, 50, 50);
  private static Color frColor = new Color(200, 200, 200);
  Statistics stats;
  JLabel lblElapsed = new JLabel("Seconds elapsed:");
  JLabel lvlElapsed = new JLabel("???");
  JLabel lblBusyDuration = new JLabel("Recalc duration:");
  JLabel lvlBusyDuration = new JLabel("???");
  JLabel lblFinished = new JLabel("Served planes:");
  JLabel lvlFinished = new JLabel("0");
  JLabel lblMaxInGame = new JLabel("Max in game:");
  JLabel lvlMaxInGame = new JLabel("0");
  JLabel lblMaxApp = new JLabel("max under APP:");
  JLabel lvlMaxApp = new JLabel("0");
  JLabel lblCurInGame = new JLabel("Now in game:");
  JLabel lvlCurInGame = new JLabel("0");
  JLabel lblCurApp = new JLabel("Now under APP:");
  JLabel lvlCurApp = new JLabel("0");

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

    JPanel pnlLabels = LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.left, 0,
        lblElapsed,
        lblBusyDuration,
        lblFinished,
        lblMaxInGame,
        lblCurInGame,
        lblMaxApp,
        lblCurApp
    );

    JPanel pnlValues = LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.left, 0,
        lvlElapsed,
        lvlBusyDuration,
        lvlFinished,
        lvlMaxInGame,
        lvlCurInGame,
        lvlMaxApp,
        lvlCurApp
    );

    JPanel pnlStats = LayoutManager.createBorderedPanel(null, null, pnlLabels, pnlValues, null);

    LayoutManager.fillBorderedPanel(this, 4, pnlStats);

    this.add(pnlStats);

    setDarkTheme(this);
  }

  private void setDarkTheme(Component c) {
    c.setBackground(bgColor);
    c.setForeground(frColor);

    if (c instanceof Container) {
      for (Component i : ((Container) c).getComponents()) {
        setDarkTheme(i);
      }
    }


  }

  private void update() {

    setLblText(stats.secondsElapsed.get(), "%d", lvlElapsed);
    setLblText(stats.durationOfSecondElapse.get(), "%.3f", lvlBusyDuration);

    updateFinished();
    updateMaxAll();
    updateMaxApp();
    updateCurAll();
    updateCurApp();


  }

  private void updateCurApp() {
    String s = String.format("%d / %d / %d",
        stats.currentPlanes.appDepartures,
        stats.currentPlanes.appArrivals,
        stats.currentPlanes.appTotal
    );
    lvlCurApp.setText(s);
  }

  private void updateCurAll() {
    String s = String.format("%d / %d / %d",
        stats.currentPlanes.departures,
        stats.currentPlanes.arrivals,
        stats.currentPlanes.total
    );
    lvlCurInGame.setText(s);
  }

  private void updateMaxApp() {
    String s = String.format("%d / %d / %d",
        stats.maximumResponsiblePlanes.departures.getInt(),
        stats.maximumResponsiblePlanes.arrivals.getInt(),
        stats.maximumResponsiblePlanes.total.getInt()
    );
    lvlMaxApp.setText(s);
  }

  private void updateMaxAll() {
    String s = String.format("%d / %d / %d",
        stats.maxumumTotalPlanes.departures.getInt(),
        stats.maxumumTotalPlanes.arrivals.getInt(),
        stats.maxumumTotalPlanes.total.getInt()
    );
    lvlMaxInGame.setText(s);
  }

  private void updateFinished() {
    String s = String.format("%d / %d / %d",
        stats.finishedDepartures.get(),
        stats.finishedArrivals.get(),
        stats.finishedDepartures.get() + stats.finishedArrivals.get()
    );
    lvlFinished.setText(s);
  }

  private void setLblText(Object value, String format, JLabel target) {
    String s = String.format(format, value);
    target.setText(s);
  }
}
