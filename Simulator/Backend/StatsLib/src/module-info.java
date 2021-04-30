module JAtcSim.Modules.Backend.StatsLib {
  requires transitive JAtcSim.Modules.Backend.SharedLib;
  requires eng.eSystem;
  requires JAtcSim.Modules.Backend.MoodLib;
  requires AnotherXml;
  exports eng.jAtcSim.newLib.stats;
  exports eng.jAtcSim.newLib.stats.context to
      JAtcSim.Modules.Backend.AtcLib,
      JAtcSim.Modules.Backend.SimulationLib;
  exports eng.jAtcSim.newLib.newStats;
}
