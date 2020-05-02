package eng.jAtcSim.newLib.airplanes;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;

import java.util.Iterator;

public class AirplaneList implements Iterable<IAirplane> {
  private final IList<Airplane> inner = new EList<>();
  private final IList<IAirplane> readers = new EList<>();

  @Override
  public Iterator<IAirplane> iterator() {
    return readers.iterator();
  }
}
