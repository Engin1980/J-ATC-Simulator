package eng.jAtcSim.newLib.speeches.system.user2system;

import eng.jAtcSim.newLib.speeches.system.ISystemUserRequest;

import java.util.Optional;

public class GetHelpRequest implements ISystemUserRequest {
  private final Optional<String> specificCommand;

  public GetHelpRequest() {
    this.specificCommand = Optional.empty();
  }

  public GetHelpRequest(String specificCommand) {
    this.specificCommand = Optional.of(specificCommand);
  }

  public Optional<String> tryGetSpecificCommand() {
    return specificCommand;
  }
}
