package eng.jAtcSim;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XDocument;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.exceptions.EXmlException;
import eng.eSystem.utilites.ExceptionUtils;
import eng.jAtcSim.abstractRadar.global.SoundManager;
import eng.jAtcSim.app.FrmIntro;
import eng.jAtcSim.app.FrmProgress;
import eng.jAtcSim.app.extenders.swingFactory.FileHistoryManager;
import eng.jAtcSim.app.extenders.swingFactory.SwingFactory;
import eng.jAtcSim.app.startupSettings.StartupSettings;
import eng.jAtcSim.contextLocal.Context;
import eng.jAtcSim.layouting.Layout;
import eng.jAtcSim.layouting.LayoutFactory;
import eng.jAtcSim.newLib.gameSim.IGame;
import eng.jAtcSim.newLib.gameSim.ISimulation;
import eng.jAtcSim.newLib.gameSim.game.GameFactoryAndRepository;
import eng.jAtcSim.newLib.gameSim.game.sources.SourceFactory;
import eng.jAtcSim.newLib.gameSim.game.startupInfos.GameStartupInfo;
import eng.jAtcSim.newLib.gameSim.game.startupInfos.SimulationSettings;
import eng.jAtcSim.newLib.gameSim.game.startupInfos.TrafficSettings;
import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.shared.context.AppAcc;
import eng.jAtcSim.newLib.shared.context.IAppAcc;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;
import eng.jAtcSim.newLib.shared.logging.LogItemType;
import eng.jAtcSim.newLib.shared.logging.ProgressInfo;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;
import eng.jAtcSim.newLib.speeches.system.StringMessage;
import eng.jAtcSim.newLib.weather.Weather;
import eng.jAtcSim.newLib.xml.area.internal.XmlLoader;
import eng.jAtcSim.newPacks.NewPack;
import eng.jAtcSim.settings.AppSettings;
import eng.jAtcSim.shared.MessageBox;
import exml.loading.XLoadContext;
import exml.saving.XSaveContext;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class JAtcSim {

  public static class InitLoadSaveSimulation {

    private static boolean isAutoSaveInitialized = false;

    static void autosaveSimulation() {
      if (!isAutoSaveInitialized) initializeAutoSave();

      ISimulation sim = pack.getGame().getSimulation();
      EDayTimeStamp now = sim.getNow();

      String fileName = sim.getAirport().getIcao() + "_" +
              String.format("%d_%02d_%02d_%02d", now.getDays(), now.getHours(), now.getMinutes(), now.getSeconds()) +
              ".autosave.sm.xml";

      Path p = Paths.get(appSettings.autosave.path.toAbsolutePath().toString(), fileName);

      IMap<String, Object> customData = pack.collectCustomData();
      try {
        new GameFactoryAndRepository().save(pack.getGame(), customData, p.toAbsolutePath().toString());
      } catch (GameFactoryAndRepository.GFRException ex) {
        Context.getApp().getAppLog().write(LogItemType.warning, sf("Game save failed. %s", ExceptionUtils.toFullString(ex)));
        MessageBox.show("Game save failed. " + ex.getMessage(), "Game save failed.");
        pack.getGame().getSimulation().stop();
      }
      pack.getGame().getSimulation().sendSystemCommandAnonymous(new StringMessage("Autosave"));
    }

    static void saveSimulation() {
      boolean wasRunning = pack.getGame().getSimulation().isRunning();
      pack.getGame().getSimulation().stop();

      JFileChooser jf = SwingFactory.createFileDialog(SwingFactory.FileDialogType.game, lastGameFileName);
      int res = jf.showSaveDialog(null);
      if (res != JFileChooser.APPROVE_OPTION) return;

      String fileName = jf.getSelectedFile().getAbsolutePath();

      if (!fileName.endsWith(SwingFactory.SAVED_SIMULATION_EXTENSION))
        fileName += SwingFactory.SAVED_SIMULATION_EXTENSION;

      Context.getApp().getAppLog().write(LogItemType.info, sf("Trying save game to '%s'.", fileName));

      Context.getApp().getAppLog().write(LogItemType.info, "Collecting custom data for game-save.");
      IMap<String, Object> customData = pack.collectCustomData();

      Context.getApp().getAppLog().write(LogItemType.info, "Saving game to xml.");
      try {
        new GameFactoryAndRepository().save(pack.getGame(), customData, fileName);
      } catch (Exception ex) {
        Context.getApp().getAppLog().write(LogItemType.warning, sf("Game save failed. %s", ExceptionUtils.toFullString(ex)));
        MessageBox.show("Game save failed. " + ex.getMessage(), "Game save failed.");
        return;
      }

      lastGameFileName = fileName;
      pack.getGame().getSimulation().sendSystemCommandAnonymous(new StringMessage("Game saved."));

      if (wasRunning)
        pack.getGame().getSimulation().start();
    }

    public static void startSimulation(StartupSettings startupSettings) {
      ProgressInfo pi = new ProgressInfo();
      FrmProgress frm = new FrmProgress(pi);
      IGame g;
      frm.setVisible(true);
      pi.init(19);

      pi.increase("Saving current startup settings");
      try {
        resolveShortXmlFileNamesInStartupSettings(appSettings, startupSettings);
        startupSettings.files.normalizeSlashes();

        XSaveContext ctx = new XSaveContext().withDefaultFormatters();
        ctx.setFormatter(LocalTime.class, q -> q.format(DateTimeFormatter.ofPattern("H:mm")));
        XElement root = ctx.saveObject(startupSettings, "startupSettings");
        XDocument doc = new XDocument(root);
        doc.save(appSettings.startupSettingsFile);
      } catch (EApplicationException | EXmlException ex) {
        throw new EApplicationException("Failed to normalize or save default settings.", ex);
      }

      pi.increase("Loading simulation");
      Context.getApp().getAppLog().write(LogItemType.info, "Starting new simulation game");
      try {
        GameStartupInfo gsi = new GameStartupInfo();

        pi.increase("Loading area");
        gsi.areaSource = SourceFactory.createAreaSource(
                startupSettings.files.areaXmlFile,
                startupSettings.recent.icao
        );

        pi.increase("Loading simulation settings");
        gsi.simulationSettings = new SimulationSettings();
        gsi.simulationSettings.secondLengthInMs = startupSettings.simulation.secondLengthInMs;
        gsi.simulationSettings.startTime = new ETimeStamp(startupSettings.recent.time);
        gsi.simulationSettings.statsSnapshotDistanceInMinutes = appSettings.stats.snapshotIntervalDistance;

        pi.increase("Loading traffic settings");
        gsi.trafficSettings = new TrafficSettings();
        gsi.trafficSettings.emergencyPerDayProbability = startupSettings.traffic.emergencyPerDayProbability;
        gsi.trafficSettings.maxTrafficPlanes = startupSettings.traffic.maxPlanes;
        gsi.trafficSettings.trafficDensityPercentage = startupSettings.traffic.densityPercentage;
        if (startupSettings.traffic.allowDelays) {
          gsi.trafficSettings.trafficDelayStepProbability = 0.2;
          gsi.trafficSettings.trafficDelayStep = 15;
        } else {
          gsi.trafficSettings.trafficDelayStepProbability = 0;
          gsi.trafficSettings.trafficDelayStep = 0;
        }

        pi.increase("Loading fleets settings");
        gsi.fleetsSource = SourceFactory.createFleetsSource(startupSettings.files.generalAviationFleetsXmlFile,
                startupSettings.files.companiesFleetsXmlFile);
        gsi.airplaneTypesSource = SourceFactory.createAirplaneTypesSource(startupSettings.files.planesXmlFile);

        pi.increase("Loading traffic");
        gsi.trafficSource = SourceFactory.createTrafficXmlSource(startupSettings.files.trafficXmlFile);

        pi.increase("Loading/downloading weather");
        Weather customWeather = convertStartupWeatherToInitialWeather(startupSettings.weather);
        switch (startupSettings.weather.type) {
          case user:
            gsi.weatherSource = SourceFactory.createWeatherUserSource(customWeather);
            break;
          case online:
            gsi.weatherSource = SourceFactory.createWeatherOnlineSource(startupSettings.recent.icao, customWeather);
            break;
          case xml:
            gsi.weatherSource = SourceFactory.createWeatherXmlSource(startupSettings.files.weatherXmlFile);
            break;
          default:
            throw new EEnumValueUnsupportedException(startupSettings.weather.type);
        }

        //TODO do somehow configurable - nahore se to nahrava. Nevim k cemu se to tady pouziva.
//      gsi.parserFormatterStartInfo = new ParserFormatterStartInfo(
//          new ParserFormatterStartInfo.Parsers(
//              new PlaneParser(),
//              new AtcParser(),
//              new SystemParser()
//          ),
//          new ParserFormatterStartInfo.Formatters<>(
//              new DynamicPlaneFormatter(speechResponses),
//              new AtcFormatter(),
//              new SystemFormatter()
//          )
//      );

        pi.increase("Creating game");

        g = new GameFactoryAndRepository().create(gsi, pi);

        // enable duplicates
        //TODO fix the following
//      try {
//        g.getSimulation().getArea().checkForDuplicits();
//      } catch (Exception ex) {
//        throw new EApplicationException("Some element in source XML files is not unique. Some of the input XML files is not valid.", ex);
//      }

        pi.increase("Initializing sound");
        Context.getApp().getAppLog().write(LogItemType.info, "Initializing sound environment");
        // sound
        SoundManager.init(appSettings.soundFolder.toString());

        Context.getApp().getAppLog().write(LogItemType.info, "Loading layout & pack");
        pi.increase("Loading layout");
        String layoutFile = startupSettings.layout.layoutXmlFile;
        Layout layout = new LayoutFactory().loadFromXml(layoutFile);
        pi.increase("Starting GUI");
        pack = new NewPack();
        pack.init(g, layout, appSettings);
        linkPackEvents();
        pack.show();

      } catch (GameFactoryAndRepository.GFRException ex) {
        Context.getApp().getAppLog().write(
                LogItemType.warning,
                "Failed to init new game. " + ExceptionUtils.toFullString(ex));
        pi.done();
        frmIntro.setVisible(true);
        return;
      }
      pi.done();
      g.getSimulation().start();
      frm.setVisible(false);
      pack.focus();
    }

    public static void loadSimulation(StartupSettings startupSettings, String xmlFileName) {

      Context.getApp().getAppLog().write(LogItemType.info, "Loading simulation game");

      IMap<String, Object> customData = new EMap<>();

      ProgressInfo pi = new ProgressInfo();
      FrmProgress frm = new FrmProgress(pi);
      frm.setVisible(true);
      pi.init(16);

      GameFactoryAndRepository.LoadResult gfrlr;

      try {
        pi.increase();
        Context.getApp().getAppLog().write(LogItemType.info, "Loading game from '%s'", xmlFileName);
        gfrlr = new GameFactoryAndRepository().load(xmlFileName);
      } catch (GameFactoryAndRepository.GFRException ex) {
        Context.getApp().getAppLog().write(LogItemType.warning, sf("Loading game from '%s' failed. %s.",
                xmlFileName, ExceptionUtils.toFullString(ex)));
        MessageBox.show(sf("Loading game from '%s' failed. %s", xmlFileName, ex.getMessage()), "Loading failed.");
        pi.done();
        throw new EApplicationException("Load error", ex);
        //TODO fix this
        //return;
      }

      pi.increase("Initializing sound environment");
      Context.getApp().getAppLog().write(LogItemType.info, "Initializing sound environment");
      SoundManager.init(appSettings.soundFolder.toString());

      // starting pack & simulation
      pi.increase("Loading layout");
      Context.getApp().getAppLog().write(LogItemType.info, "Loading layout from '%s'", startupSettings.layout.layoutXmlFile);
      String layoutFile = startupSettings.layout.layoutXmlFile;
      Layout layout = new LayoutFactory().loadFromXml(layoutFile);

      pi.increase("Building GUI");
      Context.getApp().getAppLog().write(LogItemType.info, "Building GUI");
      pack = new NewPack();
      pack.init(gfrlr.game, layout, appSettings);
      linkPackEvents();

      try {
        pack.applyCustomData(gfrlr.customData);
      } catch (Exception e) {
        Context.getApp().getAppLog().write(LogItemType.warning,
                sf("Applying custom saved data on pack failed. %s", ExceptionUtils.toFullString(e)));
      }

      pack.show();
      pi.done();

      pack.getGame().getSimulation().start();
    }

    private static void linkPackEvents() {
      pack.onSave.add(InitLoadSaveSimulation::saveSimulation);
      pack.onAutoSave.add(InitLoadSaveSimulation::autosaveSimulation);
      pack.onQuit.add(JAtcSim::quit);
    }

    private static void initializeAutoSave() {
      if (java.nio.file.Files.exists(appSettings.autosave.path) == false) {
        try {
          java.nio.file.Files.createDirectories(appSettings.autosave.path);
        } catch (IOException e) {
          throw new EApplicationException("Unable to create directory " + appSettings.autosave.path + " specified in appSettings for autosaves.");
        }
      }
      isAutoSaveInitialized = true;
    }
  }

  private static AppSettings appSettings;
  private static NewPack pack;
  private static String lastGameFileName;
  private static FrmIntro frmIntro;

  public static JLabel getAppImage(JFrame frm) {
    URL url = frm.getClass().getResource("/intro.png");
    Toolkit tk = frm.getToolkit();
    Image img = tk.getImage(url);
    JLabel ret = new JLabel(new ImageIcon(img));
    return ret;
  }

  public static void main(String[] args) {

    AppSettings.init();

    initStylist();

    // initial path here only until settings are loaded from xml file
    AppAcc appContext = new AppAcc(new ApplicationLog(), Paths.get("C:\\Temp\\"));
    ContextManager.setContext(IAppAcc.class, appContext);

    appSettings = AppSettings.loadOrCreate();
    appContext.updateLogPath(appSettings.logFolder);
    ensureLogPathExists(appSettings.logFolder);
    XmlLoader.onLog.add(JAtcSim::areaXmlLoader_log);

    // various inits
    FileHistoryManager.init();
//    Recorder.init(appSettings.logFolder.toString(), appSettings.speechFormatterFile);


    // startupSettings wizard
    StartupSettings startupSettings;
    {
      try {
        XDocument doc = XDocument.load(appSettings.startupSettingsFile);
        XLoadContext ctx = new XLoadContext().withDefaultParsers();
        ctx.setParser(LocalTime.class, q -> LocalTime.parse(q, DateTimeFormatter.ofPattern("H:mm")));
        startupSettings = ctx.loadObject(doc.getRoot(), StartupSettings.class);
      } catch (Exception ex) {
        Context.getApp().getAppLog().write(
                LogItemType.warning,
                "Failed to load startup settings from " + appSettings.startupSettingsFile.toString() +
                        ". Defaults used. Reason: " + ExceptionUtils.toFullString(ex, "\n\t"));
        startupSettings = new StartupSettings();
      }
    }

    frmIntro = new FrmIntro(startupSettings);
    Stylist.apply(frmIntro, true);
    try {
      Thread.sleep(300);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    frmIntro.setVisible(true);
  }

  public static void quit() {
    System.exit(0);
  }

  public static void setAppIconToFrame(JFrame frm) {
    setIconToFrame(frm, "icon.png");
  }

  public static void setIconToFrame(JFrame frm, String iconFileName) {
    URL url = frm.getClass().getResource("/" + iconFileName);
    Toolkit tk = frm.getToolkit();
    Image img = tk.getImage(url);
    frm.setIconImage(img);
  }

  private static void areaXmlLoader_log(String msg) {
    Context.getApp().getAppLog().write(LogItemType.verbose, "XmlLoader", msg);
  }

  private static Weather convertStartupWeatherToInitialWeather(StartupSettings.Weather weather) {
    Weather.eSnowState snowState;
    switch (weather.snowState) {
      case none:
        snowState = Weather.eSnowState.none;
        break;
      case normal:
        snowState = Weather.eSnowState.normal;
        break;
      case intensive:
        snowState = Weather.eSnowState.intensive;
        break;
      default:
        throw new EEnumValueUnsupportedException(weather.snowState);
    }
    Weather ret =
            new Weather(weather.windDirection,
                    weather.windSpeed,
                    weather.windSpeed,
                    weather.visibilityInM,
                    weather.cloudBaseAltitudeFt,
                    weather.cloudBaseProbability,
                    snowState);
    return ret;
  }

  private static void ensureLogPathExists(Path path) {
    if (java.nio.file.Files.exists(path) == false) {
      try {
        java.nio.file.Files.createDirectories(path);
      } catch (IOException e) {
        throw new EApplicationException(sf(
                "Failed to create/use log path '%s'", path.toString()), e);
      }
    }
  }

  private static void initDefault() {
    // default theme
    Stylist.add(
            "Global - components font",
            new Stylist.TypeFilter(java.awt.Component.class, true),
            q -> {
              Font fnt = new Font("Verdana", 0, 12);
              q.setFont(fnt);
            });
  }

  private static void initIntro() {
    // intro page
    Stylist.add(
            "Frm Intro - frm flightStripSize",
            new Stylist.TypeFilter(FrmIntro.class, false),
            q -> {
              Dimension d = new Dimension(500, 420);
              q.setMinimumSize(d);
              q.setPreferredSize(d);
            });

    Stylist.add(
            "Frm Intro - JButton style",
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
            "Frm Intro - JPanel dark background style",
            new Stylist.AndFilter(
                    new Stylist.TypeFilter(JPanel.class, true),
                    new Stylist.ParentTypeFilter(FrmIntro.class, true)
            ),
            q -> q.setBackground(Color.DARK_GRAY)
    );
  }

  private static void initMainMenus() {
    Stylist.add(
            "JMenuBar style",
            new Stylist.TypeFilter(JMenuBar.class, true),
            q -> {
              q.setBackground(Color.DARK_GRAY);
              q.setForeground(Color.LIGHT_GRAY);
            });
    Stylist.add(
            "JMenu style",
            new Stylist.TypeFilter(JMenu.class, false),
            q -> {
              q.setBackground(Color.DARK_GRAY);
              q.setForeground(Color.LIGHT_GRAY);
              ((JMenu) q).setOpaque(true);
            });
    Stylist.add(
            "JMenuItem style",
            new Stylist.TypeFilter(JMenuItem.class, false),
            q -> {
              q.setBackground(Color.DARK_GRAY);
              q.setForeground(Color.LIGHT_GRAY);
            });
    Stylist.add(
            "JMenuChecked style",
            new Stylist.TypeFilter(JCheckBoxMenuItem.class, false),
            q -> {
              q.setBackground(Color.DARK_GRAY);
              q.setForeground(Color.LIGHT_GRAY);
            });
  }

  private static void initStartupProgress() {
    Stylist.add(
            "Startup progress form background dark",
            new Stylist.TypeFilter(FrmProgress.class, true),
            q -> q.setBackground(Color.DARK_GRAY)
    );

    Stylist.add(
            "Startup progress - panels dark",
            new Stylist.AndFilter(
                    new Stylist.TypeFilter(javax.swing.JPanel.class, false),
                    new Stylist.ParentTypeFilter(FrmProgress.class, true)
            )
            ,
            q -> q.setBackground(Color.DARK_GRAY));
  }

  private static void initStartupSettings() {

    Stylist.add(
            "Startup settings - JPanel dark",
            new Stylist.TypeFilter(JPanel.class, true),
            q -> q.setBackground(Color.DARK_GRAY)
    );

    Stylist.add(
            "Startup settings - JPanel border style",
            new Stylist.TypeFilter(javax.swing.JPanel.class, true),
            q -> {
              javax.swing.JPanel p = (javax.swing.JPanel) q;
              TitledBorder b = (TitledBorder) p.getBorder();
              if (b != null) b.setTitleColor(Color.LIGHT_GRAY);
            });

    Stylist.add(
            "Startup settings - JLabel style",
            new Stylist.TypeFilter(JLabel.class, false),
            q -> {
              q.setForeground(Color.LIGHT_GRAY);
            }
    );

    Stylist.add(
            "Startup settings - JRadioButton style",
            new Stylist.TypeFilter(JRadioButton.class, false),
            q -> {
              q.setForeground(Color.LIGHT_GRAY);
              q.setBackground(Color.DARK_GRAY);
            }
    );
    Stylist.add(
            "Startup settings - JCheckBox style",
            new Stylist.TypeFilter(JCheckBox.class, false),
            q -> {
              q.setForeground(Color.LIGHT_GRAY);
              q.setBackground(Color.DARK_GRAY);
            }
    );
    Stylist.add(
            "Startup settings - JTextField style",
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

  private static void initStylist() {
    initDefault();
    initIntro();
    initStartupSettings();
    initStartupProgress();
    initMainMenus();
  }

  private static void resolveShortXmlFileNamesInStartupSettings(AppSettings appSettings, StartupSettings
          startupSettings) {
    Path tmp;
    Path appPath;
    appPath = AppSettings.getApplicationFolder();

    tmp = Paths.get(startupSettings.files.areaXmlFile);
    if (tmp.isAbsolute()) {
      tmp = appPath.relativize(tmp);
      startupSettings.files.areaXmlFile = tmp.toString();
    }

    tmp = Paths.get(startupSettings.files.companiesFleetsXmlFile);
    if (tmp.isAbsolute()) {
      tmp = appPath.relativize(tmp);
      startupSettings.files.companiesFleetsXmlFile = tmp.toString();
    }

    tmp = Paths.get(startupSettings.files.generalAviationFleetsXmlFile);
    if (tmp.isAbsolute()) {
      tmp = appPath.relativize(tmp);
      startupSettings.files.generalAviationFleetsXmlFile = tmp.toString();
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

    if (startupSettings.files.weatherXmlFile != null) {
      tmp = Paths.get(startupSettings.files.weatherXmlFile);
      if (tmp.isAbsolute()) {
        tmp = appPath.relativize(tmp);
        startupSettings.files.weatherXmlFile = tmp.toString();
      }
    }
  }
}

