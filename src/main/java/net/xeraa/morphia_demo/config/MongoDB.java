package net.xeraa.morphia_demo.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;

import net.xeraa.morphia_demo.entities.BaseEntity;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.net.UnknownHostException;
import java.util.logging.Logger;

/**
 * MongoDB providing the database connection for main.
 */
public class MongoDB {

  public static final int DB_PORT = 27017;
  public static final String DB_NAME = "morphia_demo";

  private static final Logger LOG = Logger.getLogger(MongoDB.class.getName());

  private static final MongoDB INSTANCE = new MongoDB();

  private final Datastore datastore;

  private MongoDB() {
    MongoClientOptions mongoOptions = MongoClientOptions.builder()
	.socketTimeout(
	    60000) // How long should we wait for a query to finish? 1m should be sufficient (http://blog.mongolab.com/2013/10/do-you-want-a-timeout/ and https://jira.mongodb.org/browse/JAVA-1076)
	.connectTimeout(
	    15000) // When establishing the initial connection, try for 15s (http://blog.mongolab.com/2013/10/do-you-want-a-timeout/)
	.maxConnectionIdleTime(
	    600000) // Keep idle connections only for 10m, so we don't run into failed connections long after a failover
	.readPreference(ReadPreference
			    .primaryPreferred()) // Read from the primary, but if it is not available, use a secondary instead
	.build();
    MongoClient mongoClient;
    try {
      mongoClient = new MongoClient(new ServerAddress("127.0.0.1", DB_PORT), mongoOptions);
    } catch (UnknownHostException e) {
      throw new RuntimeException("Error initializing MongoDB", e);
    }

    mongoClient.setWriteConcern(WriteConcern.SAFE);
    datastore = new Morphia().mapPackage(BaseEntity.class.getPackage().getName())
	.createDatastore(mongoClient, DB_NAME);
    datastore.ensureIndexes();
    LOG.info("Connection to database '" + DB_NAME + "' initialized");
  }

  public static MongoDB instance() {
    return INSTANCE;
  }

  // Creating the mongo connection is expensive - (re)use a singleton for performance reasons.
  // Both the underlying Java driver and Datastore are thread safe.
  public Datastore getDatabase() {
    return datastore;
  }
}
