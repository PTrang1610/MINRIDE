package Manager;
import java.util.*;

import Models.Customer;
import Models.Driver;
import Utils.TablePrinter;

public class DriverManager extends BaseManager<Driver>
{
    private List<Driver> drivers = new LinkedList<>();
    // Thêm
    public void addDriver(Driver driver) {
        super.Add(driver);
        System.out.println(" Đã thêm thành công tài xế:" + driver.getName() + " (ID: " + driver.getId() + ")");
    }

    //Xóa
    public void deleteDriverById(String id) {
        Driver removedDriver = findById(id);
        super.Delete(id);
        System.out.println("Đã xóa thành công tài xế: " + removedDriver.getName() + " (ID: " + id + ")");
    }

    // Hiển thị tất cả tài xế
    public void displayAllDrivers() {
        if (entities.isEmpty()) {
            System.out.println("Danh sách tài xế hiện đang trống!");
            return;
        }
        System.out.println("Danh sách tài xế:");
        for (Driver d : entities) {
            System.out.println(d);
        }
    }

    //Hiển thị top K tài xế
    public void displayTopKDriver(int k,boolean start)
    {
        if (entities.isEmpty())
        {
            System.out.println("Danh sách tài xế hiện đang trống! ");
            return ;
        }
        int size=entities.size();
        int limit=Math.min(k,size);
        System.out.println(" Top "+limit+" tài xế "+(start? " từ đầu:": "từ cuối:"));
        if (start)
        {
            for (int i=0; i<limit;i++)
            {
                System.out.println(entities.get(i));
            }
        }
        else
        {
            for (int i=size-limit;i<size;i++)
            {
                System.out.println(entities.get(i));
            }
        }
    }

    // Tìm tài xế bằng id
    public Driver findDriverById(String id)
    {
        return super.findById(id);
    }

    // Tìm tài xế bằng tên
    public List<Driver> findDriversByName(String name)
    {
        return super.findByName(name);
    }
    // Cập nhật dựa theo id nếu trùng tên
    public void updateDriverByName(String name, Driver updated, String idIfDuplicate) {
        List<Driver> list = findDriversByName(name);
        if (list.size() == 1) {
            Update(list.get(0).getId(), updated);
        } else {
            Update(idIfDuplicate, updated);
        }
        System.out.println("Cập nhật thành công thông tin tài xế: " + updated.getName());
    }
    // Lấy top K tài xế theo rating
    public List<Driver> getTopKDriversRating(int k, boolean highestFirst)
    {
        sortByRatingQuickSort(!highestFirst);
        int limit = Math.min(k, entities.size());
        return new LinkedList<>(entities.subList(0, limit));
    }

    // Hàm quicksort cho rating
    private void quickSort(List<Driver> list, int left, int right, boolean ascending) {
        if (left < right) {
            int pivotIndex = partition(list, left, right, ascending);
            quickSort(list, left, pivotIndex - 1, ascending);
            quickSort(list, pivotIndex + 1, right, ascending);
        }
    }

    // Hàm chia mảng
    private int partition(List<Driver> list, int left, int right, boolean ascending) {
        double pivot = list.get(right).getRating();
        int i = left - 1;

        for (int j = left; j < right; j++) {
            if (ascending) {
                if (list.get(j).getRating() <= pivot) {
                    i++;
                    Collections.swap(list, i, j);
                }
            } else {
                if (list.get(j).getRating() >= pivot) {
                    i++;
                    Collections.swap(list, i, j);
                }
            }
        }

        Collections.swap(list, i + 1, right);
        return i + 1;
    }
    // Sắp xếp tài xế theo rating bằng quicksort
    public void sortByRatingQuickSort(boolean ascending)
    {
        if (entities.isEmpty())
        {
            System.out.println("Danh sách tài xế hiện đang trống!");
            return ;
        }
        List<Driver>   list= new ArrayList<>(entities);
        quickSort(list,0,list.size()-1,ascending);
        entities.clear();
        entities.addAll(list);
        System.out.println("Danh sách tài xế được sắp xếp theo rating"+(ascending?" tăng dần.":"giảm dần."));
    }

    // The method you provided: Tìm tài xế gần trong bán kính r
    public List<Driver> findNearByDrivers(CustomerManager customerManager, String customerId, double radius) {
        Customer customer = customerManager.findById(customerId);
        if (customer == null) {
            System.out.println("Không tìm thấy khách hàng với ID: " + customerId);
            return Collections.emptyList();
        }

        double customerX = customer.getX();
        double customerY = customer.getY();

        List<Driver> nearbyDrivers = new ArrayList<>();
        for (Driver d : getAll()) {
            double distance = Math.sqrt(Math.pow(d.getX() - customerX, 2) + Math.pow(d.getY() - customerY, 2));
            if (distance <= radius) {
                nearbyDrivers.add(d);
            }
        }

        // Sắp xếp theo khoảng cách tăng dần
        nearbyDrivers.sort(Comparator
                .comparingDouble((Driver d) -> Math.sqrt(Math.pow(d.getX() - customerX, 2) + Math.pow(d.getY() - customerY, 2)))
                // Tiêu chí phụ (tùy chọn)
                .thenComparing(Driver::getRating, Comparator.reverseOrder())
        );

        if (nearbyDrivers.isEmpty()) {
            System.out.println("⚠Không có tài xế nào trong phạm vi " + radius + " km quanh khách hàng " + customer.getName());
        } else {
            System.out.println("Danh sách tài xế gần khách hàng " + customer.getName() + " (R = " + radius + "):");
            for (Driver d : nearbyDrivers) {
                double dist = Math.sqrt(Math.pow(d.getX() - customerX, 2) + Math.pow(d.getY() - customerY, 2));
                System.out.println(String.format("- %-15s | Cách: %.2f km | Rating: %.1f |", d.getName(), dist, d.getRating()));
            }
        }

        return nearbyDrivers;
    }

    public Driver matchDriverToRide(CustomerManager customerManager, String customerId, double radius) {
        List<Driver> nearbyDrivers = findNearByDrivers(customerManager, customerId, radius);
        return nearbyDrivers.isEmpty() ? null : nearbyDrivers.getFirst();
    }

}