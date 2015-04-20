package actor.messages;

import java.util.List;

import joinSession.viewmodel.Workspace;

public class Sectors
{
  public List<Workspace> sectors;
  public String topic;

  public Sectors()
  {
    this.topic = "sectors";
  }

  public Sectors(List<Workspace> sectors)
  {
    this.topic = "sectors";
    this.sectors = sectors;

  }
}