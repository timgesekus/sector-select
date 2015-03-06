package actor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import play.Logger;
import actor.JoinSessionWS.Event;
import actor.SessionChat.ChatMessage;
import actor.messages.Sector;
import actor.messages.Sectors;
import actor.messages.Subscribe;
import actor.messages.Unsubscribe;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.Creator;
import akka.japi.pf.ReceiveBuilder;

public class SessionActor extends AbstractActor {
	private final Map<String, String> sectors = new HashMap<>();
	private final Map<ActorRef, String> subscribers = new HashMap<>();
	private int sessionId;
	private ActorRef sessionChaTActorRef;

	public static Props props(int sessionId, int exerciseId, String ownerName) {
		return Props.create(new Creator<SessionActor>() {
			private static final long serialVersionUID = 1L;

			@Override
			public SessionActor create() throws Exception {
				return new SessionActor(sessionId, exerciseId, ownerName);
			}
		});
	}

	public SessionActor(int sessionId, int exerciseId, String ownerName) {
		this.sessionId = sessionId;
		configureSectors();
		configureMessageHandling();
		createChat();
	}

	private void configureSectors() {
		sectors.put("WUR", "");
		sectors.put("ERL", "");
		sectors.put("FRA", "");
	}

	private void configureMessageHandling() {
		receive(ReceiveBuilder
		  .match(Event.class, this::handleSelectionEvent)
		  .match(Subscribe.class, this::handleSubscription)
		  .match(Unsubscribe.class, this::handleUnsubscribe)
		  .match(ChatMessage.class, this::forwardToChatActor)
		  .matchAny(this::unhandled)
		  .build());
	}

	private void createChat() {
		Props props = SessionChat.props();
		sessionChaTActorRef = getContext().actorOf(
		  props,
		  "sessionChatActor-" + sessionId);
	}

	private void handleSelectionEvent(Event event) {
		String selectedSector = event.sector;
		Optional<String> userNameOption = getUserNameOfSender(sender());
		if (userNameOption.isPresent() && sectors.containsKey(selectedSector)) {
			handleAssignement(selectedSector, userNameOption.get());
		}
		sendAssignementState();

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

	private void handleUnsubscribe(Unsubscribe unsubscribe) {
		Logger.info("Received unsubscribe {}:{}: ", sender());
		Optional<String> userNameOfSender = getUserNameOfSender(sender());
		if (userNameOfSender.isPresent()) {
			List<String> keys = getKeysByValue(sectors, userNameOfSender.get());
			for (String key : keys) {
				sectors.put(key, "");
			}
			subscribers.remove(sender());
			sessionChaTActorRef.tell(unsubscribe, sender());
			sendAssignementState();

		}
	}

	private void handleSubscription(Subscribe subscribe) {
		Logger.info("Received subscription {}:{}: ", sender(), subscribe.userName);
		subscribers.put(sender(), subscribe.userName);
		sessionChaTActorRef.tell(subscribe, sender());
		sendAssignementState();
	}

	private void forwardToChatActor(Object message) {
		sessionChaTActorRef.tell(message, sender());
	}

	private void sendAssignementState() {
		Set<ActorRef> subscribersList = subscribers.keySet();
		for (ActorRef subscriber : subscribersList) {
			String userName = subscribers.get(subscriber);
			Sectors sectorStates = getSectorStates(userName);
			subscriber.tell(sectorStates, self());
		}
	}

	public Sectors getSectorStates(String userName) {
		List<Sector> sectorList = new ArrayList<>();
		for (String sectorName : sectors.keySet()) {
			String allocatedUser = sectors.get(sectorName);
			Sector sector = new Sector(sectorName);
			if (sectors.values().contains(userName)) {
				if (isAssignedBy(sectorName, userName) || isUnassigned(sectorName)) {
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
		return map
		  .entrySet()
		  .stream()
		  .filter(entry -> entry.getValue().equals(value))
		  .map(entry -> entry.getKey())
		  .findFirst();
	}

	public static <T, E> List<T> getKeysByValue(Map<T, E> map, E value) {
		return map
		  .entrySet()
		  .stream()
		  .filter(entry -> entry.getValue().equals(value))
		  .map(entry -> entry.getKey())
		  .collect(Collectors.toList());
	}

}
