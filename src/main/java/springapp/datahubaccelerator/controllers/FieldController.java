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
    public String generateColumns(Model model) {
        Input input = inputService.getLastInput();
        Integer inputId = input.getId();
        fieldService.generateField(input);
        List<Field> allFields = fieldService.getAllFields(inputId);
        model.addAttribute("fields", allFields);
        return "rowgenerator";
    }

    @RequestMapping("/scriptgenerator")
    public String generateScript(Model model) {
        Input input = inputService.getLastInput();
        Integer inputId = input.getId();
        List<Field> allFields = fieldService.getAllFields(inputId);
        String ddlScript = fieldService.generateScripts(allFields).get(0);
        model.addAttribute("ddlscript", ddlScript);
        String dmlScript = fieldService.generateScripts(allFields).get(1);
        model.addAttribute("dmlscript", dmlScript);
        return "scriptgenerator";
    }

    @RequestMapping("/testgenerator")
    public String generateTest(Model model) {
        Input input = inputService.getLastInput();
        Integer inputId = input.getId();
        List<Field> allFields = fieldService.getAllFields(inputId);
        String testCase = fieldService.generateTest(allFields);
        model.addAttribute("testcase", testCase);
        return "testgenerator";
    }
}
