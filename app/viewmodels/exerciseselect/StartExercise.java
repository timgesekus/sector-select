package viewmodels.exerciseselect;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StartExercise {
	public String topic;
	public String ownerName;
	public int exerciseId;
}
