module JAtcSimLib.Modules.Backend.TrafficLib {
  exports eng.jAtcSim.newLib.traffic;
  exports eng.jAtcSim.newLib.traffic.models;
  exports eng.jAtcSim.newLib.traffic.movementTemplating;
  requires JAtcSim.Modules.Backend.SharedLib;
  requires eng.eSystem;
  requires eng.jAtcSim.XmlUtilsLib;
  requires eng.newXmlUtils;
}
