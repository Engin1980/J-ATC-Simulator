package eng.jAtcSim.newLib.textProcessing.implemented.planeFormatter;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.shared.exceptions.ToDoException;
import eng.jAtcSim.newLib.speeches.airplane.IFromPlaneSpeech;
import eng.jAtcSim.newLib.speeches.airplane.IPlaneSpeech;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PlaneConfirmation;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PlaneRejection;
import eng.jAtcSim.newLib.textProcessing.formatting.IPlaneFormatter;
import eng.jAtcSim.newLib.textProcessing.implemented.formatterHelpers.TextSpeechFormatter;
import eng.jAtcSim.newLib.textProcessing.implemented.formatterHelpers.TextSpeechFormatterList;
import eng.jAtcSim.newLib.textProcessing.implemented.planeFormatter.typeFormatters.ChangeAltitudeCommandFormatter;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class PlaneFormatter implements IPlaneFormatter<String> {

  private final TextSpeechFormatterList<IPlaneSpeech> formatters;
  private final Class<?> rejectionType = PlaneRejection.class;
  private final Class<?> confirmationType = PlaneConfirmation.class;


  public PlaneFormatter(){
    formatters = new TextSpeechFormatterList<>();
    formatters.add(new ChangeAltitudeCommandFormatter());
  }

  @Override
  public String format(IFromPlaneSpeech input) {
    if (rejectionType.isAssignableFrom(input.getClass()))
      processRejection((PlaneRejection) input);
    else if (confirmationType.isAssignableFrom(input.getClass()))
      processConfirmation((PlaneConfirmation) input);
    else
      processNormal(input);
//    TextSpeechFormatter<IFromPlaneSpeech> fmt = formatters.get(input);
//    String ret = fmt.format(input);
//    return ret;
    throw new ToDoException();
  }
}
