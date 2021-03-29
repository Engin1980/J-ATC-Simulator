module JAtcSim.Modules.Backend.MessagingLib {
  exports eng.jAtcSim.newLib.messaging;
  requires JAtcSim.Modules.Backend.SharedLib;
  requires eng.eSystem;
  requires AnotherXml;
  exports eng.jAtcSim.newLib.messaging.context to
      JAtcSim.Modules.Backend.AirplanesLib,
      JAtcSim.Modules.Backend.AtcLib,
      JAtcSim.Modules.Backend.SimulationLib;
}
