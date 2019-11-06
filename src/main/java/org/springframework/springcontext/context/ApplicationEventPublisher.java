package org.springframework.springcontext.context;

@FunctionalInterface
public interface ApplicationEventPublisher {
  default void publishEvent(ApplicationEvent event) {
    this.publishEvent((Object)event);
  }

  void publishEvent(Object var1);
}