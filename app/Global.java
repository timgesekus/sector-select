import modules.ActorModule;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import akka.actor.ActorRef;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import de.dfs.utils.config.injector.TypeSafeConfigInjectorModule;

public class Global extends GlobalSettings
{
  private static final Injector INJECTOR = createInjector();

  @Override
  public <A> A getControllerInstance(Class<A> controllerClass) throws Exception
  {
    Logger.info("Get instance called" + controllerClass.toString());
    return INJECTOR.getInstance(controllerClass);
  }

  private static Injector createInjector()
  {
    Logger.info("Creating injector");
    return Guice.createInjector(new ActorModule(), new TypeSafeConfigInjectorModule(play.Play.application().configuration()));
  }

  @Override
  public void onStart(Application app)
  {
    super.beforeStart(app);
    // Side effect needed don´t delte
    ActorRef chatService = INJECTOR.getInstance(Key.get(
      ActorRef.class,
      Names.named("ChatService")));
    ActorRef workspacesService = INJECTOR.getInstance(Key.get(
      ActorRef.class,
      Names.named("WorkspacesService")));

  }

}
