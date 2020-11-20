package eng.jAtcSim.newLib.shared;

import eng.eSystem.collections.ESet;
import eng.eSystem.collections.ISet;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.functionalInterfaces.Producer;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class PostContracts {

  private static class PostContract {
    public final Object sender;
    public final Producer<Boolean> predicate;
    public final String message;

    public PostContract(Object sender, Producer<Boolean> predicate, String message) {
      this.sender = sender;
      this.predicate = predicate;
      this.message = message;
    }
  }

  private static final ISet<PostContract> postContracts = new ESet<>();

  public static void register(Object sender, Producer<Boolean> check) {
    postContracts.add(new PostContract(sender, check, null));
  }

  public static void register(Object sender, Producer<Boolean> check, String message) {
    postContracts.add(new PostContract(sender, check, message));
  }

  public static void checkAndClear() {
    for (PostContract postContract : postContracts) {
      if (postContract.predicate.invoke() == false)
        throwFailedPostContract(postContract);
    }
    postContracts.clear();
  }

  private static void throwFailedPostContract(PostContract postContract) {
    String msg = sf("Post-contract check failed for '%s' ('%s')",
            postContract.sender,
            postContract.sender.getClass());
    if (postContract.message != null)
      msg += ": " + postContract.message;
    throw new EApplicationException(msg);
  }
}
