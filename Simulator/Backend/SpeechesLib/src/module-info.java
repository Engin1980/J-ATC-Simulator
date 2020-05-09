module JAtcSim.Modules.Backend.SpeechesLib {
  requires transitive JAtcSim.Modules.Backend.SharedLib;
  requires transitive JAtcSim.Modules.Backend.MessagingLib;
  requires eng.eSystem;
  exports eng.jAtcSim.newLib.speeches;
  exports eng.jAtcSim.newLib.speeches.airplane.atc2airplane.afterCommands;
  exports eng.jAtcSim.newLib.speeches.airplane.airplane2atc;
  exports eng.jAtcSim.newLib.speeches.airplane.airplane2atc.responses;
  exports eng.jAtcSim.newLib.speeches.atc;
  exports eng.jAtcSim.newLib.speeches.airplane;
  exports eng.jAtcSim.newLib.speeches.airplane.atc2airplane;
  exports eng.jAtcSim.newLib.speeches.base;
  exports eng.jAtcSim.newLib.speeches.system;
  exports eng.jAtcSim.newLib.speeches.atc.user2atc;
  exports eng.jAtcSim.newLib.speeches.system.user2system;
}