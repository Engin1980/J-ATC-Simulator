package eng.jAtcSim.lib.speaking;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.EList;
import eng.eSystem.utilites.StringUtil;
import eng.jAtcSim.lib.exceptions.ERuntimeException;
import eng.jAtcSim.lib.messaging.IMessageContent;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;

public class SpeechList<T extends ISpeech> extends EList<T> implements IMessageContent {

  public SpeechList() {
  }

  public SpeechList(T... speeches) {
    super(speeches);
  }


  public SpeechList(Iterable<? extends T> lst) {
    super(lst);
  }

  public IAtcCommand getAsCommand(int index) {
    IAtcCommand ret = getAs(index);
    return ret;
  }

  public INotification getAsNotification(int index) {
    INotification ret = getAs(index);
    return ret;
  }

  public <U extends T> U tryGetAs(int index) {
    U ret;
    ISpeech tmp = this.get(index);
    try {
      ret = (U) tmp;
    } catch (Exception ex) {
      ret = null;
    }
    return ret;
  }

  public <U extends T> U getAs(int index) {
    U ret = this.tryGetAs(index);
    if (ret == null)
      throw new ERuntimeException("Element at index {" + index + "} cannot be cast to requested type.");
    return ret;
  }

  public SpeechList<T> clone() {
    SpeechList ret = new SpeechList(this);
    return ret;
  }

  public boolean containsType(Class type) {
    for (ISpeech speech : this) {
      if (speech.getClass().equals(type))
        return true;
    }
    return false;
  }

  public <T extends ISpeech> SpeechList<T> convertTo() {
    SpeechList<T> ret = new SpeechList<>();
    for (ISpeech item : this) {
      T convertedItem = (T) item;
      ret.add(convertedItem);
    }
    return ret;
  }

  @Override
  public T get(int index) {
    return super.get(index);
  }

  @Override
  public String toString(){
    EStringBuilder sb = new EStringBuilder();
    sb.appendItems(this, q -> q.toString(), ";");
    return sb.toString();
  }
}
