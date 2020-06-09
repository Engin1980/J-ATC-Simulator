module JAtcSim.Modules.Backend.AtcLib {
  exports eng.jAtcSim.newLib.atcs;
  requires eng.eSystem;
  requires JAtcSim.Modules.Backend.AirplanesLib;
  requires JAtcSim.Modules.Backend.AreaLib;
  requires JAtcSim.Modules.Backend.SharedLib;
  requires JAtcSim.Modules.Backend.MessagingLib;
  requires JAtcSim.Modules.Backend.SpeechesLib;
  requires JAtcSim.Modules.Backend.AirplaneTypeLib;
  requires JAtcSim.Modules.Backend.WeatherLib;
  requires JAtcSim.Modules.Backend.StatsLib;
  exports eng.jAtcSim.newLib.atcs.context to
      JAtcSim.Modules.Backend.SimulationLib;
}