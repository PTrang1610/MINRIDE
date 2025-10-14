package Manager;

import Manager.DriverManager;
import Models.Ride;
import Models.Driver;
import Models.Customer;

import java.util.*;

public class RideManager extends BaseManager<Ride> {
    //Attributes
    private Map<String, List<Ride>> driverRides;    //storage list rides according to driverId
    private Queue<Ride> pendingRides;       //Storage rides are waiting accept (FIFO)
    private int nextRideId;         //variable to create ID automatic for ride - Can repair variable

    // Constructor initialization RideManager
    public RideManager() {
        super();                    // Call constructor of BaseManager -> Inheritance
        driverRides = new HashMap<>();
        pendingRides = new LinkedList<>();
        nextRideId = 1;             // Initialize first ID is 1
    }

    @Override   //Polymorphism
    public void Add(Ride entity) {  //T -> Ride
        super.Add(entity);

        // Only add to driverRides if driver have assigned
        if (entity.getDriverId() != null && !entity.getDriverId().trim().isEmpty()) {
            //computeIfAbsent: if driverId not in map ->creat new list
            driverRides.computeIfAbsent(entity.getDriverId(), k -> new ArrayList<>()).add(entity);
        }
    }

    // Create new ride if NOT HAVE driver
    public Ride createRide(String customerId, double distance) {
        String rideId = "R" + String.format("%04d", nextRideId++);
        Ride ride = new Ride(rideId, customerId, null, distance, 0);    //Not yet counted

        // Add ride to pending (Wait confirm) - offer is method of Queue
        pendingRides.offer(ride);

        return ride;
    }

    public Ride createRideWithDriver(String customerId, String driverId,
                                     double tripDistance, double driverToCustomerDist) {
        double totalDistance = tripDistance + driverToCustomerDist;
        double price = calculatePrice(totalDistance);
        String rideId = "R" + String.format("%04d", nextRideId++);

        Ride ride = new Ride(rideId, customerId, driverId, totalDistance, price);

        pendingRides.offer(ride);

        return ride;
    }

    // Confirmed a particular ride from pending
    public boolean confirmRide(String rideId) {
        Iterator<Ride> iterator = pendingRides.iterator();

        while (iterator.hasNext()) {
            Ride ride = iterator.next();
            if (ride.getId().equals(rideId)) {
                iterator.remove();
                Add(ride);
                return true;
            }
        }
        return false;
    }

    // Confirmed ALL rides are pending
    public void confirmAllRides() {
        while (!pendingRides.isEmpty()) {
            Ride ride = pendingRides.poll(); // Get and clear head of queue ride (FIFO)
            Add(ride);
        }
    }

    // Canceled a particular ride in Pending
    public boolean cancelRide(String rideId) {
        Iterator<Ride> iterator = pendingRides.iterator();

        while (iterator.hasNext()) {
            Ride ride = iterator.next();
            if (ride.getId().equals(rideId)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    // Canceled the LAST ride in pending queue
    public void cancelLastRide() {
        if (!pendingRides.isEmpty()) {
            pendingRides.poll();
        }
    }

    // Get ALL lists rides are pending
    public List<Ride> getPendingRides() {
        return new ArrayList<>(pendingRides);
    }

    // Get lists rides of a particular driver
    public List<Ride> getDriverRides(String driverId) {
        if (driverId == null || driverId.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<Ride> rides = driverRides.getOrDefault(driverId, new ArrayList<>());

        // Sort by time Decreasing (newest is first)
        rides.sort((r1, r2) -> Long.compare(r2.getTimestamp(), r1.getTimestamp()));

        return new ArrayList<>(rides);
    }

    // Get list rides of a particular customer
    public List<Ride> getCustomerRides(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<Ride> customerRides = new ArrayList<>();

        for (Ride ride : getAll()) {
            if (customerId.equals(ride.getCustomerId())) {
                customerRides.add(ride);
            }
        }

        customerRides.sort((r1, r2) -> Long.compare(r2.getTimestamp(), r1.getTimestamp()));
        return customerRides;
    }

    public Ride autoAssignDriver(String customerId, double customerX, double customerY,
                                 DriverManager driverManager, double radius) {
        if (driverManager == null) {
            return null;
        }

        List<Driver> nearbyDrivers = driverManager.findNearByDrivers(customerX, customerY, radius);

        if (nearbyDrivers.isEmpty()) {
            return null;
        }

        Driver bestDriver = nearbyDrivers.get(0);

        double distanceToCustomer = calculateDistance(
                customerX, customerY,
                bestDriver.getX(), bestDriver.getY()
        );

        // Suppose distance of ride permanent is 5km
        return createRideWithDriver(customerId, bestDriver.getId(), 5.0, distanceToCustomer);
    }

    public boolean assignDriverToRide(String rideId, String driverId, DriverManager driverManager) {
        for (Ride ride : pendingRides) {
            if (ride.getId().equals(rideId)) {
                Driver driver = driverManager.findById(driverId);
                if (driver != null) {
                    ride.setDriverId(driverId);

                    double price = calculatePrice(ride.getDistance());
                    ride.setPrice(price);

                    return true;
                }
            }
        }
        return false;
    }

    // Calculate total revenue from ALL rides (be confirmed)
    public double calculateTotalRevenue() {
        //Convert List -> Stream
        return getAll().stream()
                .mapToDouble(Ride::getPrice)
                .sum();
    }

    // Calculate revenue of a PARTICULAR driver
    public double calculateDriverRevenue(String driverId) {
        return getDriverRides(driverId).stream()
                .mapToDouble(Ride::getPrice)
                .sum();
    }

    // Calculate price ride based on Distance (private method)
    private double calculatePrice(double distance) {
        double baseFare = 10000;
        double ratePerKm = 12000;
        return baseFare + (distance * ratePerKm);
    }

    // Calculate distance by Euclid between 2 points (private method)
    private double calculateDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    // Get an overview of rides (Statistical)
    public Map<String, Object> getRideStatistics() {
        Map<String, Object> stats = new HashMap<>();
        List<Ride> allRides = getAll();

        // Base Statistical
        stats.put("totalRides", allRides.size());
        stats.put("totalRevenue", calculateTotalRevenue());
        stats.put("pendingRides", pendingRides.size());

        //Revenue statistics by driver
        Map<String, Double> driverRevenue = new HashMap<>();
        for (Ride ride : allRides) {
            if (ride.getDriverId() != null) {
                driverRevenue.merge(ride.getDriverId(), ride.getPrice(), Double::sum);
            }
        }
        stats.put("driverRevenue", driverRevenue);

        return stats;
    }

    @Override
    public void Delete(String id) {
        Ride ride = findById(id);

        // If ride existed and had driverId
        if (ride != null && ride.getDriverId() != null) {
            List<Ride> driverRideList = driverRides.get(ride.getDriverId());
            if (driverRideList != null) {
                driverRideList.remove(ride);

                if (driverRideList.isEmpty()) {
                    driverRides.remove(ride.getDriverId());
                }
            }
        }
        super.Delete(id);
    }
}