package eng.jAtcSim.newPacks;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IMap;
import eng.eSystem.collections.ISet;
import eng.eSystem.events.EventAnonymousSimple;
import eng.eSystem.exceptions.ApplicationException;
import eng.eSystem.exceptions.ToDoException;
import eng.eSystem.utilites.ExceptionUtils;
import eng.eSystem.utilites.awt.ComponentUtils;
import eng.jAtcSim.Stylist;
import eng.jAtcSim.abstractRadar.global.SoundManager;
import eng.jAtcSim.app.FrmAbout;
import eng.jAtcSim.layouting.Layout;
import eng.jAtcSim.layouting.MenuFactory;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.gameSim.IGame;
import eng.jAtcSim.newLib.gameSim.ISimulation;
import eng.jAtcSim.newLib.shared.logging.LogItemType;
import eng.jAtcSim.newLib.speeches.system.user2system.TickSpeedRequest;
import eng.jAtcSim.newPacks.context.ViewGlobalEventContext;
import eng.jAtcSim.newPacks.layout.JFrameFactory;
import eng.jAtcSim.newPacks.layout.JFrameInfo;
import eng.jAtcSim.newPacks.layout.JPanelInfo;
import eng.jAtcSim.newPacks.utils.ViewGameInfo;
import eng.jAtcSim.settings.AppSettings;
import eng.jAtcSim.shared.MessageBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class NewPack {
  private IGame game;
  private ISimulation sim;
  private Area area;
  private Airport airport;
  private AppSettings appSettings;
  private Layout layout;
  private IList<JFrameInfo> frameInfos;
  private final ViewGlobalEventContext viewGlobalEventContext = new ViewGlobalEventContext();
  public final EventAnonymousSimple onSave = new EventAnonymousSimple();
  public final EventAnonymousSimple onAutoSave = new EventAnonymousSimple();
  public final EventAnonymousSimple onQuit = new EventAnonymousSimple();
  private ViewGameInfo vii;

  public void applyCustomData(IMap<String, Object> customData) {
    int fi = 0;
    for (JFrameInfo frameInfo : this.frameInfos) {
      int pi = 0;
      for (JPanelInfo panel : frameInfo.getPanels()) {
        if (panel.getView() instanceof IViewWithCustomData == false) continue;
        String key = sf("%d;%d", fi, pi);
        Object value = customData.get(key);
        ((IViewWithCustomData) panel.getView()).setCustomDataOnLoad(value);
        pi++;
      }
      fi++;
    }
  }

  public IMap<String, Object> collectCustomData() {
    EMap<String, Object> ret = new EMap<>();

    int fi = 0;
    for (JFrameInfo frameInfo : this.frameInfos) {
      int pi = 0;
      for (JPanelInfo panel : frameInfo.getPanels()) {
        if (panel.getView() instanceof IViewWithCustomData == false) continue;
        String key = sf("%d;%d", fi, pi);
        Object value = ((IViewWithCustomData) panel.getView()).getCustomDataToSave();
        ret.set(key, value);
        pi++;
      }
      fi++;
    }

    return ret;
  }

  public void focus() {
    this.frameInfos.getFirst().getFrame().requestFocus();
  }

  public IGame getGame() {
    return game;
  }

  public void init(IGame game, Layout layout, AppSettings appSettings) {
    this.appSettings = appSettings;
    this.layout = layout;

    // init sim & area
    this.game = game;
    this.sim = game.getSimulation();
    this.area = game.getSimulation().getArea();
    this.airport = sim.getAirport();

    JFrameFactory frameFactory = new JFrameFactory();
    frameInfos = frameFactory.buildFrames(layout);

    IList<JPanelInfo> allPanels = frameInfos.selectMany(q -> q.getPanels());
    frameInfos
            .where(q -> q.getMenuSimProxy() != null)
            .forEach(q -> bindMenuProxy(q.getMenuSimProxy(), this.sim));
    frameInfos.forEach(q -> Stylist.apply(q.getFrame(), true));

    vii = new ViewGameInfo();
    vii.setSimulation(this.sim);
    vii.setAirport(this.airport);
    vii.setSettings(this.appSettings);
    vii.setUserAtcId(this.sim.getUserAtcIds().get(0));
    vii.setDynamicAirplaneSpeechFormatter(this.appSettings.getDynamicPlaneFormatter()); //TODO improve somehow

    allPanels.forEach(q -> q.getView().init(q.getPanel(), vii, q.getOptions(), viewGlobalEventContext));

    //printSummary(frames);

    if (appSettings.autosave.intervalInSeconds > 0)
      this.sim.registerOnSecondElapsed(s -> {
        if (this.sim.getNow().getValue() % appSettings.autosave.intervalInSeconds == 0) this.onAutoSave.raise();
      });
  }

  public void quit() {
    for (JFrameInfo frame : frameInfos) {
      frame.getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.getFrame().setVisible(false);
    }
    this.onQuit.raise();
  }

  public void show() {
    frameInfos.forEach(q -> q.getFrame().setVisible(true));
    System.out.println("Viewed:");
  }

  private void save() {
    this.onSave.raise();
  }

  private void bindMenuProxy(MenuFactory.MenuSimProxy menuSimProxy, ISimulation sim) {
    menuSimProxy.onTogglePause.add(() -> sim.pauseUnpauseSim());
    menuSimProxy.onShowProject.add(() -> {
      ProcessBuilder pb;
      String url = "https://github.com/Engin1980/J-ATC-Simulator/wiki";
      String osName = System.getProperty("os.name");
      if (osName.contains("Windows"))
        pb = new ProcessBuilder("cmd", "/c", "start", url);
      else
        pb = new ProcessBuilder("xsd-open", url);
      try {
        pb.start();
      } catch (IOException e) {
        this.sim.getAppLog().write(LogItemType.warning, "Failed to start project web pages." + ExceptionUtils.toFullString(e));
        MessageBox.show("Failed to open web url.", "Failed to open web");
      }
    });
    menuSimProxy.onSimSpeed.add(q -> sim.sendSystemCommandAnonymous(TickSpeedRequest.createSet(q)));
    menuSimProxy.onToggleSound.add(() -> {
      SoundManager.switchEnabled();
      //TODO do somehow: s.setState(SoundManager.isEnabled());
    });
    menuSimProxy.onQuit.add(() -> {
      this.quit();
    });
    menuSimProxy.onShowAbout.add(() -> {
      new FrmAbout().setVisible(true);
    });
    menuSimProxy.onRecordingRequest.add(() -> viewRecordingPanel());
    menuSimProxy.onSave.add(() -> {
      this.save();
    });
    menuSimProxy.onOpenWindow.add(this::onOpenWindow);
  }

  private void onOpenWindow(String windowName) {
    JFrameFactory frameFactory = new JFrameFactory();
    JFrameInfo fi = frameFactory.buildFrame(layout, windowName);

    if (fi.getMenuSimProxy() != null)
      bindMenuProxy(fi.getMenuSimProxy(), this.sim);
    Stylist.apply(fi.getFrame(), true);

    fi.getPanels().forEach(q -> q.getView().init(q.getPanel(), vii, q.getOptions(), viewGlobalEventContext));
    fi.getFrame().setVisible(true);
  }

  private void printSummary(ISet<JFrameInfo> frames) {
    for (JFrameInfo frame : frames) {
      System.out.println("Layout summary");
      System.out.println("Frame " + frame.getFrame().getTitle());
      printSummaryPanel((JPanel) frame.getFrame().getContentPane(), 0);
    }
  }

  private void printSummaryPanel(JPanel panel, int index) {
    for (int i = 0; i <= index; i++) {
      System.out.print("\t");
    }
    System.out.println(panel);
    for (Component component : panel.getComponents()) {
      if (component instanceof JPanel) {
        JPanel inner = (JPanel) component;
        printSummaryPanel(inner, index + 1);
      }
    }
  }

  private void viewRecordingPanel() {
    throw new ToDoException();
//    RecordingPanel pnl;
//    if (recording != null)
//      pnl = new RecordingPanel(recording.getSettings());
//    else
//      pnl = new RecordingPanel(null);
//    pnl.getRecordingStarted().add(q -> recording_recordingStarted(q));
//    pnl.getRecordingStopped().add(() -> recording_recordingStopped());
//    pnl.getViewRecordingFolderRequest().add(() -> recording_viewRecordingFolderRequest());
//    SwingFactory.show(pnl, "Recording");
  }

}
