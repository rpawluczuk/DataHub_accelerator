package springapp.datahubaccelerator.controllers;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import springapp.datahubaccelerator.Components.ExcelComponent;
import springapp.datahubaccelerator.Components.FormHandlerComponent;
import springapp.datahubaccelerator.domain.Excel;
import springapp.datahubaccelerator.domain.Field;
import springapp.datahubaccelerator.domain.SheetOfExcelInput;
import springapp.datahubaccelerator.domain.repository.ExcelRepository;
import springapp.datahubaccelerator.domain.repository.FieldRepository;
import springapp.datahubaccelerator.domain.repository.SheetOfExcelInputRepository;
import springapp.datahubaccelerator.generators.ScriptGenerator;
import springapp.datahubaccelerator.services.ExcelInputService;
import springapp.datahubaccelerator.services.FieldService;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class ExcelInputController {

    @Autowired
    ExcelRepository excelRepository;

    @Autowired
    SheetOfExcelInputRepository sheetOfExcelInputRepository;

    @Autowired
    ExcelInputService excelInputService;

    @Autowired
    ExcelComponent excelComponent;

    @Autowired
    FormHandlerComponent formHandlerComponent;

    @Autowired
    FieldRepository fieldRepository;

    @Autowired
    FieldService fieldService;

    @RequestMapping(value = "/addinput")
    public String addExcelInput(Model model) {
        model.addAttribute("excelinput", new Excel());
        return "addexcel";
    }

    @RequestMapping(value = "/sheetlist", method = RequestMethod.POST)
    public String saveExcelInput(Excel excel) throws Exception {
        fieldRepository.deleteAll();
        sheetOfExcelInputRepository.deleteAll();
        excelInputService.saveDataFromUploadedFile(excel);
        return "redirect:/sheetlist";
    }

    @RequestMapping(value = "/sheetlist")
    public String sheetGenerator(Model model) {
        List<SheetOfExcelInput> allSheets = (List<SheetOfExcelInput>) sheetOfExcelInputRepository.findAll();
        model.addAttribute("allsheets", allSheets);
        return "sheetlist";
    }

    @RequestMapping(value = "/excel/rowgenerator/{id}")
    public String generateRowsFromExcel(@PathVariable("id") Integer id, Model model) {
        String choosenSheetName = sheetOfExcelInputRepository.findById(id).get().getName();
        Sheet sheet = excelComponent.getListOfSheets().stream()
                .filter(s -> s.getSheetName().equals(choosenSheetName))
                .findFirst().get();
        excelInputService.extractFieldsFromExcel(sheet);
        return "redirect:/excel/rowgenerator";
    }

    @RequestMapping("/excel/rowgenerator")
    public String generateColumns(Model model) {
        List<Field> allFields = (List<Field>) fieldRepository.findAll();
        model.addAttribute("fields", allFields);
        Set<String> allUserStories = allFields.stream()
                .map(x -> x.getReasonAdded())
                .collect(Collectors.toSet());
        if (allUserStories.size() <= 1){
            this.formHandlerComponent.setSelectedUserStories(allUserStories);
            return "redirect:/keyfields";
        }
        model.addAttribute("userStories", allUserStories);
        model.addAttribute("formHandlerComponent", formHandlerComponent);
        return "rowgenerator";
    }

    @RequestMapping(value = "/handleSelectedUserStories", method = RequestMethod.POST)
    public String handleSelectedUserStories(
            @Valid @ModelAttribute("formHandlerComponent") FormHandlerComponent formHandlerComponent, BindingResult bindingResult) {
        this.formHandlerComponent.setSelectedUserStories(formHandlerComponent.getSelectedUserStories());
        return "redirect:/keyfields";
    }

    @RequestMapping("/keyfields")
    public String checkJoinedTables(Model model) {
        List<Field> allFields = (List<Field>) fieldRepository.findAll();
        String primaryKeyColumn = allFields.get(0).getColumnName();
        List<Field> keyFields = allFields.stream()
                .filter(f -> f.getJoinedTable() != null)
                .filter(x -> formHandlerComponent.getSelectedUserStories().contains(x.getReasonAdded()) ||
                        x.getColumnName().equals(primaryKeyColumn))
                .collect(Collectors.toList());
        model.addAttribute("keyfields", keyFields);
        return "keyfields";
    }

    @RequestMapping("/keyfields/edit/{id}")
    public String editKeyField(@PathVariable("id") Integer id, Model model) {
        Field keyFieldToEdit = fieldService.findFieldById(id);
        model.addAttribute("keyfield", keyFieldToEdit);
        return "editkeyfield";
    }

    @RequestMapping(value = "/keyfields", method = RequestMethod.POST)
    public String saveKeyField(@Valid @ModelAttribute("keyfield") Field keyField, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
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

    @RequestMapping("/scriptgenerator")
    public String generateScript(Model model) {
        List<Field> allFields = (List<Field>) fieldRepository.findAll();
        String ddlScript = fieldService.generateScripts(allFields).get(0);
        model.addAttribute("ddlscript", ddlScript);
        String dmlScript = fieldService.generateScripts(allFields).get(1);
        model.addAttribute("dmlscript", dmlScript);
        return "scriptgenerator";
    }
}
