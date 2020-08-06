package springapp.datahubaccelerator.services;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import springapp.datahubaccelerator.Components.ExcelComponent;
import springapp.datahubaccelerator.DontAllowedSheets;
import springapp.datahubaccelerator.domain.Excel;
import springapp.datahubaccelerator.domain.Field;
import springapp.datahubaccelerator.domain.SheetOfExcelInput;
import springapp.datahubaccelerator.domain.repository.ExcelRepository;
import springapp.datahubaccelerator.domain.repository.FieldRepository;
import springapp.datahubaccelerator.domain.repository.SheetOfExcelInputRepository;
import springapp.datahubaccelerator.generators.ScriptGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ExcelInputService {

    @Autowired
    ExcelRepository excelRepository;

    @Autowired
    ExcelComponent excelComponent;

    @Autowired
    SheetOfExcelInputRepository sheetOfExcelInputRepository;

    @Autowired
    FieldRepository fieldRepository;

    public void saveDataFromUploadedFile(Excel excel) throws Exception {
        MultipartFile excelFile = excel.getExcelFile();
        String extension = FilenameUtils.getExtension(excelFile.getOriginalFilename());
        if (extension.equalsIgnoreCase("xls") || extension.equalsIgnoreCase("xlsx")){
            Workbook workbook = getWorkBook(excelFile);
            excel.setNumberOfSheets(workbook.getNumberOfSheets());
            List<Sheet> allSheets= new ArrayList<>();
            for (int i = 0; i < excel.getNumberOfSheets(); i++) {
                String sheetName = workbook.getSheetAt(i).getSheetName();
                boolean isSheetAllowed = !Arrays.stream(DontAllowedSheets.values())
                        .anyMatch(s -> s.getNameOfSheet().equals(sheetName));
                if (isSheetAllowed){
                    SheetOfExcelInput sheetOfExcelInput = new SheetOfExcelInput();
                    sheetOfExcelInput.setName(workbook.getSheetAt(i).getSheetName());
                    allSheets.add(workbook.getSheetAt(i));
                    sheetOfExcelInputRepository.save(sheetOfExcelInput);
                }
            }
            excelComponent.setListOfSheets(allSheets);
        } else {
            throw new Exception("Wrong file format");
        }
    }

    private Workbook getWorkBook(MultipartFile excelFile) {
        Workbook workbook = null;
        try{
                ZipSecureFile.setMinInflateRatio(0);
                workbook = new XSSFWorkbook(excelFile.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return workbook;
    }

    public void extractFieldsFromExcel(Sheet sheet) {
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
                    String pureColumnName = field.getColumnName().replace("_KEY", "");
                    if (field.getSourceTable().contains("CCX") || field.getSourceTable().contains("PCX")){
                        joinedTable = "Z_CS_" + pureColumnName + "_BASE";
                        Field.setPrimaryTable(joinedTable);
                        primaryKeyOfJoinedTable = field.getColumnName();
                    } else {
                        joinedTable = "CS_" + pureColumnName+ "_BASE";
                        Field.setPrimaryTable(joinedTable);
                        primaryKeyOfJoinedTable = field.getColumnName();
                    }
                } else {
                    joinedTable = ScriptGenerator.generateJoinedTableName(field.getSourceTable(), field.getColumnName());
                    primaryKeyOfJoinedTable = ScriptGenerator.generatePrimaryKeyOfJoinedTableName(field.getSourceTable(), field.getColumnName());
                }
                field.setJoinedTable(joinedTable);
                field.setPrimaryKeyOfJoinedTable(primaryKeyOfJoinedTable);
            }
            if (field.getSourceTable().contains("BC")){
                ScriptGenerator.extractSourceColumnName(field);
            }
            fieldRepository.save(field);
        }
    }
}
