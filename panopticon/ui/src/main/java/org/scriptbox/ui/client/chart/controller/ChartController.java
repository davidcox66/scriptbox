package org.scriptbox.ui.client.chart.controller;

import java.util.Date;

import org.scriptbox.ui.shared.chart.ChartGWTServiceAsync;

public abstract class ChartController {

	/**
	 * When doing updates, we fetch the most recent data for a chart. Since some reports
	 * may take into previous data into account, we fetch a few previous records to 
	 * get correct data.
	 */
	private static final int LOOKBACK = 2;
	
	protected ChartGWTServiceAsync service; 
	protected Date start;
	protected Date end;
	protected int resolution;
	protected Date last;
	
	public abstract void reload();
	public abstract void update();
	
	protected Date getUpdateDate() {
		if( last != null ) {
				return start != null ?
					new Date(Math.max(start.getTime(),last.getTime()-(resolution*1000*LOOKBACK))) :
					last;
		}
		else {
			return start;
		}
		
	}
}
