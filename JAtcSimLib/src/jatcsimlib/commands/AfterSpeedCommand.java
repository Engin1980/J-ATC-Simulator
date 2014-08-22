/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.commands;

/**
 *
 * @author Marek
 */
public class AfterSpeedCommand extends Command {

  private final int speedInKts;

  public AfterSpeedCommand(int speedInKts) {
    this.speedInKts = speedInKts;
  }

  public int getSpeedInKts() {
    return speedInKts;
  }

}
