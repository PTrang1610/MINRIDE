package Manager;

import Models.Customer;

import java.util.*;
import java.util.stream.Collectors;


public class CustomerManager extends BaseManager<Customer> {

    // Nhóm khách theo quận/huyện để truy vấn nhanh
    private final Map<String, List<Customer>> districtMap = new HashMap<>();

    // ================== CRUD: đồng bộ districtMap ==================
    @Override
    public void add(Customer c) {
        super.add(Objects.requireNonNull(c, "customer == null"));
        districtMap.computeIfAbsent(safeDistrict(c), k -> new ArrayList<>()).add(c);
    }

    @Override
    public void update(String id, Customer c) {
        Objects.requireNonNull(id, "id == null");
        Objects.requireNonNull(c,  "customer == null");

        Customer old = findById(id);
        if (old != null) {
            List<Customer> oldList = districtMap.get(safeDistrict(old));
            if (oldList != null) oldList.remove(old);
        }
        super.update(id, c);
        districtMap.computeIfAbsent(safeDistrict(c), k -> new ArrayList<>()).add(c);
    }

    @Override
    public void delete(String id) {
        Objects.requireNonNull(id, "id == null");
        Customer old = findById(id);
        if (old != null) {
            List<Customer> oldList = districtMap.get(safeDistrict(old));
            if (oldList != null) oldList.remove(old);
        }
        super.delete(id);
    }

    // ================== APIs riêng cho Customer ==================

    /** Top-k khách theo tên (asc=true: A→Z; false: Z→A) */
    public List<Customer> getTopKCustomersByName(int k, boolean asc) {
        Comparator<Customer> cmp = Comparator
                .comparing(Customer::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(Customer::getId, Comparator.nullsLast(String::compareTo));
        if (!asc) cmp = cmp.reversed();

        int n = entities.size();
        if (k <= 0) return List.of();
        k = Math.min(k, n);

        return entities.stream().sorted(cmp).limit(k).collect(Collectors.toList());
    }

    /** Top-k theo VỊ TRÍ trong danh sách gốc: fromHead=true => đầu; false => cuối */
    public List<Customer> getTopKByPosition(int k, boolean fromHead) {
        int n = entities.size();
        if (k <= 0 || n == 0) return List.of();
        k = Math.min(k, n);
        if (fromHead) return new ArrayList<>(entities.subList(0, k));
        return new ArrayList<>(entities.subList(n - k, n));
    }

    /** Tìm kiếm theo ID (ưu tiên khớp tuyệt đối) hoặc theo tên (contains, không phân biệt hoa/thường) */
    public List<Customer> searchByIdOrName(String query) {
        if (query == null || query.isBlank()) return List.of();
        String needle = query.trim();
        // Ưu tiên ID khớp tuyệt đối
        Customer byId = findById(needle);
        if (byId != null) return List.of(byId);

        String lower = needle.toLowerCase();
        return entities.stream()
                .filter(c -> {
                    String name = c.getName();
                    return name != null && name.toLowerCase().contains(lower);
                })
                .collect(Collectors.toList());
    }

    /** Liệt kê khách thuộc một quận/huyện (read-only list, KHÔNG cho sửa trực tiếp) */
    public List<Customer> listCustomersInDistrict(String district) {
        return Collections.unmodifiableList(
                districtMap.getOrDefault(normalizeDistrict(district), Collections.emptyList())
        );
    }

    /** Trang kết quả: total = tổng số khách trong quận; items = danh sách theo trang */
    public static final class Page<T> {
        public final long total;
        public final List<T> items;
        public Page(long total, List<T> items) { this.total = total; this.items = items; }
    }

    /**
     * Liệt kê khách trong một quận:
     * - Sắp xếp theo ID tăng dần
     * - Mặc định 10 khách/trang
     * - Dùng cho flow: hiển thị tổng + 10 khách đầu, người dùng "xem thêm" -> tăng page
     */
    public Page<Customer> listDistrictByIdAsc(String district, int page, int pageSize) {
        String key = normalizeDistrict(district);
        List<Customer> all = districtMap.getOrDefault(key, Collections.emptyList())
                .stream()
                .sorted(Comparator.comparing(Customer::getId, Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList());

        int n = all.size();
        if (pageSize <= 0) pageSize = 10;
        if (page <= 0) page = 1;

        int from = Math.min((page - 1) * pageSize, n);
        int to   = Math.min(from + pageSize, n);
        return new Page<>(n, all.subList(from, to));
    }

    /** Dựng lại districtMap (gọi khi load dữ liệu từ file/DB) */
    public void rebuildDistrictMap() {
        districtMap.clear();
        for (Customer c : entities) {
            districtMap.computeIfAbsent(safeDistrict(c), k -> new ArrayList<>()).add(c);
        }
    }

    // ================== Helpers ==================
    private static String safeDistrict(Customer c) {
        return normalizeDistrict(c != null ? c.getDistrict() : null);
    }

    private static String normalizeDistrict(String d) {
        return d == null ? "" : d.trim();
    }
}
