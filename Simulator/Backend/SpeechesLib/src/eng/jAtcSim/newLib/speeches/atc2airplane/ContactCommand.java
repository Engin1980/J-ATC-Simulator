
package eng.jAtcSim.newLib.speeches.atc2airplane;


import eng.jAtcSim.newLib.shared.enums.eAtcType;
import eng.jAtcSim.newLib.speeches.ICommand;

/**
 *
 * @author Marek
 */
public class ContactCommand implements ICommand {
  private final eAtcType atcType;

  public ContactCommand(eAtcType atcType) {
    this.atcType = atcType;
  }

  public eAtcType getAtcType() {
    return atcType;
  }

  @Override
  public String toString(){
    String ret = "Contact " + atcType.toString() + " {command}";

    return ret;
  }
}
