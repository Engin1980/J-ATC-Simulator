package jatcsimlib.speaking;

import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.newMessaging.IMessageContent;
import jatcsimlib.speaking.commands.Command;

import java.util.ArrayList;
import java.util.Collection;

public class SpeechList extends ArrayList<Speech> implements IMessageContent {

  /**
   * Gets element at ith. index cast to Command. If its not command, exception is thrown
   * @param index Index to get
   * @return Element cast to Command
   */
  public Command getCommand(int index){
//    Command ret;
//    Speech tmp = this.get(index);
//    if (tmp instanceof Command)
//      ret = (Command) tmp;
//    else
//      throw new ERuntimeException("Element at index {" + index + "} is not of type Command, but is " + tmp.getClass().getName());
    Command ret = getAs(index);

    return ret;
  }

   public <T> T tryGetAs(int index){
    T ret;
    Speech tmp = this.get(index);
    try{
      ret = (T) tmp;
    } catch (Exception ex){
      ret = null;
    }
    return ret;
   }

  public <T> T getAs(int index){
    T ret = this.tryGetAs(index);
    if (ret == null)
      throw new ERuntimeException("Element at index {" + index +"} cannot be cast to requested type.");
    return ret;
  }

  public SpeechList(){}

  public SpeechList(Collection<? extends Speech> lst){
    super(lst);
  }

  public SpeechList clone(){
    SpeechList ret = new SpeechList(this);
    return ret;
  }

  public boolean containsType(Class type){
    for (Speech speech : this) {
      if (speech.getClass().equals(type))
        return true;
    }
    return false;
  }

}
