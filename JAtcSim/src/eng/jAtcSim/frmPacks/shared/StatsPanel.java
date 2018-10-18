package eng.jAtcSim.frmPacks.shared;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.Simulation;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.stats.read.StatsView;
import eng.jAtcSim.lib.stats.Statistics;
import eng.eSystem.swing.LayoutManager;

import javax.swing.*;
import java.awt.*;

import static eng.jAtcSim.lib.stats.Statistics.toTime;

public class StatsPanel extends JPanel {

  private static Color bgColor = new Color(50, 50, 50);
  private static Color frColor = new Color(200, 200, 200);
  Statistics stats;
  JLabel lblElapsed = new JLabel("Seconds elapsed:");
  JLabel lvlElapsed = new JLabel("-");
  JLabel lblBusyDuration = new JLabel("Recalc duration:");
  JLabel lvlBusyDuration = new JLabel("-");
  JLabel lblFinished = new JLabel("Served planes:");
  JLabel lvlFinished = new JLabel("-");
  JLabel lblMaxInGame = new JLabel("Max in simulation:");
  JLabel lvlMaxInGame = new JLabel("-");
  JLabel lblMovementsGame = new JLabel("Moves/hour:");
  JLabel lvlMovementsGame = new JLabel("-");
  JLabel lblMaxApp = new JLabel("Max under APP:");
  JLabel lvlMaxApp = new JLabel("-");
  JLabel lblCurInGame = new JLabel("Now in simulation:");
  JLabel lvlCurInGame = new JLabel("-");
  JLabel lblCurApp = new JLabel("Now under APP:");
  JLabel lvlCurApp = new JLabel("-");
  JLabel lblHpCount = new JLabel("H-P count:");
  JLabel lvlHpCount = new JLabel("-");
  JLabel lblHpTime = new JLabel("H-P time:");
  JLabel lvlHpTime = new JLabel("-");

  JLabel lblErrors = new JLabel("Errs (a/m):");
  JLabel lvlErrors = new JLabel("-");

  JLabel lblDelay = new JLabel("Delay:");
  JLabel lvlDelay = new JLabel("-");

  public StatsPanel() {
    initComponents();
  }

  public void init(Simulation sim) {
    this.stats = sim.getStats();
    sim.getOnSecondElapsed().add((a) -> update());
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
        lblMovementsGame,
        lblMaxInGame,
        lblCurInGame,
        lblMaxApp,
        lblCurApp,
        lblErrors,
        lblDelay,
        lblHpCount,
        lblHpTime
    );

    JPanel pnlValues = LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.left, 0,
        lvlElapsed,
        lvlBusyDuration,
        lvlFinished,
        lvlMovementsGame,
        lvlMaxInGame,
        lvlCurInGame,
        lvlMaxApp,
        lvlCurApp,
        lvlErrors,
        lvlDelay,
        lvlHpCount,
        lvlHpTime
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

  private StatsView view;

  private void update() {

    view = stats.createView(Acc.now().addHours(-1));

    lvlElapsed.setText(toTime(view.getSecondStats().getSecondsElapsed()));
    lvlBusyDuration.setText(String.format("%.3f (%.3f)",
        view.getSecondStats().getDuration().getMean(),
        view.getSecondStats().getDuration().getMaximum()
    ));

    updateFinished();
    updateMaxAll();
    updateMaxApp();
    updateCurAll();
    updateCurApp();
    updateMovementsPerHour();
    updateHoldingPointInfo();
    updateDelays();
    updateAirproxAndMrvas();

  }

  private void updateAirproxAndMrvas() {
    double air = view.getErrors().getAirproxes().getMean();
    double mrv = view.getErrors().getMrvas().getMean();
    String s = String.format("%.1f‰ / %.1f‰", air, mrv);
    lvlErrors.setText(s);
  }

  private void updateDelays() {
    ETime tmean = new ETime((int) view.getPlanes().getDelay().getTogether().getMean());
    ETime tmax = new ETime((int) view.getPlanes().getDelay().getTogether().getMaximum());

    String s = String.format("%s / %s", tmean.toMinuteSecondString(), tmax.toMinuteSecondString());
    lvlDelay.setText(s);
  }

  private void updateHoldingPointInfo() {
    String tmp = String.format("%.0f / %.0f",
        view.getHoldingPoint().getCount().getCurrent(),
        view.getHoldingPoint().getCount().getMaximum());
    lvlHpCount.setText(tmp);

    tmp = String.format("%s / %s",
        toTime(view.getHoldingPoint().getDelay().getMean()),
        toTime(view.getHoldingPoint().getDelay().getMaximum())
    );
    lvlHpTime.setText(tmp);
  }

  private void updateMovementsPerHour() {
    double deps = view.getPlanes().getFinishedPlanes().getDepartures().getCount() *
        view.getPlanes().getFinishedPlanes().getDepartures().getMean();
    double arrs = view.getPlanes().getFinishedPlanes().getArrivals().getCount() *
        view.getPlanes().getFinishedPlanes().getArrivals().getMean();
    double tots = view.getPlanes().getFinishedPlanes().getTogether().getCount() *
        view.getPlanes().getFinishedPlanes().getTogether().getMean();
    double secs = view.getSecondStats().getSecondsElapsed();
    secs /= 3600d;
    deps /= secs;
    arrs /= secs;
    tots /= secs;
    String s = String.format("%.1f / %.1f / %.1f",
        deps,
        arrs,
        tots
    );
    lvlMovementsGame.setText(s);
  }

  private void updateCurApp() {
    String s = String.format("%.0f / %.0f / %.0f",
        view.getPlanes().getPlanesUnderApp().getDepartures().getCurrent(),
        view.getPlanes().getPlanesUnderApp().getArrivals().getCurrent(),
        view.getPlanes().getPlanesUnderApp().getTogether().getCurrent()
    );
    lvlCurApp.setText(s);
  }

  private void updateCurAll() {
    String s = String.format("%.0f / %.0f / %.0f",
        view.getPlanes().getPlanesInSim().getDepartures().getCurrent(),
        view.getPlanes().getPlanesInSim().getArrivals().getCurrent(),
        view.getPlanes().getPlanesInSim().getTogether().getCurrent()
    );
    lvlCurInGame.setText(s);
  }

  private void updateMaxApp() {
    String s = String.format("%.0f / %.0f / %.0f",
        view.getPlanes().getPlanesUnderApp().getDepartures().getMaximum(),
        view.getPlanes().getPlanesUnderApp().getArrivals().getMaximum(),
        view.getPlanes().getPlanesUnderApp().getTogether().getMaximum()
    );
    lvlMaxApp.setText(s);
  }

  private void updateMaxAll() {
    String s = String.format("%.0f / %.0f / %.0f",
        view.getPlanes().getPlanesInSim().getDepartures().getMaximum(),
        view.getPlanes().getPlanesInSim().getArrivals().getMaximum(),
        view.getPlanes().getPlanesInSim().getTogether().getMaximum()
    );
    lvlMaxInGame.setText(s);
  }

  private void updateFinished() {
    String s = String.format("%d / %d / %d",
        view.getPlanes().getFinishedPlanes().getDepartures().getCount(),
        view.getPlanes().getFinishedPlanes().getArrivals().getCount(),
        view.getPlanes().getFinishedPlanes().getTogether().getCount()
    );
    lvlFinished.setText(s);
  }
}
