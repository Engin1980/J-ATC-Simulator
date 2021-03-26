package eng.jAtcSim.newLib.traffic;

import eng.eSystem.collections.*;
import eng.eSystem.exceptions.ToDoException;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.time.EDayTime;
import eng.jAtcSim.newLib.traffic.contextLocal.Context;
import eng.jAtcSim.newLib.traffic.movementTemplating.MovementTemplate;

import exml.IXPersistable;
import exml.annotations.XConstructor;
import exml.annotations.XIgnored;

import java.io.DataInput;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class TrafficProvider implements IXPersistable {

  @XIgnored private final ITrafficModel trafficModel;
  private final IMap<Integer, IList<MovementTemplate>> movementsForDay = new EMap<>();

  @XConstructor

  private TrafficProvider() {
    this.trafficModel = null;
  }

  public TrafficProvider(ITrafficModel trafficModel) {
    EAssert.isNotNull(trafficModel);
    this.trafficModel = trafficModel;
  }

  public IReadOnlyList<ExpectedMovement> getExpectedTimesForDay(int dayIndex) {
    throw new ToDoException();
  }

  public IReadOnlyList<MovementTemplate> getMovementsUntilTime(EDayTime untilTime) {
    EAssert.Argument.isNotNull(untilTime, "untilTime");
    EAssert.isTrue(movementsForDay.containsKey(untilTime.getDays()));

    IList<MovementTemplate> ret = new EList<>();
    for (Integer dayKey : movementsForDay.getKeys()) {
      if (dayKey > untilTime.getDays()) continue;
      IReadOnlyList<MovementTemplate> tmp = movementsForDay.get(dayKey).where(q -> q.getAppearanceTime().isBeforeOrEq(untilTime.getTime()));
      ret.addMany(tmp);
      movementsForDay.get(dayKey).removeMany(tmp);
    }

    // TODO
    // delete movements for days that have already passed
//    movementsForDay.removeMany(
//            movementsForDay.whereValue(q -> q.isEmpty()).getKeys());

    return ret;
  }

  public void init() {
    if (movementsForDay.getKeys().isEmpty()) {
      prepareTrafficForDay(0);
      movementsForDay.get(0).remove(q->q.getAppearanceTime().isBefore(Context.getShared().getNow().getTime()));
    }
  }

  public void prepareTrafficForDay(int dayIndex) {
    EAssert.isTrue(
            movementsForDay.containsKey(dayIndex) == false,
            sf("Cannot generate movements for day %d, as it has been already generated.", dayIndex));
    prepareMovementsForDay(dayIndex);
  }

  private void prepareMovementsForDay(int dayIndex) {
    IReadOnlyList<MovementTemplate> tmp = trafficModel.generateMovementsForOneDay();
    IList<MovementTemplate> oth = new EList<>(tmp);
    movementsForDay.set(dayIndex, oth);
  }
}
