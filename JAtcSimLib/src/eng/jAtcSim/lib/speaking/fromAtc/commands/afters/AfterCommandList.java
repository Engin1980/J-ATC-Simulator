/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.speaking.fromAtc.commands.afters;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.exceptions.ENotSupportedException;
import eng.jAtcSim.lib.speaking.ICommand;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeHeadingCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.HoldCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ProceedDirectCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ShortcutCommand;
import eng.jAtcSim.lib.world.Navaid;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Marek
 */
public class AfterCommandList {

  private final List<Item> inner = new LinkedList<>();

  public void consolePrint() {
    for (Item item : inner) {
      String s = String.format("%s -> %s", item.antecedent.toString(), item.consequent.toString());
      System.out.println(s);
    }
  }

  public void clear() {
    inner.clear();
  }

  public void add(AfterCommand afterCommand, IAtcCommand consequent) {
    Item it = new Item(afterCommand, consequent);
    inner.add(it);
  }

  public void removeByAntecedent(Class commandType, boolean removeWholeSequence) {
    List<Item> toRem = new ArrayList<>();
    for (Item item : inner) {
      if (item.antecedent.getClass().equals(commandType))
        toRem.add(item);
    }

    for (Item item : toRem) {
      if (removeWholeSequence) {
        removeWithSequenceForward(item);
      } else {
        inner.remove(item);
      }
    }
  }

  public void removeByConsequent(Class commandType, boolean removeSequenceBackward) {
    List<Item> toRem = inner.stream().filter(q -> q.consequent.getClass().equals(commandType)).collect(Collectors.toList());

    for (Item item : toRem) {
      if (removeSequenceBackward) {
        removeWithSequenceBackward(item);
      } else {
        inner.remove(item);
      }
    }
  }

  public List<IAtcCommand> getAndRemoveSatisfiedCommands(Airplane referencePlane, Coordinate currentTargetCoordinateOrNull) {
    double spd = referencePlane.getSpeed();
    Coordinate cor = referencePlane.getCoordinate();

    List<IAtcCommand> ret = new LinkedList<>();

    int i = 0;
    while (i < inner.size()) {
      if (inner.get(i).antecedent instanceof AfterAltitudeCommand) {
        AfterAltitudeCommand cmd = (AfterAltitudeCommand) inner.get(i).antecedent;
        boolean isPassed = isAfterAltitudePassed(cmd, referencePlane.getAltitude());
        if (isPassed) {
          ret.add(inner.get(i).consequent);
          inner.remove(i);
        } else {
          i++;
        }
      } else if (inner.get(i).antecedent instanceof AfterSpeedCommand) {
        int trgSpd = ((AfterSpeedCommand) inner.get(i).antecedent).getSpeedInKts();
        if (Math.abs(trgSpd - spd) < 10) {
          ret.add(inner.get(i).consequent);
          inner.remove(i);
        } else {
          i++;
        }
      } else if (inner.get(i).antecedent instanceof AfterNavaidCommand) {
        AfterNavaidCommand anc = (AfterNavaidCommand) inner.get(i).antecedent;
        if (anc.getNavaid().getCoordinate().equals(currentTargetCoordinateOrNull) == false) {
          // flying over some navaid, but not over current targeted by plane(pilot)
          i++;
        } else {
          double dist
              = Coordinates.getDistanceInNM(
              ((AfterNavaidCommand) inner.get(i).antecedent).getNavaid().getCoordinate(),
              cor);
          if (dist < 1.5) {
            ret.add(inner.get(i).consequent);
            inner.remove(i);
          } else {
            i++;
          }
        }
      } else {
        throw new ENotSupportedException();
      }
    }
    return ret;
  }

  public boolean isEmpty() {
    return inner.isEmpty();
  }

  public boolean hasLateralDirectionToNavaid(Navaid navaid) {
    boolean ret = this.inner.stream().anyMatch(q ->
        q.consequent instanceof ProceedDirectCommand &&
            ((ProceedDirectCommand) q.consequent).getNavaid().equals(navaid));
    return ret;
  }

  public boolean hasLateralDirectionAfterCoordinate(Coordinate coordinate) {
    boolean ret = this.inner.stream().anyMatch(
        o -> isLateralDirectionAfterNavaidCommand(coordinate, o));
    return ret;
  }

  private boolean isAfterAltitudePassed(AfterAltitudeCommand cmd, double altitudeInFt) {
    int trgAlt = cmd.getAltitudeInFt();
    boolean ret;
    switch (cmd.getRestriction()) {
      case exact:
        ret = Math.abs(trgAlt - altitudeInFt) < 100;
        break;
      case andAbove:
        ret = altitudeInFt > trgAlt;
        break;
      case andBelow:
        ret = altitudeInFt < trgAlt;
        break;
      default:
        throw new UnsupportedOperationException();
    }

    return ret;
  }

  private void removeWithSequenceForward(Item item) {
    inner.remove(item);

    List<Item> toRems = inner.stream().filter(q -> q.antecedent.getDerivationSource() == item.consequent).collect(Collectors.toList());
    for (Item toRem : toRems) {
      removeWithSequenceForward(toRem);
    }
  }

  private void removeWithSequenceBackward(Item item) {
    inner.remove(item);

    if (item.antecedent.getDerivationSource() == null) return;

    List<Item> toRems = inner.stream().filter(q -> item.antecedent.getDerivationSource() == q.consequent).collect(Collectors.toList());
    for (Item toRem : toRems) {
      removeWithSequenceBackward(toRem);
    }
  }

  private boolean isLateralDirectionAfterNavaidCommand(Coordinate coordinate, Item item) {
    boolean ret = false;
    if (item.antecedent instanceof AfterNavaidCommand) {
      AfterNavaidCommand anc = (AfterNavaidCommand) item.antecedent;
      if (anc.getNavaid().getCoordinate().equals(coordinate)) {
        if (item.consequent instanceof ChangeHeadingCommand ||
            item.consequent instanceof ProceedDirectCommand ||
            item.consequent instanceof HoldCommand ||
            item.consequent instanceof ShortcutCommand)
          ret = true;
      }
    }
    return ret;
  }

}

class Item {

  public final AfterCommand antecedent;
  public final IAtcCommand consequent;

  public Item(AfterCommand antecedent, IAtcCommand consequent) {
    this.antecedent = antecedent;
    this.consequent = consequent;
  }

  @Override
  public String toString() {
    return String.format("%s ==> %s", antecedent, consequent);
  }
}
