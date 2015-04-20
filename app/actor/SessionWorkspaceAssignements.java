package actor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import play.Logger;
import eventBus.EventBus;
import joinSessionView.JoinSessionWS.WorkspaceSelection;
import actor.messages.Subscribe;
import actor.messages.Unsubscribe;
import actor.util.Subscriptions;
import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.Creator;
import akka.japi.pf.ReceiveBuilder;

public class SessionWorkspaceAssignements extends AbstractActor
{

  private final WorkspaceAssignements workspaceAssignements;
  private final Subscriptions subscriptions;
  private EventBus eventBus;

  public static Props props(EventBus eventBus)
  {
    return Props.create(new Creator<SessionWorkspaceAssignements>()
    {

      @Override
      public SessionWorkspaceAssignements create() throws Exception
      {
        return new SessionWorkspaceAssignements(eventBus);
      }
    });
  }

  public SessionWorkspaceAssignements(EventBus eventBus)
  {
    this.eventBus = eventBus;
    workspaceAssignements = new WorkspaceAssignements();
    subscriptions = new Subscriptions();
    this.eventBus.subscribe(self(), "workspaceSelectionEvent");
    configureSectors();
    configureMessageHandling();

  }

  private void configureMessageHandling()
  {
    receive(ReceiveBuilder
      .match(Subscribe.class, this::handleSubscribe)
      .match(
        Unsubscribe.class,
        unsubscribe -> subscriptions.handleUnsubscribe(unsubscribe, sender()))
      .match(WorkspaceSelection.class, this::handleSelectionEvent)
      .matchAny(this::unhandled)
      .build());
  }

  private void configureSectors()
  {
    workspaceAssignements.assign("", "WUR");
    workspaceAssignements.assign("", "ERL");
    workspaceAssignements.assign("", "FRA");
  }

  public void handleSubscribe(Subscribe subscribe)
  {
    subscriptions.handleSubscription(subscribe, sender());
    subscriptions.publish(workspaceAssignements, self());
  }

  public void handleUnsubscribe(Unsubscribe unsubscribe)
  {
    String username = subscriptions.getUserName(sender());
    subscriptions.handleUnsubscribe(unsubscribe, sender());
    workspaceAssignements.removeAssignement(username);
  }

  private void handleSelectionEvent(WorkspaceSelection event)
  {
    System.exit(1);
    Logger.info("Received handleSelectionEvent");
    String selectedSector = event.sector;
    Optional<String> userNameOption = Optional.of(event.userName);
    if (userNameOption.isPresent()
        && workspaceAssignements.containsWorkspace(selectedSector))
    {
      handleAssignement(selectedSector, userNameOption.get());
    }
    subscriptions.publish(workspaceAssignements, self());
  }

  private void handleAssignement(String selectedSector, String userName)
  {
    if (workspaceAssignements.isUnassigned(selectedSector))
    {
      workspaceAssignements.switchAssignement(selectedSector, userName);
    } else
    {
      if (workspaceAssignements.isAssignedTo(selectedSector, userName))
      {
        workspaceAssignements.removeAssignement(userName);
      }
    }
  }

  public static <T, E> Optional<T> getFirstKeyByValue(Map<T, E> map, E value)
  {
    return map
      .entrySet()
      .stream()
      .filter(entry -> entry.getValue().equals(value))
      .map(entry -> entry.getKey())
      .findFirst();
  }

  public static <T, E> List<T> getKeysByValue(Map<T, E> map, E value)
  {
    return map
      .entrySet()
      .stream()
      .filter(entry -> entry.getValue().equals(value))
      .map(entry -> entry.getKey())
      .collect(Collectors.toList());
  }

  public static class WorkspaceAssignements
  {
    private final Map<String, String> workspaceAssignements;

    public WorkspaceAssignements()
    {
      workspaceAssignements = new HashMap<String, String>();
    }

    public Map<String, String> getWorkspaceAssignements()
    {
      return workspaceAssignements;
    }

    public void removeAssignement(String userName)
    {
      Optional<String> sectorNameOptional = getSectorForUserName(userName);
      if (sectorNameOptional.isPresent())
      {
        workspaceAssignements.put(sectorNameOptional.get(), "");
      }
    }

    public boolean containsWorkspace(String workspaceName)
    {
      return workspaceAssignements.containsKey(workspaceName);
    }

    private boolean isAssignedTo(String workspaceName, String userName)
    {
      return workspaceAssignements.get(workspaceName).equals(userName);
    }

    private void switchAssignement(String selectedWorkspace, String userName)
    {
      removeAssignement(userName);
      assign(userName, selectedWorkspace);
    }

    private void assign(String userName, String sectorName)
    {
      workspaceAssignements.put(sectorName, userName);
    }

    private Optional<String> getSectorForUserName(String userName)
    {
      return getFirstKeyByValue(workspaceAssignements, userName);
    }

    private boolean isUnassigned(String sectorName)
    {
      return workspaceAssignements.get(sectorName).equals("");
    }

  }
}
