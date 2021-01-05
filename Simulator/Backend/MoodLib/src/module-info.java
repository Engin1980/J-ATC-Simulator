module JAtcSim.Modules.Backend.MoodLib {
  exports eng.jAtcSim.newLib.mood;
  requires eng.eSystem;
  requires JAtcSim.Modules.Backend.SharedLib;
  requires eng.newXmlUtils;
  requires AnotherXml;
  exports eng.jAtcSim.newLib.mood.context to
      JAtcSim.Modules.Backend.SimulationLib;
}
