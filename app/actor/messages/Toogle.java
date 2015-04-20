package actor.messages;

public class Toogle
{
  private final String sector;
  private final String userName;

  public Toogle(String sector, String userName)
  {
    this.sector = sector;
    this.userName = userName;
  }
}