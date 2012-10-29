package org.scriptbox.metrics.query.exp;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.scriptbox.metrics.model.MetricTreeNode;
import org.scriptbox.metrics.model.MetricTreeVisitor;
import org.scriptbox.metrics.query.main.MetricQueryContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreePathQueryExp implements MetricQueryExp {
	
	private static final Logger LOGGER = LoggerFactory.getLogger( TreePathQueryExp.class );
	
	  private String name;
	  private String type;
	  private Pattern pattern;
	  
	  public TreePathQueryExp( String type, Pattern pattern ) {
		  this( null, type, pattern );
	  }
	  public TreePathQueryExp( String name, String type, Pattern pattern ) {
	    this.name = name;
	    this.type = type;
	    this.pattern = pattern;
	  }
	  
	  public Object evaluate( MetricQueryContext ctx ) {
		final Set<MetricTreeNode> ret = new HashSet<MetricTreeNode>();
		ctx.getTree().visitNodes( new MetricTreeVisitor() {
			public boolean visit( MetricTreeNode node ) {
				if( node.getType().equals(type) && pattern.matcher(node.getName()).matches() ) {
					addLeaves( node, ret );
					return false;
				}
				return true;
			}
		} );
	    return ret;
	  }
	  
	  private void addLeaves( MetricTreeNode node, Set<MetricTreeNode> nodes ) {
		  if( node.isSequenceAvailable() ) {
			  nodes.add( node );
		  }
		  for( MetricTreeNode child : node.getChildren().values() ) {
			  addLeaves( child, nodes );
		  }
	  }
	  
	  public String toString() {
	    return name != null ? name : type + "(" + pattern + ")";
	  }
}
