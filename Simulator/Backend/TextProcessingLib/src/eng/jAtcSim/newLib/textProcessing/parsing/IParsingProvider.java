package eng.jAtcSim.newLib.textProcessing.parsing;

public interface IParsingProvider<TOutputType> {

  TOutputType parse(Object input);

  boolean acceptsType(Class<?> type);

  boolean canAccept(Object input);
}
