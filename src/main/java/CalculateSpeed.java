
import java.io.File;

public class CalculateSpeed implements Runnable {
    private static final double TIMER_TASK_DELAY = 3;
    private long previousSize = 0;
    private final File file;

    public CalculateSpeed(File file) {
        this.file = file;
    }

    @Override
    public void run() {
        long fileSize = file.length();
        System.out.println(file.getName() + " speed = " + (double) (fileSize - previousSize) / TIMER_TASK_DELAY + " bytes");
        previousSize = fileSize;
    }
}