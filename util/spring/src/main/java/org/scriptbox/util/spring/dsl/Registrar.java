package org.scriptbox.util.spring.dsl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.scriptbox.util.spring.context.eval.Evaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.Conventions;
import org.springframework.util.Assert;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;


public class Registrar {

	private static final Logger LOGGER = LoggerFactory.getLogger(Evaluator.class);

	/**
	 * Searches for nested bean elements and registers each of those with
	 * Spring. This returns the first element from the collection. Since the
	 * Spring BeanDefinitionParser only returns a single element, we return the
	 * first bean creates so that it can be passed by to Spring. Though we
	 * register it manually anyway, so any additional beans will be available
	 * even though they are not returned.
	 * 
	 * @param element
	 * @param parserContext
	 * @return
	 */
	public static BeanDefinition parseAndRegisterNestedBean(Element element, ParserContext parserContext) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("parseAndRegisterNestedBean: parsing nested bean definition: " + element);
		}
		List<Element> beanElements = DomUtils.getChildElementsByTagName( element, "bean");
		BeanDefinition ret = null;
		for (Element beanElement : beanElements) {
			BeanDefinition bd = Registrar.parseAndRegisterBean(beanElement, parserContext);
			if (ret == null) {
				ret = bd;
			}
		}
		return ret;
	}

	/**
	 * Registers the given bean definition with Spring.
	 * 
	 * @param element
	 * @param parserContext
	 * @return
	 */
	public static BeanDefinition parseAndRegisterBean(Element element, ParserContext parserContext) {
		BeanDefinitionParserDelegate delegate = parserContext.getDelegate();
		BeanDefinitionHolder holder = delegate .parseBeanDefinitionElement(element);
		Registrar.addAttributeProperties(element, parserContext, holder);
		BeanDefinitionReaderUtils.registerBeanDefinition(holder, parserContext.getRegistry());

		BeanDefinition ret = holder.getBeanDefinition();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("parseAndRegisterBean: " + "element=" + element
					+ ", ret=" + ret + ", properties="
					+ (ret != null ? ret.getPropertyValues() : null));
		}
		return ret;
	}

	/**
	 * Handle properties in the spring p: namespace
	 * 
	 * @param element
	 * @param parserContext
	 * @param holder
	 */
	private static void addAttributeProperties(Element element, ParserContext parserContext, BeanDefinitionHolder holder) {
		NamedNodeMap attributes = element.getAttributes();
		for (int x = 0; x < attributes.getLength(); x++) {
			Attr attribute = (Attr) attributes.item(x);
			if (isEligibleAttribute(attribute, parserContext)) {
				String propertyName = extractPropertyName(attribute .getLocalName());
				Assert.state( StringUtils.isNotBlank(propertyName),
					"Illegal property name returned from 'extractPropertyName(String)': cannot be null or empty.");
				MutablePropertyValues values = holder.getBeanDefinition().getPropertyValues();
				if (isBeanReferenceAttribute(attribute.getName())) {
					values.addPropertyValue(propertyName.substring(0, propertyName.length() - 3), new RuntimeBeanReference(attribute.getValue()));
				} 
				else {
					values.addPropertyValue(propertyName, attribute.getValue());
				}
			}
		}
	}

	private static boolean isEligibleAttribute(Attr attribute, ParserContext parserContext) {
		/*
		 * LOGGER.debug( "isEligibleAttribute: attribute=" + attribute +
		 * ", name=" + attribute.getName() + ", namespaceURI=" +
		 * attribute.getNamespaceURI() + ", baseURI=" + attribute.getBaseURI()
		 * );
		 */
		String fullName = attribute.getName();
		return (!fullName.equals("xmlns") && !fullName.startsWith("xmlns:") && isEligibleAttribute(attribute .getName()));
	}

	private static boolean isEligibleAttribute(String attributeName) {
		return attributeName.startsWith("p:");
	}

	private static boolean isBeanReferenceAttribute(String attributeName) {
		return attributeName.endsWith("-ref");
	}

	private static String extractPropertyName(String attributeName) {
		return Conventions.attributeNameToPropertyName(attributeName);
	}

}
