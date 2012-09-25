package org.scriptbox.util.cassandra;

import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import me.prettyprint.hector.api.exceptions.HectorException;
import me.prettyprint.hector.api.exceptions.HInvalidRequestException;
import org.apache.cassandra.thrift.InvalidRequestException;

public class CassandraExceptionHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(CassandraExceptionHelper.class);

	private static final Pattern KEYSPACE_NOT_EXIST = Pattern .compile("^Keyspace .* does not exist$");
	private static final Pattern COLUMN_FAMILY_NOT_DEFINED = Pattern .compile("^CF is not defined in that keyspace.$");
	private static final Pattern ALL_HOSTS_DOWN = Pattern .compile(".*All host pools marked down.*");
	private static final Pattern SCHEMA_NOT_AGREED = Pattern .compile( "^Cluster schema does not yet agree$" );

	/**
	 * Provides a convenient way of running a block of code where any exception
	 * related to the keyspace not existing will be ignored. This may be used by
	 * various initialization/cleanup logic which needs to run without error
	 * regardless of whether the keyspace exists.
	 * 
	 * @param closure
	 */
	public static void runIgnoringMissingKeyspace(Runnable closure) {
		try {
			closure.run();
		} 
		catch (HInvalidRequestException ex) {
			if (isKeyspaceNotExistException(ex)) {
				LOGGER.debug("Keyspace does not exist yet");
			} 
			else {
				throw ex;
			}
		}
	}

	public static void runIgnoringExistingColumnFamily(Runnable closure) {
		try {
			closure.run();
		} 
		catch (HInvalidRequestException ex) {
			if (isExistingColumnFamilyException(ex)) {
				LOGGER.debug("Column family already exists");
			} 
			else {
				throw ex;
			}
		}
	}

	static void runIgnoringMissingColumnFamily(Runnable closure) {
		try {
			closure.run();
		} 
		catch (HInvalidRequestException ex) {
			if (isKeyspaceNotExistException(ex) || isColumnFamilyNotExistException(ex)) {
				LOGGER.debug("Keyspace/Column Family does not exist yet");
			} 
			else {
				throw ex;
			}
		}
	}

	/**
	 * Checks if the given exception results from a keyspace not existing.
	 * 
	 * @param ex
	 * @return
	 */
	public static boolean isKeyspaceNotExistException( HInvalidRequestException ex) {
		return isExceptionWhyMatchingPattern(ex, KEYSPACE_NOT_EXIST);
	}

	/**
	 * Checks if the given exception indicates that a pending schema update has
	 * not been replicated to all nodes.
	 * 
	 * @param ex
	 * @return
	 */
	public static boolean isClusterSchemaNotSyncedException( HInvalidRequestException ex )
	    {
	      return isExceptionWhyMatchingPattern( ex, SCHEMA_NOT_AGREED  );
	    }

	public static boolean isExistingColumnFamilyException( HInvalidRequestException ex) {
		return isExceptionWhyMatchingPattern(ex, KEYSPACE_NOT_EXIST);
	}

	public static boolean isColumnFamilyNotExistException(HInvalidRequestException ex) {
		return isExceptionWhyMatchingPattern(ex, COLUMN_FAMILY_NOT_DEFINED);
	}

	public static boolean isPoolsDownException(HectorException ex) {
		String msg = ex.getMessage();
		return msg != null && ALL_HOSTS_DOWN.matcher(msg).matches();
	}

	/**
	 * Checks if the 'why' property of the exception matches the given pattern.
	 * Also, looks at the root cause of the exception to work around any lost
	 * exception translation from ExceptionTranslater.
	 * 
	 * @param ex
	 * @param pattern
	 * @return
	 */
	public static boolean isExceptionWhyMatchingPattern( HInvalidRequestException ex, Pattern pattern) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug( "isExceptionWhyMatchingPattern: pattern=${pattern}, why=${ex.why}, cause.why=${ex?.cause.why}", ex);
		}
		return pattern.matcher(ex.getWhy()).matches() || 
			(ex.getCause() != null && ex.getCause() instanceof InvalidRequestException && pattern
			.matcher(((InvalidRequestException)ex.getCause()).getWhy()).matches());
	}

}
