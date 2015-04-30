package actor;

import java.util.HashMap;
import java.util.Map;

import chat.command.ChatCommand.CreateChat;
import eventBus.EventBus;
import eventBus.Topics;
import joinSessionView.JoinSessionWS.WorkspaceSelection;
import play.Logger;
import actor.messages.Subscribe;
import actor.messages.Unsubscribe;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.Creator;
import akka.japi.pf.ReceiveBuilder;

public class SessionActor extends AbstractActor
{
  private final Map<ActorRef, String> subscribers = new HashMap<>();
  private int sessionId;
  private ActorRef sessionWorkspaceAssignement;
  private EventBus eventBus;
  final private Logger.ALogger logger = Logger.of(this.getClass());

  public static Props props(
    int sessionId,
    int exerciseId,
    String ownerName,
    EventBus eventBus)
  {
    return Props.create(new Creator<SessionActor>()
    {
      private static final long serialVersionUID = 1L;

      @Override
      public SessionActor create() throws Exception
      {
        return new SessionActor(sessionId, exerciseId, ownerName, eventBus);
      }
    });
  }

  public SessionActor(
    int sessionId,
    int exerciseId,
    String ownerName,
    EventBus eventBus)
  {
    logger.info("Create Session with id {}", sessionId);
    this.sessionId = sessionId;
    this.eventBus = eventBus;
    configureMessageHandling();
    createChat();
    createWorkspaceAssignements();
  }

  private void configureMessageHandling()
  {
    receive(ReceiveBuilder
      .match(WorkspaceSelection.class, this::forwardToWorkspaceAssignementActor)
      .match(Subscribe.class, this::handleSubscription)
      .match(Unsubscribe.class, this::handleUnsubscribe)
      .matchAny(this::unhandled)
      .build());
  }

  private void createChat()
  {
    String chatId = "chat-" + sessionId;
    logger.info("Create Chat with id {}", chatId);
    CreateChat createChat = CreateChat.newBuilder().setChatId(chatId).build();
    eventBus.publish(Topics.CHAT_COMMAND.toString(), createChat);
  }

  private void createWorkspaceAssignements()
  {
    Props props = SessionWorkspaceAssignements.props(eventBus);
    sessionWorkspaceAssignement = getContext().actorOf(
      props,
      "workspaceAssignement-" + sessionId);
  }

  private void forwardToWorkspaceAssignementActor(WorkspaceSelection event)
  {
    sessionWorkspaceAssignement.tell(event, sender());
  }

  private void handleSubscription(Subscribe subscribe)
  {
    Logger.info("Received subscription {}:{}: ", sender(), subscribe.userName);
    subscribers.put(sender(), subscribe.userName);
    sendToChildren(subscribe);
  }

  private void handleUnsubscribe(Unsubscribe unsubscribe)
  {
    Logger.info("Received unsubscribe {}:{}: ", sender());
    subscribers.remove(sender());
    sendToChildren(unsubscribe);
  }

  private void sendToChildren(Object message)
  {
    sessionWorkspaceAssignement.forward(message, getContext());
  }
}
