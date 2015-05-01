package services;

import java.util.HashMap;
import java.util.Map;

import play.Logger;
import session.command.SessionComand.StartSession;
import session.command.SessionComand.StopSession;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.Creator;
import akka.japi.pf.ReceiveBuilder;
import eventBus.EventBus;
import eventBus.Topic;

public class SessionService extends AbstractActor
{
  public static Props props(EventBus eventBus)
  {
    return Props.create(new Creator<SessionService>()
    {
      private static final long serialVersionUID = 279988858865554278L;

      @Override
      public SessionService create() throws Exception
      {
        return new SessionService(eventBus);
      }
    });
  }

  final Logger.ALogger logger = Logger.of(this.getClass());
  private final Map<String, ActorRef> sessions = new HashMap<>();
  private EventBus eventBus;

  public SessionService(EventBus eventBus)
  {
    this.eventBus = eventBus;

    subscribteToChatSeriveCommands();
    configureMessageHandling();

  }

  private void configureMessageHandling()
  {
    receive(ReceiveBuilder
      .match(StartSession.class, this::startSession)
      .match(StopSession.class, this::stopSession)
      .matchAny(this::unhandled)
      .build());
  }

  public void unhandled(Object message)
  {
    logger.info("Received unknown message {}", message);
  }

  private void subscribteToChatSeriveCommands()
  {
    logger.info("Subscribing to chat commands");
    eventBus.subscribe(self(), Topic.CHAT_SERVICE_COMMAND);
  }

  private void startSession(StartSession startSession)
  {
    String owner = startSession.getOwneringUserId();
    String exerciseId = startSession.getExerciseId();
    String sessionId = owner + "-" + exerciseId;
    Props sessionProps = Session.props(
      sessionId,
      exerciseId,
      owner,
      sender(),
      eventBus);
    ActorRef session = getContext().actorOf(
      sessionProps,
      "session-" + sessionId);
    sessions.put(sessionId, session);
  }

  private void stopSession(StopSession stopSession)
  {
    String sessionIdToStop = stopSession.getSessionId();
    if (sessions.containsKey(sessionIdToStop))
    {
      ActorRef sessionToStop = sessions.get(sessionIdToStop);
      context().stop(sessionToStop);
    }
  }
}
