package Interface;
import java.util.List;

public interface IRepository<T extends IEntity> {
    void Add(T item);
    void Update(String id, T entity);
    void Delete(String id);
    T findById (String id);
    List<T> findByName(String name);
    List<T> getAll();
}
