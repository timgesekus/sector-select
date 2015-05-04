package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import presenter.ExercisesPresenter;
import utils.WebSocketUtils;
import akka.actor.ActorRef;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import eventBus.EventBus;

public class ExerciseSelection extends Controller
{

  private final ActorRef sessionService;
  private final ActorRef exerciseService;
  private final EventBus eventBus;

  @Inject
  public ExerciseSelection(
    @Named("SessionService") ActorRef sessionService,
    @Named("ExerciseService") ActorRef exerciseService,
    EventBus eventBus)
  {
    this.sessionService = sessionService;
    this.exerciseService = exerciseService;
    this.eventBus = eventBus;
  }

  @SubjectPresent
  public static Result exerciseSelect()
  {
    return ok(views.html.exerciseSelect.render("Select exercise."));
  }

  public WebSocket<String> exerciseSelectionWS()
  {

    String userName = session("userName");
    if (userName != null)
    {
      play.Logger.info("username " + userName);
      return WebSocket.withActor(out -> ExercisesPresenter.props(
        userName,
        eventBus,
        exerciseService,
        sessionService,
        out));
    } else
    {
      return WebSocketUtils.notAuthorizedWebSocket();
    }
  }
}
