package eng.jAtcSim.lib.global.logging;

public class CommonRecorder extends Recorder {
  public CommonRecorder(String recorderName, String fileName, String fromTimeSeparator) {
    super(recorderName,
        Recorder.createRecorderFileOutputStream(fileName),
        fromTimeSeparator);
  }

  public void write(String format, Object... params) {
    super.writeLine(String.format(format, params));
  }
}
