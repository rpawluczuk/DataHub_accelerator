package springapp.datahubaccelerator.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import springapp.datahubaccelerator.domain.Field;
import springapp.datahubaccelerator.domain.Input;
import springapp.datahubaccelerator.services.InputService;
import springapp.datahubaccelerator.services.FieldService;

import java.util.List;

@Controller
public class FieldController {

    @Autowired
    InputService inputService;

    @Autowired
    FieldService fieldService;

    @RequestMapping("/rowgenerator")
    public String generateColumns(Model model){
        Input input = inputService.getLastInput();
        fieldService.generateField(input);
        Field.setUserStoryNumber(input.getUserStoryNumber());
        Field.setEntityName(input.getEntityName());
        List<Field> allFields = fieldService.getAllFields();
        model.addAttribute("fields", allFields);
        return "rowgenerator";
    }

    @RequestMapping("/scriptgenerator")
    public String generateScript(Model model){
        List<Field> allFields = fieldService.getAllFields();
        String script = fieldService.generateScript(allFields);
        model.addAttribute("script", script);
        return "scriptgenerator";
    }
}