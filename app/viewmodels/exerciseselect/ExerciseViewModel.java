package viewmodels.exerciseselect;

import java.util.List;

public class ExerciseViewModel {
	public String topic = "exerciseViewModel";
	public List<String> breadcrumb;
	public String heading;
	public List<SelectableItem> selectableItems;

	public ExerciseViewModel(List<String> breadcrumb, String heading, List<SelectableItem> selectableItems) {
		this.breadcrumb = breadcrumb;
		this.heading = heading;
		this.selectableItems = selectableItems;
		
	}
}
