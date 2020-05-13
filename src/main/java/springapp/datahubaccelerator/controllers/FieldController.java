package springapp.datahubaccelerator.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import springapp.datahubaccelerator.domain.Field;
import springapp.datahubaccelerator.services.FieldService;

import javax.validation.Valid;
import java.util.List;

@Controller
public class FieldController {

    @Autowired
    FieldService fieldService;

    @RequestMapping("/keyfields/edit/{id}")
    public String editKeyField(@PathVariable("id") Integer id, Model model) {
        Field keyFieldToEdit = fieldService.findFieldById(id);
        model.addAttribute("keyfield", keyFieldToEdit);
        return "editkeyfield";
    }

    @RequestMapping(value = "/keyfields", method = RequestMethod.POST)
    public String saveKeyField(@Valid @ModelAttribute("keyfield") Field keyField, BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            System.out.println("There were errors");
            bindingResult.getAllErrors().forEach(error -> {
                System.out.println(error.getObjectName() + " " + error.getDefaultMessage());
            });
            return "editkeyfield";
        } else {
            fieldService.saveField(keyField);
            return "redirect:/keyfields";
        }
    }
}
