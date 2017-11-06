package jatcsimlib.speaking;

import jatcsimlib.Acc;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class SpeechDelayer {

  private final int minimalDelay;
  private final int maximalDelay;

  public SpeechDelayer(int minimalDelay, int maximalDelay) {
    this.minimalDelay = minimalDelay;
    this.maximalDelay = maximalDelay;
  }

  class DelayedSpeech {
    public final ISpeech speech;
    public int delayLeft;

    public DelayedSpeech(ISpeech speech, int delay) {
      this.speech = speech;
      this.delayLeft = delay;
    }
  }

  private List<DelayedSpeech> inner = new LinkedList<>();

  /**
   * Adds speech with random delay
   * @param speech
   */
  public void add(ISpeech speech){
    int delay = Acc.rnd().nextInt(minimalDelay,  maximalDelay+1);
    add(speech, delay);
  }

  public void add (ISpeech speech, int delay){
    int minDelay = getLastDelay() + delay; // todo here can be also "min" function
    DelayedSpeech delayedMessage = new DelayedSpeech(speech, minDelay);
    inner.add(delayedMessage);
  }

  public void add (Collection<? extends ISpeech> speeches, int delay){
    int minDelay = getLastDelay() + delay; // todo here can be also "min" function
    for (ISpeech speech : speeches) {
      DelayedSpeech delayedMessage = new DelayedSpeech(speech, minDelay);
      inner.add(delayedMessage);
    }
  }

  public SpeechList get(){
    lowerDelay();
    SpeechList ret = new SpeechList();
    while (inner.isEmpty() == false){
      DelayedSpeech delayedMessage = inner.get(0);
      if (delayedMessage.delayLeft > 0) break;

      inner.remove(0);
      ret.add(delayedMessage.speech);
    }
    return ret;
  }

  private void lowerDelay() {
    for (DelayedSpeech delayedMessage : inner) {
      delayedMessage.delayLeft = delayedMessage.delayLeft - 1;
    }
  }

  private int getLastDelay(){
    if (inner.isEmpty())
      return 0;
    else
      return inner.get(inner.size()-1).delayLeft;
  }

  public void clear(){
    inner.clear();
  }

  public int size(){
    return inner.size();
  }

  public ISpeech get(int index){
    return inner.get(index).speech;
  }

  public void removeAt(int index){
    inner.remove(index);
  }

}
