package org.scriptbox.box.plugins.jmx.proc;

import org.scriptbox.box.plugins.jmx.JmxConnection;

public class JmxProcess {

	private String name;
	private JmxConnection connection;
	
	public JmxProcess( String name, JmxConnection connection ) {
		this.name = name;
		this.connection = connection;
	}

	public String getName() {
		return name;
	}

	public JmxConnection getConnection() {
		return connection;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((connection == null) ? 0 : connection.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JmxProcess other = (JmxProcess) obj;
		if (connection == null) {
			if (other.connection != null)
				return false;
		} else if (!connection.equals(other.connection))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	
}
