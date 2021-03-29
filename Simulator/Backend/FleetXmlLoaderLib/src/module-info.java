module FleetXmlLoaderLib {
  requires  eng.eSystem;
  requires JAtcSim.Modules.Backend.SharedLib;
  requires JAtcSimLib.Modules.Backend.FleetLib;
  requires AnotherXml;
  exports eng.jAtcSim.newLib.xml.fleets;
}
