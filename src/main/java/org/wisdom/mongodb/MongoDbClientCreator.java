package org.wisdom.mongodb;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.configuration.Configuration;

import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Instantiates {@link MongoDBClient} from the Wisdom Application Configuration.
 */
@Component
@Instantiate
public class MongoDbClientCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDbClientCreator.class);

    @Requires(filter = "(configuration.name=mongodb)")
    protected Configuration configuration;

    @Requires(filter = "(factory.name=org.wisdom.mongodb.MongoDBClient)")
    protected Factory factory;

    protected Set<ComponentInstance> instances = new LinkedHashSet<>();

    @Validate
    public void start() {
        Set<String> keys = configuration.asMap().keySet();
        for (String key : keys) {
            createInstance(key, configuration.getConfiguration(key));
        }
    }

    private void createInstance(String key, Configuration configuration) {
        try {
            final Properties properties = configuration.asProperties();
            if (! properties.containsKey("datasources")) {
                properties.put("datasources", new String[] { key });
            }
            factory.createComponentInstance(properties);
        } catch (Exception e) {
            LOGGER.error("Cannot create the MongoDB Client for {} ({})", key, configuration, e);
        }
    }


    @Invalidate
    public void stop() {
        for (ComponentInstance instance : instances) {
            instance.dispose();
        }
        instances.clear();
    }

}
