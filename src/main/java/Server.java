import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {
    public static final int ARGS_NUMBER_REQUIRED = 1;
    private static final int SOCKET_BUFFER_SIZE = 65536;
    private static final int TIMER_TASK_DELAY = 3;
    private static final String UPLOADS_DIRECTORY = System.getProperty("user.dir") + "\\UPLOADS\\";

    public static final ExecutorService threadPool = Executors.newCachedThreadPool();

    public static void downloadFile(Socket socket) throws IOException {
        var scheduledThreadPool = Executors.newScheduledThreadPool(1);

        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

        String fileName = dataInputStream.readUTF();
        Long fileSize = dataInputStream.readLong();

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
            Main.LOG.info("File " + fileName + " downloaded successfully!");
            dataOutputStream.writeUTF("SUCCESS");
        } else {
            Main.LOG.error("Failure during download of " + fileName + ". File may be broken!!!");
            dataOutputStream.writeUTF("FAILURE");
        }

        socket.shutdownInput();
        socket.shutdownOutput();
        dataInputStream.close();
        dataOutputStream.close();
        fileOutputStream.close();
    }
}