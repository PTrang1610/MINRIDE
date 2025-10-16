package MainProgram;

import Manager.*;
import Models.*;
import Utils.*;

import java.util.*;
import java.util.function.Function;

public class Test {


    private static final Scanner sc = new Scanner(System.in);
    private static final DriverManager driverManager = new DriverManager();
    private static final CustomerManager customerManager = new CustomerManager();
    private static final RideManager rideManager = new RideManager();


    // Global undo stack: lưu Runnable để undo thao tác đã commit (thêm/cập nhật/xóa/chuyến)
    private static final Deque<Runnable> undoStack = new ArrayDeque<>();

    public static void main(String[] args) {
        List<Driver> drivers = DataReader.readDrivers("data/drivers.txt");
        List<Customer> customers = DataReader.readCustomers("data/customers.txt");
        List<Ride> rides = DataReader.readRides("data/rides.txt");
        for (Driver d : drivers) {
            driverManager.addDriver(d);
        }
        for (Customer c : customers) {
            customerManager.addCustomer(c);
        }
        for (Ride r : rides) {
            rideManager.addRide(r);
        }
        TablePrinter.printCustomers(customers);
        TablePrinter.printDrivers(drivers);
        while (true) {
            try {
                printMainMenu();
                String input = sc.nextLine().trim();
                if (input.equalsIgnoreCase("z")) { // global undo shortcut
                    performGlobalUndo();
                    continue;
                }
                int choice = Integer.parseInt(input);
                switch (choice) {
                    case 1 -> manageDrivers();
                    case 2 -> manageCustomers();
                    case 3 -> manageRides();
                    case 4 -> findSuitableDriver();
                    case 5 -> placeRide();
                    case 6 -> autoMatchRide();
                    case 7 -> performGlobalUndo();
                    case 0 -> {
                        System.out.println("Tạm biệt!");
                        return;
                    }
                    default -> System.out.println("Lựa chọn không hợp lệ. Hãy thử lại.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Lỗi: Vui lòng nhập số hợp lệ!");
            } catch (Exception e) {
                System.out.println("Đã xảy ra lỗi: " + e.getMessage());
            }
            System.out.print("Bạn có muốn tiếp tục không? (y/n): ");
            String again = sc.nextLine().trim();
            if (!again.equalsIgnoreCase("y")) {
                System.out.println("Hệ thống kết thúc.");
                break;
            }
        }
    }

    private static void printMainMenu() {
        System.out.println("\n===== MINRIDE MANAGEMENT SYSTEM =====");
        System.out.println("NẾU BẠN MUỐN HOÀN TÁC THAO TÁC THÌ NHẤN Z (trong quá trình nhập để quay lại bước trước)");
        System.out.println("1. Quản lý tài xế");
        System.out.println("2. Quản lý khách hàng");
        System.out.println("3. Quản lý danh sách chuyến đi");
        System.out.println("4. Tìm tài xế phù hợp");
        System.out.println("5. Đặt xe");
        System.out.println("6. Tự động ghép cặp");
        System.out.println("7. Hoàn tác thao tác trước (Undo)");
        System.out.println("0. Thoát");
        System.out.print("Chọn chức năng: ");
    }

    // ====================== GLOBAL UNDO ======================
    private static void performGlobalUndo() {
        if (!undoStack.isEmpty()) {
            try {
                undoStack.pop().run();
                System.out.println("Đã hoàn tác thao tác cuối cùng!");
            } catch (Exception e) {
                System.out.println("Lỗi khi hoàn tác: " + e.getMessage());
            }
        } else {
            System.out.println("Không có thao tác nào để hoàn tác!");
        }
    }

    // ====================== SMART FIELD READER (with step-back 'z') ======================
    // Hàm đọc một trường với parser; nếu nhập "z" => trả về Optional.empty() với dấu hiệu goBack
    // Trả về Optional.of(parsedValue) khi nhập hợp lệ.
    private static <T> Optional<T> readField(String prompt, Function<String, T> parser) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            if (line.equalsIgnoreCase("z")) {
                return Optional.empty(); // signal: go back one step
            }
            try {
                T value = parser.apply(line);
                return Optional.of(value);
            } catch (Exception e) {
                System.out.println("Giá trị không hợp lệ: " + e.getMessage() + " — Vui lòng nhập lại (hoặc 'z' để quay lại).");
            }
        }
    }

    // ====================== DRIVER MANAGEMENT ======================
    private static void manageDrivers() {
        while (true) {
            System.out.println("\n--- QUẢN LÝ TÀI XẾ ---");
            System.out.println("1. Thêm tài xế");
            System.out.println("2. Cập nhật tài xế");
            System.out.println("3. Xóa tài xế");
            System.out.println("4. Tìm kiếm tài xế");
            System.out.println("5. Hiển thị top K tài xế");
            System.out.println("6. Sắp xếp theo rating");
            System.out.println("0. Quay lại");
            System.out.print("Chọn: ");
            String opt = sc.nextLine().trim();
            if (opt.equalsIgnoreCase("z")) { performGlobalUndo(); continue; }
            if (opt.equals("0")) return;
            switch (opt) {
                case "1" -> addDriverFlow();
                case "2" -> updateDriverFlow();
                case "3" -> deleteDriverFlow();
                case "4" -> {
                    System.out.print("Nhập ID hoặc tên: ");
                    String key = sc.nextLine().trim();
                    // try by id first
                    try {
                        Driver d = driverManager.findById(key);
                        if (d != null) System.out.println(d);
                        else {
                            List<Driver> list = driverManager.findByName(key);
                            if (list.isEmpty()) System.out.println("Không tìm thấy tài xế.");
                            else list.forEach(System.out::println);
                        }
                    } catch (Exception e) {
                        System.out.println("Lỗi tìm kiếm: " + e.getMessage());
                    }
                }
                case "5" -> {
                    try {
                        System.out.print("Nhập k: ");
                        int k = Integer.parseInt(sc.nextLine().trim());
                        System.out.print("Hiển thị đầu danh sách? (true/false): ");
                        boolean asc = Boolean.parseBoolean(sc.nextLine().trim());
                        TablePrinter.printDrivers(driverManager.getTopKDriversRating(k, asc));
                    } catch (Exception e) {
                        System.out.println("Lỗi: " + e.getMessage());
                    }
                }
                case "6" ->{
                    driverManager.sortByRatingQuickSort(true);
                    TablePrinter.printDrivers(driverManager.getAll());
                }
                default -> System.out.println("Lựa chọn không hợp lệ.");
            }
        }
    }

    private static void addDriverFlow() {
        // fields: id (String), name(String), rating(double), x(double), y(double)
        List<String> prompts = List.of("Nhập ID: ",
                                        "Nhập tên: ",
                                        "Nhập rating: ",
                                        "Nhập x: ",
                                        "Nhập y: ");
        // store values as strings then parse where needed
        String[] values = new String[prompts.size()];
        int idx = 0;
        while (idx < prompts.size()) {
            int cur = idx;
            Optional<String> opt = readField(prompts.get(cur), s -> s);
            if (opt.isEmpty()) {
                // go back one step
                if (idx == 0) {
                    System.out.println("Không thể quay lại bước trước nữa.");
                    continue;
                } else {
                    idx--;
                    continue;
                }
            } else {
                values[cur] = opt.get();
                // validate numeric where needed immediately:
                if (cur == 2) { // rating
                    try {
                        Double.parseDouble(values[cur]);
                    } catch (NumberFormatException e) {
                        System.out.println("Rating phải là số. Nhập lại.");
                        continue;
                    }
                } else if (cur == 3 || cur == 4) {
                    try {
                        Double.parseDouble(values[cur]);
                    } catch (NumberFormatException e) {
                        System.out.println("Vị trí phải là số. Nhập lại.");
                        continue;
                    }
                }
                idx++;
            }
        }

        // After all fields collected, try to add driver (handle duplicate ID)
        String id = values[0];
        String name = values[1];
        double rating = Double.parseDouble(values[2]);
        double x = Double.parseDouble(values[3]);
        double y = Double.parseDouble(values[4]);

        try {
            Driver d = new Driver(id, name, rating, x, y);
            // Use manager wrapper if exists (addDriver)
            try {
                driverManager.addDriver(d);
            } catch (UnsupportedOperationException | NoSuchMethodError ex) {
                driverManager.Add(d); // fallback
            }
            // push undo: delete by id
            undoStack.push(() -> {
                try {
                    driverManager.Delete(id);
                } catch (Exception ignored) {}
            });
            System.out.println("Thêm tài xế thành công!");
        } catch (Exception e) {
            System.out.println("Lỗi khi thêm tài xế: " + e.getMessage());
        }
    }

    private static void updateDriverFlow() {
        // Get ID first
        Optional<String> idOpt = readField("Nhập ID cần cập nhật: ", s -> s);
        if (idOpt.isEmpty()) { System.out.println("Đã hủy cập nhật."); return; }
        String id = idOpt.get();
        Driver old = null;
        try {
            old = driverManager.findById(id);
        } catch (Exception e) {
            System.out.println("Lỗi: " + e.getMessage());
            return;
        }
        if (old == null) {
            System.out.println("Không tìm thấy tài xế!");
            return;
        }

        // fields to update: name, rating, x, y
        String[] values = new String[4];
        int idx = 0;
        List<String> prompts = List.of("Nhập tên mới: ",
                                        "Nhập rating mới: ",
                                        "Nhập x mới: ",
                                        "Nhập y mới: ");
        while (idx < prompts.size()) {
            Optional<String> opt = readField(prompts.get(idx), s -> s);
            if (opt.isEmpty()) {
                if (idx == 0) {
                    System.out.println("Quay lại bước ID (không hỗ trợ từ đây).");
                    return;
                }
                idx--; continue;
            } else {
                values[idx] = opt.get();
                if (idx == 1) {
                    try {
                        Double.parseDouble(values[idx]);
                    } catch (NumberFormatException e) {
                        System.out.println("Rating phải là số.");
                        continue;
                    }
                } else if (idx == 2 || idx == 3) {
                    try {
                        Double.parseDouble(values[idx]);
                    } catch (NumberFormatException e) {
                        System.out.println("Tọa độ phải là số.");
                        continue;
                    }
                }
                idx++;
            }
        }

        try {
            String name = values[0];
            double rating = Double.parseDouble(values[1]);
            double x = Double.parseDouble(values[2]);
            double y = Double.parseDouble(values[3]);

            Driver updated = new Driver(id, name, rating, x, y);
            driverManager.Update(id, updated);
            // undo = restore old
            Driver finalOld = old;
            undoStack.push(() -> {
                try {
                    driverManager.Update(id, finalOld);
                } catch (Exception ignored) {}
            });
            System.out.println("Cập nhật thành công!");
        } catch (Exception e) {
            System.out.println("Lỗi khi cập nhật: " + e.getMessage());
        }
    }

    private static void deleteDriverFlow() {
        Optional<String> idOpt = readField("Nhập ID cần xóa: ", s -> s);
        if (idOpt.isEmpty()) { System.out.println("Đã hủy xóa."); return; }
        String id = idOpt.get();
        Driver old = driverManager.findById(id);
        if (old == null) { System.out.println("Không tìm thấy tài xế!"); return; }
        try {
            driverManager.deleteDriverById(id);
            // undo = add back old
            undoStack.push(() -> {
                try { driverManager.Add(old); } catch (Exception ignored) {}
            });
            System.out.println("Đã xóa tài xế!");
        } catch (Exception e) {
            System.out.println("Lỗi khi xóa: " + e.getMessage());
        }
    }

    // ====================== CUSTOMER MANAGEMENT ======================
    private static void manageCustomers() {
        while (true) {
            System.out.println("\n--- QUẢN LÝ KHÁCH HÀNG ---");
            System.out.println("1. Thêm khách hàng");
            System.out.println("2. Cập nhật khách hàng");
            System.out.println("3. Xóa khách hàng");
            System.out.println("4. Liệt kê khách theo quận");
            System.out.println("0. Quay lại");
            System.out.print("Chọn: ");
            String opt = sc.nextLine().trim();
            if (opt.equals("0")) return;
            switch (opt) {
                case "1" -> addCustomerFlow();
                case "2" -> updateCustomerFlow();
                case "3" -> deleteCustomerFlow();
                case "4" -> {
                    System.out.print("Nhập tên quận: ");
                    String district = sc.nextLine().trim();
                    List<Customer> list = customerManager.listCustomersInDistrict(district);
                    System.out.println("Có " + list.size() + " khách hàng trong quận " + district);
                    TablePrinter.printCustomers(list);
                }
                default -> System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }

    private static void addCustomerFlow() {
        List<String> prompts = List.of("Nhập ID: ",
                                        "Nhập tên: ",
                                        "Nhập quận: ",
                                        "Nhập x: ",
                                        "Nhập y: ");
        String[] values = new String[prompts.size()];
        int idx = 0;
        while (idx < prompts.size()) {
            Optional<String> opt = readField(prompts.get(idx), s -> s);
            if (opt.isEmpty()) {
                if (idx == 0) {
                    System.out.println("Không thể quay lại bước trước nữa.");
                    continue;
                }
                idx--; continue;
            } else {
                values[idx] = opt.get();
                if (idx == 3 || idx == 4) {
                    try {
                        Double.parseDouble(values[idx]);
                    } catch (NumberFormatException e) {
                        System.out.println("⚠️ Tọa độ phải là số.");
                        continue;
                    }
                }
                idx++;
            }
        }

        try {
            String id = values[0], name = values[1], district = values[2];
            double x = Double.parseDouble(values[3]), y = Double.parseDouble(values[4]);
            Customer c = new Customer(id, name, district, x, y);
            customerManager.addCustomer(c);
            undoStack.push(() -> {
                try {
                    customerManager.Delete(id);
                } catch (Exception ignored) {}
            });
            System.out.println("Đã thêm khách hàng!");
        } catch (Exception e) {
            System.out.println("Lỗi khi thêm khách hàng: " + e.getMessage());
        }
    }

    private static void updateCustomerFlow() {
        Optional<String> idOpt = readField("Nhập ID: ", s -> s);
        if (idOpt.isEmpty()) { System.out.println("Đã hủy."); return; }
        String id = idOpt.get();
        Customer old = customerManager.findById(id);
        if (old == null) { System.out.println("Không tìm thấy khách hàng!"); return; }

        List<String> prompts = List.of("Nhập tên mới: ",
                                        "Nhập quận mới: ",
                                        "Nhập x mới: ",
                                        "Nhập y mới: ");
        String[] values = new String[prompts.size()];
        int idx = 0;
        while (idx < prompts.size()) {
            Optional<String> opt = readField(prompts.get(idx), s -> s);
            if (opt.isEmpty()) { if (idx == 0) return; idx--; continue; }
            values[idx] = opt.get();
            if (idx == 2 || idx == 3) {
                try { Double.parseDouble(values[idx]); } catch (NumberFormatException e) { System.out.println("⚠️ Tọa độ phải là số."); continue; }
            }
            idx++;
        }

        try {
            String name = values[0], district = values[1];
            double x = Double.parseDouble(values[2]), y = Double.parseDouble(values[3]);
            Customer updated = new Customer(id, name, district, x, y);
            customerManager.updateCustomer(id, updated);
            Customer finalOld = old;
            undoStack.push(() -> {
                try { customerManager.Update(id, finalOld); } catch (Exception ignored) {}
            });
            System.out.println("Đã cập nhật!");
        } catch (Exception e) {
            System.out.println("Lỗi khi cập nhật khách hàng: " + e.getMessage());
        }
    }

    private static void deleteCustomerFlow() {
        Optional<String> idOpt = readField("Nhập ID cần xóa: ", s -> s);
        if (idOpt.isEmpty()) { System.out.println("Đã hủy."); return; }
        String id = idOpt.get();
        Customer old = customerManager.findById(id);
        if (old == null) { System.out.println("Không tìm thấy khách hàng!"); return; }
        try {
            customerManager.deleteCustomer(id);
            Customer finalOld = old;
            undoStack.push(() -> {
                try { customerManager.Add(finalOld); } catch (Exception ignored) {}
            });
            System.out.println("Đã xóa!");
        } catch (Exception e) {
            System.out.println("Lỗi khi xóa khách hàng: " + e.getMessage());
        }
    }

    // ====================== RIDE MANAGEMENT ======================
    private static void manageRides() {
        while (true) {
            System.out.println("\n--- QUẢN LÝ DANH SÁCH CHUYẾN ĐI ---");
            System.out.println("1. Thêm chuyến đi (có driver)");
            System.out.println("2. Hiển thị chuyến của tài xế");
            System.out.println("3. Hủy chuyến");
            System.out.println("4. Xác nhận tất cả chuyến đi hợp lệ");
            System.out.println("5. Xem toàn bộ lịch sử chuyến đi");
            System.out.println("0. Quay lại");
            System.out.print("Chọn: ");
            String opt = sc.nextLine().trim();
            if (opt.equals("0")) return;
            switch (opt) {
                case "1" -> addRideFlow();
                case "2" -> {
                    System.out.print("Nhập ID tài xế: ");
                    String driverId = sc.nextLine().trim();
                    List<Ride> rides = rideManager.getDriverRides(driverId);
                    if (rides.isEmpty()) System.out.println("Tài xế chưa có chuyến đi nào.");
                    else TablePrinter.printRides(rides);
                }
                case "3" -> {
                    System.out.print("Nhập ID chuyến đi cần hủy: ");
                    String id = sc.nextLine().trim();
                    Ride old = rideManager.findById(id);
                    if (old != null) {
                        rideManager.cancelRide(id);
                        Ride finalOld = old;
                        undoStack.push(() -> {
                            try { rideManager.addRide(finalOld); } catch (Exception ignored) {}
                        });
                        System.out.println("Đã hủy chuyến đi!");
                    } else System.out.println("Không tìm thấy chuyến đi!");
                }
                case "4" -> { rideManager.confirmAllRides(); System.out.println("✅ Tất cả chuyến đi đã được xác nhận!"); }
                case "5" -> {
                    List<Ride> history = rideManager.getAll();
                    if (history.isEmpty()) System.out.println("Chưa có chuyến đi nào!");
                    else TablePrinter.printRides(history);
                }
                default -> System.out.println("Lựa chọn không hợp lệ!");
            }
        }
    }

    private static void addRideFlow() {
        // fields: rideId, customerId, driverId, distance (as double)
        List<String> prompts = List.of("Nhập ID chuyến đi: ", "Nhập ID khách hàng: ", "Nhập ID tài xế: ", "Nhập quãng đường (km): ");
        String[] values = new String[prompts.size()];
        int idx = 0;
        while (idx < prompts.size()) {
            Optional<String> opt = readField(prompts.get(idx), s -> s);
            if (opt.isEmpty()) {
                if (idx == 0) { System.out.println("Không thể quay lại bước trước."); continue; }
                idx--; continue;
            } else {
                values[idx] = opt.get();
                if (idx == 3) {
                    try { Double.parseDouble(values[idx]); } catch (NumberFormatException e) { System.out.println("⚠️ Quãng đường phải là số."); continue; }
                }
                idx++;
            }
        }
        try {
            String rideId = values[0], customerId = values[1], driverId = values[2];
            double distance = Double.parseDouble(values[3]);
            Ride newRide = new Ride(rideId, customerId, driverId, distance, 0);
            rideManager.addRide(newRide);
            undoStack.push(() -> { try { rideManager.cancelRide(rideId); } catch (Exception ignored) {} });
            System.out.println("Đã thêm chuyến đi thành công!");
        } catch (Exception e) {
            System.out.println("Lỗi khi thêm chuyến đi: " + e.getMessage());
        }
    }

    // ====================== OTHER FEATURES ======================
    private static void findSuitableDriver() {
        try {
            System.out.println("\n--- TÌM TÀI XẾ PHÙ HỢP ---");
            System.out.print("Nhập ID khách hàng: ");
            String customerId = sc.nextLine().trim();

            System.out.print("Nhập bán kính R (km): ");
            double radius = Double.parseDouble(sc.nextLine().trim());

            List<Driver> nearby = driverManager.findNearByDrivers(customerManager, customerId, radius);

        } catch (Exception e) {
            System.out.println("Lỗi khi tìm tài xế phù hợp: " + e.getMessage());
        }
    }

    private static void placeRide() {
        List<String> prompts = List.of(
                "Nhập ID khách hàng: ",
                "Nhập ID tài xế: ",
                "Nhập quãng đường chuyến đi (km): ",
                "Nhập khoảng cách tài xế đến khách (km): "
        );

        String[] values = new String[prompts.size()];
        int idx = 0;
        while (idx < prompts.size()) {
            Optional<String> opt = readField(prompts.get(idx), s -> s);
            if (opt.isEmpty()) {
                if (idx == 0) {
                    System.out.println("Không thể quay lại bước trước.");
                    continue;
                }
                idx--; // cho phép quay lại 1 bước
                continue;
            }

            values[idx] = opt.get();

            // Validate số
            if (idx == 2 || idx == 3) {
                try {
                    Double.parseDouble(values[idx]);
                } catch (NumberFormatException e) {
                    System.out.println("Giá trị phải là số.");
                    continue;
                }
            }
            idx++;
        }

        try {
            String customerId = values[0];
            String driverId = values[1];
            double rideDistance = Double.parseDouble(values[2]);
            double driverDistance = Double.parseDouble(values[3]);
            double totalDistance = rideDistance + driverDistance;

            Ride newRide = rideManager.createRideWithDriver(customerId, driverId, totalDistance, driverDistance);
            undoStack.push(() -> {
                try {
                    rideManager.cancelRide(newRide.getId());
                } catch (Exception ignored) {}
            });

            System.out.println("\nĐặt xe thành công!");
            System.out.println(newRide);
        } catch (Exception e) {
            System.out.println("Lỗi khi đặt xe: " + e.getMessage());
        }
    }

    private static void autoMatchRide() {
        List<String> prompts = List.of(
                "Nhập ID khách hàng: ",
                "Nhập bán kính tìm kiếm (km): "
        );

        String[] values = new String[prompts.size()];
        int idx = 0;
        while (idx < prompts.size()) {
            Optional<String> opt = readField(prompts.get(idx), s -> s);
            if (opt.isEmpty()) {
                if (idx == 0) { System.out.println("Không thể quay lại bước trước."); continue; }
                idx--; continue;
            }

            values[idx] = opt.get();
            if (idx == 1) {
                try { Double.parseDouble(values[idx]); }
                catch (NumberFormatException e) { System.out.println("Bán kính phải là số."); continue; }
            }
            idx++;
        }

        try {
            String customerId = values[0];
            double radius = Double.parseDouble(values[1]);
            Customer c = customerManager.findById(customerId);
            if (c == null) {
                System.out.println("Không tìm thấy khách hàng!");
                return;
            }

            Ride newRide = rideManager.autoAssignDriver(customerId, customerManager, driverManager, radius);
            if (newRide == null) {
                System.out.println("Không có tài xế phù hợp trong phạm vi " + radius + " km.");
                return;
            }

            undoStack.push(() -> {
                try { rideManager.cancelRide(newRide.getId()); } catch (Exception ignored) {}
            });

            System.out.println("\nGhép chuyến thành công!");
            System.out.println(newRide);
        } catch (Exception e) {
            System.out.println("Lỗi khi ghép chuyến: " + e.getMessage());
        }
    }
}
