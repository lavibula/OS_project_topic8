package topic8.os_project_topic8;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Semaphore;

public class BarberShop {
    private static final int MAX_CUSTOMERS = 50;
    private static final int OPENING_HOUR = 8;
    private static final int CLOSING_HOUR = 19;
    private static final int LUNCH_START_HOUR = 12;
    private static final int LUNCH_END_HOUR = 13;

    private Semaphore barberSemaphore;
    private Semaphore customerSemaphore;
    private Semaphore mutex;
    private List<Integer> waitingCustomers;
    private int numChairs;
    private TextArea simulationTextArea;  // Reference to the TextArea
    private List<Integer> servedCustomers;
    private List<Integer> remainingCustomers;


    public BarberShop(int numChairs) {
        barberSemaphore = new Semaphore(0);
        customerSemaphore = new Semaphore(0);
        mutex = new Semaphore(1);
        waitingCustomers = new ArrayList<>();
        this.numChairs = numChairs;  // Default value
        servedCustomers = new ArrayList<>();
        remainingCustomers = new ArrayList<>();
        for (int i = 0; i < MAX_CUSTOMERS; i++) {
            remainingCustomers.add(i);
        }
    }

    public void runBarberShop(TextArea simulationTextArea) {
        this.simulationTextArea = simulationTextArea;  // Set the reference to the TextArea

        Thread barberThread = new Thread(this::barber);
        List<Thread> customerThreads = new ArrayList<>();

        barberThread.start();

        for (int i = 0; i < MAX_CUSTOMERS; i++) {
            int index = i;
            Thread customerThread = new Thread(() -> customer(index));
            customerThreads.add(customerThread);
            customerThread.start();

            try {
                Thread.sleep(getRandomTime());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            for (Thread customerThread : customerThreads) {
                customerThread.join();
            }

            barberThread.interrupt();  // Interrupt the barber thread to stop the loop
            barberThread.join();  // Wait for the barber thread to complete
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void updateSimulationTextArea(String message) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String currentTime = dateFormat.format(new Date());
        Platform.runLater(() -> simulationTextArea.appendText("[" + currentTime + "] " + message));
    }

    private boolean isShopOpen(int currentHour) {
        return currentHour >= OPENING_HOUR && currentHour < CLOSING_HOUR
                && !(currentHour >= LUNCH_START_HOUR && currentHour < LUNCH_END_HOUR);
    }

    private void barber() {
        while (!Thread.currentThread().isInterrupted()) {
            int currentHour = getCurrentHour();
            if (isShopOpen(currentHour)) {
                if (!waitingCustomers.isEmpty()) {
                    List<Integer> customersToServe = new ArrayList<>(waitingCustomers);
                    waitingCustomers.clear();

                    for (int customer : customersToServe) {
                        servedCustomers.add(customer);
                        updateSimulationTextArea("Thợ đang cắt tóc cho khách hàng " + customer + "\n");
                        mutex.release();
                        try {
                            int haircutTime = getRandomHaircutTime();
                            Thread.sleep(haircutTime);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt(); // Restore interrupted status
                        }
                        updateSimulationTextArea("Thợ đã cắt xong cho khách hàng " + customer + "\n");
                        customerSemaphore.release();
                    }
                } else {
                    mutex.release();
                }
            } else {
                // Barber shop is closed
                servedCustomers.clear(); // Reset served customers when the shop is closed
            }
        }
    }
    private int getRandomHaircutTime() {
        Random random = new Random();
        return random.nextInt(5000) + 1000; // Random time between 1000 and 6000 milliseconds
    }

    private void customer(int index) {
        try {
            Thread.sleep(getRandomTime());
            int currentHour = getCurrentHour();
            mutex.acquire();

            if (waitingCustomers.size() < numChairs && isShopOpen(currentHour)) {
                waitingCustomers.add(index);
                updateSimulationTextArea("Khách hàng " + index + " đang chờ ở hàng đợi. Số khách hàng trong hàng đợi: " + waitingCustomers.size() + "\n");
                mutex.release();
                barberSemaphore.release();
                customerSemaphore.acquire();
            } else {
                if (waitingCustomers.contains(index)) {
                    if (customerDecidesToLeave()) {
                        waitingCustomers.remove(Integer.valueOf(index));
                        updateSimulationTextArea("Khách hàng " + index + " rời đi vì đợi quá lâu\n");
                    } else {
                        updateSimulationTextArea("Khách hàng " + index + " đang rời đi vì hàng đợi đã đầy hoặc cửa hàng đã đóng cửa\n");
                    }
                }
                mutex.release();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean customerDecidesToLeave() {
        Random random = new Random();
        return random.nextDouble() < 0.3;
    }

    private int getCurrentHour() {
        return java.time.LocalTime.now().getHour();
    }

    private int getRandomTime() {
        Random random = new Random();
        return random.nextInt(5000) + 1000;
    }


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Nhập số ghế của hàng đợi: ");
        int numChairs = scanner.nextInt();
        BarberShop barberShop = new BarberShop(numChairs);
        TextArea simulationTextArea = null;
        barberShop.runBarberShop(simulationTextArea);
    }

    public int getMaxCustomers() {
        return MAX_CUSTOMERS;
    }

    public boolean isCustomerBeingServed(int i) {
        return servedCustomers.contains(i);
    }

    public Collection<Integer> getWaitingCustomers() {
        return waitingCustomers;
    }
    public Collection<Integer> getRemainingCustomers() {
        return remainingCustomers;
    }
}
