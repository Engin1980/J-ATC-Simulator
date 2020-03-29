module JAtcSim.Modules.Backend.AreaLib {
  requires transitive JAtcSim.Modules.Backend.SharedLib;
  requires eng.eSystem;
  requires JAtcSim.Modules.Backend.SpeechesLib;
  exports eng.jAtcSim.newLib.area;
  exports eng.jAtcSim.newLib.area.approaches;
  exports eng.jAtcSim.newLib.area.approaches.entryLocations;
  exports eng.jAtcSim.newLib.area.approaches.stages;
  exports eng.jAtcSim.newLib.area.approaches.stages.checks;
  exports eng.jAtcSim.newLib.area.approaches.stages.exitConditions;
  exports eng.jAtcSim.newLib.area.routes;
  exports eng.jAtcSim.newLib.area.speeches;
}