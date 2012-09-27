package org.scriptbox.util.gwt.server.remote;

import javax.servlet.http.HttpServletRequest;

public enum ServiceScope {

	REQUEST{
		public Object getLock( HttpServletRequest request ) {
			return request;
		}
		public Object get( HttpServletRequest request, String name ) {
			return request.getAttribute( name );
		}
		public void set( HttpServletRequest request, String name, Object value ) {
			request.setAttribute( name, value );
		}
	},
	SESSION {
		public Object getLock( HttpServletRequest request ) {
			return request.getSession(true);
		}
		public Object get( HttpServletRequest request, String name ) {
			return request.getSession(true).getAttribute( name );
		}
		public void set( HttpServletRequest request, String name, Object value ) {
			request.getSession(true).setAttribute( name, value );
		}
	},
	APPLICATION {
		public Object getLock( HttpServletRequest request ) {
			return request.getSession(true).getServletContext();
		}
		public Object get( HttpServletRequest request, String name ) {
			return request.getSession(true).getServletContext().getAttribute( name );
		}
		public void set( HttpServletRequest request, String name, Object value ) {
			request.getSession(true).getServletContext().setAttribute( name, value );
		}
	},
	NONE {
		public Object getLock( HttpServletRequest request ) {
			return null;
		}
		public Object get( HttpServletRequest request, String name ) {
			return null;
		}
		public void set( HttpServletRequest request, String name, Object value ) {
		}
	};
	
	public abstract Object getLock( HttpServletRequest request );
	public abstract Object get( HttpServletRequest request, String name ); 
	public abstract void set( HttpServletRequest request, String name, Object value ); 
	
}
