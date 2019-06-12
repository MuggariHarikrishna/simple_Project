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
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.StaticHandler;


public class MainVerticle extends AbstractVerticle {

    private final static Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);
    private OrdersManagement orders=null;
    public MongoClient mongoClient=null;

    public static void main(String[] args) {
        System.out.println("Main Method()");


        VertxOptions vertxOptions = new VertxOptions();

        vertxOptions.setClustered(true);

        Vertx.clusteredVertx(vertxOptions, results -> {

            if (results.succeeded()) {

                Vertx vertx = results.result();
                ConfigRetriever configRetriever = ConfigRetriever.create(vertx);
                configRetriever.getConfig(jsonObjectAsyncResult -> {
                    if (jsonObjectAsyncResult.succeeded()) {
                        JsonObject jsonObject = jsonObjectAsyncResult.result();

                        DeploymentOptions deploymentOptions = new DeploymentOptions().setConfig(jsonObject);
                        vertx.deployVerticle(new MainVerticle(), deploymentOptions);
                    } else {
                        LOGGER.info("json Retrieval Failed");
                    }
                });
            }
        });


    }


    @Override
    public void start() throws Exception {
        LOGGER.info("MainVerticle start Method()");
        Router router = Router.router(vertx);
        router.route().handler(CookieHandler.create());

        JsonObject dbObject=new JsonObject();

        dbObject.put("connection_string", "mongodb://" + config().getString("mongodb.host") + ":" + config().getInteger("mongodb.port") + "/" + config().getString("mongodb.databasename"));
        dbObject.put("useObjectId", true);
        mongoClient= MongoClient.createShared(vertx,dbObject);

        orders = new OrdersManagement(mongoClient);
        router.mountSubRouter("/api/", orders.ApiSubRouter(vertx));

        router.route().handler(StaticHandler.create().setCachingEnabled(false));

        vertx.createHttpServer().requestHandler(router::accept).listen(3000, httpServerAsyncResult -> {
            if (httpServerAsyncResult.succeeded()) {
                LOGGER.info("HTTP Server Listening in :: 3000");
            } else {
                LOGGER.info("HTTP Server Starting Failure");
            }
        });

    }
}
