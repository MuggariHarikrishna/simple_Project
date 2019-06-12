package com.resource;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class MongoManager {
    MongoClient mongoClient;

    public MongoManager(MongoClient mongoClient){
        this.mongoClient=mongoClient;
    }

    public void registerConsumer(Vertx vertx) {
        vertx.eventBus().consumer("com.orders.mongoservice", message -> {

            System.out.println("Recevied message: " + message.body());

            JsonObject jsonObject=new JsonObject(message.body().toString());

            String key=jsonObject.getString("cmd");

            message.reply(new JsonObject().put("name","lenovo"));
//            if(key.equalsIgnoreCase("findAll")){
//
//                System.out.println("in findALl Consumer :: ");
//                message.reply(new JsonObject().put("name","lenovo"));
//
//            }else if(key.equalsIgnoreCase("findById")){
//
//            }else if(key.equalsIgnoreCase("create")){
//
//            }else if(key.equalsIgnoreCase("update")){
//
//            }else{
//
//            }
        });
    }
}
