package actor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import play.Logger;
import actor.SessionWebsocketHandler.Event;
import actor.messages.Sector;
import actor.messages.Sectors;
import actor.messages.Subscribe;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;

public class SessionActor extends UntypedActor {
	private final Map<String, String> sectors = new HashMap<>();
	private final Map<ActorRef, String> subscribers = new HashMap<>();

	public static Props props(int sessionId, int exerciseId, String ownerName) {
		return Props.create(new Creator<SessionActor>() {
			private static final long serialVersionUID = -5374920134795108497L;

			@Override
			public SessionActor create() throws Exception {
				return new SessionActor(sessionId, exerciseId, ownerName);
			}

		});
	}

	public SessionActor(int sessionId, int exerciseId, String ownerName) {
		sectors.put("WUR", "");
		sectors.put("ERL", "");
		sectors.put("FRA", "");
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof Subscribe) {
			Subscribe subscribe = (Subscribe) message;
			handleSubscription(subscribe);
		}
		if (message instanceof Event) {
			Event event = (Event) message;
			handleSelectionEvent(event);
			sendAssignementState();
		}
	}

	private void handleSelectionEvent(Event event) {
		String selectedSector = event.sector;
		Optional<String> userNameOption = getUserNameOfSender(getSender());
		if (userNameOption.isPresent() && sectors.containsKey(selectedSector)) {
			String userName = userNameOption.get();
			handleAssignement(selectedSector, userName);
		}
	}

	private void handleAssignement(String selectedSector, String userName) {
		if (isUnassigned(selectedSector)) {
			switchAssignement(selectedSector, userName);
		} else {
			if (isAssignedBy(selectedSector, userName)) {
				removeAssignement(userName);
			}
		}
	}

	private void switchAssignement(String selectedSector, String userName) {
		removeAssignement(userName);
		assign(userName, selectedSector);
	}

	private boolean isAssignedBy(String sectorName, String userName) {
		return sectors.get(sectorName).equals(userName);
	}

	private void assign(String userName, String sectorName) {
		sectors.put(sectorName, userName);
	}

	private void removeAssignement(String userName) {
		Optional<String> sectorNameOptional = getSectorForUserName(userName);
		if (sectorNameOptional.isPresent()) {
			sectors.put(sectorNameOptional.get(), "");
		}
	}

	private boolean isUnassigned(String sectorName) {
		return sectors.get(sectorName).equals("");
	}

	private Optional<String> getUserNameOfSender(ActorRef sender) {
		return Optional.ofNullable(subscribers.get(sender));
	}

	private Optional<String> getSectorForUserName(String userName) {
		return getKeyByValue(sectors, userName);
	}

	private void handleSubscription(Subscribe subscribe) {
		Logger.info("Received subscription: " + subscribe.userName);
		subscribers.put(getSender(), subscribe.userName);
		sendAssignementState();
	}

	private void sendAssignementState() {
		Set<ActorRef> subscribersList = subscribers.keySet();
		for (ActorRef subscriber : subscribersList) {
			String userName = subscribers.get(subscriber);
			Sectors sectorStates = getSectorStates(userName);
			subscriber.tell(sectorStates, getSelf());
		}
	}

	public Sectors getSectorStates(String userName) {
		List<Sector> sectorList = new ArrayList<>();
		for (String sectorName : sectors.keySet()) {
			String allocatedUser = sectors.get(sectorName);
			Sector sector = new Sector(sectorName);
			if (sectors.values().contains(userName)) {
				if (isAssignedBy(sectorName, userName)
						|| isUnassigned(sectorName)) {
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

	public static <T, E> Optional<T> getKeyByValue(Map<T, E> map, E value) {
		return map.entrySet().stream()
				.filter(entry -> entry.getValue().equals(value))
				.map(entry -> entry.getKey()).findFirst();
	}

}
