module JAtcSim.Modules.Backend.AtcLib {
  requires eng.eSystem;
  requires JAtcSim.Modules.Backend.AirplanesLib;
  requires JAtcSim.Modules.Backend.AreaLib;
  requires JAtcSim.Modules.Backend.SharedLib;
  requires JAtcSim.Modules.Backend.MessagingLib;
  requires JAtcSim.Modules.Backend.SpeechesLib;
  requires JAtcSim.Modules.Backend.AirplaneTypeLib;
  requires JAtcSim.Modules.Backend.WeatherLib;
  requires JAtcSim.Modules.Backend.StatsLib;
  requires eng.newXmlUtils;
  requires AnotherXml;
  exports eng.jAtcSim.newLib.atcs;
  exports eng.jAtcSim.newLib.atcs.context to
      JAtcSim.Modules.Backend.SimulationLib;
}
