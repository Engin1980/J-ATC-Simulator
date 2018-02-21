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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Marek
 */
public class AfterCommandList {

  public void consolePrint(){
    for (Item item : inner) {
      String s = String.format("%s -> %s", item.after.toString(), item.consequent.toString());
      System.out.println(s);
    }
  }

  private final List<Item> inner = new LinkedList<>();

  public void add(AfterCommand afterCommand, IAtcCommand consequent) {
    Item it = new Item(afterCommand, consequent);
    inner.add(it);
  }

  public void removeByAntecedent(Class commandType, boolean removeWholeSequence) {
    List<Item> toRem = new ArrayList<>();
    for (Item item : inner) {
      if (item.after.getClass().equals(commandType))
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
    double alt = referencePlane.getAltitude();
    double spd = referencePlane.getSpeed();
    Coordinate cor = referencePlane.getCoordinate();

    List<IAtcCommand> ret = new LinkedList<>();

    int i = 0;
    while (i < inner.size()) {
      if (inner.get(i).after instanceof AfterAltitudeCommand) {
        int trgAlt = ((AfterAltitudeCommand) inner.get(i).after).getAltitudeInFt();
        if (Math.abs(trgAlt - alt) < 100) {
          ret.add(inner.get(i).consequent);
          inner.remove(i);
        } else {
          i++;
        }
      } else if (inner.get(i).after instanceof AfterSpeedCommand) {
        int trgSpd = ((AfterSpeedCommand) inner.get(i).after).getSpeedInKts();
        if (Math.abs(trgSpd - spd) < 10) {
          ret.add(inner.get(i).consequent);
          inner.remove(i);
        } else {
          i++;
        }
      } else if (inner.get(i).after instanceof AfterNavaidCommand) {
        AfterNavaidCommand anc = (AfterNavaidCommand) inner.get(i).after;
        if (anc.getNavaid().getCoordinate().equals(currentTargetCoordinateOrNull) == false) {
          // flying over some navaid, but not over current targeted by plane(pilot)
          i++;
        } else {
          double dist
              = Coordinates.getDistanceInNM(
              ((AfterNavaidCommand) inner.get(i).after).getNavaid().getCoordinate(),
              cor);
          if (dist < 2) {
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

  public ICommand getLast() {
    return inner.get(inner.size() - 1).consequent;
  }

  private void removeWithSequenceForward(Item item) {
    inner.remove(item);

    List<Item> toRems = inner.stream().filter(q -> q.after.getDerivationSource() == item.consequent).collect(Collectors.toList());
    for (Item toRem : toRems) {
      removeWithSequenceForward(toRem);
    }
  }

  private void removeWithSequenceBackward(Item item) {
    inner.remove(item);

    if (item.after.getDerivationSource() == null) return;

    List<Item> toRems = inner.stream().filter(q -> item.after.getDerivationSource() == q.consequent).collect(Collectors.toList());
    for (Item toRem : toRems) {
      removeWithSequenceBackward(toRem);
    }
  }

}

class Item {

  public final AfterCommand after;
  public final IAtcCommand consequent;

  public Item(AfterCommand after, IAtcCommand consequent) {
    this.after = after;
    this.consequent = consequent;
  }

}
