package org.wisdom.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.ServiceReference;
import org.ow2.chameleon.testing.helpers.OSGiHelper;
import org.wisdom.test.parents.WisdomTest;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Check the behavior of the Mongo DB Client.
 */
public class MongoDBClientIT extends WisdomTest {

    private OSGiHelper osgi;

    @Before
    public void setUp() {
        osgi = new OSGiHelper(context);
    }

    @After
    public void after() {
        osgi.dispose();
    }

    @Test
    public void testOSGiService() {
        DB db = osgi.waitForService(DB.class, null, 5000);
        ServiceReference reference = osgi.getServiceReference(DB.class);
        // Check properties
        assertThat(reference.getProperty("host")).isEqualTo("localhost");
        assertThat(reference.getProperty("port")).isNotNull();
        assertThat(reference.getProperty("name")).isEqualTo("kitten");
        assertThat((String[]) reference.getProperty("datasources")).contains("test").hasSize(1);

        // Check Mongo
        DBCollection col = db.getCollection("testCol");
        if (col == null) {
            col = db.createCollection("testCol", new BasicDBObject());
        }
        col.save(new BasicDBObject("testDoc", new Date()));
    }

}
