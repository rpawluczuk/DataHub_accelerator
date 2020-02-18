package springapp.datahubaccelerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import springapp.datahubaccelerator.domain.repository.InputRepository;
import springapp.datahubaccelerator.domain.repository.FieldRepository;

import javax.transaction.Transactional;

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
//        Input input = new Input();
//        input.setId(1);
//        input.setTargetExtract("DISC_TRANS_KEY (PK) ROW_PROC_DTS (PK) DISCOUNT_PATTERN_KEY JOB_KEY DISC_BDG_CSTR_KEY" +
//                " SOURCE_SYSTEM DED_AMT DED_AMT_CURR_CD RES_STATUS_CD X_SEQ_NO");
//        input.setDatatype("varchar(100) datetimevarchar(100) varchar(100) varchar(100) varchar(10) decimal(18,2)" +
//                " varchar(255) varchar(255) decimal(19,0)");
//
//        rowRepository.generateRows(input);
    }
}
