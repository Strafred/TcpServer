
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class Server {
    private static final int ARGS_NUMBER_REQUIRED = 1;
    private static final int SOCKET_BUFFER_SIZE = 65536;
    private static final int TIMER_TASK_DELAY = 3;
    private static final String UPLOADS_DIRECTORY = "C:\\Users\\bred7\\IdeaProjects\\Server\\uploads\\";

    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    private static void downloadFile(Socket socket) throws IOException {
        var scheduledThreadPool = Executors.newScheduledThreadPool(1);
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

        String fileName = dataInputStream.readUTF();
        Long fileSize = dataInputStream.readLong();
        String extension = dataInputStream.readUTF();

        String filePath = UPLOADS_DIRECTORY + fileName;
        File file = new File(filePath);
        Files.createFile(file.toPath());

        FileOutputStream fileOutputStream = new FileOutputStream(file.getPath());

        byte[] receiveBuffer = new byte[socket.getReceiveBufferSize()];
        int count;

        CalculateSpeed calculateSpeed = new CalculateSpeed(file);
        scheduledThreadPool.scheduleAtFixedRate(calculateSpeed, TIMER_TASK_DELAY, TIMER_TASK_DELAY, TimeUnit.NANOSECONDS);
        do {
            count = dataInputStream.read(receiveBuffer);
            fileOutputStream.write(receiveBuffer, 0, count);
        } while (count >= SOCKET_BUFFER_SIZE);
        scheduledThreadPool.shutdown();

        if (file.length() == fileSize) {
            dataOutputStream.writeUTF("SUCCESS");
        } else {
            dataOutputStream.writeUTF("FAILURE");
        }

        socket.shutdownInput();
        socket.shutdownOutput();
        dataInputStream.close();
        dataOutputStream.close();
        fileOutputStream.close();
    }

    public static void main(String[] args) {
        if (args.length != ARGS_NUMBER_REQUIRED) {
            System.err.println("Enter the port!");
            return;
        }

        try {
            var serverSocket = new ServerSocket(Integer.parseInt(args[0]));
            while (!serverSocket.isClosed()) {
                var newConnection = serverSocket.accept();
                threadPool.execute(() -> {
                    try {
                        downloadFile(newConnection);
                    } catch (IOException e) {
                        System.err.println("Error while downloading the file from some socket!");
                    }
                });
            }
        } catch (IOException e) {
            System.err.println("Can't setup the server on this port!");
        } finally {
            threadPool.shutdown();
        }
    }
}