package springapp.datahubaccelerator.generators;

import springapp.datahubaccelerator.domain.Field;

import java.util.List;
import java.util.stream.Collectors;

public class ScriptGeneratorForEditingEntitiesPCCC extends ScriptGenerator {

    private List<Field> newFields;
    private String userStoryNumber;
    private String targetExtract;
    private String pureTableName;
    private String primaryTableName;

    public ScriptGeneratorForEditingEntitiesPCCC(List<Field> newFields) {
        this.newFields = newFields;
        this.userStoryNumber = newFields.get(0).getReasonAdded();
        this.targetExtract = newFields.get(0).getTargetExtract();
        this.pureTableName = Field.getPrimaryTable().replace("Z_", "")
                .replace("CS_", "")
                .replace("_BASE", "");
        this.primaryTableName = Field.getPrimaryTable();
    }

    public String generateDDLScript() {
        return generateTRFPart() +
                "\n/* ODS Changes */" +
                generateODSBasePart() +
                generateODSDeltaPart() +
                generateConstraintsPart() +
                generateIndexesPart();
    }

    public String generateDMLScript() {
        List<Field> allKeyFields = getKeyFieldsList(newFields);
        String dmlPartScript = "";
        for (Field field : allKeyFields) {
            dmlPartScript = dmlPartScript + generateSingleInsertion(field, primaryTableName);
        }
        return "/* User Story: " + userStoryNumber + " " + generateEntityName(targetExtract) + "_Bde */\n" +
                "\n" +
                "/* MD_FK_REF Inserts */" +
                dmlPartScript;
    }

    private String generateTRFPart() {
        return "/* User Story: P17152_" + userStoryNumber + "_" + generateEntityName(targetExtract) + " */\n" +
                "\n" +
                "/* TRF Changes */\n" +
                "IF (NOT EXISTS (SELECT * \n" +
                "\t\t\tFROM INFORMATION_SCHEMA.COLUMNS\n" +
                "\t\t\tWHERE TABLE_SCHEMA = 'dbo' \n" +
                "\t\t\tAND  TABLE_NAME = 'TRF_" + pureTableName + "'\n" +
                "\t\t\tAND COLUMN_NAME IN (" +
                listingOfColumnNames(newFields, 0) + ")))\n" +
                "BEGIN\n" +
                "\tALTER TABLE TRF_" + pureTableName + " \n" +
                "\tADD" +
                "\t" + generateRowsForScript(newFields, 0) +
                "\nEND\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][TRF] P17152-" + userStoryNumber +
                ": TRF_" + pureTableName + "'\n\nGO\n";
    }

    private String generateODSBasePart() {
        if (newFields.stream().noneMatch(f -> f.getScdType().equals("1"))) {
            return "";
        }
        return "\nIF (NOT EXISTS (SELECT * \n" +
                "\t\t\tFROM INFORMATION_SCHEMA.COLUMNS\n" +
                "\t\t\tWHERE TABLE_SCHEMA = 'dbo' \n" +
                "\t\t\tAND  TABLE_NAME = 'CS_" + pureTableName + "_BASE'\n" +
                "\t\t\tAND COLUMN_NAME IN (" +
                listingOfColumnNames(newFields
                        .stream()
                        .filter(x -> !x.getScdType().equals("2"))
                        .collect(Collectors.toList()), 0) +
                ")))\n" +
                "BEGIN\n" +
                "\tALTER TABLE CS_" + pureTableName + "_BASE\n" +
                "\tADD" +
                generateRowsForScript(newFields
                        .stream()
                        .filter(x -> !x.getScdType().equals("2"))
                        .collect(Collectors.toList()), 0) +
                "\nEND\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][ODS] P17152-" + userStoryNumber +
                ": CS_" + pureTableName + "_BASE'\n\nGO\n";
    }

    private String generateODSDeltaPart() {
        if (newFields.stream().noneMatch(f -> f.getScdType().equals("2"))) {
            return "";
        }
        return "\nIF (NOT EXISTS (SELECT * \n" +
                "\t\t\tFROM INFORMATION_SCHEMA.COLUMNS\n" +
                "\t\t\tWHERE TABLE_SCHEMA = 'dbo' \n" +
                "\t\t\tAND  TABLE_NAME = 'CS_" + pureTableName + "_DELTA'\n" +
                "\t\t\tAND COLUMN_NAME IN (" +
                listingOfColumnNames(newFields
                        .stream()
                        .filter(x -> !x.getScdType().equals("1"))
                        .collect(Collectors.toList()), 0) +
                ")))\n" +
                "BEGIN\n" +
                "\tALTER TABLE CS_" + pureTableName + "_DELTA\n" +
                "\tADD" +
                generateRowsForScript(newFields
                        .stream()
                        .filter(x -> !x.getScdType().equals("1"))
                        .collect(Collectors.toList()), 0) +
                "\nEND\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][ODS] P17152-" + userStoryNumber +
                ": CS_" + pureTableName + "_DELTA'\n\nGO\n";
    }

    private String generateConstraintsPart() {
        List<Field> allKeyFields = getKeyFieldsList(newFields);
        String constraintPartScript = "";
        for (Field field : allKeyFields) {
            constraintPartScript = constraintPartScript + generateSingleConstraint(field, primaryTableName);
        }
        if (!constraintPartScript.equals("")) {
            return "\n/* Constraints */" + constraintPartScript;
        } else {
            return "";
        }
    }

    private String generateIndexesPart() {
        List<Field> allKeyFields = getKeyFieldsList(newFields);
        String indexPartScript = "";
        for (Field field : allKeyFields) {
            indexPartScript = indexPartScript +
                    generateSingleIndex(field.getScdType(), field.getColumnName(), field, primaryTableName);
        }
        return "\n/* Indexes */" + indexPartScript;
    }
}
