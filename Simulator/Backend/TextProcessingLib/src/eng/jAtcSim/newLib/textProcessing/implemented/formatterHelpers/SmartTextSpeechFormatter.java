package eng.jAtcSim.newLib.textProcessing.implemented.formatterHelpers;

import eng.eSystem.exceptions.ApplicationException;
import eng.eSystem.utilites.ReflectionUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.speeches.airplane.IFromPlaneSpeech;
import eng.jAtcSim.newLib.speeches.base.ISpeech;
import eng.jAtcSim.newLib.textProcessing.formatting.IFormatter;
import eng.jAtcSim.newLib.textProcessing.implemented.formatterHelpers.TextSpeechFormatter;

import java.lang.reflect.Type;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

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
  public final String format(ISpeech input){
    T typedInput;
    try {
      typedInput = (T) input;
    } catch (Exception ex){
      throw new ApplicationException(sf(
          "SmartTextSpeechFormatter tried to cast instance of '%s' into required type '%s', but was not succesfull.",
          input.getClass().getName(),
          type.getName()), ex);
    }
    String ret = _format(typedInput);
    return ret;
  }

  protected abstract String _format(T input);

  @Override
  public final Class<? extends T> getSourceType() {
    return type;
  }
}
