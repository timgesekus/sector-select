package controllers;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import play.libs.Akka;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import utils.WebSocketUtils;
import views.html.exerciseselect;
import actor.GroupsAndExerciseServiceActor;
import actor.SessionActor;
import actor.GroupListActor.PropCreater;
import akka.actor.ActorRef;

public class ExerciseSelection extends Controller {

	private static ActorRef groupsAndServicesService = Akka.system().actorOf(
	  GroupsAndExerciseServiceActor.props());
	@SubjectPresent
	public static Result exerciseSelect() {
		return ok(exerciseselect.render("Select exercise."));
	}

	public static WebSocket<String> groups() {
		String userName = session("userName");
		if (userName != null) {
			play.Logger.info("username " + userName);
			PropCreater propCreater = new PropCreater(userName,groupsAndServicesService);
			return WebSocket.withActor(propCreater::props);
		} else {
			return WebSocketUtils.notAuthorizedWebSocket();
		}
	}

}
