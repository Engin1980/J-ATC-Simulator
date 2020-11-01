package eng.jAtcSim.newLib.gameSim.game;

import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.jAtcSim.newLib.gameSim.contextLocal.Context;
import eng.jAtcSim.newLib.gameSim.game.sources.*;
import eng.jAtcSim.newLib.gameSim.game.startupInfos.GameStartupInfo;
import eng.jAtcSim.newLib.gameSim.simulation.Simulation;
import eng.jAtcSim.newLib.gameSim.simulation.modules.WorldModule;
import eng.jAtcSim.newLib.gameSim.simulation.SimulationSettings;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;

public class GameFactory {
  public Game create(GameStartupInfo gsi) {
    Game game;
    ApplicationLog appLog = Context.getApp().getAppLog();

    AreaSource areaSource;
    AirplaneTypesSource airplaneTypesSource;
    FleetsSource fleetsSource;
    TrafficSource trafficSource;
    WeatherSource weatherSource;
    Simulation simulation;

    try {
      appLog.write(ApplicationLog.eType.info, "Loading area");
      areaSource = new AreaSource(gsi.areaSource.areaXmlFile, gsi.areaSource.icao);
      areaSource.init();
    } catch (Exception ex) {
      throw new EApplicationException("Unable to load or initialize area.", ex);
    }

    try {
      appLog.write(ApplicationLog.eType.info, "Loading plane types");
      airplaneTypesSource = new AirplaneTypesSource(gsi.planesXmlFile);
      airplaneTypesSource.init();
    } catch (Exception ex) {
      throw new EApplicationException("Unable to load or initialize plane types.", ex);
    }

    try {
      appLog.write(ApplicationLog.eType.info, "Loading fleets");
      fleetsSource = new FleetsSource(gsi.generalAviationFleetsXmlFile, gsi.companyFleetsXmlFile);
      fleetsSource.init();
    } catch (Exception ex) {
      throw new EApplicationException("Unable to load or initialize fleets.", ex);
    }

    try {
      appLog.write(ApplicationLog.eType.info, "Loading traffic");
      if (gsi.trafficSource.specificTraffic != null) {
        trafficSource = new TrafficUserSource(gsi.trafficSource.specificTraffic);
      } else {
        trafficSource = new TrafficXmlSource(gsi.trafficSource.trafficXmlFile);
      }
      trafficSource.init();
    } catch (Exception ex) {
      throw new EApplicationException("Unable to load or initialize traffic.", ex);
    }

    try {
      appLog.write(ApplicationLog.eType.info, "Initializing weather");
      switch (gsi.weatherSource.weatherProviderType) {
        case online:
          weatherSource = new WeatherOnlineSource(true, gsi.areaSource.icao, gsi.weatherSource.initialWeather);
          break;
        case xml:
          weatherSource = new WeatherXmlSource(gsi.weatherSource.weatherXmlFile);
          break;
        case user:
          weatherSource = new WeatherUserSource(gsi.weatherSource.initialWeather);
          break;
        default:
          throw new EEnumValueUnsupportedException(gsi.weatherSource.weatherProviderType);
      }
      weatherSource.init();
    } catch (Exception ex) {
      throw new EApplicationException("Unable to load, download or initialize weather.", ex);
    }

//    TrafficManager.TrafficManagerSettings tms;
//    try {
//      tms = new TrafficManager.TrafficManagerSettings(
//          gsi.allowTrafficDelays, gsi.maxTrafficPlanes, gsi.trafficDensityPercentage);
//    } catch (Exception ex){
//      throw new EApplicationException("Unable to initialize the traffic manager.", ex);
//    }

    try {
      appLog.write(ApplicationLog.eType.info, "Creating the simulation");
      SimulationStartupContext simulationContext = new SimulationStartupContext(
          areaSource.getContent(),
          areaSource.getIcao(),
          airplaneTypesSource.getContent(),
          fleetsSource.getContent().companyFleets,
          fleetsSource.getContent().gaFleets,
          trafficSource.getContent(),
          weatherSource.getContent()
      );
      SimulationSettings simulationSettings = new SimulationSettings(
          gsi.trafficSettings,
          gsi.simulationSettings
      );

      simulation = new Simulation(simulationContext, simulationSettings);
      game = new Game(
          areaSource,
          airplaneTypesSource,
          fleetsSource,
          trafficSource,
          weatherSource,
          simulation
      );
      appLog.write(ApplicationLog.eType.info, "Initializing the simulation");
    } catch (Exception ex) {
      throw new EApplicationException("Unable to create or initialize the simulation.", ex);
    }
    return game;
  }

  public Game load(String fileName){

  }
}
