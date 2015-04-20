package viewmodels.exerciseselect;

public class Redirect
{
  private String url;
  private String topic = "redirect";

  public String getTopic()
  {
    return topic;
  }

  public void setTopic(String topic)
  {
    this.topic = topic;
  }

  public String getUrl()
  {
    return url;
  }

  public void setUrl(String url)
  {
    this.url = url;
  }
}
