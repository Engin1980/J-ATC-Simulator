package eng.jAtcSim.newLib.shared.logging.writers;

import eng.eSystem.collections.*;

import java.io.IOException;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class ErrorForgivingWriter extends PipeLogWriter {

  private final boolean logErrorToConsole;

  public ErrorForgivingWriter(ILogWriter innerWriter, boolean logErrorToConsole) {
    super(innerWriter);
    this.logErrorToConsole = logErrorToConsole;
  }

  @Override
  public void write(String text) throws IOException {
    try{
      this.innerWriter.write(text);
    }catch (IOException ex){
      if (logErrorToConsole){
        System.out.println("ErrorForgivingWriter - write error");
        System.out.println("\tMessage: " + text);
        String prep = "\t";
        Throwable t = ex;
        while (t != null){
          System.out.println(prep + "Error: " + t.getMessage());
          System.out.println(prep + "\t" + t.getClass().getName());
          t = t.getCause();
          prep = prep + "\t";
        }
      }
    }
  }
}
