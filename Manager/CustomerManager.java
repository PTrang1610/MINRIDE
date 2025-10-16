package Manager;

import Models.Customer;
import Models.Driver;

import java.util.*;

public class CustomerManager extends BaseManager<Customer> {

    private final Map<String, List<Customer>> districtMap = new HashMap<>();
    private List<Customer> customers = new LinkedList<>();

    public void addCustomer(Customer c) {
        super.Add(c);
        addToDistrictIndex(c);
    }

    public void updateCustomer(String id, Customer c) {
        Customer old = findById(id);
        if (old != null) {
            removeFromDistrictIndex(old);
        }
        super.Update(id, c);
        addToDistrictIndex(c);
    }

    public void deleteCustomer(String id) {
        Customer old = findById(id);
        if (old != null) {
            removeFromDistrictIndex(old);
        }
        super.Delete(id);
    }

    private void addToDistrictIndex(Customer c) {
        String district = safeDistrict(c.getDistrict());
        List<Customer> list = districtMap.computeIfAbsent(district, k -> new ArrayList<>());
        list.add(c);
    }

    private void removeFromDistrictIndex(Customer c) {
        String district = safeDistrict(c.getDistrict());
        List<Customer> list = districtMap.get(district);
        if (list != null) {
            list.remove(c);
            if (list.isEmpty()) {
                districtMap.remove(district);
            }
        }
    }
    private String safeDistrict(String district) {
        return district == null ? "" : district;
    }
    public List<Customer> getTopKCustomers(int k, boolean asc) {
        List<Customer> copy = new ArrayList<>(entities);
        copy.sort(new Comparator<Customer>() {
            @Override
            public int compare(Customer a, Customer b) {
                String na = a.getName() == null ? "" : a.getName();
                String nb = b.getName() == null ? "" : b.getName();
                return na.compareTo(nb);
            }
        });

        if (!asc) {
            Collections.reverse(copy);
        }
        int size = copy.size();
        int take = Math.max(0, Math.min(k, size));
        return new ArrayList<>(copy.subList(0, take));
    }
    public List<Customer> listCustomersInDistrict(String district) {
        String key = safeDistrict(district);
        List<Customer> list = districtMap.get(key);
        if (list == null) return Collections.emptyList();
        return Collections.unmodifiableList(list);
    }

    public void rebuildDistrictMap() {
        districtMap.clear();
        for (Customer c : entities) {
            addToDistrictIndex(c);
        }
    }
    public List<Customer> getTopKCustomers(int k, boolean ascending) {
        List<Customer> sorted = new ArrayList<>(entities);
        // Sort by ID
        sorted.sort((c1, c2) -> ascending ? c1.getId().compareTo(c2.getId()) 
                                            :c2.getId().compareTo(c1.getId()));
        return sorted.subList(0, Math.min(k, sorted.size()));
    }
}
