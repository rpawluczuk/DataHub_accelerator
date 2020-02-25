package springapp.datahubaccelerator.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import springapp.datahubaccelerator.domain.Field;
import springapp.datahubaccelerator.domain.Input;
import springapp.datahubaccelerator.domain.repository.FieldRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class FieldService {

    @Autowired
    FieldRepository fieldRepository;

    public ArrayList<Field> getAllFields(){
        return new ArrayList<Field>(fieldRepository.getAllFields());
    }

    public void generateField(Input input) {
        fieldRepository.generateField(input);
    }

    public List<String> generateScripts(List<Field> allFields) {
        return fieldRepository.generateScripts(allFields);
    }

    public void addField(Field field) {
        fieldRepository.saveField(field);
    }

    public String generateTest(List<Field> allFields) {
        return fieldRepository.generateTest(allFields);
    }

    public boolean isNewEntity(List<Field> allFields) {
        return fieldRepository.isNewEntity(allFields);
    }
}
