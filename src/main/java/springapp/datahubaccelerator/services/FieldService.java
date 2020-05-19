package springapp.datahubaccelerator.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import springapp.datahubaccelerator.domain.Field;
import springapp.datahubaccelerator.domain.repository.FieldRepository;
import springapp.datahubaccelerator.generators.ScriptGenerator;
import springapp.datahubaccelerator.generators.ScriptGeneratorForCreatingEntitiesBC;
import springapp.datahubaccelerator.generators.ScriptGeneratorForCreatingEntitiesPCCC;
import springapp.datahubaccelerator.generators.ScriptGeneratorForEditingEntitiesPCCC;

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

    public List<String> generateScripts(List<Field> allFields) {
        if (isNewEntity(allFields) && !isBC(allFields.get(0))){
            ScriptGeneratorForCreatingEntitiesPCCC scriptGeneratorForCreatingEntitiesPCCC =
                    new ScriptGeneratorForCreatingEntitiesPCCC(allFields);
            return Arrays.asList(
                    scriptGeneratorForCreatingEntitiesPCCC.generateDDLScript()
                    ,scriptGeneratorForCreatingEntitiesPCCC.generateDMLScript());
        } else if ((!isNewEntity(allFields) && !isBC(allFields.get(0)))){
            ScriptGeneratorForEditingEntitiesPCCC scriptGeneratorForEditingEntitiesPCCC =
                    new ScriptGeneratorForEditingEntitiesPCCC(allFields);
            return Arrays.asList(scriptGeneratorForEditingEntitiesPCCC.generateDDLScript()
                    ,scriptGeneratorForEditingEntitiesPCCC.generateDMLScript());
        } else {
            ScriptGeneratorForCreatingEntitiesBC scriptGeneratorForCreatingEntitiesBC =
                    new ScriptGeneratorForCreatingEntitiesBC(allFields);
            return Arrays.asList(scriptGeneratorForCreatingEntitiesBC.generateDDLScript()
                    ,scriptGeneratorForCreatingEntitiesBC.generateDMLScript());
        }
    }

    public boolean isNewEntity(List<Field> allFields) {
        for (Field field : allFields) {
            if(!field.getReasonAdded().equals(allFields.get(0).getReasonAdded()))
                return false;
        }
        return true;
    }

    public boolean isBC(Field field) {
        if (field.getSourceTable().substring(0, 2).equalsIgnoreCase("BC")){
            return true;
        }
        return false;
    }


    public Field findFieldById(Integer id) {
        return fieldRepository.findById(id).get();
    }
}
