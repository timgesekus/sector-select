package controllers;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import play.libs.Akka;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import utils.WebSocketUtils;
import views.html.exerciseselect;
import actor.ExerciseService;
import actor.SessionActor;
import actor.ExerciseSelectionWebsocketHandler.PropCreater;
import akka.actor.ActorRef;

public class ExerciseSelection extends Controller {

	private final ActorRef sessionManager;

	@Inject
	public ExerciseSelection(@Named("SessionManager") ActorRef sessionManager) {
		this.sessionManager = sessionManager;
		// TODO Auto-generated constructor stub
	}

	@SubjectPresent
	public static Result exerciseSelect() {
		return ok(exerciseselect.render("Select exercise."));
	}

	public WebSocket<String> groups() {
		String userName = session("userName");
		if (userName != null) {
			play.Logger.info("username " + userName);
			ActorRef exerciseServices = Akka.system().actorOf(
					ExerciseService.props(), "EcerciseServices");
			PropCreater propCreater = new PropCreater(userName, sessionManager,
					exerciseServices);
			return WebSocket.withActor(propCreater::props);
		} else {
			return WebSocketUtils.notAuthorizedWebSocket();
		}
	}
}
