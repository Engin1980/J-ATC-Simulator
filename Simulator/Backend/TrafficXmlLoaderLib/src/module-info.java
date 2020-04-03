module TrafficXmlLoaderLib {
  requires eng.eSystem;
  requires JAtcSimLib.Modules.Backend.TrafficLib;
  requires JAtcSim.Modules.Backend.SharedLib;
  exports eng.jAtcSim.newLib.xml.traffic;
}