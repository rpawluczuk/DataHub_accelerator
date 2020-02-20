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

    public String generateDDLScript(List<Field> allFields) {
        return fieldRepository.generateDDLScript(allFields);
    }

    public String generateDMLScript(List<Field> allFields) {
        return fieldRepository.generateDMLScript(allFields);
    }
}
