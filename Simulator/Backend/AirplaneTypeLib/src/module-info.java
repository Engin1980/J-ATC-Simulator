module JAtcSim.Modules.Backend.AirplaneTypeLib {
  exports eng.jAtcSim.newLib.airplaneType;
  requires transitive JAtcSim.Modules.Backend.SharedLib;
  requires eng.eSystem;
  requires AnotherXml;
  exports eng.jAtcSim.newLib.airplaneType.context to
      JAtcSim.Modules.Backend.SimulationLib;
}
