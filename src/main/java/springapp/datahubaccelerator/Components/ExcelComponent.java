package springapp.datahubaccelerator.Components;

import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Component;
import springapp.datahubaccelerator.domain.SheetOfExcelInput;

import java.util.List;
import java.util.Set;

@Component
public class ExcelComponent {

    private List<Sheet> listOfSheets;

    public ExcelComponent() {
    }

    public List<Sheet> getListOfSheets() {
        return listOfSheets;
    }

    public void setListOfSheets(List<Sheet> listOfSheets) {
        this.listOfSheets = listOfSheets;
    }
}
