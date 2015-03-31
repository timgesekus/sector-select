package joinSession.viewmodel;

import java.util.List;

public class WorkspaceAssignementViewModel {
	public List<Workspace> workspaceAssignements;
	public String topic;

	public WorkspaceAssignementViewModel() {
		this.topic = "workspaceAssignement";
	}

	public WorkspaceAssignementViewModel(List<Workspace> workspaceAssignements) {
		this.topic = "workspaceAssignement";
		this.workspaceAssignements = workspaceAssignements;

	}
}
