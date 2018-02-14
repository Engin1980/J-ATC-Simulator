/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.atcs;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.global.ETime;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Marek
 */
public class WaitingList {

  private static final int AWAITING_SECONDS = 20;

  private final List<ETime> times = new LinkedList<>();
  private final List<Airplane> planes = new LinkedList<>();

  public void add(Airplane plane) {
    this.times.add(Acc.now().addSeconds(AWAITING_SECONDS));
    this.planes.add(plane);
  }

  public void remove(Airplane plane) {
    int index = planes.indexOf(plane);
    planes.remove(index);
    times.remove(index);
  }

  List<Airplane> getAwaitings() {
    List<Airplane> ret = new LinkedList<>();

    ETime now = Acc.now();
    for (int i = 0; i < planes.size(); i++) {
      if (now.isAfter(times.get(i))) {
        times.set(i, now.addSeconds(AWAITING_SECONDS));
        ret.add(planes.get(i));
      }
    }

    return ret;
  }

  boolean contains(Airplane plane) {
    return planes.contains(plane);
  }
}