package org.wisdom.mongodb;

import com.mongodb.*;

import com.sun.corba.se.impl.orbutil.concurrent.Sync;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.wisdom.api.concurrent.ManagedFutureTask;
import org.wisdom.api.concurrent.ManagedScheduledExecutorService;

import java.net.UnknownHostException;
import java.util.concurrent.*;

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

    private DB db;
    private ServiceRegistration<DB> reg;
    private final BundleContext bundleContext;

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);


    public MongoDataBase(final BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        System.out.println("AM I HERE?");

    }


    protected void invalidate(){

    }

    @Validate
    void start(){
        System.out.println("I do stuff" +reg);

        //runs forever?
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try{
                    System.out.println("inside run");
                    check();
                    registerIfNeeded();

                } catch(Exception e){
                    unRegisterIfNeeded();
                }
            }
        };


        //comment this out to stop run
        final ScheduledFuture<?> handler = scheduler.scheduleWithFixedDelay(runnable,1,1, TimeUnit.SECONDS);
        System.out.println("after run" +reg);
        openMongoConnection();
        stop();
        //doesnt stop?
        handler.cancel(true);
        System.out.println("grrrrr");

    }

    @Invalidate
    void stop(){
        System.out.println("I do nothing still");


    }
    private void openMongoConnection(){
        MongoClient mongoClient = null;
        try {
            mongoClient = new MongoClient(confHost);
            db = mongoClient.getDB(confMongoDB);
            System.out.println(db.getCollectionNames());



        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.out.println("SOMETHING HAS GONE WRONG");
        }
     //   System.out.println("I AM HERE");


      //  MongoClientOptions options = MongoClientOptions.builder().build();
       // WriteConcern c = new WriteConcern();
        //MongoClientURI uri = new MongoClientURI("");




    }



    private synchronized void unRegisterIfNeeded() {

        if(reg!= null){
            reg.unregister();
            reg = null;
        }

    }

    private synchronized void registerIfNeeded() {
        System.out.println("inside register");
        if(reg == null){
            reg=bundleContext.registerService(DB.class,db,null);
        }
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
