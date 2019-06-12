package com;

import com.resource.MongoManager;
import com.resource.OrdersManagement;
import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;

public class MongoVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoVerticle.class);
    public static MongoClient mongoClient=null;
    private MongoManager mongoManager;

    public static void main(String[] args) {

        System.out.println("MongoVerticle Main()");
        VertxOptions vertxOptions = new VertxOptions();
        vertxOptions.setClustered(true);

        Vertx.clusteredVertx(vertxOptions, vertxAsyncResult -> {
            if (vertxAsyncResult.succeeded()) {
                Vertx vertx = vertxAsyncResult.result();
                ConfigRetriever configRetriever = ConfigRetriever.create(vertx);
                configRetriever.getConfig(jsonObjectAsyncResult -> {
                    if (jsonObjectAsyncResult.succeeded()) {
                        JsonObject jsonObject = jsonObjectAsyncResult.result();

                        DeploymentOptions deploymentOptions = new DeploymentOptions().setConfig(jsonObject);
                        vertx.deployVerticle(new MongoVerticle(), deploymentOptions);
                    } else {
                        LOGGER.info("json Retrieval Failed");
                    }
                });
            }
        });

    }

    @Override
    public void start() throws Exception {
        System.out.println("MongoVerticle Start()");
        JsonObject dbObject=new JsonObject();

        dbObject.put("connection_string", "mongodb://" + config().getString("mongodb.host") + ":" + config().getInteger("mongodb.port") + "/" + config().getString("mongodb.databasename"));
        dbObject.put("useObjectId", true);
        mongoClient=MongoClient.createShared(vertx,dbObject);
        mongoManager=new MongoManager(mongoClient);
        mongoManager.registerConsumer(vertx);
    }
}
