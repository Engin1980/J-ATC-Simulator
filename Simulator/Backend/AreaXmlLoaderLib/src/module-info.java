module AreaXmlLoaderLib {
  requires eng.eSystem;
  requires transitive JAtcSim.Modules.Backend.SharedLib;
  requires JAtcSim.Modules.Backend.AreaLib;
  requires JAtcSim.Modules.Backend.SpeechesLib;
  exports eng.jAtcSim.newLib.xml.area;
}