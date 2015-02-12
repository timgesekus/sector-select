package actor.messages;

public class Sector {
	public String name;
	public String userName;
	public boolean selected;
	public boolean toggable;

	public Sector(String aName) {
		name = aName;
		userName = "";
		selected = false;
		toggable = true;
	}
}