package eng.jAtcSim.newLib.textProcessing.implemented.planeFormatter.typeFormatters;

import eng.eSystem.utilites.ReflectionUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.speeches.airplane.IFromPlaneSpeech;
import eng.jAtcSim.newLib.speeches.base.ISpeech;
import eng.jAtcSim.newLib.textProcessing.formatting.IFormatter;
import eng.jAtcSim.newLib.textProcessing.implemented.formatterHelpers.TextSpeechFormatter;

import java.lang.reflect.Type;

public abstract class SmartTextSpeechFormatter<T extends ISpeech> extends TextSpeechFormatter<T> {

  private final Class<? extends T> type;

  public SmartTextSpeechFormatter() {
    Type[] tmp = ReflectionUtils.getParameterizedTypes(this);
    EAssert.isTrue(tmp.length == 1, "Exactly one generic parameter expected here.");
    EAssert.isNotNull(tmp[0]);
    EAssert.isTrue(tmp[0] instanceof Class, "Type is expected to be instance of Class.");
    this.type = (Class<? extends T>) tmp[0];
  }

  @Override
  public abstract String format(T input);

  @Override
  public final Class<? extends T> getSourceType() {
    return type;
  }
}
