module JAtcSim.Modules.Backend.AirplanesLib {
  exports eng.jAtcSim.newLib.airplanes;
  exports eng.jAtcSim.newLib.airplanes.templates;
  requires JAtcSim.Modules.Backend.AirplaneTypeLib;
  requires JAtcSim.Modules.Backend.SharedLib;
  requires JAtcSim.Modules.Backend.AreaLib;
  requires eng.eSystem;
  requires JAtcSim.Modules.Backend.MoodLib;
  requires JAtcSim.Modules.Backend.SpeechesLib;
  requires JAtcSim.Modules.Backend.WeatherLib;
  requires JAtcSimLib.Modules.Backend.TrafficLib;
  requires JAtcSim.Modules.Backend.StatsLib;
  requires eng.jAtcSim.XmlUtilsLib;
  requires eng.newXmlUtils;
  exports eng.jAtcSim.newLib.airplanes.context to
      JAtcSim.Modules.Backend.AtcLib,
      JAtcSim.Modules.Backend.SimulationLib;
}
