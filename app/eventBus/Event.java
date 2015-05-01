package eventBus;

import akka.actor.ActorRef;

public class Event
{
  public Topic topic;
  public Object payload;
  public ActorRef sender;

  public Event(Topic topic, Object payload, ActorRef sender)
  {
    this.topic = topic;
    this.payload = payload;
    this.sender = sender;
  }
}
