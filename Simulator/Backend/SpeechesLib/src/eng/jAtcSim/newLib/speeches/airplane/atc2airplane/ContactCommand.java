
package eng.jAtcSim.newLib.speeches.airplane.atc2airplane;


import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;

/**
 *
 * @author Marek
 */
public class ContactCommand implements ICommand {
  private final AtcId atc;

  public ContactCommand(AtcId atc) {
    this.atc = atc;
  }

  public AtcId getAtc() {
    return atc;
  }

  @Override
  public String toString(){
    String ret = "Contact " + atc.getName() + " {command}";

    return ret;
  }
}
