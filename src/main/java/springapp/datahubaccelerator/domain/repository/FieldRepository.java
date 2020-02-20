package springapp.datahubaccelerator.domain.repository;

import org.springframework.stereotype.Repository;
import springapp.datahubaccelerator.domain.Field;
import springapp.datahubaccelerator.domain.Input;
import springapp.datahubaccelerator.generators.ScriptGenerator;

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
        List<String> targetExtractList = Arrays.asList(input.getTargetExtract().split("\r\n"));
        List<String> columnNameList = Arrays.asList(input.getColumnName().split("\r\n"));
        List<String> datatypeList = Arrays.asList(input.getDatatype().split("\r\n"));
        List<String> scdTypeList = Arrays.asList(input.getScdType().split("\r\n"));
        List<String> generalRuleAppliedList = Arrays.asList(input.getGeneralRuleApplied().split("\r\n"));
        for (int i = 0; i < targetExtractList.size(); i++) {
            Field field = new Field();
            field.setTargetExtract(targetExtractList.get(i));
            field.setColumnName(columnNameList.get(i));
            field.setDatatype(datatypeList.get(i));
            field.setScdType(scdTypeList.get(i));
            try {
                field.setGeneralRuleApplied(generalRuleAppliedList.get(i));
            }
            catch(ArrayIndexOutOfBoundsException e) {
                field.setGeneralRuleApplied("");
            }
            saveField(field);
        }
    }

    public String generateDDLScript(List<Field> allFields) {
        ScriptGenerator scriptGenerator = new ScriptGenerator();
        return scriptGenerator.generateTRFPart(allFields) +
                scriptGenerator.generateODSBasePart(allFields) +
                scriptGenerator.generateODSDeltaPart(allFields) +
                scriptGenerator.generateConstraintsPart(allFields) +
                scriptGenerator.generateIndexesPart(allFields);
    }


}
