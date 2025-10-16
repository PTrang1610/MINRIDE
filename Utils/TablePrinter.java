package Utils;
import Models.*;
import java.util.List;
public class TablePrinter {

    public static void printDrivers(List<Driver> drivers) {
        System.out.println("Bảng tài xế (Drivers)");
        System.out.println("------------------------------------------------------------");
        System.out.printf("| %-4s | %-10s | %-6s | %-12s |\n", "ID", "Name", "Rating", "Location (x, y)");
        System.out.println("------------------------------------------------------------");
        for (Driver d : drivers) {
            System.out.printf("| %-4s | %-10s | %-6.1f | (%-2.0f, %-2.0f)        |\n",
                    d.getId(), d.getName(), d.getRating(), d.getX(), d.getY());
        }
        System.out.println("------------------------------------------------------------\n");
    }

    public static void printCustomers(List<Customer> customers) {
        System.out.println("Bảng khách hàng (Customers)");
        System.out.println("------------------------------------------------------------");
        System.out.printf("| %-4s | %-10s | %-10s | %-12s |\n", "ID", "Name", "District", "Location (x, y)");
        System.out.println("------------------------------------------------------------");
        for (Customer c : customers) {
            System.out.printf("| %-4s | %-10s | %-10s | (%-2.0f, %-2.0f)        |\n",
                    c.getId(), c.getName(), c.getDistrict(), c.getX(), c.getY());
        }
        System.out.println("------------------------------------------------------------\n");
    }

    public static void printRides(List<Ride> rides) {
        System.out.println("Bảng chuyến đi (Rides)");
        System.out.println("---------------------------------------------------------------------");
        System.out.printf("| %-7s | %-10s | %-9s | %-12s | %-10s |\n",
                "RideID", "CustomerID", "DriverID", "Distance (km)", "Fare (VND)");
        System.out.println("---------------------------------------------------------------------");
        for (Ride r : rides) {
            System.out.printf("| %-7s | %-10s | %-9s | %-12.1f | %-10.0f |\n",
                    r.getId(), r.getCustomerId(), r.getDriverId(), r.getDistance(), r.getPrice());
        }
        System.out.println("---------------------------------------------------------------------\n");
    }
}