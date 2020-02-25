package springapp.datahubaccelerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import springapp.datahubaccelerator.domain.Field;
import springapp.datahubaccelerator.domain.Input;
import springapp.datahubaccelerator.domain.repository.InputRepository;
import springapp.datahubaccelerator.domain.repository.FieldRepository;
import springapp.datahubaccelerator.services.FieldService;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;

@Component
@Scope("singleton")
public class Starter implements CommandLineRunner {

    @Autowired
    InputRepository inputRepository;

    @Autowired
    FieldRepository fieldRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        List<String> targetExtractList = Arrays.asList("DISC_TRANS", "DISC_TRANS", "DISC_TRANS", "DISC_TRANS"
                , "DISC_TRANS", "DISC_TRANS", "DISC_TRANS", "DISC_TRANS", "DISC_TRANS", "DISC_TRANS");
        List<String> columnNameList = Arrays.asList("DISC_TRANS_KEY (PK)","ROW_PROC_DTS (PK)","DISCOUNT_PATTERN_KEY",
                "JOB_KEY","DISC_BDG_CSTR_KEY","SOURCE_SYSTEM","DED_AMT","DED_AMT_CURR_CD","RES_STATUS_CD", "X_SEQ_NO");
        List<String> datatypeList = Arrays.asList("varchar(100)", "datetime", "varchar(100)", "varchar(100)"
                , "varchar(100)", "varchar(10)", "decimal(18,2)", "varchar(255)", "varchar(255)", "decimal(19,0)");
        List<String> scdTypeList = Arrays.asList("N/A", "N/A", "1", "1", "1", "N/A", "2", "2", "2", "N/A");
        List<String> generalRuleAppliedList = Arrays.asList("General Primary Key Rule 1", "", "General Foreign Key Rule 1"
                , "General Foreign Key Rule 1", "General Foreign Key Rule 1", "General Source System Rule 1"
                , "General Number Rule 1", "General Rule 1", "General Rule 1", "");
        List<String> reasonAddedList = Arrays.asList("P17152-31319", "P17152-31320", "P17152-31319", "P17152-31319"
                , "P17152-31319", "P17152-31319", "P17152-31319", "P17152-31319", "P17152-31320", "P17152-31320");


        for (int i = 0; i < targetExtractList.size(); i++) {
            Field field = new Field();
            field.setTargetExtract(targetExtractList.get(i));
            field.setColumnName(columnNameList.get(i));
            field.setDatatype(datatypeList.get(i));
            field.setScdType(scdTypeList.get(i));
            try {
                field.setGeneralRuleApplied(generalRuleAppliedList.get(i));
            }
            catch(ArrayIndexOutOfBoundsException e) {
                field.setGeneralRuleApplied("");
            }
            field.setReasonAdded(reasonAddedList.get(i));
            fieldRepository.saveField(field);
        }
    }
}
