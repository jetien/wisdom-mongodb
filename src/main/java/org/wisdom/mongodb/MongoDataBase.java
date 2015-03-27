package org.wisdom.mongodb;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.apache.commons.io.IOUtils;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.concurrent.ManagedScheduledExecutorService;

import java.io.Closeable;
import java.net.UnknownHostException;
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
    private String confHost;
    @Property(name = "port")
    private String confPort;
    @Property(name = "user")
    private String confUser;
    @Property(name = "pwd")
    private String confPwd;
    //TODO Check these:
    @Property(name = "confMongoSafe", value = "true")
    private boolean confMongoSafe;
    @Property(name = "confMongoJ", value = "true")
    private boolean confMongoJ;

    @Property(name = "name")
    private String confMongoDB;

    // TODO Add the set of collections.

    @Property(name = "heartbeat", value = "5")
    private long heartbeatPeriod;

    private DB database;
    private ServiceRegistration<DB> reg;
    private final BundleContext bundleContext;

    @Requires(filter = "(name=" + ManagedScheduledExecutorService.SYSTEM + ")")
    ManagedScheduledExecutorService executors;
    private ScheduledFuture<?> heartbeat;
    private MongoClient mongoClient;


    public MongoDataBase(final BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        System.out.println("AM I HERE?");

    }

    @Validate
    void start() {
        if (confMongoDB == null) {
            // Use host and port
            confMongoDB = confHost + ":" + confPort;
        }

        //runs forever?
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                System.out.println("inside run");
                if (ping()) {
                    registerIfNeeded();
                } else {
                    unregisterIfNeeded();
                }
            }
        };

        //comment this out to stop run
        heartbeat = executors.scheduleAtFixedRate(runnable, 0l, heartbeatPeriod, TimeUnit.SECONDS);
        System.out.println("after run" + reg);
        stop();
        System.out.println("grrrrr");

    }

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

    private void openMongoConnection() {
        mongoClient = null;
        try {
            mongoClient = new MongoClient(confHost);
            database = mongoClient.getDB(confMongoDB);
            System.out.println(database.getCollectionNames());
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.out.println("SOMETHING HAS GONE WRONG");
        }
        //   System.out.println("I AM HERE");


        //  MongoClientOptions options = MongoClientOptions.builder().build();
        // WriteConcern c = new WriteConcern();
        //MongoClientURI uri = new MongoClientURI("");


    }


    private synchronized void unregisterIfNeeded() {
        if (reg != null) {
            reg.unregister();
            reg = null;
        }
    }

    private synchronized void registerIfNeeded() {
        System.out.println("inside register");

        if (mongoClient == null) {
            openMongoConnection();
        }

        if (reg == null) {
            reg = bundleContext.registerService(DB.class, database, null);
        }
    }

    private synchronized boolean ping() {
        try {
            if (mongoClient == null) {
                return false;
            }
            mongoClient.getDatabaseNames();
            return true;
        } catch (Exception e) {
            LOGGER.warn("Cannot connect to database {}:{}", confMongoDB, confPort, e);
        }

        return false;
    }

    private Properties buildServiceProperty() {
        // TODO Build a Properties object containing the host, port, and the set of collections.
        return new Properties();
    }

}
