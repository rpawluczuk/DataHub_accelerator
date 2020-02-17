package springapp.datahubaccelerator.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import springapp.datahubaccelerator.domain.Input;
import springapp.datahubaccelerator.services.InputService;

@Controller
public class RowController {

    @Autowired
    InputService inputService;

    @RequestMapping("/rowgenerator")
    public String generateColumns(Model model){
        Input input = inputService.getLastInput();
        model.addAttribute("input", input);
        return "rowgenerator";
    }
}
