package springapp.datahubaccelerator.controllers;

import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import springapp.datahubaccelerator.Components.ExcelComponent;
import springapp.datahubaccelerator.domain.Excel;
import springapp.datahubaccelerator.domain.SheetOfExcelInput;
import springapp.datahubaccelerator.domain.repository.ExcelRepository;
import springapp.datahubaccelerator.domain.repository.SheetOfExcelInputRepository;
import springapp.datahubaccelerator.services.ExcelInputService;

import java.util.List;

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

    @RequestMapping(value="/addexcel")
    public String addExcelInput(Model model) {
        model.addAttribute("excelinput", new Excel());
        return "addexcel";
    }

    @RequestMapping(value = "/sheetlist", method = RequestMethod.POST)
    public String saveExcelInput(Excel excel) throws Exception {
        excelInputService.saveDataFromUploadedFile(excel);
//        excelInput.setSheetName(sheetname);
//        excelInputRepository.save(excelInput);
        return "redirect:/sheetlist";
    }

    @RequestMapping(value="/sheetlist")
    public String sheetGenerator(Model model) {
        List<SheetOfExcelInput> allSheets = (List<SheetOfExcelInput>) sheetOfExcelInputRepository.findAll();
        model.addAttribute("allsheets", allSheets);
        return "sheetlist";
    }

    @RequestMapping("//excel/rowgenerator/{id}")
    public String editKeyField(@PathVariable("id") Integer id, Model model) {
        String choosenSheetName = sheetOfExcelInputRepository.findById(id).get().getName();
        Sheet sheet = excelComponent.getListOfSheets().stream()
                .filter(s -> s.getSheetName().equals(choosenSheetName))
                .findFirst().get();
        String sheetName = sheet.getSheetName();
        System.out.println(sheetName);
        return "editkeyfield";
    }
}
