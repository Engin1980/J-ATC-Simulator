/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.speaking.fromAtc.commands.afters;

/**
 *
 * @author Marek
 */
public class AfterAltitudeCommand extends AfterCommand {

  public enum ERestriction{
    exact,
    andAbove,
    andBelow
  }

  private final int altitudeInFt;
  private final ERestriction restriction;

  public AfterAltitudeCommand(int altitudeInFt, ERestriction restriction) {

    this.altitudeInFt = altitudeInFt;
    this.restriction = restriction;
  }

  public int getAltitudeInFt() {
    return altitudeInFt;
  }

  public ERestriction getRestriction() {
    return restriction;
  }

  @Override
  public String toString() {
    return "AA{"+ altitudeInFt + " " + restriction.toString() + '}';
  }
  
}
