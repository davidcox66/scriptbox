package org.scriptbox.panopticon.jmx;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;

import org.scriptbox.box.container.BoxContext;
import org.scriptbox.box.exec.ExecContext;
import org.scriptbox.box.exec.ExecRunnable;
import org.scriptbox.box.jmx.conn.JmxConnection;
import org.scriptbox.box.jmx.proc.JmxProcess;
import org.scriptbox.panopticon.capture.CaptureContext;
import org.scriptbox.panopticon.capture.CaptureResult;
import org.scriptbox.panopticon.capture.CaptureStore;
import org.scriptbox.util.common.obj.ParameterizedRunnable;
import org.scriptbox.util.common.obj.ParameterizedRunnableWithResult;
import org.scriptbox.util.common.regex.Matching;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmxCapture implements ExecRunnable {
	
	private static final Logger LOGGER = LoggerFactory .getLogger(JmxCapture.class);

	private Set<String> attributeNames;
	private Set<Pattern> attributePatterns;

	private Set<String> exclusionNames;
	private Set<Pattern> exclusionPatterns;

	private Map<ObjectName, Set<String>> matchedNames = new HashMap<ObjectName, Set<String>>();

	private boolean caching;
	private ParameterizedRunnableWithResult<CaptureResult,CaptureContext> closure;

	public JmxCapture(
		Object oneOrMoreAttributes, 
		Object oneOrMoreExclusions,
		boolean caching, 
		ParameterizedRunnableWithResult<CaptureResult,CaptureContext> closure) 
	{
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
		this.caching = caching;
		this.closure = closure;
	}

	/**
	 * Iterates through each mbean defined in the enclosing query() element and
	 * gets its specified attributes.
	 */
	@Override
	public void run() throws Exception {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("processing attribute: names=" + attributeNames + ", patterns=" + attributePatterns);
		}
		CaptureStore cs = BoxContext.getCurrentContext().getBeans().getEx("store", CaptureStore.class );
		JmxProcess proc = ExecContext.getNearestEnclosing(JmxProcess.class);
		JmxConnection connection = proc.getConnection();
		Set<ObjectName> objectNames = ExecContext.getEnclosing(Set.class);
		for (ObjectName objectName : objectNames) {
			Set<String> names = getMatchingAttributeNames(connection, objectName);
			if (names != null && names.size() > 0) {
				try {
					AttributeList attributeList = getAttributes(connection, objectName, names);
					for (Attribute attr : attributeList.asList()) {
						capture(cs,connection,proc,objectName, attr);
					}
				} 
				catch (Exception ex) {
					LOGGER.error( "Error getting attributes - objectName=" + objectName + ", attributes=" + names, ex);
				}
			}
		}
	}

	private AttributeList getAttributes(JmxConnection connection, ObjectName objectName, Collection attrs) throws Exception {
		String[] namesArray = new String[attrs.size()];
		attrs.toArray(namesArray);
		return connection.getServer().getAttributes(objectName, namesArray);
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
		return connection.getServer().getMBeanInfo(objectName).getAttributes();
	}

	/**
	 * Consolidates some of the work involved with capturing a statistic. If
	 * there is a closure defined for this capture() element, then it will be
	 * called with the contextual and statstic data. The closure then returns
	 * may return a CaptureResult with the statistics in whatever form was
	 * necessary.
	 * 
	 * @param ctx
	 * @param objectName
	 * @param attr
	 */
	protected void capture(CaptureStore cs, JmxConnection connection, JmxProcess proc, ObjectName objectName, Attribute attr)  throws Exception {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("storing attribute: " + objectName + "." + attr.getName() + "=" + attr.getValue() );
		}
		CaptureContext ctx = new CaptureContext(connection, objectName, attr);
		CaptureResult result = null;
		if (closure != null) {
			if( result.millis == 0 ) {
				result.millis = System.currentTimeMillis();
			}
			result = closure.run(ctx);
		} 
		else {
			result = new CaptureResult(proc, objectName.toString(), attr.getName(), attr.getValue(), System.currentTimeMillis());
		}
		if (result != null ) {
			cs.store(result);
		}
	}

	private boolean isIncluded(String attr) {
		return Matching.isMatch(attr, attributeNames, attributePatterns);
	}

	private boolean isExcluded(String attr) {
		return Matching.isMatch(attr, exclusionNames, exclusionPatterns);
	}
}
