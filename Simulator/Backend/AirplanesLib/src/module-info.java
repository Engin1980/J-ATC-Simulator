module JAtcSim.Modules.Backend.AirplanesLib {
  exports eng.jAtcSim.newLib.airplanes;
  requires JAtcSim.Modules.Backend.AirplaneTypeLib;
  requires JAtcSim.Modules.Backend.SharedLib;
  requires JAtcSim.Modules.Backend.AreaLib;
  requires eng.eSystem;
  requires JAtcSim.Modules.Backend.MoodLib;
  requires JAtcSim.Modules.Backend.SpeechesLib;
  requires JAtcSim.Modules.Backend.WeatherLib;
  requires JAtcSimLib.Modules.Backend.TrafficLib;
  requires JAtcSim.Modules.Backend.StatsLib;
}