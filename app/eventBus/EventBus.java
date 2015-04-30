package eventBus;

import akka.event.japi.LookupEventBus;
import akka.actor.ActorRef;

public class EventBus extends LookupEventBus<Event, ActorRef, String>
{
  @Override
  public int mapSize()
  {
    return (10);
  }

  @Override
  public int compareSubscribers(ActorRef subscriberA, ActorRef subscriberB)
  {
    return (subscriberA.compareTo(subscriberB));
  }

  @Override
  public void publish(Event event, ActorRef subscriber)
  {
    subscriber.tell(event.payload, event.sender);
  }

  public void publish(String topic, Object message)
  {
    Event event = new Event(topic, message, ActorRef.noSender());
    publish(event);
  }

  
  public void publish(String topic,Object message, ActorRef sender)
  {
    Event event = new Event(topic, message, sender);
    publish(event);
  }

  public String classify(Event event)
  {
    return event.topic;
  }
}
