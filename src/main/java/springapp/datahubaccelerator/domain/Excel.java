package springapp.datahubaccelerator.domain;

import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.util.Set;

@Entity
public class Excel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToMany(mappedBy = "excel")
    private Set<SheetOfExcelInput> setOfSheetOfExcelInputs;

    private int numberOfSheets;

//    @Lob
//    private byte[] excelFile;

    @Transient
    private MultipartFile excelFile;

    public Excel() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Set<SheetOfExcelInput> getSetOfSheetOfExcelInputs() {
        return setOfSheetOfExcelInputs;
    }

    public void setSetOfSheetOfExcelInputs(Set<SheetOfExcelInput> setOfSheetOfExcelInputs) {
        this.setOfSheetOfExcelInputs = setOfSheetOfExcelInputs;
    }

    public int getNumberOfSheets() {
        return numberOfSheets;
    }

    public void setNumberOfSheets(int numberOfSheets) {
        this.numberOfSheets = numberOfSheets;
    }

//    public byte[] getExcelFile() {
//        return excelFile;
//    }
//
//    public void setExcelFile(byte[] excelFile) {
//        this.excelFile = excelFile;
//    }

    public MultipartFile getExcelFile() {
        return excelFile;
    }

    public void setExcelFile(MultipartFile excelFile) {
        this.excelFile = excelFile;
    }
}
