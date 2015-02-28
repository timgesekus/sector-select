package actor.messages;

import java.util.List;

public class Sectors {
	public List<Sector> sectors;
	public String topic;

	public Sectors() {
		this.topic = "sectors";
	}

	public Sectors(List<Sector> sectors) {
		this.topic = "sectors";
		this.sectors = sectors;

	}
}