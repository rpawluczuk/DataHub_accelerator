package springapp.datahubaccelerator.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import springapp.datahubaccelerator.domain.Field;
import springapp.datahubaccelerator.domain.Input;
import springapp.datahubaccelerator.services.FieldService;
import springapp.datahubaccelerator.services.InputService;

import javax.validation.Valid;
import java.util.List;

@Controller
public class InputController {

    @Autowired
    InputService inputService;

    @Autowired
    FieldService fieldService;

    @RequestMapping(value="/addinput")
    public String addInput(Model model) {
        model.addAttribute("input", new Input());
        return "addinput";
    }

    @RequestMapping(value = "/rowgenerator", method = RequestMethod.POST)
    public String saveStructure(Input input) {
        List<Input> listOfAllInputs = inputService.getAllInputs();
        if (listOfAllInputs.size() >= 1) {
            Input inputToDelete = listOfAllInputs.get(0);
            List<Field> listOfAllFields = fieldService.getAllFields(inputToDelete.getId());
            inputService.saveInput(input);
            for (Field field : listOfAllFields) {
                fieldService.deleteField(field);
            }
            inputService.deleteInput(inputToDelete);
        } else {
            inputService.saveInput(input);
        }
        return "redirect:/rowgenerator";
    }
}
