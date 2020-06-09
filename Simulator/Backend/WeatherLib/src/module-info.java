module JAtcSim.Modules.Backend.WeatherLib {
  exports eng.jAtcSim.newLib.weather;
  exports eng.jAtcSim.newLib.weather.downloaders;
  requires JAtcSim.Modules.Backend.SharedLib;
  requires eng.eSystem;
  exports eng.jAtcSim.newLib.weather.context to
      JAtcSim.modules.backend.TextProcessingLib,
      JAtcSim.Modules.Backend.AirplanesLib;
}