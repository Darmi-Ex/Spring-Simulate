package org.springframework.springcontext.context.support;

import org.springframework.springbean.beans.BeansException;
import org.springframework.springbean.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.springcontext.context.ApplicationContext;
import org.springframework.springcontext.context.ConfigurableApplicationContext;
import org.springframework.springcore.core.ResolvableType;
import org.springframework.springcore.core.io.DefaultResourceLoader;

import java.lang.annotation.Annotation;
import java.util.Map;

public abstract class AbstractApplicationContext extends DefaultResourceLoader implements ConfigurableApplicationContext {
  @Override
  public void setId(String var1) {

  }

  @Override
  public void setParent(ApplicationContext var1) {

  }

  @Override
  public void setEnvironment(ConfigurableEnvironment var1) {

  }

  @Override
  public ConfigurableEnvironment getEnvironment() {
    return null;
  }

  @Override
  public void addBeanFactoryPostProcessor(BeanFactoryPostProcessor var1) {

  }

  @Override
  public void addApplicationListener(ApplicationListener<?> var1) {

  }

  @Override
  public void addProtocolResolver(ProtocolResolver var1) {

  }

  @Override
  public void refresh() throws BeansException, IllegalStateException {

  }

  @Override
  public void registerShutdownHook() {

  }

  @Override
  public void close() {

  }

  @Override
  public boolean isActive() {
    return false;
  }

  @Override
  public ConfigurableListableBeanFactory getBeanFactory() throws IllegalStateException {
    return null;
  }

  @Override
  public String getId() {
    return null;
  }

  @Override
  public String getApplicationName() {
    return null;
  }

  @Override
  public String getDisplayName() {
    return null;
  }

  @Override
  public long getStartupDate() {
    return 0;
  }

  @Override
  public ApplicationContext getParent() {
    return null;
  }

  @Override
  public AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException {
    return null;
  }

  @Override
  public boolean containsBeanDefinition(String var1) {
    return false;
  }

  @Override
  public int getBeanDefinitionCount() {
    return 0;
  }

  @Override
  public String[] getBeanDefinitionNames() {
    return new String[0];
  }

  @Override
  public String[] getBeanNamesForType(ResolvableType var1) {
    return new String[0];
  }

  @Override
  public String[] getBeanNamesForType(Class<?> var1) {
    return new String[0];
  }

  @Override
  public String[] getBeanNamesForType(Class<?> var1, boolean var2, boolean var3) {
    return new String[0];
  }

  @Override
  public <T> Map<String, T> getBeansOfType(Class<T> var1) throws BeansException {
    return null;
  }

  @Override
  public <T> Map<String, T> getBeansOfType(Class<T> var1, boolean var2, boolean var3) throws BeansException {
    return null;
  }

  @Override
  public String[] getBeanNamesForAnnotation(Class<? extends Annotation> var1) {
    return new String[0];
  }

  @Override
  public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> var1) throws BeansException {
    return null;
  }

  @Override
  public <A extends Annotation> A findAnnotationOnBean(String var1, Class<A> var2) throws NoSuchBeanDefinitionException {
    return null;
  }

  @Override
  public Object getBean(String var1) throws BeansException {
    return null;
  }

  @Override
  public <T> T getBean(String var1, Class<T> var2) throws BeansException {
    return null;
  }

  @Override
  public Object getBean(String var1, Object... var2) throws BeansException {
    return null;
  }

  @Override
  public <T> T getBean(Class<T> var1) throws BeansException {
    return null;
  }

  @Override
  public <T> T getBean(Class<T> var1, Object... var2) throws BeansException {
    return null;
  }

  @Override
  public boolean containsBean(String var1) {
    return false;
  }

  @Override
  public boolean isSingleton(String var1) throws NoSuchBeanDefinitionException {
    return false;
  }

  @Override
  public boolean isPrototype(String var1) throws NoSuchBeanDefinitionException {
    return false;
  }

  @Override
  public boolean isTypeMatch(String var1, ResolvableType var2) throws NoSuchBeanDefinitionException {
    return false;
  }

  @Override
  public boolean isTypeMatch(String var1, Class<?> var2) throws NoSuchBeanDefinitionException {
    return false;
  }

  @Override
  public Class<?> getType(String var1) throws NoSuchBeanDefinitionException {
    return null;
  }

  @Override
  public String[] getAliases(String var1) {
    return new String[0];
  }

  @Override
  public void start() {

  }

  @Override
  public void stop() {

  }

  @Override
  public boolean isRunning() {
    return false;
  }
}
