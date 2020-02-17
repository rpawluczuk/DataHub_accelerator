package springapp.datahubaccelerator.domain.repository;

import org.springframework.stereotype.Repository;
import springapp.datahubaccelerator.domain.Input;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

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
        int lastId = entityManager.createQuery("from Input", Input.class).getResultList().size();
        return entityManager.find(Input.class, lastId);
    }
}
