package springapp.datahubaccelerator.services;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import springapp.datahubaccelerator.Components.ExcelComponent;
import springapp.datahubaccelerator.domain.Excel;
import springapp.datahubaccelerator.domain.SheetOfExcelInput;
import springapp.datahubaccelerator.domain.repository.ExcelRepository;
import springapp.datahubaccelerator.domain.repository.SheetOfExcelInputRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelInputService {

    @Autowired
    ExcelRepository excelRepository;

    @Autowired
    ExcelComponent excelComponent;

    @Autowired
    SheetOfExcelInputRepository sheetOfExcelInputRepository;

    public void saveDataFromUploadedFile(Excel excel) throws Exception {
        MultipartFile excelFile = excel.getExcelFile();
        String extension = FilenameUtils.getExtension(excelFile.getOriginalFilename());
        if (extension.equalsIgnoreCase("xls") || extension.equalsIgnoreCase("xlsx")){
            Workbook workbook = getWorkBook(excelFile);
            excel.setNumberOfSheets(workbook.getNumberOfSheets());
            List<Sheet> allSheets= new ArrayList<>();
            for (int i = 0; i < excel.getNumberOfSheets(); i++) {
                SheetOfExcelInput sheetOfExcelInput = new SheetOfExcelInput();
                sheetOfExcelInput.setName(workbook.getSheetAt(i).getSheetName());
                allSheets.add(workbook.getSheetAt(i));
                sheetOfExcelInputRepository.save(sheetOfExcelInput);
            }
            excelComponent.setListOfSheets(allSheets);
        } else {
            throw new Exception("Wrong file format");
        }
    }

    private String readDataFromExcel(MultipartFile excelFile) {
        Workbook workbook = getWorkBook(excelFile);
//        Sheet sheet = workbook.getSheetAt(0);
//        String sheetName = sheet.getSheetName();
        String sheetName = "" + workbook.getNumberOfSheets();
        return sheetName;
    }

    private Workbook getWorkBook(MultipartFile excelFile) {
        Workbook workbook = null;
        try{
                ZipSecureFile.setMinInflateRatio(0);
                workbook = new XSSFWorkbook(excelFile.getInputStream());
//                workbook = new HSSFWorkbook(excelFile.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return workbook;
    }
}
