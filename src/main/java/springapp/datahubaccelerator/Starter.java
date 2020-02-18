//package springapp.datahubaccelerator;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Scope;
//import org.springframework.stereotype.Component;
//import springapp.datahubaccelerator.domain.Input;
//import springapp.datahubaccelerator.domain.repository.InputRepository;
//import springapp.datahubaccelerator.domain.repository.FieldRepository;
//
//import javax.transaction.Transactional;
//
//@Component
//@Scope("singleton")
//public class Starter implements CommandLineRunner {
//
//    @Autowired
//    InputRepository inputRepository;
//
//    @Autowired
//    FieldRepository fieldRepository;
//
//    @Override
//    @Transactional
//    public void run(String... args) throws Exception {
//        Input input = new Input();
//        input.setId(1);
//        input.setUserStoryNumber("31319");
//        input.setEntityName("DiscountTransaction");
//        input.setTargetExtract(
//                "DISC_TRANS\r\nDISC_TRANS\r\nDISC_TRANS\r\nDISC_TRANS\r\nDISC_TRANS\r\nDISC_TRANS\r\nDISC_TRANS\r\n" +
//                        "DISC_TRAN\r\nDISC_TRANS\r\nDISC_TRANS\r\n");
//        input.setColumnName("DISC_TRANS_KEY (PK)\r\nROW_PROC_DTS (PK)\r\nDISCOUNT_PATTERN_KEY\r\nJOB_KEY\r\n" +
//                "DISC_BDG_CSTR_KEY\r\nSOURCE_SYSTEM\r\nDED_AMT\r\nDED_AMT_CURR_CD\r\nRES_STATUS_CD\r\nX_SEQ_NO\r\n");
//        input.setDatatype("varchar(100)\r\ndatetime\r\nvarchar(100)\r\nvarchar(100)\r\nvarchar(100)\r\n" +
//                "varchar(10)\r\ndecimal(18,2)\r\nvarchar(255)\r\nvarchar(255)\r\ndecimal(19,0)\r\n");
//        input.setGeneralRuleApplied("General Primary Key Rule 1\r\n\r\nGeneral Foreign Key Rule 1\r\n" +
//                "General Foreign Key Rule 1\r\nGeneral Foreign Key Rule 1\r\nGeneral Source System Rule 1\r\n" +
//                "General Number Rule 1\r\nGeneral Rule 1\r\nGeneral Rule 1\r\n\r\n");
//
//        fieldRepository.generateField(input);
//    }
//}
