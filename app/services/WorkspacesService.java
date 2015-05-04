package services;

import java.util.HashMap;
import java.util.Map;

import play.Logger;
import workspaces.command.WorkspacesCommand.CreateWorkspaces;
import workspaces.command.WorkspacesCommand.RemoveWorkspaces;
import workspaces.event.WorkspacesEvent.WorkspacesCreated;
import workspaces.event.WorkspacesEvent.WorkspacesRemoved;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.Creator;
import akka.japi.pf.ReceiveBuilder;
import eventBus.EventBus;
import eventBus.Topic;

public class WorkspacesService extends AbstractActor
{
  public static Props props(EventBus eventBus)
  {
    return Props.create(new Creator<WorkspacesService>()
    {
      private static final long serialVersionUID = 279988858865554278L;

      @Override
      public WorkspacesService create() throws Exception
      {
        return new WorkspacesService(eventBus);
      }
    });
  }

  private EventBus eventBus;
  private Map<String, ActorRef> workspaces = new HashMap<>();
  final Logger.ALogger logger = Logger.of(this.getClass());

  public WorkspacesService(EventBus eventBus)
  {
    logger.info("Workspaces  service created");
    this.eventBus = eventBus;
    configureMessageHandling();
    subscribteToWorkspacesServiceCommands();
  }

  private void configureMessageHandling()
  {
    receive(ReceiveBuilder
      .match(CreateWorkspaces.class, this::createWorkspaces)
      .match(RemoveWorkspaces.class, this::removeWorkspaces)
      .matchAny(this::unhandled)
      .build());
  }

  public void unhandled(Object message)
  {
    logger.info("Received unknown message {}", message);
  }

  private void subscribteToWorkspacesServiceCommands()
  {
    logger.info("Subscribing to workspaces commands");
    eventBus.subscribe(self(), Topic.WORKSPACES_SERVICE_COMMAND);
  }

  private void createWorkspaces(CreateWorkspaces createWorkspaces)
  {
    String workspacesId = createWorkspaces.getWorkspacesId();
    logger.info("Create workscpaces with id {}", workspacesId);
    if (workspaces.containsKey(workspacesId))
    {
      // send error message
    } else
    {
      Props workspacesProps = Workspaces.props(workspacesId, eventBus);
      ActorRef chat = getContext().actorOf(workspacesProps, workspacesId);
      workspaces.put(workspacesId, chat);
      WorkspacesCreated chatCreated = WorkspacesCreated
        .newBuilder()
        .setWorkspacesId(workspacesId)
        .build();
      eventBus.publish(Topic.WORKSPACES_SERVICE_EVENT, chatCreated);

    }
  }

  private void removeWorkspaces(RemoveWorkspaces removeWorkspaces)
  {
    String workspacesId = removeWorkspaces.getWorkspacesId();
    WorkspacesRemoved chatCreated = WorkspacesRemoved
      .newBuilder()
      .setWorkspacesId(workspacesId)
      .build();
    eventBus.publish(Topic.WORKSPACES_SERVICE_EVENT, chatCreated);
  }
}
