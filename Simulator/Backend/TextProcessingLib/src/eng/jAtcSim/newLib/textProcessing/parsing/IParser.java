package eng.jAtcSim.newLib.textProcessing.parsing;

public interface IParser<TOutputType> {

  TOutputType parse(Object input);

  boolean acceptsType(Class<?> type);

  boolean canAccept(Object input);

  //TODO delete
//  public SpeechList<IAtcCommand> parseMultipleCommands(String text) {
//    SpeechList lst = this.parseMulti(text);
//    SpeechList<IAtcCommand> ret = lst.convertTo();
//    return ret;
//  }
}
