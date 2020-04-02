module JAtcSim.Modules.Backend.AreaLib {
  requires transitive JAtcSim.Modules.Backend.SharedLib;
  requires eng.eSystem;
  requires JAtcSim.Modules.Backend.SpeechesLib;
  exports eng.jAtcSim.newLib.area;
  exports eng.jAtcSim.newLib.area.oldApproaches;
  exports eng.jAtcSim.newLib.area.oldApproaches.entryLocations;
  exports eng.jAtcSim.newLib.area.oldApproaches.stages;
  exports eng.jAtcSim.newLib.area.oldApproaches.stages.checks;
  exports eng.jAtcSim.newLib.area.oldApproaches.stages.exitConditions;
  exports eng.jAtcSim.newLib.area.routes;
  exports eng.jAtcSim.newLib.area.speeches;
  exports eng.jAtcSim.newLib.area.approaches;
  exports eng.jAtcSim.newLib.area.approaches.behaviors;
  exports eng.jAtcSim.newLib.area.approaches.conditions;
  exports eng.jAtcSim.newLib.area.approaches.locations;
  exports eng.jAtcSim.newLib.area.approaches.perCategoryValues;
}