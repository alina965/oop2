import components.*;
import config.Config;
import workers.*;
import storage.*;
import suppliers.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        List<AccessorySupplier> accessorySuppliers = new ArrayList<>();
        List<Dealer> dealers = new ArrayList<>();
        BodySupplier bodySupplier;
        EngineSupplier engineSupplier;
        Controller controller;
        ThreadPool threadPool;

        try {
            Config config = new Config("config.properties");

            Storage<Car> carStorage = new Storage<>(config.getProperty("StorageAutoSize"));
            Storage<Body> bodyStorage = new Storage<>(config.getProperty("StorageBodySize"));
            Storage<Engine> engineStorage = new Storage<>(config.getProperty("StorageEngineSize"));
            Storage<Accessory> accessoryStorage = new Storage<>(config.getProperty("StorageAccessorySize"));

            int accessorySuppliersCount = config.getProperty("AccessorySuppliers");
            for (int i = 0; i < accessorySuppliersCount; i++) {
                AccessorySupplier supplier = new AccessorySupplier(config.getProperty("AccessorySupplierFrequency"), accessoryStorage, i);
                supplier.start();
                accessorySuppliers.add(supplier);
            }

            bodySupplier = new BodySupplier(config.getProperty("BodySupplierFrequency"), bodyStorage);
            engineSupplier = new EngineSupplier(config.getProperty("EngineSupplierFrequency"), engineStorage);
            bodySupplier.start();
            engineSupplier.start();

            threadPool = new ThreadPool(config.getProperty("Workers"));

            controller = new Controller(carStorage, threadPool, bodyStorage, engineStorage, accessoryStorage);
            controller.start();

            int dealerCount = config.getProperty("Dealers");
            int dealerFrequency = config.getProperty("DealerFrequency");
            for (int i = 0; i < dealerCount; i++) {
                Dealer dealer = new Dealer(dealerFrequency, carStorage);
                dealer.start();
                dealers.add(dealer);
            }

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Завершение работы...");

                for (AccessorySupplier supplier : accessorySuppliers) {
                    supplier.stopWorking();
                }

                bodySupplier.stopWorking();
                engineSupplier.stopWorking();

                for (Dealer dealer : dealers) {
                    dealer.stopWorking();
                }

                controller.stopWorking();

                threadPool.shutdown();

                logger.info("Все потоки остановлены");
            }));

        }
        catch (IOException e) {
            logger.severe("Не удалось загрузить конфигурацию: " + e.getMessage());
            System.exit(1);
        }
    }
}