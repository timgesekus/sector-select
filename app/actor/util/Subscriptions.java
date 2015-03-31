package actor.util;

import java.util.HashMap;
import java.util.Map;

import play.Logger;
import actor.messages.Subscribe;
import actor.messages.Unsubscribe;
import akka.actor.ActorRef;

public class Subscriptions {
	private final Map<ActorRef, String> subscribers = new HashMap<>();

	public void handleSubscription(Subscribe subscribe, ActorRef sender) {
		Logger.info("Received subscription {}:{}: ", sender, subscribe.userName);
		subscribers.put(sender, subscribe.userName);
	}

	public void handleUnsubscribe(Unsubscribe unsubscribe, ActorRef sender) {
		Logger.info("Received unsubscribe {}:{}: ", sender);
		subscribers.remove(sender);
	}

	public void sendToSubscribers(Object message, ActorRef sender) {
		subscribers
		  .keySet()
		  .stream()
		  .forEach(subscriber -> subscriber.tell(message, sender));
	}
}
