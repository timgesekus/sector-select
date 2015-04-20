package eventBus;

public class Event
{
  public String topic;
  public Object payload;

  public Event(String topic, Object payload)
  {
    this.topic = topic;
    this.payload = payload;
  }
}
