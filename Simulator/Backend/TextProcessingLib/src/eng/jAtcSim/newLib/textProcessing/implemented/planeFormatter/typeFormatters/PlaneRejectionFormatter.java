package eng.jAtcSim.newLib.textProcessing.implemented.planeFormatter.typeFormatters;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.speeches.airplane.IPlaneSpeech;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PlaneRejection;
import eng.jAtcSim.newLib.speeches.base.ISpeech;
import eng.jAtcSim.newLib.textProcessing.formatting.IFormatter;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class PlaneRejectionFormatter extends SmartTextSpeechFormatter<PlaneRejection, IPlaneSpeech> {

  public PlaneRejectionFormatter(IFormatter<IPlaneSpeech, String> parent) {
    super(parent);
  }

  @Override
  public String format(PlaneRejection input) {
    EStringBuilder ret = new EStringBuilder();
    ret.append("Unable to ")
        .append("'")
        .append(parent.format(input.getOrigin()))
        .append("'. ")
        .append(input.getReason());
    return ret.toString();
  }
}
