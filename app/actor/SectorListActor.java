package actor;

import play.Logger;
import actor.messages.Sectors;
import actor.messages.Subscribe;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SectorListActor extends UntypedActor {

	private final ObjectMapper objectMapper;

	public static Props props(ActorRef out, ActorRef sessionActor) {
		return Props.create(SectorListActor.class, out, sessionActor);
	}

	private final ActorRef out;
	private ActorRef sessionActor;
	private String userName;

	public SectorListActor(ActorRef out, String userName, ActorRef sessionActor)
			throws JsonProcessingException {
		this.out = out;
		this.userName = userName;
		this.sessionActor = sessionActor;

		objectMapper = new ObjectMapper();
		subscribeToSession();

	}

	private void subscribeToSession() {
		sessionActor.tell(new Subscribe(userName), self());
	}

	public void onReceive(Object message) throws Exception {
		if (message instanceof String) {
			Logger.info("Received a message:" + message);
			Event readValue = objectMapper.readValue((String) message,Event.class);
			sessionActor.tell(readValue, getSelf());
		}

		if (message instanceof Sectors) {
			Sectors sectors = (Sectors)  message;
			Logger.info("Received a sectors message " + sectors.sectors.size() );
			String sectorsAsJson = objectMapper.writeValueAsString(message);
			out.tell(sectorsAsJson, self());
		}
	}

	public static class PropCreater {
		private final ActorRef sessionActor;
		private String userName;

		public PropCreater(ActorRef aSessionActor, String aUserName) {
			sessionActor = aSessionActor;
			userName = aUserName;
		}

		public Props props(ActorRef out) {
			return Props.create(SectorListActor.class, out, userName,
					sessionActor);
		}

	}

	public static class Event {
		public String eventName;
		public String sector;
	}
}
