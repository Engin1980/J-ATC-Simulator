package eng.jAtcSim.lib.messaging;

public class Message {

  private final IMessageParticipant source;
  private final IMessageParticipant target;
  private final IMessageContent content;

  public Message(IMessageParticipant source, IMessageParticipant target, IMessageContent content) {
    if (source == null) {
        throw new IllegalArgumentException("Value of {source} cannot not be null.");
    }
    if (target == null) {
        throw new IllegalArgumentException("Value of {target} cannot not be null.");
    }
    if (content == null) {
        throw new IllegalArgumentException("Value of {content} cannot not be null.");
    }

    this.source = source;
    this.target = target;
    this.content = content;
  }

  public boolean isSourceOfType(Class type){
    return type.isAssignableFrom(source.getClass());
  }

  public <T extends IMessageParticipant> T getSource(){
    T ret;
    try{
      ret = (T) source;
    } catch (Exception ex){
      throw new ClassCastException("Cannot convert {source} to requested type.");
    }
    return ret;
  }

  public <T extends  IMessageParticipant> T getTarget(){
    T ret;
    try{
      ret = (T) target;
    } catch (Exception ex){
      throw new ClassCastException("Cannot convert {source} to requested type.");
    }
    return ret;
  }

  public <T> T getContent(){
    T ret;
    try{
      ret = (T) content;
    } catch (Exception ex){
      throw new ClassCastException("Cannot convert {source} to requested type.");
    }
    return ret;
  }

  public boolean isContentOfType(Class type){
    return type.isAssignableFrom(content.getClass());
  }

  @Override
  public String toString() {
    return String.format("MSG: %s -> %s : %s", source.getName(), target.getName(), content.toString());
  }
}
