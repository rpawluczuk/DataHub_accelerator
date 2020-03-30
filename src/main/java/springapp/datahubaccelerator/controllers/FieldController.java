//package springapp.datahubaccelerator.controllers;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.validation.BindingResult;
//import org.springframework.web.bind.annotation.ModelAttribute;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import springapp.datahubaccelerator.domain.Field;
//import springapp.datahubaccelerator.domain.Input;
//import springapp.datahubaccelerator.generators.ScriptGenerator;
//import springapp.datahubaccelerator.services.InputService;
//import springapp.datahubaccelerator.services.FieldService;
//
//import javax.validation.Valid;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Controller
//@RequestMapping("/dh")
//public class FieldController {
//
//    @Autowired
//    InputService inputService;
//
//    @Autowired
//    FieldService fieldService;
//
//    @RequestMapping("/rowgenerator")
//    public String generateColumns(Model model) {
//        Input input = inputService.getLastInput();
//        Integer inputId = input.getId();
//        fieldService.generateField(input);
//        List<Field> allFields = fieldService.getAllFields(inputId);
//        Integer newestUserStoryNumber = ScriptGenerator.getNewestUserStoryNumber(allFields);
//        List<Field> newFields = allFields.stream()
//                .filter(f -> f.getReasonAdded().contains(newestUserStoryNumber.toString()))
//                .collect(Collectors.toList());
//        model.addAttribute("fields", newFields);
//        return "rowgenerator";
//    }
//
//    @RequestMapping("/keyfields")
//    public String checkJoinedTables(Model model) {
//        Integer inputId = inputService.getLastInput().getId();
//        List<Field> allFields = fieldService.getAllFields(inputId);
//        Integer newestUserStoryNumber = ScriptGenerator.getNewestUserStoryNumber(allFields);
//        List<Field> newFields = allFields.stream()
//                .filter(f -> f.getReasonAdded().contains(newestUserStoryNumber.toString()))
//                .collect(Collectors.toList());
//        List<Field> keyFields = newFields.stream().filter(f -> f.getJoinedTable()!=null).collect(Collectors.toList());
//        model.addAttribute("keyfields", keyFields);
//        return "keyfields";
//    }
//
//    @RequestMapping("/keyfields/edit/{id}")
//    public String editKeyField(@PathVariable("id") Integer id, Model model) {
//        Field keyFieldToEdit = fieldService.getField(id);
//        model.addAttribute("keyfield", keyFieldToEdit);
//        return "editkeyfield";
//    }
//
//    @RequestMapping(value = "/keyfields", method = RequestMethod.POST)
//    public String saveKeyField(@Valid @ModelAttribute("keyfield") Field keyField, BindingResult bindingResult) {
//        if(bindingResult.hasErrors()){
//            System.out.println("There were errors");
//            bindingResult.getAllErrors().forEach(error -> {
//                System.out.println(error.getObjectName() + " " + error.getDefaultMessage());
//            });
//            return "editkeyfield";
//        } else {
//            fieldService.saveField(keyField);
//            return "redirect:/keyfields";
//        }
//    }
//
//    @RequestMapping("/scriptgenerator")
//    public String generateScript(Model model) {
//        Input input = inputService.getLastInput();
//        Integer inputId = input.getId();
//        List<Field> allFields = fieldService.getAllFields(inputId);
//        String ddlScript = fieldService.generateScripts(allFields).get(0);
//        model.addAttribute("ddlscript", ddlScript);
//        String dmlScript = fieldService.generateScripts(allFields).get(1);
//        model.addAttribute("dmlscript", dmlScript);
//        return "scriptgenerator";
//    }
//
//    @RequestMapping("/testgenerator")
//    public String generateTest(Model model) {
//        Input input = inputService.getLastInput();
//        Integer inputId = input.getId();
//        List<Field> allFields = fieldService.getAllFields(inputId);
//        String testCase = fieldService.generateTest(allFields);
//        model.addAttribute("testcase", testCase);
//        return "testgenerator";
//    }
//}
