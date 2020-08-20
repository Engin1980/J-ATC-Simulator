module JAtcSim.modules.backend.TextProcessingLib {
  exports eng.jAtcSim.newLib.textProcessing.implemented.planeParser;
  exports eng.jAtcSim.newLib.textProcessing.implemented.atcParser;
  exports eng.jAtcSim.newLib.textProcessing.implemented.systemParser;
  exports eng.jAtcSim.newLib.textProcessing.implemented.atcFormatter;
  exports eng.jAtcSim.newLib.textProcessing.implemented.systemFormatter;
  exports eng.jAtcSim.newLib.textProcessing.formatting;
  exports eng.jAtcSim.newLib.textProcessing.parsing;
  requires eng.eSystem;
  requires JAtcSim.Modules.Backend.SharedLib;
  requires JAtcSim.Modules.Backend.SpeechesLib;
  requires JAtcSim.Modules.Backend.WeatherLib;
  requires JAtcSim.Modules.Backend.MessagingLib;
}