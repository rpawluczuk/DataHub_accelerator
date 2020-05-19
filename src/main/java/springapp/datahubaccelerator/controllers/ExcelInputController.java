package springapp.datahubaccelerator.controllers;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import springapp.datahubaccelerator.Components.ExcelComponent;
import springapp.datahubaccelerator.domain.Excel;
import springapp.datahubaccelerator.domain.Field;
import springapp.datahubaccelerator.domain.SheetOfExcelInput;
import springapp.datahubaccelerator.domain.repository.ExcelRepository;
import springapp.datahubaccelerator.domain.repository.FieldRepository;
import springapp.datahubaccelerator.domain.repository.SheetOfExcelInputRepository;
import springapp.datahubaccelerator.generators.ScriptGenerator;
import springapp.datahubaccelerator.services.ExcelInputService;
import springapp.datahubaccelerator.services.FieldService;

import java.util.List;
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
//        excelInput.setSheetName(sheetname);
//        excelInputRepository.save(excelInput);
        return "redirect:/sheetlist";
    }

    @RequestMapping(value = "/sheetlist")
    public String sheetGenerator(Model model) {
        List<SheetOfExcelInput> allSheets = (List<SheetOfExcelInput>) sheetOfExcelInputRepository.findAll();
        model.addAttribute("allsheets", allSheets);
        return "sheetlist";
    }

    @RequestMapping(value = "/excel/rowgenerator/{id}")
    public String editKeyField(@PathVariable("id") Integer id, Model model) {
        String choosenSheetName = sheetOfExcelInputRepository.findById(id).get().getName();
        Sheet sheet = excelComponent.getListOfSheets().stream()
                .filter(s -> s.getSheetName().equals(choosenSheetName))
                .findFirst().get();
        Field.setFromJoinWhere(sheet.getRow(3).getCell(19).getStringCellValue().toUpperCase());
        for (int i = 3; !sheet.getRow(i).getCell(1).getStringCellValue().isEmpty(); i++) {
            Field field = new Field();
            field.setTargetExtract(sheet.getRow(i).getCell(1).getStringCellValue()
                    .toUpperCase().replace("BDE_", ""));
            field.setColumnName(sheet.getRow(i).getCell(3).getStringCellValue().replace("(FK)", "")
                    .replace("(PK)", "").trim());
            field.setDatatype(sheet.getRow(i).getCell(4).getStringCellValue());
            if (sheet.getRow(i).getCell(8).getCellType().equals(CellType.STRING)) {
                field.setScdType(sheet.getRow(i).getCell(8).getStringCellValue());
            } else {
                field.setScdType("" + (int) sheet.getRow(i).getCell(8).getNumericCellValue());
            }
            field.setSourceTable(sheet.getRow(i).getCell(16).getStringCellValue().toUpperCase());
            field.setColumnMapping(sheet.getRow(i).getCell(18).getStringCellValue().toUpperCase());
            field.setGeneralRuleApplied(sheet.getRow(i).getCell(36).getStringCellValue());
            field.setReasonAdded(sheet.getRow(i).getCell(40).getStringCellValue());
            if (field.getColumnName().contains("KEY")) {
                String joinedTable;
                String primaryKeyOfJoinedTable;
                if (i == 3) {
                    joinedTable = "Z_CS_" + field.getTargetExtract() + "_BASE";
                    primaryKeyOfJoinedTable = field.getColumnName();
                } else {
                    joinedTable = ScriptGenerator.generateJoinedTableName(field.getSourceTable());
                    primaryKeyOfJoinedTable = ScriptGenerator.generatePrimaryKeyOfJoinedTableName(field.getSourceTable());
                }
                field.setJoinedTable(joinedTable);
                field.setPrimaryKeyOfJoinedTable(primaryKeyOfJoinedTable);
            }
            ScriptGenerator.extractSourceColumnName(field);
            fieldRepository.save(field);
        }
        return "redirect:/excel/rowgenerator";
    }

    @RequestMapping("/excel/rowgenerator")
    public String generateColumns(Model model) {
        List<Field> allFields = (List<Field>) fieldRepository.findAll();
        model.addAttribute("fields", allFields);
        return "rowgenerator";
    }

    @RequestMapping("/keyfields")
    public String checkJoinedTables(Model model) {
        List<Field> allFields = (List<Field>) fieldRepository.findAll();
        List<Field> keyFields = allFields.stream().filter(f -> f.getJoinedTable() != null).collect(Collectors.toList());
        model.addAttribute("keyfields", keyFields);
        return "keyfields";
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
