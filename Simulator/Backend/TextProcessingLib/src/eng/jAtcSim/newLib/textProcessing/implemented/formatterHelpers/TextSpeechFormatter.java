package eng.jAtcSim.newLib.textProcessing.implemented.formatterHelpers;

import eng.eSystem.collections.*;
import eng.eSystem.utilites.Selector;
import eng.jAtcSim.newLib.shared.exceptions.ToDoException;
import eng.jAtcSim.newLib.speeches.airplane.IFromPlaneSpeech;
import eng.jAtcSim.newLib.speeches.base.ISpeech;
import eng.jAtcSim.newLib.textProcessing.formatting.IFormatter;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public abstract class TextSpeechFormatter<T extends ISpeech> {

  public abstract Class<? extends T> getSourceType();

  public abstract String format(ISpeech input);
}
