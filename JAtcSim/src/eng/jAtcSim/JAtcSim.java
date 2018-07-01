/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.ERuntimeException;
import eng.jAtcSim.frmPacks.Pack;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.Game;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.global.logging.ApplicationLog;
import eng.jAtcSim.lib.global.logging.Recorder;
import eng.jAtcSim.lib.global.sources.WeatherSource;
import eng.jAtcSim.lib.traffic.GenericTraffic;
import eng.jAtcSim.lib.traffic.Traffic;
import eng.jAtcSim.lib.weathers.Weather;
import eng.jAtcSim.radarBase.global.SoundManager;
import eng.jAtcSim.startup.FrmIntro;
import eng.jAtcSim.startup.startupSettings.StartupSettings;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

/**
 * @author Marek
 */
public class JAtcSim {

  private static final boolean FAST_START = false;
  private static final Traffic enginSpecificTraffic =
        new eng.jAtcSim.lib.traffic.TestTrafficOneApproach();
      // new eng.jAtcSim.lib.traffic.TestTrafficOneDeparture();
      // null;
  private static AppSettings appSettings;

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {

    AppSettings.init();

    initStylist();

    Acc.setLog(new ApplicationLog());

    appSettings = AppSettings.create();

    Recorder.init(appSettings.getLogFolder().toString());

    // startupSettings wizard
    StartupSettings startupSettings = XmlLoadHelper.loadStartupSettings(appSettings.getStartupSettingsFile().toString());

    FrmIntro frmIntro = new FrmIntro(startupSettings);
    Stylist.apply(frmIntro, true);
    frmIntro.setVisible(true);
  }

  public static void loadSimulation(StartupSettings startupSettings, String xmlFileName) {
    System.out.println("* Loading the simulation");

    IMap<String, Object> map = new EMap<>();
    Game g = Game.load(xmlFileName, map);

    // enable duplicates
    try {
      g.getSimulation().getArea().checkForDuplicits();
    } catch (Exception ex) {
      throw new EApplicationException("Some element in source XML files is not unique. Some of the input XML files is not valid.", ex);
    }

    System.out.println("* Initializing sound environment");
    // sound
    SoundManager.init(appSettings.getSoundFolder().toString());

    System.out.println("* Starting a GUI");
    // starting pack & simulation
    String packType = startupSettings.radar.packClass;
    Pack simPack
        = createPackInstance(packType);

    simPack.initPack(g, appSettings);
    simPack.startPack();

    simPack.applyStoredData(map);
  }

  public static void startSimulation(StartupSettings startupSettings) {

    try {
      resolveShortXmlFileNamesInStartupSettings(appSettings, startupSettings);
      XmlLoadHelper.saveStartupSettings(startupSettings, appSettings.getStartupSettingsFile().toString());
    } catch (EApplicationException ex) {
      throw new EApplicationException("Failed to normalize or save default settings.", ex);
    }

    System.out.println("* Creating the simulation");

    Game.GameStartupInfo gsi = new Game.GameStartupInfo();
    gsi.areaXmlFile = startupSettings.files.areaXmlFile;
    gsi.emergencyPerDayProbability = startupSettings.traffic.emergencyPerDayProbability;
    gsi.fleetsXmlFile = startupSettings.files.fleetsXmlFile;
    gsi.icao = startupSettings.recent.icao;
    gsi.planesXmlFile = startupSettings.files.planesXmlFile;
    gsi.secondLengthInMs = startupSettings.simulation.secondLengthInMs;
    if (startupSettings.traffic.type == StartupSettings.Traffic.eTrafficType.custom)
      gsi.specificTraffic = generateCustomTraffic(startupSettings.traffic);
    if (enginSpecificTraffic != null)
      gsi.specificTraffic = enginSpecificTraffic;
    gsi.startTime = new ETime(startupSettings.recent.time);
    gsi.trafficXmlFile = startupSettings.files.trafficXmlFile;
    gsi.initialWeather = Weather.createClear();
    gsi.weatherProviderType = startupSettings.weather.useOnline ?
        WeatherSource.ProviderType.dynamicNovGoaaProvider : WeatherSource.ProviderType.staticProvider;

    Game g;
    g = Game.create(gsi);

    // enable duplicates
    try {
      g.getSimulation().getArea().checkForDuplicits();
    } catch (Exception ex) {
      throw new EApplicationException("Some element in source XML files is not unique. Some of the input XML files is not valid.", ex);
    }

    System.out.println("* Initializing sound environment");
    // sound
    SoundManager.init(appSettings.getSoundFolder().toString());

    System.out.println("* Starting a GUI");
    // starting pack & simulation
    String packType = startupSettings.radar.packClass;
    Pack simPack
        = createPackInstance(packType);

    simPack.initPack(g, appSettings);
    simPack.startPack();
  }

  public static void quit() {

  }


  public static void setAppIconToFrame(JFrame frm) {
    URL url = frm.getClass().getResource("/icon.png");
    Toolkit tk = frm.getToolkit();
    Image img = tk.getImage(url);
    frm.setIconImage(img);
  }

  private static void initStylist() {
    initDefault();
    initIntro();
    initStartupSettings();
  }

  private static void initStartupSettings() {

    Stylist.add(
        new Stylist.TypeFilter(JPanel.class, true),
        q -> q.setBackground(Color.DARK_GRAY)
    );

    Stylist.add(
        new Stylist.TypeFilter(javax.swing.JPanel.class, true),
        q -> {
          javax.swing.JPanel p = (javax.swing.JPanel) q;
          TitledBorder b = (TitledBorder) p.getBorder();
          if (b != null) b.setTitleColor(Color.LIGHT_GRAY);
        });

    Stylist.add(
        new Stylist.TypeFilter(JLabel.class, false),
        q -> {
          q.setForeground(Color.LIGHT_GRAY);
        }
    );

    Stylist.add(
        new Stylist.TypeFilter(JRadioButton.class, false),
        q -> {
          q.setForeground(Color.LIGHT_GRAY);
          q.setBackground(Color.DARK_GRAY);
        }
    );
    Stylist.add(
        new Stylist.TypeFilter(JCheckBox.class, false),
        q -> {
          q.setForeground(Color.LIGHT_GRAY);
          q.setBackground(Color.DARK_GRAY);
        }
    );
    Stylist.add(
        new Stylist.TypeFilter(JTextField.class, false),
        q -> {
          JTextField txt = (JTextField) q;
          if (txt.isEditable() == false) {
            q.setForeground(Color.LIGHT_GRAY);
            q.setBackground(Color.DARK_GRAY);
          }
        }
    );
  }

  private static void initIntro() {
    // intro page
    Stylist.add(
        new Stylist.TypeFilter(FrmIntro.class, false),
        q -> {
          Dimension d = new Dimension(500, 420);
          q.setMinimumSize(d);
          q.setPreferredSize(d);
        });

    Stylist.add(
        new Stylist.AndFilter(
            new Stylist.TypeFilter(javax.swing.JButton.class, false),
            new Stylist.ParentTypeFilter(FrmIntro.class, true)
        )
        ,
        q -> {
          Dimension d = new Dimension(450, 32);
          q.setPreferredSize(d);
          q.setMinimumSize(d);
          q.setMaximumSize(d);
        });

    Stylist.add(
        new Stylist.AndFilter(
            new Stylist.TypeFilter(JPanel.class, true),
            new Stylist.ParentTypeFilter(FrmIntro.class, true)
        ),
        q -> q.setBackground(Color.DARK_GRAY)
    );
  }

  private static void initDefault() {
    // default theme
    Stylist.add(
        new Stylist.TypeFilter(java.awt.Component.class, true),
        q -> {
          Font fnt = new Font("Verdana", 0, 12);
          q.setFont(fnt);
        });
  }

  private static void resolveShortXmlFileNamesInStartupSettings(AppSettings appSettings, StartupSettings startupSettings) {
    Path tmp;
    Path appPath;
    appPath = appSettings.getApplicationFolder();

    tmp = Paths.get(startupSettings.files.areaXmlFile);
    if (tmp.isAbsolute()) {
      tmp = appPath.relativize(tmp);
      startupSettings.files.areaXmlFile = tmp.toString();
    }

    tmp = Paths.get(startupSettings.files.fleetsXmlFile);
    if (tmp.isAbsolute()) {
      tmp = appPath.relativize(tmp);
      startupSettings.files.fleetsXmlFile = tmp.toString();
    }

    tmp = Paths.get(startupSettings.files.planesXmlFile);
    if (tmp.isAbsolute()) {
      tmp = appPath.relativize(tmp);
      startupSettings.files.planesXmlFile = tmp.toString();
    }

    if (startupSettings.files.trafficXmlFile != null) {
      tmp = Paths.get(startupSettings.files.trafficXmlFile);
      if (tmp.isAbsolute()) {
        tmp = appPath.relativize(tmp);
        startupSettings.files.trafficXmlFile = tmp.toString();
      }
    }
  }

  private static Pack createPackInstance(String packTypeName) {
    Class<?> clazz;
    Object object;
    try {
      clazz = Class.forName(packTypeName);
      Constructor<?> ctor = clazz.getConstructor();
      object = ctor.newInstance();
    } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
      throw new ERuntimeException(
          sf("Failed to create instance of radar pack '%s'.", packTypeName), ex);
    }
    Pack ret = (Pack) object;
    return ret;
  }

  private static GenericTraffic generateCustomTraffic(StartupSettings.Traffic trf) {
    GenericTraffic ret = new GenericTraffic(
        trf.customTraffic.companies, trf.customTraffic.countryCodes,
        trf.customTraffic.movementsPerHour,
        trf.customTraffic.arrivals2departuresRatio / 10d,
        trf.customTraffic.nonCommercialFlightProbability,
        trf.customTraffic.weightTypeA,
        trf.customTraffic.weightTypeB,
        trf.customTraffic.weightTypeC,
        trf.customTraffic.weightTypeD,
        trf.customTraffic.useExtendedCallsigns);
    return ret;
//    Traffic ret;
//    switch (sett.traffic.type) {
//      case xml:
//        ret = XmlLoadHelper.loadTraffic(sett.files.trafficXmlFile);
//        break;
//      case airportDefined:
//        Area area = XmlLoadHelper.loadNewArea(sett.files.areaXmlFile);
//        Airport airport = CollectionUtils.tryGetFirst(area.getAirports(), o -> o.getIcao().equals(sett.recent.icao));
//        ret = CollectionUtils.tryGetFirst(airport.getTrafficDefinitions(), o -> o.getTitle().equals(sett.traffic.trafficAirportDefinedTitle));
//        break;
//      case custom:
//        ret = new GenericTraffic(
//            sett.traffic.customTraffic.movementsPerHour,
//            1 - sett.traffic.customTraffic.arrivals2departuresRatio / 10d, // 0-10 to 0.0-1.0
//            sett.traffic.customTraffic.weightTypeA,
//            sett.traffic.customTraffic.weightTypeB,
//            sett.traffic.customTraffic.weightTypeC,
//            sett.traffic.customTraffic.weightTypeD,
//            sett.traffic.customTraffic.useExtendedCallsigns
//        );
//      default:
//        throw new UnsupportedOperationException();
//    }
//
//    return ret;
  }
}

