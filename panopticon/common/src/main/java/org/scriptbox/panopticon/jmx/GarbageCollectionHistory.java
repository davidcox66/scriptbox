package org.scriptbox.panopticon.jmx;

import java.util.HashMap;
import java.util.Map;

class GarbageCollectionHistory {
	Map<GarbageCollector,GarbageCollection> collections = new HashMap<GarbageCollector,GarbageCollection>();
}