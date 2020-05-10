package eng.jAtcSim.newLib.textProcessing.implemented.planeFormatter;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.shared.exceptions.ToDoException;
import eng.jAtcSim.newLib.speeches.airplane.IFromPlaneSpeech;
import eng.jAtcSim.newLib.textProcessing.formatting.IPlaneFormatter;
import eng.jAtcSim.newLib.textProcessing.implemented.formatterHelpers.TextSpeechFormatter;
import eng.jAtcSim.newLib.textProcessing.implemented.formatterHelpers.TextSpeechFormatterList;
import eng.jAtcSim.newLib.textProcessing.implemented.planeFormatter.typeFormatters.ChangeAltitudeCommandFormatter;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class PlaneFormatter implements IPlaneFormatter<String> {

  private final static TextSpeechFormatterList formatters;

  static{
    formatters = new TextSpeechFormatterList();
    TextSpeechFormatter<?> tsf = new ChangeAltitudeCommandFormatter();
    formatters.add(tsf);
  }

  @Override
  public String format(IFromPlaneSpeech input) {
//    TextSpeechFormatter<IFromPlaneSpeech> fmt = formatters.get(input);
//    String ret = fmt.format(input);
//    return ret;
    throw new ToDoException();
  }
}
