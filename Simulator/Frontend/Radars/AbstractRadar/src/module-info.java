module JAtcSim.Modules.Frontend.Radars.AbstractRadarModule {
  exports eng.jAtcSim.abstractRadar;
  exports eng.jAtcSim.abstractRadar.global;
  exports eng.jAtcSim.abstractRadar.global.events;
  exports eng.jAtcSim.abstractRadar.settings;
  requires eng.eSystem;
  requires JAtcSim.Modules.Backend.SharedLib;
  requires JAtcSim.Modules.Backend.AirplanesLib;
  requires JAtcSim.Modules.Backend.AreaLib;
  requires JAtcSim.Modules.Backend.AirplaneTypeLib;
  requires java.desktop;
  requires JAtcSim.Modules.Backend.MessagingLib;
  requires JAtcSim.Modules.Backend.SimulationLib;
  requires eng.eXmlSerialization;
  requires JAtcSim.Modules.Backend.SpeechesLib;
  requires JAtcSim.modules.backend.TextProcessingLib;
  requires AnotherXml;
  requires eng.newXmlUtils;
}
