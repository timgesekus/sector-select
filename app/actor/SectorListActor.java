package actor;

import java.io.IOException;

import play.Logger;
import actor.messages.Sectors;
import actor.messages.Subscribe;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.pf.ReceiveBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SectorListActor extends AbstractActor {

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

		receive(ReceiveBuilder
		  .match(String.class, this::receiveJsonFromSocket)
		  .match(Sectors.class, this::receiveSectors)
		  .build());

	}

	private void subscribeToSession() {
		sessionActor.tell(new Subscribe(userName), self());
	}

	public void receiveJsonFromSocket(String json)
	  throws JsonParseException,
	  JsonMappingException,
	  IOException {
		Logger.info("Received a message:" + json);
		Event readValue;
		readValue = objectMapper.readValue((String) json, Event.class);
		sessionActor.tell(readValue, self());

	}

	public void receiveSectors(Sectors sectors) throws JsonProcessingException {
		Logger.info("Received a sectors message " + sectors.sectors.size());
		String sectorsAsJson = objectMapper.writeValueAsString(sectors);
		out.tell(sectorsAsJson, self());
	}

	public static class PropCreater {
		private final ActorRef sessionActor;
		private String userName;

		public PropCreater(ActorRef aSessionActor, String aUserName) {
			sessionActor = aSessionActor;
			userName = aUserName;
		}

		public Props props(ActorRef out) {
			return Props.create(SectorListActor.class, out, userName, sessionActor);
		}

	}

	public static class Event {
		public String topic;
		public String sector;

		public Event() {
			topic = "event";
		}
	}
}
