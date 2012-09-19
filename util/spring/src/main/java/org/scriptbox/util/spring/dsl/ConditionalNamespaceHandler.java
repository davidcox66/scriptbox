package org.scriptbox.util.spring.dsl;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Defines handlers for various conditional tags in a Spring DSL (see Spring Authoring)
 * 
 */
public class ConditionalNamespaceHandler
  extends NamespaceHandlerSupport
{

  @Override
  public void init()
  {
    super.registerBeanDefinitionParser("test", new ConditionalTestDefinitionParser());
    super.registerBeanDefinitionParser("import", new ConditionalImportDefinitionParser());
    super.registerBeanDefinitionParser("switch", new ConditionalSwitchDefinitionParser());
    super.registerBeanDefinitionParser("dependency", new ConditionalDependencyDefinitionParser());
  }

}
