/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.global;

import eng.jAtcSim.lib.exceptions.ERuntimeException;
import eng.jAtcSim.lib.world.approaches.Approach;
import eng.jAtcSim.lib.world.approaches.CurrentApproachInfo;

/**
 *
 * @author Marek
 */
public abstract class MustBeBinded {
  private boolean binded;
  
  public void checkBinded(){
    if (!binded){
      throw new ERuntimeException(
          "Object " + this.getClass().getSimpleName() + 
              " has not been binded. Did you call \"bind()\" method over area antecedent loading?");
    }
  }
  
  public void bind(){
    _bind();
    binded = true;
  }

  protected abstract void _bind();

  public boolean isBinded() {
    return binded;
  }
}
