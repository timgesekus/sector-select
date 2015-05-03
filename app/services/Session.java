package services;

import play.Logger;
import session.command.SessionComand.RequestSessionStartedMessage;
import session.event.SessionEvent.SessionStarted;
import chat.command.ChatCommand.CreateChat;
import chat.event.ChatEvent.ChatCreated;
import eventBus.EventBus;
import eventBus.Topic;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
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
  private ActorRef creationRequestor;
  final Logger.ALogger logger = Logger.of(this.getClass());

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
    this.isChatCreated = false;

    logger.info("Session started: {}", sessionId);

    receive(ReceiveBuilder
      .match(ChatCreated.class, this::chatCreated)
      .match(
        RequestSessionStartedMessage.class,
        this::requestSessionStartMessage)
      .build());
    eventBus.subscribe(self(), Topic.CHAT_SERVICE_EVENT);
    eventBus.subscribe(self(), Topic.SESSION_COMMAND);

    CreateChat createChat = CreateChat.newBuilder().setChatId(chatId).build();
    eventBus.publish(Topic.CHAT_SERVICE_COMMAND, createChat);
  }

  private void chatCreated(ChatCreated chatCreated)
  {
    logger.info("Chat created: {}", chatCreated.getChatId());
    if (chatCreated.getChatId().equals(chatId))
    {
      isChatCreated = true;
      SessionStarted sessionStarted = buildSessionStartedMessage();
      creationRequestor.tell(sessionStarted, self());
      eventBus.publish(Topic.SESSION_SERVICE_EVENT, sessionStarted);
    }
  }

  private SessionStarted buildSessionStartedMessage()
  {
    SessionStarted sessionStarted = SessionStarted
      .newBuilder()
      .setChatId(chatId)
      .setSessionId(sessionId)
      .setExerciseId(exerciseId)
      .setOwneringUserId(ownerName)
      .build();
    return sessionStarted;
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
}
