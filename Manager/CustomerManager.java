package Manager;

import Models.Customer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Quản lý Customer: CRUD + truy vấn theo district + top-K theo tên.
 * Kế thừa BaseManager<Customer> đã có sẵn trong repo của bạn.
 */
public class CustomerManager extends BaseManager<Customer> {

    // phân nhóm khách theo quận/huyện để truy vấn nhanh
    private final Map<String, List<Customer>> districtMap = new HashMap<>();

    // === CRUD kế thừa và mở rộng để đồng bộ districtMap ===
    @Override
    public void add(Customer c) {
        super.add(c);
        districtMap.computeIfAbsent(c.getDistrict(), k -> new ArrayList<>()).add(c);
    }

    @Override
    public void update(String id, Customer c) {
        Customer old = findById(id);
        if (old != null) {
            List<Customer> oldList = districtMap.get(old.getDistrict());
            if (oldList != null) oldList.remove(old);
        }
        super.update(id, c);
        districtMap.computeIfAbsent(c.getDistrict(), k -> new ArrayList<>()).add(c);
    }

    @Override
    public void delete(String id) {
        Customer old = findById(id);
        if (old != null) {
            List<Customer> oldList = districtMap.get(old.getDistrict());
            if (oldList != null) oldList.remove(old);
        }
        super.delete(id);
    }

    // === APIs riêng cho Customer ===

    /** Top-k khách theo tên (asc=true: A→Z; false: Z→A) */
    public List<Customer> getTopKCustomers(int k, boolean asc) {
        Comparator<Customer> cmp = Comparator.comparing(Customer::getName);
        if (!asc) cmp = cmp.reversed();
        return entities.stream().sorted(cmp).limit(k).collect(Collectors.toList());
    }

    /** Liệt kê khách thuộc một quận/huyện */
    public List<Customer> listCustomersInDistrict(String district) {
        return Collections.unmodifiableList(
                districtMap.getOrDefault(district, Collections.emptyList())
        );
    }

    /** Dựng lại districtMap (gọi khi load dữ liệu từ file chẳng hạn) */
    public void rebuildDistrictMap() {
        districtMap.clear();
        for (Customer c : entities) {
            districtMap.computeIfAbsent(c.getDistrict(), k -> new ArrayList<>()).add(c);
        }
    }
}
