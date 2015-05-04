package services;

public interface WorkspacesAssignementsListener
{

  void workspaceAdd(String workspaceName);

  void workspaceSelected(String workspaceName, String userId);

  void workspaceDeselected(String workspaceName, String userId);

}