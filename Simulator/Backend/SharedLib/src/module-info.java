module JAtcSim.Modules.Backend.SharedLib {
  requires eng.eSystem;
  requires eng.newXmlUtils;
  exports eng.jAtcSim.newLib.shared;
  exports eng.jAtcSim.newLib.shared.time;
  exports eng.jAtcSim.newLib.shared.xml;
  exports eng.jAtcSim.newLib.shared.logging;
  exports eng.jAtcSim.newLib.shared.logging.writers;
  exports eng.jAtcSim.newLib.shared.enums;
  exports eng.jAtcSim.newLib.shared.context to
      JAtcSim.Modules.Backend.MoodLib,
      JAtcSim.modules.backend.TextProcessingLib,
      JAtcSim.Modules.Backend.WeatherLib,
      JAtcSimLib.Modules.Backend.TrafficLib,
      JAtcSim.Modules.Backend.StatsLib,
      JAtcSim.Modules.Backend.AirplanesLib,
      JAtcSim.Modules.Backend.AtcLib,
      JAtcSim.Modules.Backend.SimulationLib, AreaXmlLoaderLib;
}
