module JAtcSim.Modules.Backend.MoodLib {
  exports eng.jAtcSim.newLib.mood;
  requires eng.eSystem;
  requires JAtcSim.Modules.Backend.SharedLib;
  exports eng.jAtcSim.newLib.mood.context to
      JAtcSim.Modules.Backend.SimulationLib;
}