package eventBus;

import akka.actor.ActorRef;

public class Event
{
  public String topic;
  public Object payload;
  public ActorRef sender;

  public Event(String topic, Object payload, ActorRef sender)
  {
    this.topic = topic;
    this.payload = payload;
    this.sender = sender;
  }
}
