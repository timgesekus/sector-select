package services;

import java.util.ArrayList;
import java.util.List;

import play.Logger;
import workspaces.command.WorkspacesCommand.AddWorkspace;
import workspaces.command.WorkspacesCommand.DeselectWorkspace;
import workspaces.command.WorkspacesCommand.RestoreWorkspaces;
import workspaces.command.WorkspacesCommand.SelectWorkspace;
import workspaces.event.WorkspacesEvent.WorkspaceAdded;
import workspaces.event.WorkspacesEvent.WorkspaceDeselected;
import workspaces.event.WorkspacesEvent.WorkspaceSelected;
import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.japi.Creator;
import akka.japi.pf.ReceiveBuilder;
import eventBus.EventBus;
import eventBus.Topic;

public class Workspaces extends AbstractActor implements
  WorkspacesAssignementsListener
{

  final Logger.ALogger logger = Logger.of(this.getClass());

  public static Props props(String workspacesId, EventBus eventBus)
  {
    return Props.create(new Creator<Workspaces>()
    {
      private static final long serialVersionUID = 1L;

      @Override
      public Workspaces create() throws Exception
      {
        return new Workspaces(workspacesId, eventBus);
      }
    });
  }

  private String workspacesId;
  private EventBus eventBus;
  private List<Object> events = new ArrayList<>();
  private WorkspacesAssignements workspacesAssignements;

  public Workspaces(String workspacesId, EventBus eventBus)
  {
    logger.info("Workspaces with id {} created", workspacesId);
    this.workspacesId = workspacesId;
    this.eventBus = eventBus;
    configureMessageHandling();
    subscribteToWorkspacesCommands();
  }

  private void subscribteToWorkspacesCommands()
  {
    eventBus.subscribe(self(), Topic.WORKSPACES_COMMAND);
  }

  private void configureMessageHandling()
  {
    receive(ReceiveBuilder
      .match(AddWorkspace.class, this::addWorkspace)
      .match(SelectWorkspace.class, this::selectWorkspace)
      .match(DeselectWorkspace.class, this::deselectWorkspace)
      .match(RestoreWorkspaces.class, this::restoreWorkspaces)
      .matchAny(this::unhandled)
      .build());
  }

  public void unhandled(Object message)
  {
    logger.info("Received unknown message {} {}", message.getClass(), message);
  }

  private void addWorkspace(AddWorkspace addWorkspace)
  {
    logger.info("Workspace added {} ", addWorkspace.getWorkspaceName());

    if (addWorkspace.getWorkspacesId().equals(workspacesId))
    {
      logger.info("Workspace added {} ", addWorkspace.getWorkspaceName());
      workspacesAssignements.addWorkspace(addWorkspace.getWorkspaceName());
    }
  }

  private void selectWorkspace(SelectWorkspace selectWorkspace)
  {
    if (selectWorkspace.getWorkspacesId().equals(workspacesId))
    {
      workspacesAssignements.selectWorkspace(
        selectWorkspace.getWorkspaceName(),
        selectWorkspace.getUserId());
    }
  }

  private void deselectWorkspace(DeselectWorkspace deselectWorkspace)
  {
    if (deselectWorkspace.getWorkspacesId().equals(workspacesId))
    {
      workspacesAssignements.deselectWorkspace(
        deselectWorkspace.getWorkspaceName(),
        deselectWorkspace.getUserId());
    }
  }

  private void restoreWorkspaces(RestoreWorkspaces restoreWorkspaces)
  {
    logger.info(
      "Restoration requst {} sender:{} ",
      restoreWorkspaces.getWorkspacesId(),
      sender());
    events.stream().forEach(event -> sendMessageToSender(event));
    sendMessageToSender(RestoreWorkspaces
      .newBuilder()
      .setWorkspacesId(workspacesId)
      .build());
  }

  @Override
  public void workspaceAdd(String workspaceName)
  {
    WorkspaceAdded workspaceAdded = WorkspaceAdded
      .newBuilder()
      .setWorkspacesId(workspacesId)
      .setWorkspaceName(workspaceName)
      .build();
    publishAndStore(workspaceAdded);
  }

  @Override
  public void workspaceSelected(String workspaceName, String userId)
  {
    WorkspaceSelected workspaceSelected = WorkspaceSelected
      .newBuilder()
      .setWorkspacesId(workspacesId)
      .setWorkspaceName(workspaceName)
      .setUserId(userId)
      .build();
    publishAndStore(workspaceSelected);
  }

  @Override
  public void workspaceDeselected(String workspaceName, String userId)
  {
    WorkspaceDeselected workspaceDeselected = WorkspaceDeselected
      .newBuilder()
      .setWorkspacesId(workspacesId)
      .setWorkspaceName(workspaceName)
      .setUserId(userId)
      .build();
    publishAndStore(workspaceDeselected);
  }

  private void publishAndStore(Object message)
  {
    events.add(message);
    eventBus.publish(Topic.WORKSPACES_EVENT, message);
  }

  private void sendMessageToSender(Object event)
  {
    sender().tell(event, self());
  }
}
