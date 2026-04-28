package something.ru.NauGram.repository;

import org.springframework.data.repository.CrudRepository;
import something.ru.NauGram.model.User;

public interface UserRepository extends CrudRepository<User, Long> {
}
