/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.atcs;

import eng.eSystem.Tuple;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.global.ETime;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Marek
 */
public class WaitingList {

  private static final int AWAITING_SECONDS = 20;

  private final IList<Tuple<ETime, Airplane>> inner = new EList<>();

  public void add(Airplane plane) {
    this.inner.add(new Tuple<>(Acc.now().addSeconds(AWAITING_SECONDS), plane));
  }

  public void remove(Airplane plane) {
    int index = inner.getIndexOf(q -> q.getB().equals(plane));
    inner.removeAt(index);
  }

  IReadOnlyList<Airplane> getAwaitings() {
    ETime now = Acc.now();

    IList<Airplane> ret = new EList<>();

    for (int i = 0; i < inner.size(); i++) {
      if (now.isAfter(inner.get(i).getA())) {
        inner.get(i).setA(now.addSeconds(AWAITING_SECONDS));
        ret.add(inner.get(i).getB());
      }
    }

    return ret;
  }

  boolean contains(Airplane plane) {
    return inner.isAny(q -> q.getB().equals(plane));
  }
}
