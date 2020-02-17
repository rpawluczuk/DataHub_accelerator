package springapp.datahubaccelerator.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import springapp.datahubaccelerator.domain.Input;
import springapp.datahubaccelerator.services.InputService;

import javax.validation.Valid;

@Controller
public class InputController {

    @Autowired
    InputService inputService;

    @RequestMapping("/addinput")
    public String addInput(Model model){
        model.addAttribute("input", new Input());
        return "addinput";
    }

    @RequestMapping(value = "/columngenerator", method = RequestMethod.POST)
    public String saveStructure(Input input){
            inputService.saveInput(input);
            return "redirect:/columngenerator";
    }

    @RequestMapping("/columngenerator")
    public String generateColumns(Model model){
        model.addAttribute("input", new Input());
        return "columngenerator";
    }
}
