package Models;

import Interface.IEntity;

public class Ride implements IEntity {

    private String rideId;
    private String customerId;
    private String driverId;
    private double distance;
    private double price;
    private long timestamp;

    public Ride(String rideId, String customerId, String driverId, double distance, double price) {
        //Throw Exception
        if (rideId == null || rideId.trim().isEmpty())
            throw new IllegalArgumentException("Ride ID cannot be null or empty");
        if (customerId == null || customerId.trim().isEmpty())
            throw new IllegalArgumentException("Customer ID cannot be null or empty");
        if (distance < 0)
            throw new IllegalArgumentException("Distance cannot be negative");
        if (price < 0)
            throw new IllegalArgumentException("Price cannot be negative");

        this.rideId = rideId;
        this.customerId = customerId;
        this.driverId = driverId;
        this.distance = distance;
        this.price = price;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public String getId() {
        return rideId;
    }

    @Override
    public String getName() {
        return rideId;
    }

    //Getter methods
    public String getCustomerId() { return customerId; }
    public String getDriverId() { return driverId; }
    public double getDistance() { return distance; }
    public double getPrice() { return price; }
    public long getTimestamp() { return timestamp; }

    //Setter methods
    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }
    public void setPrice(double price) {
        if (price >= 0)
            this.price = price;
    }
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
    public void setDistance(double distance) {
        this.distance = distance;
    }

    public boolean isCompleted() {
        return driverId != null && price > 0 && distance > 0;
    }
    public String getFormattedTimestamp() {
        return new java.util.Date(timestamp).toString();
    }

    public String toString() {
        String header = String.format("%-5s | %-10s | %-10s | %-12s | %-12s |",
                "ID", "Customer", "Driver", "Distance(km)", "Price(VND)");
        String info = String.format("%-5s | %-10s | %-10s | %-12.1f | %-12.0f",
                rideId, customerId, driverId, distance, price);
        return header + "\n" + info;
    }
}
