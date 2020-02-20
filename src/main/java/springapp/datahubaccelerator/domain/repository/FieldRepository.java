package springapp.datahubaccelerator.domain.repository;

import org.springframework.stereotype.Repository;
import springapp.datahubaccelerator.domain.Field;
import springapp.datahubaccelerator.domain.Input;
import springapp.datahubaccelerator.generators.ScriptGeneratorForCreatingEntitiesPCCC;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
        List<String> reasonAddedList = Arrays.asList(input.getReasonAdded().split("\r\n"));
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
            field.setReasonAdded(reasonAddedList.get(i));
            saveField(field);
        }
    }

    @Transactional
    public void addField(Field field) {
        saveField(field);
    }

    public String generateDDLScript(List<Field> allFields) {
        ScriptGeneratorForCreatingEntitiesPCCC scriptGeneratorForCreatingEntitiesPCCC = new ScriptGeneratorForCreatingEntitiesPCCC();
        return scriptGeneratorForCreatingEntitiesPCCC.generateTRFPart(allFields) +
                scriptGeneratorForCreatingEntitiesPCCC.generateODSBasePart(allFields) +
                scriptGeneratorForCreatingEntitiesPCCC.generateODSDeltaPart(allFields) +
                scriptGeneratorForCreatingEntitiesPCCC.generateConstraintsPart(allFields) +
                scriptGeneratorForCreatingEntitiesPCCC.generateIndexesPart(allFields);
    }


    public String generateDMLScript(List<Field> allFields) {
        ScriptGeneratorForCreatingEntitiesPCCC scriptGeneratorForCreatingEntitiesPCCC = new ScriptGeneratorForCreatingEntitiesPCCC();
        return scriptGeneratorForCreatingEntitiesPCCC.generateDMLscript(allFields);
    }

    public String generateTest(List<Field> allFields) {
        String targetExtractList = "List<String> targetExtractList = Arrays.asList(";
        String columnNameList = "List<String> columnNameList = Arrays.asList(";
        String datatypeList = "List<String> datatypeList = Arrays.asList(";
        String scdTypeList = "List<String> scdTypeList = Arrays.asList(";
        String generalRuleAppliedList = "List<String> generalRuleAppliedList = Arrays.asList(";
        String reasonAddedList = "List<String> reasonAddedList = Arrays.asList(";
        for (Field allField : allFields) {
            targetExtractList = targetExtractList.concat("\"".concat(allField.getTargetExtract().concat("\"")));
            columnNameList = columnNameList .concat("\"".concat(allField.getColumnName().concat("\"")));
            datatypeList = datatypeList.concat("\"".concat(allField.getDatatype().concat("\"")));
            scdTypeList = scdTypeList.concat("\"".concat(allField.getScdType().concat("\"")));
            generalRuleAppliedList = generalRuleAppliedList.concat("\"".concat(allField.getGeneralRuleApplied().concat("\"")));
            reasonAddedList = reasonAddedList.concat("\"".concat(allField.getReasonAdded().concat("\"")));
        }
        return targetExtractList.replace("\"\"", "\", \"") + ");\n" +
                columnNameList.replace("\"\"", "\", \"")  + ");\n" +
                datatypeList.replace("\"\"", "\", \"")  + ");\n" +
                scdTypeList.replace("\"\"", "\", \"")  + ");\n" +
                generalRuleAppliedList.replace("\"\"", "\", \"")  + ");\n" +
                reasonAddedList.replace("\"\"", "\", \"")  + ");";
    }
}
