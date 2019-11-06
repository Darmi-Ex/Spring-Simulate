package org.springframework.springcontext.context;

public interface Lifecycle {
  void start();

  void stop();

  boolean isRunning();
}