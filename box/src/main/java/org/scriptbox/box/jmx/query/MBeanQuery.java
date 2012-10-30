package org.scriptbox.box.jmx.query;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;

import org.scriptbox.box.jmx.conn.JmxConnection;
import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.scriptbox.util.common.regex.Matching;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MBeanQuery {

	private static final Logger LOGGER = LoggerFactory .getLogger(MBeanQuery.class);

	private Set<String> attributeNames;
	private Set<Pattern> attributePatterns;

	private Set<String> exclusionNames;
	private Set<Pattern> exclusionPatterns;

	private Map<ObjectName, Set<String>> matchedNames = new HashMap<ObjectName, Set<String>>();
	private boolean caching;
	
	public MBeanQuery( Object oneOrMoreAttributes, Object oneOrMoreExclusions, boolean caching )
	{
		this.caching = caching;
		
		Matching.iterateOverElementOrCollection(oneOrMoreAttributes, new ParameterizedRunnable<Object>() {
			public void run(Object attr) {
				if (Matching.isRegex(attr)) {
					if (attributePatterns == null) {
						attributePatterns = new HashSet<Pattern>();
					}
					attributePatterns.add(Matching.toPattern(attr));
				} else {
					if (attributeNames == null) {
						attributeNames = new HashSet<String>();
					}
					attributeNames.add((String)attr);
				}
			}
		});
		Matching.iterateOverElementOrCollection(oneOrMoreExclusions, new ParameterizedRunnable<Object>() {
			public void run(Object exclusion) {
				if (Matching.isRegex(exclusion)) {
					if (exclusionPatterns == null) {
						exclusionPatterns = new HashSet<Pattern>();
					}
					exclusionPatterns.add(Matching.toPattern(exclusion));
				} else {
					if (exclusionNames == null) {
						exclusionNames = new HashSet<String>();
					}
					exclusionNames.add((String)exclusion);
				}
			}
		});
	}

	public void query( JmxConnection connection, Set<ObjectName> objectNames, MBeanQueryHandler handler ) throws Exception {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("processing attribute: names=" + attributeNames + ", patterns=" + attributePatterns);
		}
		for (ObjectName objectName : objectNames) {
			Set<String> names = getMatchingAttributeNames(connection, objectName);
			if (names != null && names.size() > 0) {
				try {
					AttributeList attributeList = connection.getAttributes(objectName, names);
					for (Attribute attr : attributeList.asList()) {
						handler.process( objectName, attr );
					}
				} 
				catch (Exception ex) {
					LOGGER.error( "Error getting attributes - objectName=" + objectName + ", attributes=" + names, ex);
				}
			}
		}
	}

	private Set<String> getMatchingAttributeNames(JmxConnection connection, ObjectName objectName) throws Exception {
		Set<String> ret = null;
		if (caching) {
			ret = matchedNames.get(objectName);
		}
		if (ret == null) {
			ret = new HashSet<String>();
			if (attributePatterns != null && attributePatterns.size() > 0) {
				MBeanAttributeInfo[] attributeInfo = getAllAttributeInfos(connection, objectName);
				for (MBeanAttributeInfo ai : attributeInfo) {
					if (ai.isReadable() && isIncluded(ai.getName()) && !isExcluded(ai.getName())) {
						ret.add(ai.getName());
					}
				}
			}
			// Add any additional explicit attribute names to be queried. Dups
			// from patterns will be filtered out by the Set.
			if (attributeNames != null && attributeNames.size() > 0) {
				ret.addAll(attributeNames);
			}
			if (caching) {
				matchedNames.put(objectName, ret);
			}
		}
		return ret;
	}

	MBeanAttributeInfo[] getAllAttributeInfos(JmxConnection connection, ObjectName objectName) throws Exception {
		return connection.getAttributeInfo( objectName );
	}

	private boolean isIncluded(String attr) {
		return Matching.isMatch(attr, attributeNames, attributePatterns);
	}

	private boolean isExcluded(String attr) {
		return Matching.isMatch(attr, exclusionNames, exclusionPatterns);
	}
}
