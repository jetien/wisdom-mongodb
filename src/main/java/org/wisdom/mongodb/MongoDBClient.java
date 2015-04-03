package org.wisdom.mongodb;

import com.mongodb.*;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.concurrent.ManagedScheduledExecutorService;

import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Component exposing {@link DB} services and responsible for the tracking of availability.
 */
@Component
public class MongoDBClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBClient.class);

    // Connection information

    @Property(name = "hostname", mandatory = true)
    String mongoDbHost;
    @Property(name = "port")
    int mongoDbPort;
    @Property(name = "user")
    String mongDbUser;
    @Property(name = "pwd")
    String mongoDbPwd;
    @Property(name = "dbname", mandatory = true)
    String mongoDbName;


    //TODO Check these:
    @Property(name = "confMongoSafe", value = "true")
    boolean confMongoSafe;
    @Property(name = "confMongoJ", value = "false")
    boolean confMongoJ;
    @Property(name = "confMongoFsync", value = "false")
    boolean confMongoFsync;
    @Property(name = "confMongoW", value = "1")
    int confMongoW;
    @Property(name = "confMongoWTimeout", value = "0")
    int confMongoWTimeout;

    @Property(name = "autoConnectRetry", value = "true")
    boolean autoConRetry;
    @Property(name="connectTimeout", value="5000")
    int connectTimeout;
    @Property(name="maxAutoConnectRetryTime", value="100")
    int maxAutoConnectRetryTime;
    @Property(name="connectionsPerHost", value="2")
    int connectionsPerHost;
    @Property(name="descripption")
    String description;
    @Property (name="maxWaitTime" ,value="5000")
    int maxWaitTime;



    /**
     * The data source names to exposed alongside the service.
     */
    @Property(mandatory = false, name = "datasources")
    String[] datasources;

    /**
     * The time period in second for the heatbeat checkng the availability of the mongodb server.
     */
    @Property(name = "heartbeat", value = "5")
    protected long heartbeatPeriod;

    @Requires(filter = "(name=" + ManagedScheduledExecutorService.SYSTEM + ")", proxy = false)
    ScheduledExecutorService executors;

    protected DB database;

    private ServiceRegistration<DB> reg;

    private final BundleContext bundleContext;

    private ScheduledFuture<?> heartbeat;
    private MongoClient mongoClient;


    /**
     * Constructor.
     *
     * @param bundleContext injected by ipojo.
     */
    public MongoDBClient(final BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    /**
     * The method that is called when the component is validated by ipojo.
     */
    @Validate
    void start() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    if (ping()) {
                        registerIfNeeded();
                    } else {
                        unregisterIfNeeded();
                    }
                } catch (Exception e) {
                    LOGGER.error("Error while detecting the availability of {} ({})", mongoDbHost, mongoDbName, e);
                }
            }
        };
        // The first execution opens the connection if possible.
        heartbeat = executors.scheduleAtFixedRate(runnable, 0L, heartbeatPeriod, TimeUnit.SECONDS);
    }

    /**
     * Invalidates the component, unregister the instance if it exists and closes the database connection if it alive.
     */
    @Invalidate
    void stop() {
        // We must start by cancelling the heartbeat, as otherwise it could potentially re-register the service.
        heartbeat.cancel(true);
        unregisterIfNeeded();
    }

    /**
     * Open a connection with a mongo database. 4 different configurations are currently available.
     */
    private void openMongoConnection() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
        }
        ServerAddress address = createAddress();
        MongoCredential credential = createMongoCredential();

        //TODO all the aspects should be configurable.
        final MongoClientOptions options = MongoClientOptions.builder()
                .autoConnectRetry(autoConRetry)
                .connectTimeout(connectTimeout)
                .maxAutoConnectRetryTime(maxAutoConnectRetryTime)
                .writeConcern(new WriteConcern(confMongoW, confMongoWTimeout, confMongoFsync, confMongoJ))
                .connectionsPerHost(connectionsPerHost)
                .description(description)
                .maxWaitTime(maxWaitTime)
                .build();

        if (credential != null) {
            mongoClient = new MongoClient(address, Collections.singletonList(credential), options);
        } else {
            mongoClient = new MongoClient(address, options);
        }


    }

    /**
     * Per the documentation of Mongo API the UnknownHost Exception is deprecated and will be removed in the next driver.
     * we currently use driver 2.13.0
     *
     * @return the server address
     */
    private ServerAddress createAddress() {
        try {
            if (mongoDbPort == 0) {
                return new ServerAddress(mongoDbHost);
            }
            return new ServerAddress(mongoDbHost, mongoDbPort);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Cannot connect to MongoDB server", e);
        }
    }

    /**
     * Creates a MongoCredential instance with an unspecified mechanism.
     *
     * @return the credential, {@code null} if not set
     */
    private MongoCredential createMongoCredential() {
        if (mongDbUser != null) {
            return MongoCredential.createMongoCRCredential(mongoDbName, mongoDbHost, mongoDbPwd.toCharArray());
        }
        return null;
    }

    /**
     * Unregister the DB database service provided by this component.
     */
    private synchronized void unregisterIfNeeded() {
        if (reg != null) {
            reg.unregister();
            reg = null;
        }
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
        }
        database = null;
    }

    /**
     * Registers the DB database as a service of this component. If the connection is not open, it opens it.
     */
    private synchronized void registerIfNeeded() {
        if (mongoClient == null) {
            openMongoConnection();
        }
        if (reg == null) {
            reg = bundleContext.registerService(DB.class, database,
                    buildServiceProperty());
        }
    }

    /**
     * Check if the database connection is still alive.     *
     *
     * @return true if the connection is still alive, otherwise return false.
     */
    private synchronized boolean ping() {
        try {
            if (mongoClient == null) {
                openMongoConnection();
            }
            mongoClient.getDatabaseNames();
            if (database == null) {
                database = mongoClient.getDB(mongoDbName);
            }
            return true;
        } catch (Exception e) {
            LOGGER.warn("Cannot connect to database {} at {}:{}", mongoDbName, mongoDbHost, mongoDbPort, e);
        }
        return false;
    }

    /**
     * @return a list of properties associated with the Mongo Client
     */
    private Dictionary<String, ?> buildServiceProperty() {
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put("host", mongoDbHost);
        properties.put("port", mongoDbPort);
        properties.put("name", mongoDbName);
        properties.put("datasources", datasources);
        LOGGER.info("Props " + properties);
        return properties;
    }

}
