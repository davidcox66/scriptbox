package org.scriptbox.ui.server.chart;

import java.util.List;

public interface LegendPostProcessor {

	public List<String> process( List<String> legends );
}
