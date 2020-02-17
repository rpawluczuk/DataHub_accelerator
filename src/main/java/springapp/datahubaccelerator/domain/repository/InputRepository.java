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
}
