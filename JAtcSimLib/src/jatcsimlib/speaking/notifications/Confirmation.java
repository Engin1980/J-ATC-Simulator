package jatcsimlib.speaking.notifications;

import jatcsimlib.speaking.commands.Command;
import jatcsimlib.speaking.commands.CommandResponse;

public class Confirmation extends CommandResponse {

  public Confirmation(Command origin) {
    super(origin);
  }

}
