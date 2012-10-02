package org.scriptbox.ui.shared.timed;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sencha.gxt.chart.client.chart.Chart;
import com.sencha.gxt.chart.client.chart.axis.TimeAxis;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.loader.LoadEvent;
import com.sencha.gxt.data.shared.loader.LoadHandler;

/**
 * 
 * @author david
 *
 * @param <C> load config object type
 * @param <M> model objects that populate the store
 * @param <D> load result passed from the loader
 */
public class TimeBasedChartUpdateBinding<C, M, D extends TimeBasedLoadResult<M>> implements LoadHandler<C, D> {

	private static final Logger logger = Logger.getLogger("TimeBasedChartUpdateBinding");
	
	private Chart<?> chart;
	private TimeAxis<?> timeAxis;
	private ListStore<M> store;
	
	/**
	  * Creates a load event handler that re-populates the given list store using
	  * the list load result provided by the loader via the event.
	  * 
	  * @param store the list store
	  */
	public TimeBasedChartUpdateBinding(Chart<?> chart, TimeAxis<?> timeAxis, ListStore<M> store) {
		this.chart = chart;
		this.timeAxis = timeAxis;
	    this.store = store;
	}

	@Override
	public void onLoad(LoadEvent<C, D> event) {
		logger.log( Level.INFO, "onLoad: " + event );
		TimeBasedLoadResult<M> loaded = event.getLoadResult();
	    store.replaceAll( loaded.getData() );
	    timeAxis.setStartDate(loaded.getStart());
        timeAxis.setEndDate(loaded.getEnd());
        chart.redrawChart();
	}
}
