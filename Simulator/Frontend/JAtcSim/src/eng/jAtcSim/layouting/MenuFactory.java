package eng.jAtcSim.layouting;

import eng.eSystem.Tuple;
import eng.eSystem.collections.ISet;
import eng.eSystem.events.EventAnonymous;
import eng.eSystem.events.EventAnonymousSimple;
import eng.eSystem.functionalInterfaces.Consumer;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class MenuFactory {

  public static class MenuSimProxy {

    public enum ViewRequest {
      FLIGHT_STRIP,
      STATS,
      MOODS,
      SCHEDULED
    }

    public enum ToolRequest {
      COMMAND_BUTTONS,
      RECORDING
    }

    public final EventAnonymousSimple onQuit = new EventAnonymousSimple();
    public final EventAnonymousSimple onSave = new EventAnonymousSimple();
    public final EventAnonymousSimple onRecordingRequest = new EventAnonymousSimple();
    public final EventAnonymous<Integer> onSimSpeed = new EventAnonymous<>();
    public final EventAnonymousSimple onShowAbout = new EventAnonymousSimple();
    public final EventAnonymousSimple onShowProject = new EventAnonymousSimple();
    public final EventAnonymousSimple onTogglePause = new EventAnonymousSimple();
    public final EventAnonymousSimple onToggleSound = new EventAnonymousSimple();
    public final EventAnonymous<String> onOpenWindow = new EventAnonymous<>();

    public void setSimulationSpeed(int speedInMs) {
      this.onSimSpeed.raise(speedInMs);
    }

    public void toolRequest(ToolRequest tool) {
//COMMANDS
//      boolean isVis = pnlCommands.isVisible();
//      isVis = !isVis;
//      pnlCommands.setVisible(isVis);
//      s.setState(isVis);
    }

    public void viewRequest(ViewRequest view) {

      //mood
//      {
//        MoodHistoryPanel pnl = new MoodHistoryPanel();
//        pnl.init(this.parent.getSim().getStats().getFullMoodHistory());
//        SwingFactory.show(pnl, "Rating board");
//      })

      //RADAR
//      {
//        FrmView f = new FrmView();
//        f.init(this.parent);
//        f.setVisible(true);
//      });

      //STATS
//      StatsGraphPanel pnl = new StatsGraphPanel();
//      pnl.init(this.parent.getSim().getStats());
//      SwingFactory.show(pnl, "Statistical graphs");
//    });

      // right recent stats + cosi
//      boolean isVis = pnlRight.isVisible();
//      isVis = !isVis;
//      pnlRight.setVisible(isVis);
//      s.setState(isVis);

      // left flight-strip view
//      {
//        boolean isVis = pnlLeft.isVisible();
//        isVis = !isVis;
//        pnlLeft.setVisible(isVis);
//        s.setState(isVis);
//      }
    }

    void showAboutPage() {
      this.onShowAbout.raise();
    }

    void showProjectPage() {
      this.onShowProject.raise();
    }

    void togglePause() {
      this.onTogglePause.raise();
    }

    void toggleSound() {
      this.onToggleSound.raise();
    }

    void quit() {
      this.onQuit.raise();
    }

    void recordingViewRequest() {
      this.onRecordingRequest.raise();
    }

    void saveSimulation() {
      this.onSave.raise();
    }
  }

  public static MenuSimProxy buildMenu(JFrame frame, ISet<String> frameNames) {
    Tuple<JMenuBar, MenuSimProxy> tmp = buildMenu(frameNames);
    frame.setJMenuBar(tmp.getA());
    return tmp.getB();
  }

  private static Tuple<JMenuBar, MenuSimProxy> buildMenu(ISet<String> frameNames) {
    JMenuBar ret = new JMenuBar();
    MenuSimProxy sp = new MenuSimProxy();

    JMenu mnuFile = new JMenu("File");
    mnuFile.setMnemonic(KeyEvent.VK_F);
    ret.add(mnuFile);

    JMenu mnuSimulation = new JMenu("Simulation");
    mnuSimulation.setMnemonic(KeyEvent.VK_S);
    ret.add(mnuSimulation);

    JMenu mnuView = new JMenu("View");
    mnuView.setMnemonic(KeyEvent.VK_V);
    ret.add(mnuView);

    JMenu mnuHelp = new JMenu("Help");
    mnuHelp.setMnemonic(KeyEvent.VK_H);
    ret.add(mnuHelp);

    {
      buildMenuItem(mnuFile, "Save", 's', s -> sp.saveSimulation());
      mnuFile.addSeparator();
      buildMenuItem(mnuFile, "Quit", 'q', s -> sp.quit());
    }

    {
      buildMenuItem(mnuSimulation, "Pause/Resume", 'p', s -> sp.togglePause());
      JMenu mnuSpeed = new JMenu("Set speed");
      mnuSpeed.setMnemonic(KeyEvent.VK_S);
      mnuSimulation.add(mnuSpeed);
      {
        buildMenuItem(mnuSpeed, "Frozen (5000ms)", null, s -> sp.setSimulationSpeed(5000));
        buildMenuItem(mnuSpeed, "Slow (2000ms)", null, s -> sp.setSimulationSpeed(2000));
        buildMenuItem(mnuSpeed, "Real (1000ms)", null, s -> sp.setSimulationSpeed(1000));
        buildMenuItem(mnuSpeed, "Accelerated (750ms)", null, s -> sp.setSimulationSpeed(750));
        buildMenuItem(mnuSpeed, "Arcade (500ms)", null, s -> sp.setSimulationSpeed(500));
        buildMenuItem(mnuSpeed, "Fast (250ms)", null, s -> sp.setSimulationSpeed(250));
        buildMenuItem(mnuSpeed, "Skip (50ms)", null, s -> sp.setSimulationSpeed(50));
      }
      mnuSimulation.addSeparator();
      buildCheckMenuItem(mnuSimulation, "Sounds", true, 's', s -> sp.toggleSound());
      buildMenuItem(mnuSimulation, "Recording", 'r', s -> sp.recordingViewRequest());
    }

    {
      buildCheckMenuItem(mnuView, "Flight strips", true, 'f',
              s -> sp.viewRequest(MenuSimProxy.ViewRequest.FLIGHT_STRIP));
      buildCheckMenuItem(mnuView, "Command buttons", true, 'c',
              s -> sp.toolRequest(MenuSimProxy.ToolRequest.COMMAND_BUTTONS));
      buildCheckMenuItem(mnuView, "Scheduled & Stats", true, 's',
              s -> sp.viewRequest(MenuSimProxy.ViewRequest.SCHEDULED));

      mnuView.addSeparator();
      buildMenuItem(mnuView, "Show mood results", null, s -> sp.viewRequest(MenuSimProxy.ViewRequest.MOODS));
      buildMenuItem(mnuView, "Show stats graphs", null, s -> sp.viewRequest(MenuSimProxy.ViewRequest.STATS));
      {
        JMenu mnu = new JMenu("Open window");
        mnuView.add(mnu);
        for (String frameName : frameNames) {
          buildMenuItem(mnu, frameName, null, s -> sp.onOpenWindow.raise(frameName));
        }
      }
    }

    {
      buildMenuItem(mnuHelp, "Project web pages", null, s -> sp.showProjectPage());
      mnuHelp.addSeparator();
      buildMenuItem(mnuHelp, "About", 'o', s -> sp.showAboutPage());
    }

    return new Tuple<>(ret, sp);
  }

  private static void buildMenuItem(JMenu mnu, String label, Character charMnemonic, Consumer<JMenuItem> action) {
    JMenuItem item = new JMenuItem(label);
    item.setName("mnu" + label);
    if (charMnemonic != null) item.setMnemonic(charMnemonic);
    mnu.add(item);
    if (action != null)
      item.addActionListener(e -> action.invoke(item));
  }

  private static void buildCheckMenuItem(JMenu mnu, String label, boolean checkBoxState,
                                         Character charMnemonic,
                                         Consumer<JCheckBoxMenuItem> action) {
    JCheckBoxMenuItem item = new JCheckBoxMenuItem(label, checkBoxState);
    item.setName("mnu" + label);
    if (charMnemonic != null) item.setMnemonic(charMnemonic);
    mnu.add(item);
    if (action != null)
      item.addActionListener(e -> action.invoke(item));
  }
}
