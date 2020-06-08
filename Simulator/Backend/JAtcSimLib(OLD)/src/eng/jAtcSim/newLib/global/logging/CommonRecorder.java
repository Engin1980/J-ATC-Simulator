package eng.jAtcSim.newLib.area.global.logging;

public class CommonRecorder extends Recorder {
  public CommonRecorder(String recorderName, String fileName, String fromTimeSeparator) {
    super(recorderName,
        new FileSaver(getRecorderFileName(fileName)),
        fromTimeSeparator);
  }

  public void write(String format, Object... params) {
    super.writeLine(String.format(format, params));
  }
}
