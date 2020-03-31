package springapp.datahubaccelerator.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import springapp.datahubaccelerator.domain.Field;
import springapp.datahubaccelerator.domain.Input;
import springapp.datahubaccelerator.domain.repository.FieldRepository;
import springapp.datahubaccelerator.generators.ScriptGenerator;
import springapp.datahubaccelerator.generators.ScriptGeneratorForCreatingEntitiesPCCC;
import springapp.datahubaccelerator.generators.ScriptGeneratorForEditingEntitiesPCCC;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FieldService {

    @Autowired
    FieldRepository fieldRepository;

    public void saveField(Field field) {
        fieldRepository.save(field);
    }

    public void deleteField(Field field) {
        fieldRepository.delete(field);
    }

    public List<Field> findAllFieldsByInputId(Integer inputId) {
        return ((List<Field>) fieldRepository.findAll()).stream()
                .filter(f -> f.getInput().getId().equals(inputId)).collect(Collectors.toList());
    }

    public void generateField(Input input) {
        List<String> targetExtractList = Arrays.asList(input.getTargetExtract().split("\r\n"));
        List<String> columnNameList = Arrays.asList(input.getColumnName().split("\r\n"));
        List<String> datatypeList = Arrays.asList(input.getDatatype().split("\r\n"));
        List<String> sourceTableList = Arrays.asList(input.getSourceTable().split("\r\n"));
        List<String> scdTypeList = Arrays.asList(input.getScdType().split("\r\n"));
        List<String> generalRuleAppliedList = Arrays.asList(input.getGeneralRuleApplied().split("\r\n"));
        List<String> reasonAddedList = Arrays.asList(input.getReasonAdded().split("\r\n"));
        for (int i = 0; i < targetExtractList.size(); i++) {
            Field field = new Field();
            field.setInput(input);
            field.setTargetExtract(targetExtractList.get(i).toUpperCase().replace("BDE_", ""));
            String columnName = columnNameList.get(i).replace("(FK)", "")
                    .replace("(PK)", "").trim();
            field.setColumnName(columnName);
            field.setDatatype(datatypeList.get(i));
            field.setSourceTable(sourceTableList.get(i));
            field.setScdType(scdTypeList.get(i));
            try {
                field.setGeneralRuleApplied(generalRuleAppliedList.get(i));
            }
            catch(ArrayIndexOutOfBoundsException e) {
                field.setGeneralRuleApplied("");
            }
            field.setReasonAdded(reasonAddedList.get(i));
            if (columnNameList.get(i).contains("KEY")){
                String joinedTable;
                String primaryKeyOfJoinedTable;
                if(i == 0){
                    joinedTable = "Z_CS_" + field.getTargetExtract() + "_BASE";
                    primaryKeyOfJoinedTable = field.getColumnName();
                }else{
                    joinedTable = ScriptGenerator.generateJoinedTableName(sourceTableList.get(i));
                    primaryKeyOfJoinedTable = ScriptGenerator.generatePrimaryKeyOfJoinedTableName(sourceTableList.get(i));
                }
                field.setJoinedTable(joinedTable);
                field.setPrimaryKeyOfJoinedTable(primaryKeyOfJoinedTable);
            }
            saveField(field);
        }
    }

    public List<String> generateScripts(List<Field> allFields) {
        ScriptGeneratorForCreatingEntitiesPCCC scriptGeneratorForCreatingEntitiesPCCC =
                new ScriptGeneratorForCreatingEntitiesPCCC(allFields);
        ScriptGeneratorForEditingEntitiesPCCC scriptGeneratorForEditingEntitiesPCCC =
                new ScriptGeneratorForEditingEntitiesPCCC(allFields);
        if (isNewEntity(allFields)){
            return Arrays.asList(
                    scriptGeneratorForCreatingEntitiesPCCC.generateDDLScript()
                    ,scriptGeneratorForCreatingEntitiesPCCC.generateDMLScript());
        } else {
            return Arrays.asList(scriptGeneratorForEditingEntitiesPCCC.generateDDLScript()
                    ,scriptGeneratorForEditingEntitiesPCCC.generateDMLScript());
        }
    }

    public boolean isNewEntity(List<Field> allFields) {
        for (Field field : allFields) {
            if(!field.getReasonAdded().equals(allFields.get(0).getReasonAdded()))
                return false;
        }
        return true;
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

    public Field findFieldById(Integer id) {
        return fieldRepository.findById(id).get();
    }
}
