package joinSession.viewmodel;

import java.util.List;

public class ChatViewModel
{
  public List<String> chatLines;
  public String topic;

  private ChatViewModel()
  {
  }

  public ChatViewModel(List<String> chatLines)
  {
    this.topic = "ChatViewModel";
    this.chatLines = chatLines;

  }

}
