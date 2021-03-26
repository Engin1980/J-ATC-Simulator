package eng.jAtcSim.newLib.speeches.airplane.atc2airplane;


import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;

import exml.annotations.XConstructor;

/**
 * @author Marek
 */
public class ContactCommand implements ICommand {
  private final AtcId atc;

  public ContactCommand(AtcId atc) {
    this.atc = atc;
  }

  @XConstructor

  private ContactCommand() {
    atc = null;
  }

  public AtcId getAtc() {
    return atc;
  }

  @Override
  public String toString() {
    String ret = "Contact " + atc.getName() + " {command}";

    return ret;
  }
}
