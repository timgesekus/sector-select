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
    return (subscriberA.compareTo(subscriberA));
  }

  @Override
  public void publish(Event event, ActorRef subscriber)
  {
    subscriber.tell(event, null);
  }

  public String classify(Event event)
  {
    return event.topic;
  }
}
