package eng.jAtcSim.newPacks;

import eng.eSystem.Tuple;
import eng.eSystem.collections.ESet;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IMap;
import eng.eSystem.collections.ISet;
import eng.eSystem.exceptions.EApplicationException;
import eng.eXmlSerialization.XmlSerializer;
import eng.jAtcSim.layouting.JFrameFactory;
import eng.jAtcSim.layouting.Layout;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.gameSim.IGame;
import eng.jAtcSim.newLib.gameSim.ISimulation;
import eng.jAtcSim.newLib.textProcessing.implemented.dynamicPlaneFormatter.DynamicPlaneFormatter;
import eng.jAtcSim.newLib.textProcessing.implemented.dynamicPlaneFormatter.types.Sentence;
import eng.jAtcSim.newPacks.views.ViewInitInfo;
import eng.jAtcSim.settings.AppSettings;
import eng.jAtcSim.xmlLoading.XmlSerialization;
import eng.jAtcSim.xmlLoading.XmlSerializationFactory;

import java.nio.file.Path;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class NewPack {
  private IGame game;
  private ISimulation sim;
  private Area area;
  private Airport aip;
  private AppSettings settings;


  public void init(IGame game, Layout layout, AppSettings appSettings) {
    settings = appSettings;

    // init sim & area
    this.game = game;
    this.sim = game.getSimulation();
    this.area = game.getSimulation().getArea();
    this.aip = sim.getAirport();

    JFrameFactory frameFactory = new JFrameFactory();
    //TODO how to set menu?
    ISet<JFrameFactory.JFrameInfo> frames = frameFactory.build(layout);

    ISet<Tuple<IView, JFrameFactory.JPanelInfo>> view2panelMap = new ESet<>();
    for (JFrameFactory.JFrameInfo frame : frames) {
      for (JFrameFactory.JPanelInfo panel : frame.getPanels()) {
        String viewName = panel.getViewName();
        IView view = ViewFactory.getView(viewName);
        view2panelMap.add(new Tuple<>(view, panel));
      }
    }

    ViewInitInfo vii = new ViewInitInfo();
    vii.setSimulation(this.sim);
    vii.setAirport(this.aip);
    vii.setSettings(this.settings);
    vii.setUserAtcId(this.sim.getUserAtcIds().get(0));
    vii.setDynamicAirplaneSpeechFormatter(loadDynamicPlaneFormatter(this.settings.speechFormatterFile)); //TODO improve somehow

    for (Tuple<IView, JFrameFactory.JPanelInfo> item : view2panelMap) {
      item.getA().init(item.getB().getPanel(), vii);
    }
  }

  public void start() {

  }

  private DynamicPlaneFormatter loadDynamicPlaneFormatter(Path speechFormatterFile) {
    IMap<Class<?>, IList<Sentence>> speechResponses;
    try {
      XmlSerializer ser = XmlSerializationFactory.createForSpeechResponses();
      speechResponses = XmlSerialization.loadFromFile(ser, speechFormatterFile.toFile(), IMap.class);
    } catch (EApplicationException ex) {
      throw new EApplicationException(
              sf("Unable to load speech responses from xml file '%s'.", speechFormatterFile), ex);
    }
    DynamicPlaneFormatter ret = new DynamicPlaneFormatter(speechResponses);
    return ret;
  }

}
