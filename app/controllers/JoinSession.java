package controllers;

import static akka.pattern.Patterns.ask;

import javax.inject.Inject;

import session.event.SessionEvent.SessionStarted;
import session.command.SessionComand.*;
import play.Logger;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import scala.concurrent.Future;
import utils.WebSocketUtils;
import akka.actor.ActorRef;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import session.command.SessionComand.StartSession;

import com.google.inject.name.Named;

import eventBus.EventBus;

public class JoinSession extends Controller
{

  private final ActorRef sessionService;
  private EventBus eventBus;

  @Inject
  public JoinSession(
    @Named("SessionService") ActorRef sessionService,
    EventBus eventBus)
  {
    this.sessionService = sessionService;
    this.eventBus = eventBus;
  }

  @SubjectPresent
  public Promise<Result> createSession(String exerciseId)
  {

    String userName = session("userName");
    StartSession startExercise = StartSession
      .newBuilder()
      .setExerciseId(exerciseId)
      .setOwneringUserId(userName)
      .build();

    Future<Object> startExerciseAnswer = ask(
      sessionService,
      startExercise,
      1000);
    return Promise.wrap(startExerciseAnswer).map(this::handleExerciseStarted);
  }

  @SubjectPresent
  public Promise<Result> joinSession(String sessionId)
  {
    RequestSessionStartedMessage requestSessionStartedMessage = RequestSessionStartedMessage
      .newBuilder()
      .setSessionId(sessionId)
      .build();

    // return ok(views.html.joinSession.render(sessionId));
    Future<Object> startExerciseAnswer = ask(
      sessionService,
      requestSessionStartedMessage,
      1000);
    return Promise.wrap(startExerciseAnswer).map(this::handleExerciseStarted);
  }

  public Result joinSession(SessionStarted sessionStarted)
  {
    String userName = session("userName");
    return ok(views.html.joinSession.render(
      sessionStarted.getSessionId(),
      sessionStarted.getChatId(),
      sessionStarted.getWorkspacesId(),
      userName));

  }

  private Result handleExerciseStarted(Object startExerciseAnswer)
  {
    if (startExerciseAnswer instanceof SessionStarted)
    {
      SessionStarted sessionStarted = (SessionStarted) startExerciseAnswer;
      return joinSession(sessionStarted);
    }
    return internalServerError("Failed to create session: "
        + startExerciseAnswer.toString());
  }
}
