package springapp.datahubaccelerator.generators;

import springapp.datahubaccelerator.domain.Field;

import java.util.List;
import java.util.stream.Collectors;

public class ScriptGeneratorForCreatingEntitiesPCCC extends ScriptGenerator {

    private List<Field> allFields;
    private String targetExtract;
    private String userStoryNumber;
    private String primaryKeyColumnName;

    public ScriptGeneratorForCreatingEntitiesPCCC(List<Field> allFields) {
        this.allFields = allFields;
        this.targetExtract = allFields.get(0).getTargetExtract().toUpperCase();
        this.userStoryNumber = allFields.get(0).getReasonAdded();
        this.primaryKeyColumnName = allFields.get(0).getColumnName();
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
            dmlPartScript = dmlPartScript + generateSingleInsertion(field);
        }
        return "/* User Story: " + userStoryNumber + "_Bde_" + generateEntityName(targetExtract) + " */\n" +
                "\n" +
                "/* MD_FK_REF Inserts */" +
                dmlPartScript;
    }

    private String generateTRFPart() {
        return "/* User Story: " + userStoryNumber + "_Bde_" + generateEntityName(targetExtract) + " */\n" +
                "\n" +
                "/* TRF Changes */" +
                "IF (NOT EXISTS (SELECT * \n" +
                "\t\t\tFROM INFORMATION_SCHEMA.TABLES \n" +
                "\t\t\tWHERE TABLE_SCHEMA = 'dbo' \n" +
                "\t\t\tAND  TABLE_NAME = 'Z_TRF_" + targetExtract + "'))\n" +
                "BEGIN\n" +
                "CREATE TABLE [dbo].[Z_TRF_" + targetExtract + "](" +
                "\n\t" + generateRowsForScript(allFields, 0) +
                "\n\t,[ETL_ROW_EFF_DTS]      datetime2(7)\tNOT NULL\n" +
                ")\n" +
                "END\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][TRF] " +
                userStoryNumber + ": Z_TRF_" + targetExtract + "'\n" +
                "\nGO\n";
    }

    private String generateODSBasePart() {
        return "\n/* ODS Changes */" +
                "\n" +
                "IF (NOT EXISTS (SELECT * \n" +
                "\t\t\tFROM INFORMATION_SCHEMA.TABLES \n" +
                "\t\t\tWHERE TABLE_SCHEMA = 'dbo' \n" +
                "\t\t\tAND  TABLE_NAME = 'Z_CS_" + targetExtract + "_BASE'))\n" +
                "BEGIN\n" +
                "\tCREATE TABLE [dbo].[Z_CS_" + targetExtract + "_BASE](\n" +
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
                "CONSTRAINT [Z_" + generateShortcut(targetExtract, "BASE") + "_PK] PRIMARY KEY NONCLUSTERED \n" +
                "(\n" +
                "\t[" + primaryKeyColumnName + "] ASC\n" +
                "),\n" +
                "CONSTRAINT [Z_" + generateShortcut(targetExtract, "BASE") + "_AK1] UNIQUE NONCLUSTERED \n" +
                "(\n" +
                "\t[" + primaryKeyColumnName.replace("KEY", "BID") + "] ASC\n" +
                ")\n" +
                ")\n" +
                "END\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][ODS] " +
                userStoryNumber + ": Z_CS_" + targetExtract + "_BASE'\n" +
                "\nGO\n";
    }

    private String generateODSDeltaPart() {
        String primaryKeyColumnName = allFields.get(0).getColumnName().replace("(PK)", "").trim();
        return "\nIF (NOT EXISTS (SELECT * \n" +
                "\t\t\tFROM INFORMATION_SCHEMA.TABLES \n" +
                "\t\t\tWHERE TABLE_SCHEMA = 'dbo' \n" +
                "\t\t\tAND  TABLE_NAME = 'Z_CS_" + targetExtract + "_DELTA'))\n" +
                "BEGIN\n" +
                "\tCREATE TABLE [dbo].[Z_CS_" + targetExtract + "_DELTA](\n" +
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
                " CONSTRAINT [Z_" + generateShortcut(targetExtract, "DELTA") + "_PK] PRIMARY KEY NONCLUSTERED \n" +
                "(\n" +
                "\t[" + primaryKeyColumnName + "] ASC,\n" +
                "\t[ETL_ROW_EFF_DTS] ASC\n" +
                "),\n" +
                " CONSTRAINT [Z_" + generateShortcut(targetExtract, "DELTA") + "_AK1] UNIQUE NONCLUSTERED \n" +
                "(\n" +
                "\t[" + primaryKeyColumnName.replace("KEY", "DID") + "] ASC\n" +
                ")\n" +
                ")\n" +
                "END\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][ODS] " +
                userStoryNumber + ": Z_CS_" + targetExtract + "_DELTA'\n" +
                "\nGO\n";
    }

    private String generateConstraintsPart() {
        List<Field> allKeyFields = allFields.stream()
                .filter(f -> f.getJoinedTable() != null)
                .collect(Collectors.toList());
        String constraintPartScript = "";
        for (Field field : allKeyFields) {
            constraintPartScript = constraintPartScript + generateSingleConstraint(field);
        }
        return "\n/* Constraints */" + constraintPartScript + "\nGO\n";
    }

    private String generateIndexesPart() {
        List<Field> allKeyFields = allFields.stream()
                .filter(f -> f.getJoinedTable() != null)
                .collect(Collectors.toList());
        String indexPartScript = "";
        for (Field field : allKeyFields) {
            indexPartScript = indexPartScript + generateSingleIndex(field.getScdType(), field.getColumnName());
        }
        return "\n/* Indexes */" + indexPartScript +
                generateSingleIndex("1", "ETL_LAST_UPDATE_DTS") +
                generateSingleIndex("2", "ETL_LAST_UPDATE_DTS") + "\nGO\n";
    }

    private String generateSingleConstraint(Field field) {
        String scdType;
        if (field.getScdType().equals("1")) {
            scdType = "BASE";
        } else {
            scdType = "DELTA";
        }
        String pureJoinedTableName = field.getJoinedTable()
                .replace("Z_CS_", "")
                .replace("CS_", "")
                .replace("_BASE", "")
                .replace("_DELTA", "");
        return "\nIF (EXISTS (SELECT * \n" +
                "\t\t\tFROM INFORMATION_SCHEMA.TABLES \n" +
                "\t\t\tWHERE TABLE_SCHEMA = 'dbo' \n" +
                "\t\t\tAND  TABLE_NAME = 'Z_CS_" + targetExtract + "_" + scdType + "')\n" +
                "\tAND EXISTS (SELECT * \n" +
                "\t\t\t\tFROM INFORMATION_SCHEMA.TABLES \n" +
                "\t\t\t\tWHERE TABLE_SCHEMA = 'dbo' \n" +
                "\t\t\t\tAND  TABLE_NAME = '" + field.getJoinedTable() + "')\n" +
                "\tAND NOT EXISTS (SELECT * \n" +
                "\t\t\t\t\tFROM sys.foreign_keys\n" +
                "\t\t\t\t\tWHERE name = 'Z_FK_" + generateShortcut(targetExtract, scdType) +
                "_" + generateShortcut(pureJoinedTableName, "BASE") + "')\n" +
                "\t\t\t\t)\n" +
                "BEGIN\n" +
                "\n" +
                "\tALTER TABLE [dbo].[Z_CS_" + targetExtract + "_" + scdType + "] ADD CONSTRAINT " +
                "[Z_FK_" + generateShortcut(targetExtract, scdType) + "_" + generateShortcut(pureJoinedTableName, "BASE") +
                "] FOREIGN KEY([" + field.getColumnName() + "])\n" +
                "\tREFERENCES [dbo].[" + field.getJoinedTable() + "] ([" + field.getPrimaryKeyOfJoinedTable() + "])\n" +
                "END\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][FK] " +
                userStoryNumber + ": Z_FK_" +
                generateShortcut(targetExtract, scdType) + "_" + generateShortcut(pureJoinedTableName, "BASE") + "'\n";
    }


    private String generateSingleIndex(String scdType, String columnName) {
        if (scdType.equals("1")) {
            scdType = "BASE";
        } else {
            scdType = "DELTA";
        }
        String foreginField = columnName.replace("_KEY", "");
        String index;
        if (columnName.equals("ETL_LAST_UPDATE_DTS")) {
            index = "Z_" + generateShortcut(targetExtract, scdType) + "_XUPDT";
        } else {
            index = "Z_FK_" + generateShortcut(targetExtract, scdType) + "_" +
                    generateShortcut(foreginField, "BASE") + "_XFK";
        }
        return "\nIF (EXISTS (SELECT *\n" +
                "\t\t\tFROM INFORMATION_SCHEMA.COLUMNS\n" +
                "\t\t\tWHERE TABLE_SCHEMA = 'dbo'\n" +
                "\t\t\tAND TABLE_NAME = 'Z_CS_" + targetExtract + "_" + scdType + "'\n" +
                "\t\t\tAND COLUMN_NAME = '" + columnName + "')\n" +
                "\tAND NOT EXISTS (SELECT *\n" +
                "\t\t\t\t\tFROM sys.indexes\n" +
                "\t\t\t\t\tWHERE name = '" + index + "')\n" +
                "\t)\n" +
                "BEGIN\n" +
                "\tcreate nonclustered index " + index + " on Z_CS_" + targetExtract + "_" + scdType +
                " (" + columnName + " ASC)\n" +
                "END\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][index] " +
                userStoryNumber + ": " + index + "'\n";
    }

    private String generateSingleInsertion(Field field) {
        String scdType;
        if (field.getScdType().equals("1")) {
            scdType = "BASE";
        } else {
            scdType = "DELTA";
        }
        String foreginField = field.getColumnName().replace("_KEY", "");
        return "\n/* Insert MD_FK_REF record: Z_FK_" + generateShortcut(targetExtract, scdType) + "_" +
                generateShortcut(foreginField, "BASE") + " */\n" +
                "IF (EXISTS (SELECT * \n" +
                "\t\t\tFROM sys.foreign_keys\n" +
                "\t\t\tWHERE name = 'Z_FK_" + generateShortcut(targetExtract, scdType) + "_" +
                generateShortcut(foreginField, "BASE") + "')\n" +
                "\tAND NOT EXISTS (SELECT *\n" +
                "\t\t\t\t\tFROM MD_FK_REF\n" +
                "\t\t\t\t\tWHERE FK_CONSTR_NAME = 'Z_FK_" + generateShortcut(targetExtract, scdType) + "_" +
                generateShortcut(foreginField, "BASE") + "')\n" +
                ")\n" +
                "BEGIN\n" +
                "\tINSERT INTO MD_FK_REF(TRF_NAME,FK_TAB_NAME,FK_COL_NAME,PK_TAB_NAME,PK_COL_NAME,LOB_CD,FK_CONSTR_NAME," +
                "OOTB_FUTURE_USE_FL,MULTI_SRCE_FL,SELF_REF_FL) \nVALUES " +
                "('Z_TRF_" + targetExtract + "','Z_CS_" + targetExtract + "_" + scdType + "','" + field.getColumnName() +
                "','Z_CS_" + foreginField + "_BASE','" + field.getColumnName() + "','All','" +
                "Z_FK_" + generateShortcut(targetExtract, scdType) + "_" +
                generateShortcut(foreginField, "BASE") + "','N','N','N');\n" +
                "END\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][MD_FK_REF] " + userStoryNumber +
                ": Z_FK_" + generateShortcut(targetExtract, scdType) + "_" +
                generateShortcut(foreginField, "BASE") + "'\n\nGO\n";
    }
}
