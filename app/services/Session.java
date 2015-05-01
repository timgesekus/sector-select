package services;

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

    CreateChat createChat = CreateChat.newBuilder().setChatId(chatId).build();
    eventBus.publish(Topic.CHAT_COMMAND, createChat);
    eventBus.subscribe(self(), Topic.CHAT_SERVICE_EVENT);
    eventBus.subscribe(self(), Topic.SESSION_COMMAND);

    receive(ReceiveBuilder.match(ChatCreated.class, this::chatCreated).build());
  }

  private void chatCreated(ChatCreated chatCreated)
  {
    if (chatCreated.getChatId().equals(chatId))
    {
      isChatCreated = true;
      SessionStarted sessionStarted = SessionStarted
        .newBuilder()
        .setChatId(chatId)
        .setSessionId(sessionId)
        .setExerciseId(exerciseId)
        .setOwneringUserId(ownerName)
        .build();
      creationRequestor.tell(sessionStarted, self());
      eventBus.publish(Topic.SESSION_SERVICE_EVENT, sessionStarted);
    }
  }
}
