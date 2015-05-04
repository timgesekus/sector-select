package services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.Logger;
import session.command.SessionComand;
import session.command.SessionComand.RequestSessionStartedMessage;
import session.command.SessionComand.StartSession;
import session.command.SessionComand.StopSession;
import session.command.SessionComand.RestoreSessions;
import session.event.SessionEvent.RestoreSessionsCompleted;
import session.event.SessionEvent.SessionStarted;
import session.event.SessionEvent.SessionStopped;
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

  private final Logger.ALogger logger = Logger.of(this.getClass());
  private final Map<String, ActorRef> sessions = new HashMap<>();
  private final EventBus eventBus;
  private final List<Object> events = new ArrayList<>();

  public SessionService(EventBus eventBus)
  {
    this.eventBus = eventBus;

    subscribteToChatServiceCommands();
    configureMessageHandling();

  }

  private void configureMessageHandling()
  {
    receive(ReceiveBuilder
      .match(StartSession.class, this::startSession)
      .match(StopSession.class, this::stopSession)
      .match(
        RequestSessionStartedMessage.class,
        this::requestSessionStartMessage)
      .match(SessionStarted.class, this::sessionStarted)
      .match(SessionStopped.class, this::sessionStopped)
      .match(RestoreSessions.class, this::restoreSessions)
      .matchAny(this::unhandled)
      .build());
  }

  public void unhandled(Object message)
  {
    logger.info("Received unknown message {} {}", message.getClass(), message);
  }

  private void subscribteToChatServiceCommands()
  {
    logger.info("Subscribing to chat commands");
    eventBus.subscribe(self(), Topic.CHAT_SERVICE_COMMAND);
  }

  private void startSession(StartSession startSession)
  {
    String owner = startSession.getOwneringUserId();
    String exerciseId = startSession.getExerciseId();
    String sessionId = owner + "-" + exerciseId;
    if (sessions.containsKey(sessionId))
    {
      ActorRef session = sessions.get(sessionId);
      RequestSessionStartedMessage requestSessionStartedMessage = RequestSessionStartedMessage
        .newBuilder()
        .setSessionId(sessionId)
        .build();
      session.forward(requestSessionStartedMessage, getContext());
    } else
    {
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

  private void restoreSessions(RestoreSessions restoreSessions)
  {
    events.stream().forEach(event -> sender().tell(event, self()));
    sender().tell(RestoreSessionsCompleted.newBuilder().build(), self());
  }

  private void sessionStarted(SessionStarted sessionStarted)
  {
    events.add(sessionStarted);
  }

  private void sessionStopped(SessionStopped sessionStopped)
  {
    events.add(sessionStopped);
  }

  private void requestSessionStartMessage(
    RequestSessionStartedMessage requestSessionStartedMessage)
  {
    String sessionId = requestSessionStartedMessage.getSessionId();
    if (sessions.containsKey(sessionId))
    {
      ActorRef session = sessions.get(sessionId);
      session.forward(requestSessionStartedMessage, getContext());
    } else
    {
      logger.error(
        "Received requestSessionStartedMessage for unknown session {}",
        sessionId);
      sessions
        .keySet()
        .stream()
        .forEach(session -> logger.info("Known session {}", session));
    }
  }
}
