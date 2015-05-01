package modules;

import play.Logger;
import play.libs.Akka;
import services.ChatService;
import services.SessionService;
import actor.ExerciseService;
import akka.actor.ActorRef;
import akka.actor.Props;
import eventBus.EventBus;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import controllers.JoinSession;

/**
 * Guice doesn't handle java 8 very well. So we need to catch exception by
 * ourselfs. Can be removed with guice 4.0
 */
public class ActorModule extends AbstractModule
{

  @Override
  protected void configure()
  {
    bind(EventBus.class).toInstance(new eventBus.EventBus());

  }

  @Provides
  @Singleton
  @Named("SessionService")
  ActorRef provideSessionManager(eventBus.EventBus eventBus)
  {
    try
    {
      Logger.info("Creating session manager");
      Props props = SessionService.props(eventBus);
      return Akka.system().actorOf(props, "SessionService");
    } catch (Exception e)
    {
      Logger.error("Failed in provide", e);
    }
    Logger.info("All is well");
    return null;
  }

  @Provides
  @Singleton
  @Named("ExerciseService")
  ActorRef provideExerciseService()
  {
    try
    {
      Logger.info("Creating exercise service");
      Props props = ExerciseService.props();
      return Akka.system().actorOf(props, "EcerciseServices");
    } catch (Exception e)
    {
      Logger.error("Failed in provide exercise service", e);
    }
    Logger.info("All is well");
    return null;
  }

  @Provides
  @Singleton
  @Named("ChatService")
  ActorRef provideChatService(EventBus eventBus)
  {
    try
    {
      Logger.info("Creating chat service");
      Props props = ChatService.props(eventBus);
      return Akka.system().actorOf(props, "ChatService");
    } catch (Exception e)
    {
      Logger.error("Failed in provide chat service", e);
    }
    return null;
  }

  @Provides
  JoinSession provideJoinSession(
    @Named("SessionService") ActorRef sessionService,
    EventBus eventBus)
  {
    try
    {
      Logger.info("Creating session controller");
      return new JoinSession(sessionService, eventBus);
    } catch (Exception e)
    {
      Logger.error("Failed in provide", e);
    }
    Logger.info("All is well");
    return null;
  }
}
