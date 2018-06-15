/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.atcs;

import eng.jAtcSim.lib.Acc;
import eng.eSystem.EStringBuilder;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.global.logging.AbstractSaver;
import eng.jAtcSim.lib.global.logging.FileSaver;
import eng.jAtcSim.lib.global.logging.Recorder;
import eng.jAtcSim.lib.messaging.Message;

import java.io.OutputStream;
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
    String fileName = Recorder.getRecorderFileName(atc.getName() + ".log");
    AtcRecorder ret = new AtcRecorder(atc, new FileSaver(fileName));
    return ret;
  }

  private AtcRecorder(Atc atc, AbstractSaver os) {
    super(atc.getName(), os, SEPARATOR);
  }

  public void write(Message m) {
    sb.clear();

    String src = getMessageObjectString(m.getSource());
    String trg = getMessageObjectString(m.getTarget());
    String cnt = getMessageContentString(m.getContent());

    ETime now = Acc.now();
    sb.clear();

    sb.append("MSG ").append(SEPARATOR);
    sb.appendFormat(" %s ", now.toString()).append(SEPARATOR);
    sb.appendFormat("FROM: %s ", src).append(SEPARATOR);
    sb.appendFormat("TO: %s ", trg).append(SEPARATOR);
    sb.appendFormat(" %s ", cnt);

    sb.appendLine();
    super.writeLine(sb.toString());
  }

  public void write(Atc atc, String type, String content){
    String s = String.format("%s %s %s %s %s %s %s\r\n",
      type,
      SEPARATOR,
      Acc.now().toString(),
      SEPARATOR,
      atc.getName(),
      SEPARATOR,
      content);
    super.writeLine(s);
  }
}
