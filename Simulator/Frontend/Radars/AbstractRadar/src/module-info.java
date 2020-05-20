module JAtcSim.Modules.Frontend.Radars.AbstractRadarModule {
  exports eng.jAtcSim.abstractRadar;
  exports eng.jAtcSim.abstractRadar.global;
  exports eng.jAtcSim.abstractRadar.global.events;
  requires eng.eSystem;
  requires JAtcSim.Modules.Backend.SharedLib;
  requires JAtcSim.Modules.Backend.AirplanesLib;
  requires JAtcSim.Modules.Backend.AreaLib;
  requires JAtcSim.Modules.Backend.AirplaneTypeLib;
  requires java.desktop;
  requires JAtcSim.Modules.Backend.MessagingLib;
  requires JAtcSim.Modules.Backend.SimulationLib;
}