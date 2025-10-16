package Manager;

import java.util.*;

import Interface.IEntity;
import Interface.IRepository;

public abstract class BaseManager<T extends IEntity> implements IRepository<T>{
    protected List<T> entities = new LinkedList<>();
    protected Map<String, T> idMap = new HashMap<>(); // Ánh xạ ID -> entity
    protected Map<String, List<T>> nameMap = new HashMap<>(); // Ánh xạ tên -> danh sách entity
    public Deque<Operation> undoStack = new ArrayDeque<>();

    // Kiểm tra entity và ID
    protected void validateEntity(T entity) {
        if (entity == null || entity.getId() == null || entity.getId().isEmpty()) {
            throw new IllegalArgumentException("Entity hoặc ID không được để trống!");
        }
    }

    // Kiểm tra ID trùng lặp
    protected boolean isDuplicateId(String id) {
        return idMap.containsKey(id);
    }

    @Override
    public void Add(T entity) {
        validateEntity(entity);
        if (isDuplicateId(entity.getId())) {
            throw new IllegalArgumentException("ID " + entity.getId() + " đã tồn tại");
        }
        entities.add(entity);
        idMap.put(entity.getId(), entity);
        nameMap.computeIfAbsent(entity.getName().toLowerCase(), k -> new LinkedList<>()).add(entity);
        saveOperation("ADD", entity);
    }

    @Override
    public void Update(String id, T updatedEntity) {
        validateEntity(updatedEntity);
        if (!id.equals(updatedEntity.getId()) && isDuplicateId(updatedEntity.getId())) {
            throw new IllegalArgumentException("ID " + updatedEntity.getId() + " đã tồn tại!");
        }
        T oldEntity = findById(id);
        if (oldEntity == null) {
            throw new IllegalArgumentException("ID " + id + " không thể tìm thấy!");
        }
        // Cập nhật entities
        for (int i = 0; i < entities.size(); i++) {
            if (entities.get(i).getId().equals(id)) {
                entities.set(i, updatedEntity);
                break;
            }
        }
        // Cập nhật idMap
        idMap.remove(id);
        idMap.put(updatedEntity.getId(), updatedEntity);
        // Cập nhật nameMap
        nameMap.getOrDefault(oldEntity.getName().toLowerCase(), new LinkedList<>()).remove(oldEntity);
        if (nameMap.get(oldEntity.getName().toLowerCase()).isEmpty()) {
            nameMap.remove(oldEntity.getName().toLowerCase());
        }
        nameMap.computeIfAbsent(updatedEntity.getName().toLowerCase(), k -> new LinkedList<>()).add(updatedEntity);
        saveOperation("UPDATE", oldEntity);
    }

    @Override
    public void Delete(String id) {
        T entity = findById(id);
        if (entity == null) {
            throw new IllegalArgumentException("ID " + id + " không thể tìm thấy!");
        }
        entities.remove(entity);
        idMap.remove(id);
        List<T> nameList = nameMap.getOrDefault(entity.getName().toLowerCase(), new LinkedList<>());
        nameList.remove(entity);
        if (nameList.isEmpty()) {
            nameMap.remove(entity.getName().toLowerCase());
        }
        saveOperation("DELETE", entity);
    }

    @Override
    public T findById(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ID không được để trống!");
        }
        return idMap.get(id); // Tìm kiếm O(1) trung bình
    }

    @Override
    public List<T> findByName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Tên không được để trống!");
        }
        return new LinkedList<>(nameMap.getOrDefault(name.toLowerCase(), new LinkedList<>())); // Tìm kiếm O(1)
    }

    @Override
    public List<T> getAll() {
        return new LinkedList<>(entities);
    }

    protected void saveOperation(String type, T entity) {
        undoStack.addLast(new Operation(type, entity));
    }
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public Operation peekUndo() {
        return undoStack.peekLast();
    }
    public void undo() {
        if (undoStack.isEmpty()) {
            System.out.println("Không có thao tác nào để hoàn tác!");
            return;
        }
        Operation op = undoStack.removeLast();
        switch (op.type) {
            case "ADD":
                entities.remove(op.entity);
                idMap.remove(op.entity.getId());
                List<T> nameListAdd = nameMap.getOrDefault(op.entity.getName().toLowerCase(), new LinkedList<>());
                nameListAdd.remove(op.entity);
                if (nameListAdd.isEmpty()) {
                    nameMap.remove(op.entity.getName().toLowerCase());
                }
                break;
            case "DELETE":
                entities.add(op.entity);
                idMap.put(op.entity.getId(), op.entity);
                nameMap.computeIfAbsent(op.entity.getName().toLowerCase(), k -> new LinkedList<>()).add(op.entity);
                break;
            case "UPDATE":
                for (int i = 0; i < entities.size(); i++) {
                    if (entities.get(i).getId().equals(op.entity.getId())) {
                        T updatedEntity = entities.get(i);
                        entities.set(i, op.entity);
                        idMap.put(op.entity.getId(), op.entity);
                        nameMap.getOrDefault(updatedEntity.getName().toLowerCase(), new LinkedList<>()).remove(updatedEntity);
                        if (nameMap.get(updatedEntity.getName().toLowerCase()).isEmpty()) {
                            nameMap.remove(updatedEntity.getName().toLowerCase());
                        }
                        nameMap.computeIfAbsent(op.entity.getName().toLowerCase(), k -> new LinkedList<>()).add(op.entity);
                        break;
                    }
                }
                break;
        }
        System.out.println("Hoàn tác thành công: " + op.type);
    }

    public class Operation {
        String type;
        T entity;
        public long timestamp;

        Operation(String type, T entity) {
            this.type = type;
            this.entity = entity;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
