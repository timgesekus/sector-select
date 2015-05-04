package services;

import com.googlecode.protobuf.format.JsonFormat;

import play.Logger;
import session.command.SessionComand.RequestSessionStartedMessage;
import session.event.SessionEvent.SessionStarted;
import workspaces.command.WorkspacesCommand.AddWorkspace;
import workspaces.command.WorkspacesCommand.CreateWorkspaces;
import workspaces.event.WorkspacesEvent.WorkspacesCreated;
import chat.command.ChatCommand.CreateChat;
import chat.event.ChatEvent.ChatCreated;
import eventBus.EventBus;
import eventBus.Topic;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.japi.Creator;
import akka.japi.pf.ReceiveBuilder;

public class Session extends AbstractActor
{

  public static Props props(
    String sessionId,
    String exerciseId,
    String ownerName,
    ActorRef creationRequestor,
    EventBus eventBus)
  {
    return Props.create(new Creator<Session>()
    {
      private static final long serialVersionUID = 1L;

      @Override
      public Session create() throws Exception
      {
        return new Session(
          sessionId,
          exerciseId,
          ownerName,
          eventBus,
          creationRequestor);
      }
    });
  }

  private final String sessionId;
  private final String exerciseId;
  private final String ownerName;
  private final EventBus eventBus;
  private String chatId;
  private boolean isChatCreated;
  private boolean isWorkspacesCreated;

  private ActorRef creationRequestor;
  final Logger.ALogger logger = Logger.of(this.getClass());
  private String workspacesId;

  public Session(
    String sessionId,
    String exerciseId,
    String ownerName,
    EventBus eventBus,
    ActorRef creationRequestor)
  {
    this.sessionId = sessionId;
    this.exerciseId = exerciseId;
    this.ownerName = ownerName;
    this.eventBus = eventBus;
    this.creationRequestor = creationRequestor;
    this.chatId = "chat-for-session-" + sessionId;
    this.workspacesId = "workspaces-for-session-" + sessionId;
    this.isChatCreated = false;

    logger.info("Session started: {}", sessionId);

    receive(ReceiveBuilder
      .match(ChatCreated.class, this::chatCreated)
      .match(WorkspacesCreated.class, this::workspacesCreated)
      .match(
        RequestSessionStartedMessage.class,
        this::requestSessionStartMessage)
      .build());
    subscribteToEvents();
    createChat();
    createWorkspaces();

  }

  private void subscribteToEvents()
  {
    eventBus.subscribe(self(), Topic.CHAT_SERVICE_EVENT);
    eventBus.subscribe(self(), Topic.WORKSPACES_SERVICE_EVENT);
    eventBus.subscribe(self(), Topic.SESSION_COMMAND);
  }

  private void createChat()
  {
    CreateChat createChat = CreateChat.newBuilder().setChatId(chatId).build();
    eventBus.publish(Topic.CHAT_SERVICE_COMMAND, createChat);
  }

  private void createWorkspaces()
  {
    logger.info("send create workspaces {}", workspacesId);
    CreateWorkspaces createWorkspaces = CreateWorkspaces
      .newBuilder()
      .setWorkspacesId(workspacesId)
      .build();
    eventBus.publish(Topic.WORKSPACES_SERVICE_COMMAND, createWorkspaces);
  }

  private void chatCreated(ChatCreated chatCreated)
  {
    logger.info("Chat created: {}", chatCreated.getChatId());
    if (chatCreated.getChatId().equals(chatId))
    {
      isChatCreated = true;
      sendSessionStartedIfComplete();
    }
  }

  private void workspacesCreated(WorkspacesCreated workspacesCreated)
  {
    logger.info("Workspaces created: {}", workspacesCreated.getWorkspacesId());
    if (workspacesCreated.getWorkspacesId().equals(workspacesId))
    {
      isWorkspacesCreated = true;
      AddWorkspace addWorkspace = AddWorkspace
        .newBuilder()
        .setWorkspacesId(workspacesId)
        .setWorkspaceName("WURL")
        .build();
      logger.info("Sending stuff {}", JsonFormat.printToString(addWorkspace));
      eventBus.publish(Topic.WORKSPACES_COMMAND, addWorkspace);
      addWorkspace = AddWorkspace
        .newBuilder()
        .setWorkspacesId(workspacesId)
        .setWorkspaceName("WURH")
        .build();
      logger.info("Sending stuff {}", JsonFormat.printToString(addWorkspace));
      eventBus.publish(Topic.WORKSPACES_COMMAND, addWorkspace);

      sendSessionStartedIfComplete();
    }
  }

  private void sendSessionStartedIfComplete()
  {
    if (isChatCreated && isWorkspacesCreated)
    {
      logger.info("sending session started");
      SessionStarted sessionStarted = buildSessionStartedMessage();
      creationRequestor.tell(sessionStarted, self());
      ActorSelection parent = getContext().actorSelection("..");
      parent.tell(sessionStarted, self());
      sender().tell(sessionStarted, self());
    }
  }

  private void requestSessionStartMessage(
    RequestSessionStartedMessage requestSessionStartedMessage)
  {
    String requestSessionId = requestSessionStartedMessage.getSessionId();
    if (requestSessionId.equals(sessionId))
    {
      SessionStarted sessionStartedMessage = buildSessionStartedMessage();
      sender().tell(sessionStartedMessage, self());
    } else
    {
      logger.error(
        "Received requestSessionStartedMessage for unknown session {}",
        sessionId);
    }
  }

  private SessionStarted buildSessionStartedMessage()
  {
    SessionStarted sessionStarted = SessionStarted
      .newBuilder()
      .setChatId(chatId)
      .setSessionId(sessionId)
      .setExerciseId(exerciseId)
      .setWorkspacesId(workspacesId)
      .setOwneringUserId(ownerName)
      .build();
    return sessionStarted;
  }
}
