package springapp.datahubaccelerator.domain.repository;

import org.springframework.stereotype.Repository;
import springapp.datahubaccelerator.domain.Field;
import springapp.datahubaccelerator.domain.Input;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class FieldRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void saveField(Field field) {
        entityManager.merge(field);
    }

    public List<Field> getAllFields() {
        return entityManager.createQuery("from Field", Field.class).getResultList();
    }

    @Transactional
    public void generateField(Input input) {
        List<String> targetExtractList = Arrays.asList(input.getTargetExtract().split(" "));
        targetExtractList = targetExtractList.stream().filter(x -> !x.equals("(PK)")).collect(Collectors.toList());
        List<String> datatypeList = Arrays.asList(input.getDatatype().split(" "));
        for (int i = 0; i < targetExtractList.size() - 1; i++) {
            Field field = new Field();
            field.setTargetExtract(targetExtractList.get(i));
            field.setDatatype(datatypeList.get(i));
            saveField(field);
        }
    }
}
