package joinSession.viewmodel;

public class Workspace
{
  public String name;
  public String userName;
  public boolean selected;
  public boolean toggable;

  public Workspace(String aName)
  {
    name = aName;
    userName = "";
    selected = false;
    toggable = true;
  }
}