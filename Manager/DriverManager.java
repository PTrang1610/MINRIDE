package Manager;
import java.util.*;
import Models.Driver;
public class DriverManager extends BaseManager<Driver>
{
    // Hiền thị tài xế đầu tiên
    public void displayFirstDriver()
    {
        if (entities.isEmpty())
        {
            System.out.println("DANH SACH TAI XE DANG TRONG !!!");
            return;
        }
        System.out.println("TAI XE DAU TIEN : "+ entities.get(0));
    }
    // Hiền thị tài xế cuối cùng
    public void displayLastDriver()
    {
        if (entities.isEmpty())
        {
            System.out.println("DANH SACH TAI XE DANG TRONG !!!");
            return;
        }
        System.out.println("TAI XE CUOI CUNG : "+ entities.get(entities.size()-1));
    }
    // Hiển thị tất cả tài xế
    public void displayAllDrivers() {
        if (entities.isEmpty()) {
            System.out.println("DANH SACH TAI XE DANG TRONG!!!");
            return;
        }
        System.out.println("DANH SACH TAI XE");
        for (Driver d : entities) {
            System.out.println(d);
        }
    }

    //Hiển thị top K tài xế
    public void displayTopKDriver(int k,boolean start)
    {
        if (entities.isEmpty())
        {
            System.out.println("DANH SACH TAI XE DANG TRONG !!!! ");
            return ;
        }
        int size=entities.size();
        int limit=Math.min(k,size); 
        System.out.println("  "+limit+" TAI XE "+(start? " DAU DANH SACH ":" CUOI DANH SACH "));
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
        Driver d=findById(id);
        if (d==null)
        {
            System.out.println("KHONG THE TIM THAY TAI XE CO ID  "+ id);
        }
        return d;
    }

    // Tìm tài xế bằng tên
    public List<Driver> findDriversByName(String name)
    {
        List<Driver>  list= findByName(name);
        if (list.isEmpty()) 
        {
            System.out.println("KHONG THE TIM THAY TAI XE CO TEN "+name);
        }
        return list;
    }

    // Cập nhật dựa theo id nếu trùng tên
    public void updateDriverByName(String name, Driver updatedDriver)
    {
        List<Driver> drivers= findByName(name);
        if (drivers.isEmpty())
        {
            System.out.println("KHONG THE TIM THAY TAI XE CO TEN "+name);
            return;
        }
        if (drivers.size()==1)
        {
            Update(drivers.get(0).getId(),updatedDriver);
        }
        else
        {
            System.out.println("HIEN TAI CO NHIEU TAI XE TRUNG TEN " +name+". VUI LONG NHAP ID CUA TAI XE DE CAP NHAT");
            Scanner sc=new Scanner(System.in);
            String id=sc.nextLine();
            Update(id, updatedDriver);
        }
    }

    // Tìm tài xế gần trong bán kính r
    public List<Driver> findNearByDrivers(double x, double y,double r)
    {
        List<Driver> nearby= new LinkedList<>();
        for (Driver d: entities)
        {
            if (d.distanceTo(x, y)<=r)
            {
                nearby.add(d);
            }
        }
        return nearby;
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
                    swap(list, i, j);
                }
            } else {
                if (list.get(j).getRating() >= pivot) {
                    i++;
                    swap(list, i, j);
                }
            }
        }

        swap(list, i + 1, right);
        return i + 1;
    }

    // swap
    private void swap(List<Driver> list, int i, int j) {
        Driver temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }

    // Sắp xếp tài xế theo rating bằng quicksort
    public void sortByRatingQuickSort(boolean ascending)
    {
        if (entities.isEmpty())
        {
            System.out.println("DANH SACH TAI XE DANG TRONG");
            return ;
        }
        List<Driver>   list= new ArrayList<>(entities);
        quickSort(list,0,list.size()-1,ascending);
        entities.clear();
        entities.addAll(list);
        System.out.println("DANH SACH TAI XE DA SAP XEP THEO RATING"+(ascending?"TANG DAN":"GIAM DAM"));

    }

}
