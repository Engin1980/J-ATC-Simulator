package eng.jAtcSim.newLib.textProcessing.formatting;

public interface IFormatter<TInputType, TOutputType> {
  TOutputType format(TInputType input);
}
