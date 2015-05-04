package joinSessionView;

import java.util.HashMap;
import java.util.Map;

import play.Logger;
import workspaces.command.WorkspacesCommand.RestoreWorkspaces;
import workspaces.event.WorkspacesEvent.WorkspaceAdded;
import workspaces.event.WorkspacesEvent.WorkspaceDeselected;
import workspaces.event.WorkspacesEvent.WorkspaceSelected;
import workspaces.event.WorkspacesEvent.WorkspacesRestoreComplete;
import workspaces.view.WorkspacesView.Workspaces;
import workspaces.view.WorkspacesView.Workspaces.Assignement;
import workspaces.view.WorkspacesView.Workspaces.Builder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.protobuf.format.JsonFormat;

import eventBus.EventBus;
import eventBus.Topic;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.Creator;
import akka.japi.pf.ReceiveBuilder;

public class WorkspacesPresenter extends AbstractActor
{

  public static Props props(
    final String userName,
    final String workspacesId,
    final EventBus eventBus,
    final ActorRef out)
  {
    return Props.create(new Creator<WorkspacesPresenter>()
    {
      private static final long serialVersionUID = 1398731621836926808L;

      @Override
      public WorkspacesPresenter create() throws Exception
      {
        return new WorkspacesPresenter(userName, workspacesId, eventBus, out);
      }

    });

  }

  private ObjectMapper objectMapper;
  private String userName;
  private String workspacesId;
  private EventBus eventBus;
  private ActorRef out;
  final Logger.ALogger logger = Logger.of(this.getClass());
  private boolean restored = false;
  private Map<String, String> workspaceAssignements = new HashMap<>();

  public WorkspacesPresenter(
    String userName,
    String workspacesId,
    EventBus eventBus,
    ActorRef out)
  {
    this.userName = userName;
    this.workspacesId = workspacesId;
    this.eventBus = eventBus;
    this.out = out;

    objectMapper = new ObjectMapper();
    configureMessageHandling();
    subscribteForWorkspacesEvents();
    requestWorkspacesRestoration();
  }

  private void configureMessageHandling()
  {
    receive(ReceiveBuilder
      .match(
        WorkspacesRestoreComplete.class,
        this::workspaceRestorationComplete)
      .match(WorkspaceAdded.class, this::workspaceAdded)
      .match(WorkspaceSelected.class, this::workspaceSelected)
      .match(WorkspaceDeselected.class, this::workspaceDeselected)
      .build());

  }

  private void subscribteForWorkspacesEvents()
  {
    eventBus.subscribe(self(), Topic.WORKSPACES_EVENT);
  }

  private void requestWorkspacesRestoration()
  {
    RestoreWorkspaces restoreWorkspaces = RestoreWorkspaces
      .newBuilder()
      .setWorkspacesId(workspacesId)
      .build();
    eventBus.publish(Topic.WORKSPACES_COMMAND, restoreWorkspaces, self());
  }

  private void workspaceRestorationComplete(
    WorkspacesRestoreComplete workspacesRestoreComplete)
  {
    logger.info("Restore complete {} ", workspacesId);
    restored = true;
  }

  private void workspaceAdded(WorkspaceAdded workspaceAdded)
  {
    if (workspaceAdded.getWorkspacesId().equals(workspacesId))
    {
      workspaceAssignements.put(workspaceAdded.getWorkspaceName(), "");
      if (restored)
      {
        sendUpdatedViewModel();
      }
    }
  }

  private void workspaceSelected(WorkspaceSelected workspaceSelected)
  {
    if (workspaceSelected.getWorkspacesId().equals(workspacesId))
    {
      workspaceAssignements.put(
        workspaceSelected.getWorkspaceName(),
        workspaceSelected.getUserId());
      if (restored)
      {
        sendUpdatedViewModel();
      }
    }
  }

  private void workspaceDeselected(WorkspaceDeselected workspaceDeselected)
  {
    if (workspaceDeselected.getWorkspacesId().equals(workspacesId))
    {
      workspaceAssignements.put(workspaceDeselected.getWorkspaceName(), "");
      if (restored)
      {
        sendUpdatedViewModel();
      }
    }
  }

  private void sendUpdatedViewModel()
  {
    Builder builder = Workspaces.newBuilder();
    builder.setTopic("workspaces");
    workspaceAssignements
      .keySet()
      .stream()
      .forEach(
        workspaceName -> {
          Assignement assignement = Assignement
            .newBuilder()
            .setWorkspaceName(workspaceName)
            .setUserId(workspaceAssignements.get(workspaceName))
            .setSelectable(workspaceAssignements.get(workspaceName).equals(""))
            .build();
          builder.addAssignements(assignement);
        });
    Workspaces workspacesMessage = builder.build();
    String jsonMessage = JsonFormat.printToString(workspacesMessage);
    out.tell(jsonMessage, self());
  }
}
