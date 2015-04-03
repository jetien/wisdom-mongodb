package org.wisdom.mongodb;

import com.mongodb.DB;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.io.IOException;
import java.util.Dictionary;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class MongoDBClientTest {

    private MongodStarter starter;
    private IMongodConfig mongodConfig;
    private MongodExecutable mongodExecutable;
    private MongodProcess mongod;

    private static int port;

    @BeforeClass
    public static void retrieveAFreePort() throws IOException {
        port = Network.getFreeServerPort();
    }

    @Before
    public void startMongo() throws IOException {
        starter = MongodStarter.getDefaultInstance();
        mongodConfig = new MongodConfigBuilder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(port, Network.localhostIsIPv6()))
                .build();
        mongodExecutable = starter.prepare(mongodConfig);
        mongod = mongodExecutable.start();
    }

    @After
    public void stopMongo() {
        if (mongod != null){
            mongod.stop();
        }
        if (mongodExecutable != null) {
            mongodExecutable.stop();
        }
    }


    @Test
    public void testConnection() throws InterruptedException {
        BundleContext context = mock(BundleContext.class);
        MongoDBClient client = new MongoDBClient(context);
        client.executors = Executors.newSingleThreadScheduledExecutor();
        client.mongoDbHost = "localhost";
        client.mongoDbPort = port;
        client.mongoDbName = "wisdom-test";
        client.heartbeatPeriod = 1;
        client.datasources = new String[0];
        client.connectionsPerHost = 2;
        client.start();
        await().atMost(5, TimeUnit.SECONDS).until(() -> client.database != null);
        assertThat(client.database).isNotNull();
        assertThat(client.database.getCollectionNames()).isNotNull();
        client.stop();
    }

    @Test
    public void testMonitoring() throws InterruptedException, IOException {
        BundleContext context = mock(BundleContext.class);
        ServiceRegistration registration = mock(ServiceRegistration.class);
        when(context.registerService(any(Class.class), any(DB.class),
                any(Dictionary.class))).thenReturn(registration);
        final MongoDBClient client = new MongoDBClient(context);
        client.executors = Executors.newSingleThreadScheduledExecutor();
        client.mongoDbHost = "localhost";
        client.mongoDbPort = port;
        client.mongoDbName = "wisdom-test";
        client.heartbeatPeriod = 3;
        client.connectionsPerHost = 2;
        client.datasources = new String[] {"cat", "dog"};
        client.start();

        await().atMost(15, TimeUnit.SECONDS).until(() -> client.database != null);
        assertThat(client.database).isNotNull();
        assertThat(client.database.getCollectionNames()).isNotNull();
        verify(context).registerService(any(Class.class), any(DB.class),
                any(Dictionary.class));
        // Stop the database
        stopMongo();

        await().atMost(15, TimeUnit.SECONDS).until(() -> client.database == null);
        assertThat(client.database).isNull();
        verify(registration).unregister();

        // Restart the database
        startMongo();

        await().atMost(15, TimeUnit.SECONDS).until(() -> client.database != null);
        assertThat(client.database).isNotNull();
        verify(context, times(2))
                .registerService(any(Class.class), any(DB.class), any(Dictionary.class));
        client.stop();
        verify(registration, times(2)).unregister();
    }

}