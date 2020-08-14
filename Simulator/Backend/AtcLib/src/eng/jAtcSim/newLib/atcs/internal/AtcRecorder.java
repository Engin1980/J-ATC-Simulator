/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.newLib.atcs.internal;


import eng.eSystem.EStringBuilder;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.atcs.contextLocal.Context;
import eng.jAtcSim.newLib.messaging.IMessageContent;
import eng.jAtcSim.newLib.messaging.Message;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.logging.Journal;
import eng.jAtcSim.newLib.shared.logging.writers.*;

public class AtcRecorder  {

  private final static int DEFAULT_STRING_BUILDER_SIZE = 256;
  private final static String SEPARATOR = ";";
  private final Journal journal;

  public static AtcRecorder create(AtcId atcId) {
    AtcRecorder ret = new AtcRecorder(atcId);
    return ret;
  }

  private final AtcId atcId;
  private final EStringBuilder sb = new EStringBuilder(DEFAULT_STRING_BUILDER_SIZE);

  private AtcRecorder(AtcId atcId) {
    EAssert.Argument.isNotNull(atcId, "atcId");

    ILogWriter wrt = FileWriter.createToDefaultFolder(atcId.getName() + ".txt", true);
    wrt = new AutoNewLineLogWriter(wrt);
    wrt = new SimTimePipeLogWriter(wrt);
    wrt = new RealTimePipeLogWriter(wrt);
    this.journal = new Journal(
        atcId.getName(), true, wrt);

    this.atcId = atcId;
  }

  public void write(Message m) {
    sb.clear();

    String src = m.getSource().toString();
    String trg = m.getTarget().toString();
    String cnt = getMessageContentString(m.getContent());

    sb.clear();

    sb.append("MSG ").append(SEPARATOR);
    sb.appendFormat(" %s ", Context.getShared().getNow().toString()).append(SEPARATOR);
    sb.appendFormat("FROM: %s ", src).append(SEPARATOR);
    sb.appendFormat("TO: %s ", trg).append(SEPARATOR);
    sb.appendFormat(" %s ", cnt);

    sb.appendLine();
    this.journal.write(sb.toString());
  }

  public void write(String type, String content) {
    String s = String.format("%s %s %s %s %s %s %s",
        type,
        SEPARATOR,
        Context.getShared().getNow().toString(),
        SEPARATOR,
        this.atcId.getName(),
        SEPARATOR,
        content);
    this.journal.write(s);
  }

  private String getMessageContentString(IMessageContent content) {
    return content.toString();
  }
}
