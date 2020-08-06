package springapp.datahubaccelerator.generators;

import springapp.datahubaccelerator.domain.Field;

import java.util.List;
import java.util.stream.Collectors;

public class ScriptGeneratorForCreatingEntitiesPCCC extends ScriptGenerator {

    private List<Field> allFields;
    private String targetExtract;
    private String pureTableName;
    private String userStoryNumber;
    private String primaryKeyColumnName;
    private String primaryTableName;

    public ScriptGeneratorForCreatingEntitiesPCCC(List<Field> allFields) {
        this.allFields = allFields;
        this.targetExtract = allFields.get(0).getTargetExtract();
        this.pureTableName = Field.getPrimaryTable().replace("Z_", "")
                .replace("CS_", "")
                .replace("_BASE", "");
        this.userStoryNumber = allFields.get(0).getReasonAdded();
        this.primaryKeyColumnName = allFields.get(0).getColumnName();
        this.primaryTableName = allFields.get(0).getJoinedTable();
    }

    public String generateDDLScript() {
        return generateTRFPart() +
                generateODSBasePart() +
                generateODSDeltaPart() +
                generateConstraintsPart() +
                generateIndexesPart();
    }

    public String generateDMLScript() {
        List<Field> allKeyFields = getKeyFieldsList(allFields);
        String dmlPartScript = "";
        for (Field field : allKeyFields) {
            dmlPartScript = dmlPartScript + generateSingleInsertion(field, primaryTableName);
        }
        return "/* User Story: " + userStoryNumber + "_Bde_" + generateEntityName(targetExtract) + " */\n" +
                "\n" +
                "/* MD_FK_REF Inserts */" +
                dmlPartScript;
    }

    private String generateTRFPart() {
        return "/* User Story: " + userStoryNumber + "_Bde_" + generateEntityName(targetExtract) + " */\n" +
                "\n" +
                "/* TRF Changes */\n" +
                "IF (NOT EXISTS (SELECT * \n" +
                "\t\t\tFROM INFORMATION_SCHEMA.TABLES \n" +
                "\t\t\tWHERE TABLE_SCHEMA = 'dbo' \n" +
                "\t\t\tAND  TABLE_NAME = 'Z_TRF_" + pureTableName + "'))\n" +
                "BEGIN\n" +
                "CREATE TABLE [dbo].[Z_TRF_" + pureTableName + "](" +
                "\n\t" + generateRowsForScript(allFields, 0) +
                "\n\t,[ETL_ROW_EFF_DTS]      datetime2(7)\tNOT NULL\n" +
                ")\n" +
                "END\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][TRF] " +
                userStoryNumber + ": Z_TRF_" + pureTableName + "'\n" +
                "\nGO\n";
    }

    private String generateODSBasePart() {
        return "\n/* ODS Changes */" +
                "\n" +
                "IF (NOT EXISTS (SELECT * \n" +
                "\t\t\tFROM INFORMATION_SCHEMA.TABLES \n" +
                "\t\t\tWHERE TABLE_SCHEMA = 'dbo' \n" +
                "\t\t\tAND  TABLE_NAME = 'Z_CS_" + pureTableName + "_BASE'))\n" +
                "BEGIN\n" +
                "\tCREATE TABLE [dbo].[Z_CS_" + pureTableName + "_BASE](\n" +
                "\t [" + primaryKeyColumnName.replace("KEY", "BID") + "]\tint IDENTITY(1,1) NOT NULL\n" +
                "\t,[" + primaryKeyColumnName + "]\tvarchar(100)\tNOT NULL\n" +
                "\t,[SOURCE_SYSTEM]\tvarchar(10)\tNOT NULL\n\t," +
                generateRowsForScript(allFields
                        .stream()
                        .filter(x -> !x.getScdType().equals("2"))
                        .filter(x -> !x.getColumnName().contains(primaryKeyColumnName))
                        .filter(x -> !x.getColumnName().contains("ROW_PROC_DTS"))
                        .filter(x -> !x.getColumnName().contains("X_SEQ_NO"))
                        .filter(x -> !x.getColumnName().contains("SOURCE_SYSTEM"))
                        .collect(Collectors.toList()), 0) +
                "\n\t,[ETL_LATE_ARRIVING_SCD]\tvarchar(1)\tNOT NULL\n" +
                "\t,[ETL_ACTIVE_FL]\tvarchar(1)\tNOT NULL\n" +
                "\t,[ETL_ADD_DTS]\tdatetime2(7)\tNULL\n" +
                "\t,[ETL_LAST_UPDATE_DTS]\tdatetime2(7)\tNOT NULL\n" +
                "CONSTRAINT [Z_" + generateShortcut(pureTableName, "BASE") + "_PK] PRIMARY KEY NONCLUSTERED \n" +
                "(\n" +
                "\t[" + primaryKeyColumnName + "] ASC\n" +
                "),\n" +
                "CONSTRAINT [Z_" + generateShortcut(pureTableName, "BASE") + "_AK1] UNIQUE NONCLUSTERED \n" +
                "(\n" +
                "\t[" + primaryKeyColumnName.replace("KEY", "BID") + "] ASC\n" +
                ")\n" +
                ")\n" +
                "END\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][ODS] " +
                userStoryNumber + ": Z_CS_" + pureTableName + "_BASE'\n" +
                "\nGO\n";
    }

    private String generateODSDeltaPart() {
        String primaryKeyColumnName = allFields.get(0).getColumnName().replace("(PK)", "").trim();
        return "\nIF (NOT EXISTS (SELECT * \n" +
                "\t\t\tFROM INFORMATION_SCHEMA.TABLES \n" +
                "\t\t\tWHERE TABLE_SCHEMA = 'dbo' \n" +
                "\t\t\tAND  TABLE_NAME = 'Z_CS_" + pureTableName + "_DELTA'))\n" +
                "BEGIN\n" +
                "\tCREATE TABLE [dbo].[Z_CS_" + pureTableName + "_DELTA](\n" +
                "\t [" + primaryKeyColumnName.replace("KEY", "DID") + "]\tint IDENTITY(1,1) NOT NULL\n" +
                "\t,[" + primaryKeyColumnName + "]\tvarchar(100)\tNOT NULL\n" +
                "\t,[ETL_ROW_EFF_DTS]\tdatetime2(7)\tNOT NULL\n" +
                "\t,[ETL_ROW_EXP_DTS]\tdatetime2(7)\tNOT NULL\n" +
                "\t,[ROW_PROC_DTS]\tdatetime2(7)\tNOT NULL\n" +
                "\t,[SOURCE_SYSTEM]\tvarchar(10)\tNOT NULL" +
                generateRowsForScript(allFields.stream()
                        .filter(x -> !x.getScdType().equals("1"))
                        .filter(x -> !x.getColumnName().contains("X_SEQ_NO"))
                        .filter(x -> !x.getColumnName().contains("ROW_PROC_DTS"))
                        .filter(x -> !x.getColumnName().contains("SOURCE_SYSTEM"))
                        .collect(Collectors.toList()), 1) +
                "\n\t,[ETL_CURR_ROW_FL]\tvarchar(1)\tNOT NULL\n" +
                "\t,[ETL_LATE_ARRIVING_FL] varchar(1)\tNOT NULL\n" +
                "\t,[ETL_ACTIVE_FL]\tvarchar(1)\tNOT NULL\n" +
                "\t,[ETL_ADD_DTS]\tdatetime2(7)\tNULL\n" +
                "\t,[ETL_LAST_UPDATE_DTS]\tdatetime2(7)\tNOT NULL\n" +
                " CONSTRAINT [Z_" + generateShortcut(pureTableName, "DELTA") + "_PK] PRIMARY KEY NONCLUSTERED \n" +
                "(\n" +
                "\t[" + primaryKeyColumnName + "] ASC,\n" +
                "\t[ETL_ROW_EFF_DTS] ASC\n" +
                "),\n" +
                " CONSTRAINT [Z_" + generateShortcut(pureTableName, "DELTA") + "_AK1] UNIQUE NONCLUSTERED \n" +
                "(\n" +
                "\t[" + primaryKeyColumnName.replace("KEY", "DID") + "] ASC\n" +
                ")\n" +
                ")\n" +
                "END\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][ODS] " +
                userStoryNumber + ": Z_CS_" + pureTableName + "_DELTA'\n" +
                "\nGO\n";
    }

    private String generateConstraintsPart() {
        List<Field> allKeyFields = allFields.stream()
                .filter(f -> f.getJoinedTable() != null)
                .collect(Collectors.toList());
        String constraintPartScript = "";
        for (Field field : allKeyFields) {
            constraintPartScript = constraintPartScript + generateSingleConstraint(field, primaryTableName);
        }
        return "\n/* Constraints */" + constraintPartScript + "\nGO\n";
    }

    private String generateIndexesPart() {
        List<Field> allKeyFields = allFields.stream()
                .filter(f -> f.getJoinedTable() != null)
                .collect(Collectors.toList());
        String indexPartScript = "";
        for (Field field : allKeyFields) {
            indexPartScript = indexPartScript +
                    generateSingleIndex(field.getScdType(), field.getColumnName(), field, primaryTableName);
        }
        return "\n/* Indexes */" + indexPartScript +
                generateSingleIndex("1", "ETL_LAST_UPDATE_DTS", allKeyFields.get(0), primaryTableName) +
                generateSingleIndex("2", "ETL_LAST_UPDATE_DTS", allKeyFields.get(0), primaryTableName) + "\nGO\n";
    }
}
