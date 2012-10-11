package org.scriptbox.metrics.query;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.scriptbox.metrics.model.MetricTreeNode;
import org.scriptbox.metrics.model.MetricTreeVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreePathQueryExp implements MetricQueryExp {
	
	private static final Logger LOGGER = LoggerFactory.getLogger( TreePathQueryExp.class );
	
	  private String operator;
	  private String type;
	  private Pattern pattern;
	  
	  public TreePathQueryExp( String operator, String type, Pattern pattern ) {
	    this.operator = operator;
	    this.type = type;
	    this.pattern = pattern;
	  }
	  
	  public Object evaluate( MetricQueryContext ctx ) {
		final Set<MetricTreeNode> ret = new HashSet<MetricTreeNode>();
		ctx.getTree().visitNodes( new MetricTreeVisitor() {
			public boolean visit( MetricTreeNode node ) {
				if( node.getType().equals(type) && pattern.matcher(node.getName()).matches() ) {
					addSubtree( node, ret );
					return false;
				}
				return true;
			}
		} );
	    if( LOGGER.isTraceEnabled() ) {
		    LOGGER.trace( "evaluate(type): " + toString() + " result=" + ret );
	    }
	    return ret;
	  }
	  
	  private void addSubtree( MetricTreeNode node, Set<MetricTreeNode> nodes ) {
		  nodes.add( node );
		  for( MetricTreeNode child : node.getChildren().values() ) {
			  addSubtree( child, nodes );
		  }
	  }
	  
	  public String toString() {
	    return operator + "(" + pattern + ")";
	  }
}
