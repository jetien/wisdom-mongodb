package org.wisdom.mongodb;

import com.mongodb.*;
import org.apache.commons.io.IOUtils;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.concurrent.ManagedScheduledExecutorService;

import java.io.Closeable;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by jennifer on 3/26/15.
 */
@Component
@Provides
public class MongoDataBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDataBase.class);

    @Property(name = "hostname", mandatory = true)
    private String mongoDbHost;
    @Property(name = "port")
    private String mongoDbPort;
    @Property(name = "user")
    private String mongDbUser;
    @Property(name = "pwd")
    private String mongoDbPwd;
    //TODO Check these:
    @Property(name = "confMongoSafe", value = "true")
    private boolean confMongoSafe;
    @Property(name = "confMongoJ", value = "true")
    private boolean confMongoJ;
    @Property(name = "dbname", mandatory = true)
    private String mongoDbName;
    // TODO Add the set of collections.
    @Property(mandatory = false)
    private DBCollection collectionSet;

    @Property(name = "heartbeat", value = "5")
    private long heartbeatPeriod;

    private DB database;
    private ServiceRegistration<DB> reg;
    private final BundleContext bundleContext;

    @Requires(filter = "(name=" + ManagedScheduledExecutorService.SYSTEM + ")", proxy = false)
    ManagedScheduledExecutorService executors;
    private ScheduledFuture<?> heartbeat;
    private MongoClient mongoClient;


    /**
     * Constructor.
     *
     * @param bundleContext injected by ipojo.
     */
    public MongoDataBase(final BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    /**
     * The method that is called when the component is validated by ipojo.
     */
    @Validate
    void start() {
        openMongoConnection();
        //runs forever?
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (ping()) {
                    registerIfNeeded();
                } else {
                    unregisterIfNeeded();
                }
            }
        };

        heartbeat = executors.scheduleAtFixedRate(runnable, 0L, heartbeatPeriod, TimeUnit.SECONDS);
        // stop();
    }

    /**
     * Invalidates the component, unregister the instance if it exists and closes the database connection if it alive.
     */
    @Invalidate
    void stop() {
        unregisterIfNeeded();
        heartbeat.cancel(true);
        IOUtils.closeQuietly(new Closeable() {
            @Override
            public void close() {
                try {
                    if (mongoClient != null) {
                        mongoClient.close();
                    }
                } catch (Exception e) {
                    // Ignored.
                }
            }
        });
    }

    /**
     * Open a connection with a mongo database. 4 different configurations are currently available.
     */
    private void openMongoConnection() {
        //TODO delete comments when finished
        mongoClient = null;
        //create a new mango client
        if (createAddress() != null && !createMongoCredential().isEmpty() && createMongoClientOptions() != null) {
           // System.out.println("option 1");
            mongoClient = new MongoClient(createAddress(), createMongoCredential(), createMongoClientOptions());
        } else if (createAddress() != null && createMongoCredential().isEmpty() && createMongoClientOptions() != null) {
          //  System.out.println("option 2");
            mongoClient = new MongoClient(createAddress(), createMongoClientOptions());
        } else if (createAddress() != null && !createMongoCredential().isEmpty() && createMongoClientOptions() == null) {
          //  System.out.println("option 3");
            mongoClient = new MongoClient(createAddress(), createMongoCredential());
        } else {
          //  System.out.println("option 4");
            mongoClient = new MongoClient(createAddress());
        }
        //switch to correct db
        database = mongoClient.getDB(mongoDbName);
        LOGGER.info("Mongo Database Connection created for {}", database);
    }

    /**
     * Per the documentation of Mongo API the UnknownHost Exception is deprecated and will be removed in the next driver.
     * we currently use driver 2.13.
     *
     * @return
     */
    private ServerAddress createAddress() {

        try {

            if (mongoDbPort == null) {
                return new ServerAddress(mongoDbHost);
            }
            //TODO could be an error is port isnt a number
            return new ServerAddress(mongoDbHost, Integer.valueOf(mongoDbPort));

        } catch (UnknownHostException e) {
            LOGGER.warn("Mongo DB Unknown Host Error {}:{}", mongoDbHost, mongoDbPort, e);

        }
        //ugly
        return null;
    }

    /**
     * Creates a MongoCredential instance with an unspecified mechanism.
     *
     * @return
     */
    private List<MongoCredential> createMongoCredential() {
        List<MongoCredential> credList = new ArrayList<MongoCredential>();
        if (mongDbUser != null && mongoDbPwd != null) {

            credList.add(MongoCredential.createCredential(mongoDbName, mongoDbHost, mongoDbPwd.toCharArray()));
        }
        return credList;
    }

    /**
     * @return
     */
    private MongoClientOptions createMongoClientOptions() {
        //TODO should be used with and journal options? if they still exsist.
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
    }

    /**
     * Register the DB database as a service of this component.
     */
    private synchronized void registerIfNeeded() {
        if (mongoClient == null) {
            openMongoConnection();
        }

        if (reg == null) {
            reg = bundleContext.registerService(DB.class, database, null);
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
                return false;
            }
            mongoClient.getDatabaseNames();
            return true;
        } catch (Exception e) {
            LOGGER.warn("Cannot connect to database {} at {}:{}", mongoDbName, mongoDbHost, mongoDbPort, e);
        }

        return false;
    }

    /**
     * @return
     */
    private Properties buildServiceProperty() {
        // TODO Build a Properties object containing the host, port, and the set of collections.
        return new Properties();
    }

}
