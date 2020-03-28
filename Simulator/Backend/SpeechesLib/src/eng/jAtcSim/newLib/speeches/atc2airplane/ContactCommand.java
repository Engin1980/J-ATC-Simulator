
package eng.jAtcSim.newLib.speeches.atc2airplane;


import eng.jAtcSim.newLib.shared.enums.eAtcType;
import eng.jAtcSim.newLib.speeches.ICommand;

/**
 *
 * @author Marek
 */
public class ContactCommand implements ICommand {
  private final String atcName;
  private final double frequency;

  public ContactCommand(String atcName, double frequency) {
    this.atcName = atcName;
    this.frequency = frequency;
  }

  public String getAtcName() {
    return atcName;
  }

  public double getAtcFrequency() {
    return frequency;
  }

  @Override
  public String toString(){
    String ret = "Contact " + atcName + " (" + frequency + ") {command}";

    return ret;
  }
}
