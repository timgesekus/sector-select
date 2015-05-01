package controllers;

import static akka.pattern.Patterns.ask;
import session.event.SessionEvent.SessionStarted;
import joinSessionView.JoinSessionWS;
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

    // return ok(views.html.joinSession.render(sessionId));
    Future<Object> startExerciseAnswer = ask(
      sessionService,
      startExercise,
      1000);
    return Promise.wrap(startExerciseAnswer).map(this::handleExerciseStarted);
  }

  @SubjectPresent
  public static Result joinSession(String sessionId)
  {
    return ok(views.html.joinSession.render(sessionId));
  }

  public WebSocket<String> joinSessionWS(String sessionId)
  {
    Logger.info("sectors with sessionId " + sessionId);
    String userName = session("userName");
    if (userName != null)
    {
      play.Logger.info("username " + userName);
      return WebSocket.withActor(out -> JoinSessionWS.props(
        out,
        userName,
        eventBus,
        sessionId));
    } else
    {
      return WebSocketUtils.notAuthorizedWebSocket();
    }
  }

  private Result handleExerciseStarted(Object startExerciseAnswer)
  {
    if (startExerciseAnswer instanceof SessionStarted)
    {
      viewmodels.exerciseselect.JoinSession joinSession = (viewmodels.exerciseselect.JoinSession) startExerciseAnswer;
      return joinSession(joinSession.sessionid);
    }
    return internalServerError("Failed to create session: "
        + startExerciseAnswer.toString());
  }
}
