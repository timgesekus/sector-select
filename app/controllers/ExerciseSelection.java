package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import utils.WebSocketUtils;
import actor.ExerciseSelectionWS;
import akka.actor.ActorRef;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class ExerciseSelection extends Controller {

	private final ActorRef sessionManager;
	private final ActorRef exerciseService;

	@Inject
	public ExerciseSelection(
	  @Named("SessionManager") ActorRef sessionManager,
	  @Named("ExerciseService") ActorRef exerciseService) {
		this.sessionManager = sessionManager;
		this.exerciseService = exerciseService;
	}

	@SubjectPresent
	public static Result exerciseSelect() {
		return ok(views.html.exerciseSelect.render("Select exercise."));
	}

	public WebSocket<String> exerciseSelectionWS() {

		String userName = session("userName");
		if (userName != null) {
			play.Logger.info("username " + userName);
			return WebSocket.withActor(out -> ExerciseSelectionWS.props(
			  out,
			  userName,
			  sessionManager,
			  exerciseService));
		} else {
			return WebSocketUtils.notAuthorizedWebSocket();
		}
	}
}
