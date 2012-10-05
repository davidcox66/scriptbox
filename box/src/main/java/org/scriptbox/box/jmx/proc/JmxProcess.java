package org.scriptbox.box.jmx.proc;

import org.scriptbox.box.jmx.conn.JmxConnection;

public class JmxProcess extends GenericProcess {

	private JmxConnection connection;
	
	public JmxProcess( String name, JmxConnection connection ) {
		super( name );
		this.connection = connection;
	}

	public JmxConnection getConnection() {
		return connection;
	}

	public String toString() {
		return "JmxProcess{ name=" + getName() + " }";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((connection == null) ? 0 : connection.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		JmxProcess other = (JmxProcess) obj;
		if (connection == null) {
			if (other.connection != null)
				return false;
		} else if (!connection.equals(other.connection))
			return false;
		return true;
	}

	

	
}
