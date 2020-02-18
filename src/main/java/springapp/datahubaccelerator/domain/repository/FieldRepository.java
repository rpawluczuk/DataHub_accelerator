package springapp.datahubaccelerator.domain.repository;

import org.springframework.stereotype.Repository;
import springapp.datahubaccelerator.domain.Field;
import springapp.datahubaccelerator.domain.Input;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class FieldRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void saveField(Field field) {
        entityManager.merge(field);
    }

    public List<Field> getAllFields() {
        return entityManager.createQuery("from Field", Field.class).getResultList();
    }

    @Transactional
    public void generateField(Input input) {
        List<String> targetExtractList = Arrays.asList(input.getTargetExtract().split(" "));
        List<String> columnNameList = Arrays.asList(input.getColumnName().split(" "));
        columnNameList = columnNameList.stream().filter(x -> !x.equals("(PK)")).collect(Collectors.toList());
        List<String> datatypeList = Arrays.asList(input.getDatatype().split(" "));
        for (int i = 0; i < targetExtractList.size(); i++) {
            Field field = new Field();
            field.setTargetExtract(targetExtractList.get(i));
            field.setColumnName(columnNameList.get(i));
            field.setDatatype(datatypeList.get(i));
            saveField(field);
        }
    }

    public String generateScript(List<Field> allFields) {
        String targetExtract = allFields.get(0).getTargetExtract();
        String RowsForScript = generateRowsForScript(allFields);
        String script = "/* User Story: P17152-" + Field.getUserStoryNumber() + "_Bde_" + Field.getEntityName() + " */\n" +
                "\n" +
                "/* TRF Changes */\n" +
                "IF (NOT EXISTS (SELECT * \n" +
                "\t\t\tFROM INFORMATION_SCHEMA.TABLES \n" +
                "\t\t\tWHERE TABLE_SCHEMA = 'dbo' \n" +
                "\t\t\tAND  TABLE_NAME = 'Z_TRF_" + targetExtract +"'))\n" +
                "BEGIN\n" +
                "CREATE TABLE [dbo].[Z_TRF_" + targetExtract + "](\n" +
                "\t" + generateRowsForScript(allFields) +
                "[ETL_ROW_EFF_DTS]      [datetime2](7)\tNOT NULL\n" +
                ")\n" +
                "END\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][TRF] P17152-" + Field.getUserStoryNumber() + ": Z_TRF_" + targetExtract + "'";
        return script;
    }

    private String generateRowsForScript(List<Field> allFields) {
        String rowsForScript = "";
        for (int i = 0; i < allFields.size(); i++) {
            rowsForScript = rowsForScript + "[" + allFields.get(i).getColumnName() + "]\t[" +
                    handleDatatype(allFields.get(i).getDatatype()) + "\tNOT NULL\n\t,";
        }
        return rowsForScript;
    }

    private String handleDatatype(String datatype) {
        if (datatype.contains("varchar") || datatype.contains("decimal")){
            return datatype.replace("(", "](");
        }
        return datatype +"]";
    }
}
