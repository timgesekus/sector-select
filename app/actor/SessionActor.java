package actor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import play.Logger;
import actor.SectorListActor.Event;
import actor.messages.Sector;
import actor.messages.Sectors;
import actor.messages.Subscribe;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;

public class SessionActor extends UntypedActor {
	private final Map<String, String> sectors = new HashMap<>();
	private final Map<String, ActorRef> subscribers = new HashMap<>();

	public static Props props() {
		return Props.create(new Creator<SessionActor>() {
			private static final long serialVersionUID = -5374920134795108497L;

			@Override
			public SessionActor create() throws Exception {
				return new SessionActor();
			}

		});
	}

	public SessionActor() {
		sectors.put("WUR", "Tim Gesekus");
		sectors.put("ERL", "");
		sectors.put("FRA", "");
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof Subscribe) {
			Subscribe subscribe = (Subscribe) message;
			addSubscriber(subscribe);
		}
		if (message instanceof Event) {
			Event event = (Event) message;
			Optional<String> sectorName = getSectorOfReceiver();
			sectorName.map(sectors::get).filter(owner -> 
				
		}
	}

	private Optional<String> getSectorOfReceiver() {
		return getKeysByValue(subscribers, getSender());
	}

	private void addSubscriber(Subscribe subscribe) {
		Logger.info("Received subscription: " + subscribe.userName);
		subscribers.put(subscribe.userName, getSender());
		Set<String> userNames = subscribers.keySet();
		for (String userName : userNames) {
			Sectors sectorStates = getSectorStates(userName);
			ActorRef actorRef = subscribers.get(userName);
			actorRef.tell(sectorStates, getSelf());
		}
	}

	public Sectors getSectorStates(String userName) {
		List<Sector> sectorList = new ArrayList<>();
		for (String sectorName : sectors.keySet()) {
			String allocatedUser = sectors.get(sectorName);
			Sector sector = new Sector(sectorName);
			if (sectors.values().contains(userName)) {
				if (allocatedUser.equals(userName)) {
					sector.toggable = true;
				} else {
					sector.toggable = false;
				}
			}
			if (allocatedUser.equals(userName)) {
				sector.selected = true;
			}
			sector.userName = allocatedUser;
			sectorList.add(sector);
		}
		return new Sectors(sectorList);
	}
	
	public static <T, E> Optional<T> getKeysByValue(Map<T, E> map, E value) {
	    return map.entrySet()
	              .stream()
	              .filter(entry -> entry.getValue().equals(value))
	              .map(entry -> entry.getKey())
	              .findFirst();
	}
}
