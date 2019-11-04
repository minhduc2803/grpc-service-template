package vn.toanhda;

import io.vertx.core.Vertx;
import vn.toanhda.disruptor.StorageEvent;
import vn.toanhda.config.ServerConfig;
import vn.toanhda.database.SQLClientProvider;
import vn.toanhda.database.SQLClientProviderProviderImpl;
import vn.toanhda.vertx.VertxCommon;
import vn.toanhda.database.SQLClientProviderProviderVertXImpl;
import vn.toanhda.database.SQLClientProviderVertX;
import vn.toanhda.disruptor.DisruptorCreator;
import vn.toanhda.grpc.GrpcServer;
import vn.toanhda.service.PingServiceHandler;
import vn.toanhda.utils.FileConfigLoader;

import java.io.IOException;
import java.sql.SQLException;

public class Main {
  public static void main(String[] args) throws IOException, SQLException {

    ServerConfig serverConfig = FileConfigLoader.load(ServerConfig.class);
    serverConfig.verify();

    // Start disruptor
    DisruptorCreator.getInstance(StorageEvent.EVENT_FACTORY, serverConfig.getDisruptorConfig());

    // Initialize service
    Vertx vertx = VertxCommon.getVertxInstance(serverConfig.getVertxConfig());
    SQLClientProviderVertX clientProviderVertX = new SQLClientProviderProviderVertXImpl(vertx);
    clientProviderVertX.initialize();
    SQLClientProvider clientProvider = new SQLClientProviderProviderImpl();
    PingServiceHandler pingService = new PingServiceHandler(clientProviderVertX, clientProvider);

    // Start gRPC
    GrpcServer grpcServer = new GrpcServer(vertx, pingService);
    grpcServer.start(serverConfig.getGrpcConfig());
  }
}
