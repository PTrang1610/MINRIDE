package Utils;

import Models.Customer;
import Models.Driver;
import Models.Ride;

import java.io.*;
import java.util.*;

public class DataReader {

    public static List<Driver> readDrivers(String filePath) {
        List<Driver> drivers = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // bỏ dòng tiêu đề
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String id = parts[0].trim();
                    String name = parts[1].trim();
                    double rating = Double.parseDouble(parts[2].trim());
                    double x = Double.parseDouble(parts[3].trim());
                    double y = Double.parseDouble(parts[4].trim());
                    drivers.add(new Driver(id, name, rating, x, y));
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi đọc file drivers: " + e.getMessage());
        }
        return drivers;
    }

    public static List<Customer> readCustomers(String filePath) {
        List<Customer> customers = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // bỏ dòng tiêu đề
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String id = parts[0].trim();
                    String name = parts[1].trim();
                    String district = parts[2].trim();
                    double x = Double.parseDouble(parts[3].trim());
                    double y = Double.parseDouble(parts[4].trim());
                    customers.add(new Customer(id, name, district, x, y));
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi đọc file customers: " + e.getMessage());
        }
        return customers;
    }

    public static List<Ride> readRides(String filePath) {
        List<Ride> rides = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // bỏ dòng tiêu đề
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String rideId = parts[0].trim();
                    String customerId = parts[1].trim();
                    String driverId = parts[2].trim();
                    double distance = Double.parseDouble(parts[3].trim());
                    double fare = Double.parseDouble(parts[4].trim());
                    rides.add(new Ride(rideId, customerId, driverId, distance, fare));
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi đọc file rides: " + e.getMessage());
        }
        return rides;
    }
}
