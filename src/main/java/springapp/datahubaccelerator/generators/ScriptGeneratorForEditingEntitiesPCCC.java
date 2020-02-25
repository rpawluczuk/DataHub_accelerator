package springapp.datahubaccelerator.generators;

import springapp.datahubaccelerator.domain.Field;

import java.util.List;
import java.util.stream.Collectors;

public class ScriptGeneratorForEditingEntitiesPCCC extends ScriptGenerator {

    private List<Field> newFields;
    private String userStoryNumber;
    private String targetExtract;

    public ScriptGeneratorForEditingEntitiesPCCC(List<Field> allFields) {
        Integer newestUserStoryNumber = getNewestUserStoryNumber(allFields);
        List<Field> newFields = allFields.stream()
                .filter(f -> f.getReasonAdded().contains(newestUserStoryNumber.toString()))
                .collect(Collectors.toList());
        this.newFields = newFields;
        this.userStoryNumber = newestUserStoryNumber.toString();
        this.targetExtract = newFields.get(0).getTargetExtract();
    }

    private Integer getNewestUserStoryNumber(List<Field> allFields) {
        return allFields.stream()
                    .filter(f -> !f.getReasonAdded().toUpperCase().equals("BASE"))
                    .map(f -> f.getReasonAdded().replace("P17152-", ""))
                    .map(s -> Integer.valueOf(s))
                    .max(Integer::compare).get();
    }

    public String generateDDLScript() {
        return generateTRFPart();
    }

    private String generateTRFPart() {
        return "/* User Story: P17152_" +  userStoryNumber + "_" + targetExtract + " */\n" +
                "\n" +
                "/* TRF Changes */\n" +
                "IF (NOT EXISTS (SELECT * \n" +
                "\t\t\tFROM INFORMATION_SCHEMA.COLUMNS\n" +
                "\t\t\tWHERE TABLE_SCHEMA = 'dbo' \n" +
                "\t\t\tAND  TABLE_NAME = 'TRF_" + targetExtract + "'\n" +
                "\t\t\tAND COLUMN_NAME IN (\n\t\t\t\t\t " +
                listingOfColumnNames(newFields, 0)
                +
                "BEGIN\n" +
                "\tALTER TABLE TRF_" + targetExtract + " \n" +
                "\tADD\n" +
                "\t" + generateRowsForScript(newFields, 0) +
                "END\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][TRF] P17152-" + userStoryNumber +
                ": TRF_" + targetExtract + "'";
    }
}
