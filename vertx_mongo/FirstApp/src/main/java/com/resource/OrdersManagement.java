package com.resource;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.List;

public class OrdersManagement {
    public MongoClient mongoClient=null;
    public OrdersManagement(MongoClient mongoClient){
        this.mongoClient=mongoClient;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(OrdersManagement.class);
    Vertx vertx=null;

    public Router ApiSubRouter(Vertx vertx) {
        Router router = Router.router(vertx);
        this.vertx=vertx;



        router.route("/*").handler(this::authenticate);
        router.route("/orders*").handler(BodyHandler.create());
        router.get("/orders").handler(this::getAllOrders);
        router.get("/orders/:id").handler(this::getOrderById);
        router.post("/orders").handler(this::createOrder);
        router.put("/orders/:id").handler(this::updateOrderById);
        router.delete("/orders/:id").handler(this::deleteOrderById);
        return router;
    }

    public void authenticate(RoutingContext routingContext) {
        String token = routingContext.request().getHeader("authToken");
        if (token == null || !token.equals("123")) {
            LOGGER.info("Authentication Failed...");
            JsonObject errorObject = new JsonObject();
            errorObject.put("Error", "You are Not Authrized to Access");
            routingContext.response().setStatusCode(401).putHeader("content-type", "application/json").end(Json.encodePrettily(errorObject));
        } else {
            LOGGER.info("Authentication SuccessFull");
            routingContext.response().putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            routingContext.response().putHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,PUT,DELETE");
            routingContext.next();
        }
    }

    public void getAllOrders(RoutingContext routingContext) {
        JsonObject jsonObject=new JsonObject();
        jsonObject.put("cmd","findAll");

        LOGGER.info("in all OrdersManagement Finding Route");

        mongoClient.find("orders",new JsonObject(),result->{
           if(result.succeeded()){
               List<JsonObject> jsonResults=result.result();
               routingContext.response().setStatusCode(200).putHeader("content-type","application/json").end(Json.encodePrettily(jsonResults));
           } else{
               System.out.println("Finding All Orders Failed");
               routingContext.response().setStatusCode(500).putHeader("content-type","application/json").end(Json.encodePrettily(new JsonObject().put("status","Fetching results Failed")));
           }
        });

        /*vertx.eventBus().send("com.orders.mongoservice",jsonObject,messageAsyncResult -> {
            if(messageAsyncResult.succeeded()){

                System.out.println("getAll Orders Reply :: "+messageAsyncResult.toString());

            }else{
                System.out.println("error in getAll Orders Reply");
            }
        });*/


    }

    public void getOrderById(RoutingContext routingContext) {
        int key=Integer.parseInt(routingContext.request().getParam("id"));
        JsonObject jsonObject=new JsonObject();
        jsonObject.put("cmd","findById");
        jsonObject.put("id",key);
        LOGGER.info("in findById OrdersManagement Finding Route :: "+key);

        mongoClient.find("orders",new JsonObject().put("id",key),result->{
            if(result.succeeded() && result.result().size()>0){
                JsonObject jsonResult=result.result().get(0);
                routingContext.response().setStatusCode(200).putHeader("content-type","application/json").end(Json.encodePrettily(jsonResult));
            } else{
                System.out.println("Finding All Orders Failed");
                routingContext.response().setStatusCode(500).putHeader("content-type","application/json").end(Json.encodePrettily(new JsonObject().put("status","Order with Id Not Found")));
            }
        });
    }

    public void createOrder(RoutingContext routingContext) {
        JsonObject inputJson=routingContext.getBodyAsJson();
        JsonObject jsonObject=new JsonObject();
        jsonObject.put("cmd","create");
        LOGGER.info("jsonObject :: "+inputJson);
        LOGGER.info("in createOrder Route");

        mongoClient.insert("orders",inputJson,result->{
           if(result.succeeded()){
                routingContext.response().setStatusCode(201).putHeader("content-type","application/json").end(Json.encodePrettily(new JsonObject().put("status","inserted")));
           }else{
                routingContext.response().setStatusCode(500).putHeader("content-type","application/json").end(Json.encodePrettily(new JsonObject().put("status","not inserted")));
           }
        });
    }

    public void updateOrderById(RoutingContext routingContext) {
        int key=Integer.parseInt(routingContext.request().getParam("id"));
        JsonObject jsonObject=new JsonObject();
        jsonObject.put("cmd","update");
        jsonObject.put("id",key);
        JsonObject input;
        input=routingContext.getBodyAsJson();
        LOGGER.info("in updateById OrdersManagement Finding Route :: "+key);

        mongoClient.updateCollection("orders",new JsonObject().put("id",key),new JsonObject().put("$set",input),result->{
            if(result.succeeded()){
                routingContext.response().setStatusCode(202).putHeader("content-type","application/json").end(Json.encodePrettily(new JsonObject().put("status","updated")));
            }
            else{
                routingContext.response().setStatusCode(500).putHeader("content-type","application/json").end(Json.encodePrettily(new JsonObject().put("status","update failed")));
            }
        });
    }

    public void deleteOrderById(RoutingContext routingContext) {
        int key=Integer.parseInt(routingContext.request().getParam("id"));
        JsonObject jsonObject=new JsonObject();
        jsonObject.put("cmd","delete");
        jsonObject.put("id",key);
        LOGGER.info("in deleteById OrdersManagement Finding Route  ID :: "+key);

        mongoClient.findOneAndDelete("orders",new JsonObject().put("id",key),result->{
            if(result.succeeded()){
                routingContext.response().setStatusCode(200).putHeader("content-type","application/json").end(Json.encodePrettily(new JsonObject().put("status","deleted")));
            }
            else{
                routingContext.response().setStatusCode(500).putHeader("content-type","application/json").end(Json.encodePrettily(new JsonObject().put("status","delete fail")));
            }
        });
    }


}
