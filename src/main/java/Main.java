import java.io.*;
import java.net.ServerSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    public static final Logger LOG = LoggerFactory.getLogger("ServerLogger");

    public static void main(String[] args) {
        if (args.length != Server.ARGS_NUMBER_REQUIRED) {
            LOG.error("You didn't enter the port! (1 argument)");
            return;
        }

        try {
            var serverSocket = new ServerSocket(Integer.parseInt(args[0]));

            while (!serverSocket.isClosed()) {
                var newConnection = serverSocket.accept();
                LOG.info("New connection has arrived, download task given to available thread!");

                Server.threadPool.execute(() -> {
                    try {
                        Server.downloadFile(newConnection);
                    } catch (IOException e) {
                        LOG.error("Error while downloading file from some socket!!!");
                    }
                });
            }
        } catch (IOException e) {
            LOG.error("Can't setup server on port " + args[0]);
        } finally {
            Server.threadPool.shutdown();
        }
    }
}