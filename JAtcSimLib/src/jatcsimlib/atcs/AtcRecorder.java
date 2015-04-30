/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.atcs;

import jatcsimlib.Acc;
import jatcsimlib.global.EStringBuilder;
import jatcsimlib.global.ETime;
import jatcsimlib.global.Recorder;
import jatcsimlib.messaging.Message;
import java.nio.file.Path;

/**
 *
 * @author Marek Vajgl
 */
public class AtcRecorder extends Recorder {

  private final static int DEFAULT_STRING_BUILDER_SIZE = 256;
  private final static String SEPARATOR = ";";
  private final EStringBuilder sb = new EStringBuilder(DEFAULT_STRING_BUILDER_SIZE);

  public static AtcRecorder create(Atc atc) {
    Path filePath;
    filePath = Recorder.buildGenericLogFilePath(atc.getName() + ".log");
    AtcRecorder ret = new AtcRecorder(filePath);
    return ret;
  }

  private AtcRecorder(Path filePath) {
    super(filePath, false, true);
  }

  public void logMessage(Message m) {
    sb.clear();

    String src = getMessageObjectString(m.source);
    String trg = getMessageObjectString(m.target);
    String cnt = getMessageContentString(m.content);

    ETime now = Acc.now();
    sb.clear();

    sb.append("MSG ").append(SEPARATOR);
    sb.appendFormat(" %s ", now.toString()).append(SEPARATOR);
    sb.appendFormat("FROM: %s ", src).append(SEPARATOR);
    sb.appendFormat("TO: %s ", trg).append(SEPARATOR);
    sb.appendFormat(" %s ", cnt);

    sb.appendLine();
    logLine(sb.toString());
  }

  public void log (Atc atc, String type, String content){
    String s = String.format("%s %s %s %s %s\r\n",
      type,
      SEPARATOR,
      atc.getName(),
      SEPARATOR,
      content);
    super.logLine(s);
  }
}
