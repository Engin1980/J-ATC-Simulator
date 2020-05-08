package eng.jAtcSim.newLib.textProcessing.base;

import eng.jAtcSim.newLib.speeches.SpeechList;
import eng.jAtcSim.newLib.speeches.base.ISpeech;

public interface IParser<TSource, TTarget extends ISpeech> {

  SpeechList<TTarget> parse(TSource text);

  //TODO delete
//  public SpeechList<IAtcCommand> parseMultipleCommands(String text) {
//    SpeechList lst = this.parseMulti(text);
//    SpeechList<IAtcCommand> ret = lst.convertTo();
//    return ret;
//  }
}
