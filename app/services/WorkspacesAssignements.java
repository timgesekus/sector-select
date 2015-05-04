package services;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import play.Logger;

class WorkspacesAssignements
{
  private WorkspacesAssignementsListener listener;
  final Logger.ALogger logger = Logger.of(this.getClass());

  public WorkspacesAssignements(WorkspacesAssignementsListener listener)
  {
    this.listener = listener;

  }

  private Map<String, Optional<String>> assignements = new HashMap<>();

  public void addWorkspace(String workspaceName)
  {
    if (!assignements.containsKey(workspaceName))
    {
      assignements.put(workspaceName, Optional.empty());
      listener.workspaceAdd(workspaceName);
    }
  }

  public void selectWorkspace(String workspaceName, String userId)
  {
    if (isWorkspaceUnassigned(workspaceName))
    {
      Optional<String> usersWorkspaceOptional = getFirstKeyByValue(
        assignements,
        Optional.of(userId));
      if (usersWorkspaceOptional.isPresent())
      {
        logger.info("Workspaces is assigned {}", workspaceName);
        String usersWorkspace = usersWorkspaceOptional.get();
        deselectWorkspace(usersWorkspace, userId);
      }

      assignements.put(workspaceName, Optional.of(userId));
      listener.workspaceSelected(workspaceName, userId);
    }

  }

  public void deselectWorkspace(String workspaceName, String userId)
  {
    if (isWorkspaceAssignedToUser(workspaceName, userId))
    {
      String assignedUser = assignements.get(workspaceName).get();
      if (assignedUser.equals(userId))
      {
        assignements.put(workspaceName, Optional.empty());
        listener.workspaceDeselected(workspaceName, userId);
      }
    }
  }

  private boolean isWorkspaceUnassigned(String workspaceName)
  {
    return assignements.containsKey(workspaceName)
        && !assignements.get(workspaceName).isPresent();
  }

  private
    boolean
    isWorkspaceAssignedToUser(String workspaceName, String userId)
  {
    return assignements.containsKey(workspaceName)
        && assignements.get(workspaceName).isPresent()
        && assignements.get(workspaceName).get().equals(userId);
  }

  public static <T, E> Optional<T> getFirstKeyByValue(Map<T, E> map, E value)
  {
    return map
      .entrySet()
      .stream()
      .filter(entry -> entry.getValue().equals(value))
      .map(entry -> entry.getKey())
      .findFirst();
  }
}