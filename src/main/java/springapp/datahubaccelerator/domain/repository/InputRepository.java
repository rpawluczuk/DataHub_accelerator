package springapp.datahubaccelerator.domain.repository;

import org.springframework.stereotype.Repository;
import springapp.datahubaccelerator.domain.Input;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

@Repository
public class InputRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void saveInput(Input input) {
        entityManager.merge(input);
    }

    @Transactional
    public Input getLastInput() {
        List<Input> allInputsList = entityManager.createQuery("from Input", Input.class).getResultList();
        int lastId = allInputsList.get(allInputsList.size() - 1).getId();
        return entityManager.find(Input.class, lastId);
    }

    @Transactional
    public List<Input> getAllInputs() {
        return entityManager.createQuery("from Input", Input.class).getResultList();
    }

    @Transactional
    public void deleteInput(Input input) {
        entityManager.remove(input);
    }
}
