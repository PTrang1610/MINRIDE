package Manager;

import Models.Driver;
import java.util.LinkedList;
import java.util.List;

public class MatchService {

    // Method to find nearby drivers based on the provided implementation
    public List<Driver> findNearByDrivers(List<Driver> drivers, double x, double y, double r) {
        List<Driver> nearby = new LinkedList<>();
        for (Driver d : drivers) {
            if (d.distanceTo(x, y) <= r) {
                nearby.add(d);
            }
        }
        return nearby;
    }
}
