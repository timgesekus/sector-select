package controllers;

import actor.SectorListActor;
import actor.SessionActor;
import actor.SectorListActor.PropCreater;
import akka.actor.ActorRef;
import play.mvc.*;
import play.libs.Akka;

public class SectorList extends Controller {
	private static ActorRef sessionActor = Akka.system().actorOf(SessionActor.props());
	public static WebSocket<String> sectors(String userName) {
		PropCreater propCreater = new SectorListActor.PropCreater(sessionActor, userName);
	    return WebSocket.withActor(propCreater::props);
	}
}
