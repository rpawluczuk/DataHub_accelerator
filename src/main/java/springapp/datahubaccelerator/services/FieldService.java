package springapp.datahubaccelerator.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import springapp.datahubaccelerator.Components.FormHandlerComponent;
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

    @Autowired
    FormHandlerComponent formHandlerComponent;

    public void saveField(Field field) {
        fieldRepository.save(field);
    }

    public void deleteField(Field field) {
        fieldRepository.delete(field);
    }

    public List<String> generateScripts(List<Field> allFields) {
        List<Field> selectedFields;
        if (formHandlerComponent.getSelectedUserStories().size() > 0) {
            selectedFields = allFields.stream()
                    .filter(x -> formHandlerComponent.getSelectedUserStories().contains(x.getReasonAdded()))
                    .collect(Collectors.toList());
        } else if (!isNewEntity(allFields)){
                Integer newestUserStoryNumber = getNewestUserStoryNumber(allFields);
                 selectedFields = allFields.stream()
                        .filter(f -> f.getReasonAdded().contains(newestUserStoryNumber.toString()))
                        .collect(Collectors.toList());
        } else {
            selectedFields = allFields;
        }
        if (!isBC(allFields.get(0))){
            if (allFields.size() == selectedFields.size()){
                ScriptGeneratorForCreatingEntitiesPCCC scriptGeneratorForCreatingEntitiesPCCC =
                        new ScriptGeneratorForCreatingEntitiesPCCC(selectedFields);
                return Arrays.asList(
                        scriptGeneratorForCreatingEntitiesPCCC.generateDDLScript()
                        ,scriptGeneratorForCreatingEntitiesPCCC.generateDMLScript());
            } else {
                ScriptGeneratorForEditingEntitiesPCCC scriptGeneratorForEditingEntitiesPCCC =
                        new ScriptGeneratorForEditingEntitiesPCCC(selectedFields);
                return Arrays.asList(scriptGeneratorForEditingEntitiesPCCC.generateDDLScript()
                        ,scriptGeneratorForEditingEntitiesPCCC.generateDMLScript());
            }
        } else {
            ScriptGeneratorForCreatingEntitiesBC scriptGeneratorForCreatingEntitiesBC =
                    new ScriptGeneratorForCreatingEntitiesBC(selectedFields);
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

    public static Integer getNewestUserStoryNumber(List<Field> allFields) {
        return allFields.stream()
                .filter(f -> !f.getReasonAdded().toUpperCase().equals("BASE"))
                .map(f -> f.getReasonAdded().replace("P17152-", ""))
                .map(s -> Integer.valueOf(s))
                .max(Integer::compare).get();
    }


    public Field findFieldById(Integer id) {
        return fieldRepository.findById(id).get();
    }
}
