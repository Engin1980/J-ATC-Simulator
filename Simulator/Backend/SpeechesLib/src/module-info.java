module JAtcSim.Modules.Backend.SpeechesLib {
  requires transitive JAtcSim.Modules.Backend.SharedLib;
  requires transitive JAtcSim.Modules.Backend.MessagingLib;
  requires eng.eSystem;
  exports eng.jAtcSim.newLib.speeches;
  exports eng.jAtcSim.newLib.speeches.atc2airplane;
  exports eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands;
  exports eng.jAtcSim.newLib.speeches.xml;
  exports eng.jAtcSim.newLib.speeches.airplane2atc;
  exports eng.jAtcSim.newLib.speeches.airplane2atc.responses;
  exports eng.jAtcSim.newLib.speeches.atc2atc;
}