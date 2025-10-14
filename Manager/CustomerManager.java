package Manager;

import Models.Customer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Quản lý Customer: CRUD + truy vấn theo district + top-K theo tên.
 * Kế thừa BaseManager<Customer> đã có sẵn trong repo của bạn.
 */
public class CustomerManager extends BaseManager<Customer> {

    //Group customers by district for quick query
    private final Map<String, List<Customer>> districtMap = new HashMap<>();

    @Override
    public void Add(Customer c) {
        super.Add(c);
        districtMap.computeIfAbsent(c.getDistrict(), k -> new ArrayList<>()).add(c);
    }

    @Override
    public void Update(String id, Customer c) {
        Customer old = findById(id);
        if (old != null) {
            List<Customer> oldList = districtMap.get(old.getDistrict());
            if (oldList != null) oldList.remove(old);
        }
        super.Update(id, c);
        districtMap.computeIfAbsent(c.getDistrict(), k -> new ArrayList<>()).add(c);
    }

    @Override
    public void Delete(String id) {
        Customer old = findById(id);
        if (old != null) {
            List<Customer> oldList = districtMap.get(old.getDistrict());
            if (oldList != null) oldList.remove(old);
        }
        super.Delete(id);
    }

    //Personal Methods
    // Top-K customer according to name (asc=true: A→Z; false: Z→A)
    public List<Customer> getTopKCustomers(int k, boolean asc) {
        Comparator<Customer> cmp = Comparator.comparing(Customer::getName);
        if (!asc) cmp = cmp.reversed();
        return entities.stream().sorted(cmp).limit(k).collect(Collectors.toList());
    }

    // List customer in a district
    public List<Customer> listCustomersInDistrict(String district) {
        return Collections.unmodifiableList(
                districtMap.getOrDefault(district, Collections.emptyList())
        );
    }


    public void rebuildDistrictMap() {
        districtMap.clear();
        for (Customer c : entities) {
            districtMap.computeIfAbsent(c.getDistrict(), k -> new ArrayList<>()).add(c);
        }
    }
}
