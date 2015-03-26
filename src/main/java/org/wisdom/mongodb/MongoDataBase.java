package org.wisdom.mongodb;

import com.mongodb.*;

import com.sun.corba.se.impl.orbutil.concurrent.Sync;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;

import java.net.UnknownHostException;

/**
 * Created by jennifer on 3/26/15.
 */
@Component
@Provides
public class MongoDataBase {


    @Property(name ="hostname",mandatory = true)
    private  String confHost;
    @Property(name ="confPort")
    private  String confPort;
    @Property(name ="confUser")
    private  String confUser;
    @Property(name ="confPwd")
    private  String confPwd;
    @Property(name ="confMongoSafe",value = "true")
    private  boolean confMongoSafe;
    @Property(name ="confMongoJ",value= "true")
    private  boolean confMongoJ;
    @Property(name="dbname")
    private String confMongoDB;



    private final BundleContext bundleContext;

    public MongoDataBase(final BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        System.out.println("AM I HERE?");

    }


    protected void invalidate(){

    }

    @Validate
    void start(){
        System.out.println("I do stuff");
        openMongoConnection();
        stop();

    }

    @Invalidate
    void stop(){
        System.out.println("I do nothing still");
    }
    private void openMongoConnection(){
        MongoClient mongoClient = null;
        try {
            mongoClient = new MongoClient(confHost);
            DB db = mongoClient.getDB(confMongoDB);
            System.out.println(db.getCollectionNames());
            System.out.println("info: "+db.requestEnsureConnection());


        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.out.println("SOMETHING HAS GONE WRONG");
        }
     //   System.out.println("I AM HERE");


      //  MongoClientOptions options = MongoClientOptions.builder().build();
       // WriteConcern c = new WriteConcern();
        //MongoClientURI uri = new MongoClientURI("");




    }

    public void run(){
        try{
            check();
            registerIfNeeded();

        } catch(Exception e){
            unRegisterIfNeeded();
        }
    }

    private void unRegisterIfNeeded() {

    }

    private void registerIfNeeded() {

    }

    private void check() {

    }

    private void closeMongoConnection(){

    }

    private void registerMongoService(){

    }
    private  void unregisterMongoService(){

    }
    private boolean checkMongoAvailability(){
        //use runnable thrhead stuff from wisdom see docs
        return Boolean.parseBoolean(null);
    }

    private void buildServiceProperty(){

    }
    private void address(){

    }
    private void getMongoConfigurationFromAgent(){}
    private void extractMongoConfigurationFromNode(){}
    private void safeClose(){}

}
