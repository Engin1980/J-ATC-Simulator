package jatcsimlib.messaging;

import jatcsimlib.global.Recorder;

import java.nio.file.Path;

public class MessageRecorder extends Recorder {

  public enum eAction{
    ADD,
    GET
  }

  public MessageRecorder(String filePath, boolean toConsole, boolean autoOpenFile) {
    super(Recorder.buildGenericLogFilePath(filePath), toConsole, autoOpenFile);
  }

  public void recordMessage(eAction action, Message msg){
    String line = String.format(
        "%S || FROM: %s; TO: %s; CONTENT: %s\n",
        action.toString(),
        msg.getSource().toString(),
        msg.getTarget().toString(),
        msg.getContent().toString()
    );

    System.out.println(line);

    super.logLine(
        line, true);
  }
}
