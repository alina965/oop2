import components.*;
import config.Config;
import workers.*;
import other.IDGenerator;
import storage.*;
import suppliers.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<AccessorySupplier> accessorySuppliers = new ArrayList<>();
        List<Dealer> dealers = new ArrayList<>();
        BodySupplier bodySupplier;
        EngineSupplier engineSupplier;
        Controller controller;
        ThreadPool threadPool;

        try {
            Config config = new Config("config.properties");

            Storage<Car> carStorage = new Storage<>(config.getInt("StorageAutoSize"));
            Storage<Body> bodyStorage = new Storage<>(config.getInt("StorageBodySize"));
            Storage<Engine> engineStorage = new Storage<>(config.getInt("StorageEngineSize"));
            Storage<Accessory> accessoryStorage = new Storage<>(config.getInt("StorageAccessorySize"));

            IDGenerator bodyID = new IDGenerator();
            IDGenerator engineID = new IDGenerator();
            IDGenerator accessoryID = new IDGenerator();
            IDGenerator carID = new IDGenerator();

            int accessorySuppliersCount = config.getInt("AccessorySuppliers");
            for (int i = 0; i < accessorySuppliersCount; i++) {
                AccessorySupplier supplier = new AccessorySupplier(config.getInt("AccessorySupplierFrequency"), accessoryStorage, accessoryID, i);
                supplier.start();
                accessorySuppliers.add(supplier);
            }

            bodySupplier = new BodySupplier(config.getInt("BodySupplierFrequency"), bodyStorage, bodyID);
            engineSupplier = new EngineSupplier(config.getInt("EngineSupplierFrequency"), engineStorage, engineID);
            bodySupplier.start();
            engineSupplier.start();

            threadPool = new ThreadPool(config.getInt("Workers"));

            controller = new Controller(carStorage, threadPool, bodyStorage, engineStorage, accessoryStorage, carID);
            controller.start();

            int dealerCount = config.getInt("Dealers");
            int dealerFrequency = config.getInt("DealerFrequency");
            for (int i = 0; i < dealerCount; i++) {
                Dealer dealer = new Dealer(dealerFrequency, carStorage);
                dealer.start();
                dealers.add(dealer);
            }

            BodySupplier finalBodySupplier = bodySupplier;
            EngineSupplier finalEngineSupplier = engineSupplier;
            Controller finalController = controller;
            ThreadPool finalThreadPool = threadPool;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nЗавершение работы...");

                for (AccessorySupplier supplier : accessorySuppliers) {
                    supplier.stopWorking();
                }

                finalBodySupplier.stopWorking();
                finalEngineSupplier.stopWorking();

                for (Dealer dealer : dealers) {
                    dealer.stopWorking();
                }

                finalController.stopWorking();

                finalThreadPool.shutdown();

                System.out.println("Все потоки остановлены");
            }));

        }
        catch (IOException e) {
            System.err.println("Не удалось загрузить конфигурацию: " + e.getMessage());
            System.exit(1);
        }
    }
}