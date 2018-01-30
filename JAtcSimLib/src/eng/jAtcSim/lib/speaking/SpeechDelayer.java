package eng.jAtcSim.lib.speaking;

import eng.jAtcSim.lib.Acc;
import jatcsimlib.Acc;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class SpeechDelayer {

  private final int minimalDelay;
  private final int maximalDelay;
  private int currentDelay = 0;

  public SpeechDelayer(int minimalDelay, int maximalDelay) {
    this.minimalDelay = minimalDelay;
    this.maximalDelay = maximalDelay;
    newRandomDelay();
  }

  public void newRandomDelay() {
    this.currentDelay = Acc.rnd().nextInt(minimalDelay,  maximalDelay+1);
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
  public void add (ISpeech speech){
    int minDelay = Math.min(getLastDelay(maximalDelay), currentDelay);
    DelayedSpeech delayedMessage = new DelayedSpeech(speech, minDelay);
    inner.add(delayedMessage);
  }

  public void add (Collection<? extends ISpeech> speeches){
    int minDelay = Math.min(getLastDelay(maximalDelay), currentDelay);
    for (ISpeech speech : speeches) {
      DelayedSpeech delayedMessage = new DelayedSpeech(speech, minDelay);
      inner.add(delayedMessage);
    }
  }

  public void addNoDelay (Collection<? extends ISpeech> speeches){
    int minDelay = 0;
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

  private int getLastDelay(int valueIfEmpty){
    if (inner.isEmpty())
      return valueIfEmpty;
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
