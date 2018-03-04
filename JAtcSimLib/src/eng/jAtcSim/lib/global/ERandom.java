/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.global;

import java.util.Random;

/**
 *
 * @author Marek
 */
public class ERandom extends Random {
  public int nextInt (int fromInclusive, int toExclusive){
    return nextInt(toExclusive - fromInclusive) + fromInclusive;
  }
  
  @Override
  public int nextInt (int maximum){
    if (maximum == 0)
      return 0;
    else 
      return super.nextInt(maximum);
  }

  public double nextDouble(double fromInclusive, double toExclusive) {
    return nextDouble(toExclusive - fromInclusive) + fromInclusive;
  }

  public double nextDouble(double maximum){
    return super.nextDouble() * maximum;
  }
}
