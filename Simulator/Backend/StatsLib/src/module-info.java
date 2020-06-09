module JAtcSim.Modules.Backend.StatsLib {
  requires transitive JAtcSim.Modules.Backend.SharedLib;
  requires eng.eSystem;
  requires JAtcSim.Modules.Backend.MoodLib;
  exports eng.jAtcSim.newLib.stats;
}