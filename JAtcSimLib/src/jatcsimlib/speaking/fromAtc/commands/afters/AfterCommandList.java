/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.speaking.fromAtc.commands.afters;

import jatcsimlib.airplanes.Airplane;
import jatcsimlib.coordinates.Coordinate;
import jatcsimlib.coordinates.Coordinates;
import jatcsimlib.exceptions.ENotSupportedException;
import jatcsimlib.speaking.ICommand;
import jatcsimlib.speaking.fromAtc.commands.ChangeAltitudeCommand;
import jatcsimlib.speaking.fromAtc.commands.ChangeSpeedCommand;
import jatcsimlib.speaking.fromAtc.commands.ProceedDirectCommand;
import jatcsimlib.speaking.fromAtc.commands.ToNavaidCommand;
import jatcsimlib.world.Navaid;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Marek
 */
public class AfterCommandList {

  private final List<Item> inner = new LinkedList<>();

  public void add(AfterCommand afterCommand, ICommand consequent) {
    Item it = new Item(afterCommand, consequent);
    inner.add(it);
  }

  public void removeByConsequent(Class commandType) {
    int index = 0;
    while (index < inner.size()) {
      if (inner.get(index).consequent.getClass().equals(commandType)) {
        inner.remove(index);
      } else {
        index++;
      }
    }
  }

  public void removeByNavaidConsequentRecursively(Navaid navaid) {
    List<Item> toRem = new LinkedList<>();
    for (Item it : inner) {
      if (it.consequent instanceof ToNavaidCommand) {
        Navaid cmdNavaid = ((ToNavaidCommand) it.consequent).getNavaid();
        if (cmdNavaid.equals(navaid)) {
          toRem.add(it);
        }
      }
    }

    removePath(toRem);
  }

  private void removePath(List<Item> items) {
    inner.removeAll(items);
    for (Item it : items) {
      removePath(it);
    }
  }

  private void removePath(Item item) {
    List<Item> preds = findPredecessors(item);
    removePath(preds);
  }

  private List<Item> findPredecessors(Item item) {
    ICommand predCmd;
    boolean consMustBeToNavaidCommand = false;
    if (item.consequent instanceof ProceedDirectCommand) {
      Navaid n = ((ProceedDirectCommand) item.consequent).getNavaid();
      predCmd = new AfterNavaidCommand(n);
    } else if (item.consequent instanceof ChangeAltitudeCommand) {
      int alt = ((ChangeAltitudeCommand) item.consequent).getAltitudeInFt();
      predCmd = new AfterAltitudeCommand(alt);
      consMustBeToNavaidCommand = true;
    } else if (item.consequent instanceof ChangeSpeedCommand) {
      int spd = ((ChangeSpeedCommand) item.consequent).getSpeedInKts();
      predCmd = new AfterSpeedCommand((spd));
      consMustBeToNavaidCommand = true;
    } else {
      return new LinkedList<>();
    }

    List<Item> ret = new LinkedList<>();
    for (Item it : inner) {
      if (it.after.equals(predCmd)
          && (!consMustBeToNavaidCommand || (item.consequent instanceof ToNavaidCommand))) {
        ret.add(it);
      }
    }

    return ret;
  }

  public List<ICommand> getAndRemoveSatisfiedCommands(Airplane referencePlane, Coordinate currentTargetCoordinateOrNull) {
    int alt = referencePlane.getAltitude();
    int spd = referencePlane.getSpeed();
    Coordinate cor = referencePlane.getCoordinate();

    List<ICommand> ret = new LinkedList<>();

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
}

class Item {

  public final AfterCommand after;
  public final ICommand consequent;

  public Item(AfterCommand after, ICommand consequent) {
    this.after = after;
    this.consequent = consequent;
  }

}
