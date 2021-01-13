package eng.jAtcSim.newLib.messaging;

import eng.eSystem.validation.EAssert;
import eng.newXmlUtils.annotations.XmlConstructor;
import eng.newXmlUtils.annotations.XmlConstructorParameter;
import exml.IXPersistable;
import exml.annotations.XConstructor;

public class Message implements IXPersistable {

  private final Participant source;
  private final Participant target;
  private final IMessageContent content;

  @XConstructor
  @XmlConstructor
  private Message(){
    this.source = null;
    this.target = null;
    this.content = null;
  }

  @XConstructor
  @XmlConstructor
  public Message(Participant source, Participant target, IMessageContent content) {
    EAssert.Argument.isNotNull(source, "source");
    EAssert.Argument.isNotNull(target, "target");
    EAssert.Argument.isNotNull(content, "content");

    this.source = source;
    this.target = target;
    this.content = content;
  }

  public <T extends IMessageContent> T getContent() {
    T ret;
    try {
      ret = (T) content;
    } catch (Exception ex) {
      throw new ClassCastException("Cannot convert {source} to requested kind.");
    }
    return ret;
  }

  public Participant getSource() {
    return source;
  }

  public Participant getTarget() {
    return target;
  }

  public <T> boolean isContentOfType(Class<? extends T> type) {
    return type.isAssignableFrom(content.getClass());
  }

  @Override
  public String toString() {
    return String.format("MSG: %s -> %s : %s", source.toString(), target.toString(), content.toString());
  }
}
