/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.newLib.area.speaking.fromAtc.commands.afters;

import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.newLib.area.speaking.fromAtc.IAtcCommand;

/**
 *
 * @author Marek
 */
public abstract class AfterCommand implements IAtcCommand {
  @XmlIgnore
  private IAtcCommand derivationSource;

  public IAtcCommand getDerivationSource() {
    return derivationSource;
  }

  public void setDerivationSource(IAtcCommand derivationSource) {
    this.derivationSource = derivationSource;
  }
}
