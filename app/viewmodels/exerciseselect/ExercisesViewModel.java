package viewmodels.exerciseselect;

import java.util.List;
import java.util.Map;

public class ExercisesViewModel {

	public String topic = "exercises";
	public List<Group> groups;

	public ExercisesViewModel(List<Group> groups) {
		this.groups = groups;
	}

	public static class Group {
		public int id;
		public String name;
		public List<Exercise> exercises;
	}

	public static class Exercise {
		public int id;
		public String name;
	}
}
