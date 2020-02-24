package springapp.datahubaccelerator.generators;

import springapp.datahubaccelerator.domain.Field;

import java.util.List;
import java.util.stream.Collectors;

public class ScriptGeneratorForCreatingEntitiesPCCC extends ScriptGenerator {

    public ScriptGeneratorForCreatingEntitiesPCCC() {
    }

    public String generateTRFPart(List<Field> allFields) {
        String targetExtract = allFields.get(0).getTargetExtract();
        String userStoryNumber = allFields.get(0).getReasonAdded();
        return  "/* User Story: " + userStoryNumber + "_Bde_" + generateEntityName(targetExtract) + " */\n" +
                "\n" +
                "/* TRF Changes */\n" +
                "IF (NOT EXISTS (SELECT * \n" +
                "\t\t\tFROM INFORMATION_SCHEMA.TABLES \n" +
                "\t\t\tWHERE TABLE_SCHEMA = 'dbo' \n" +
                "\t\t\tAND  TABLE_NAME = 'Z_TRF_" + targetExtract +"'))\n" +
                "BEGIN\n" +
                "CREATE TABLE [dbo].[Z_TRF_" + targetExtract + "](\n" +
                "\t" + generateRowsForScript(allFields, 0) +
                "[ETL_ROW_EFF_DTS]      datetime2(7)\tNOT NULL\n" +
                ")\n" +
                "END\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][TRF] " +
                userStoryNumber + ": Z_TRF_" + targetExtract + "'";
    }

    public String generateODSBasePart(List<Field> allFields) {
        String targetExtract = allFields.get(0).getTargetExtract();
        String userStoryNumber = allFields.get(0).getReasonAdded();
        String primaryKeyColumnName = allFields.get(0).getColumnName().replace("(PK)", "").trim();
        return "/* ODS Changes */\n" +
                "\n" +
                "IF (NOT EXISTS (SELECT * \n" +
                "\t\t\tFROM INFORMATION_SCHEMA.TABLES \n" +
                "\t\t\tWHERE TABLE_SCHEMA = 'dbo' \n" +
                "\t\t\tAND  TABLE_NAME = 'Z_CS_" + targetExtract + "_BASE'))\n" +
                "BEGIN\n" +
                "\tCREATE TABLE [dbo].[Z_CS_" + targetExtract + "_BASE](\n" +
                "\t [" + primaryKeyColumnName.replace("KEY", "BID") + "]\t\t\t[int] IDENTITY(1,1) NOT NULL\n" +
                "\t," + generateRowsForScript(allFields
                .stream()
                .filter(x -> !x.getScdType().equals("2"))
                .collect(Collectors.toList()), 0) +
                "[ETL_LATE_ARRIVING_SCD]\tvarchar(1)\tNOT NULL\n" +
                "\t,[ETL_ACTIVE_FL]\t\t\tvarchar(1)\tNOT NULL\n" +
                "\t,[ETL_ADD_DTS]\t\t\tdatetime2(7)\tNULL\n" +
                "\t,[ETL_LAST_UPDATE_DTS]\t\tdatetime2(7)\tNOT NULL\n" +
                "CONSTRAINT [Z_C" + generateShortcut(targetExtract, "BASE") + "_PK] PRIMARY KEY NONCLUSTERED \n" +
                "(\n" +
                "\t[" + primaryKeyColumnName + "] ASC\n" +
                "),\n" +
                "CONSTRAINT [Z_C" + generateShortcut(targetExtract, "BASE") + "_AK1] UNIQUE NONCLUSTERED \n" +
                "(\n" +
                "\t[" + primaryKeyColumnName.replace("KEY", "BID") + "] ASC\n" +
                ")\n" +
                ")\n" +
                "END\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][ODS] " +
                userStoryNumber + ": Z_CS_" + targetExtract + "_BASE'\n" +
                "\n";
    }

    public String generateODSDeltaPart(List<Field> allFields) {
        String targetExtract = allFields.get(0).getTargetExtract();
        String primaryKeyColumnName = allFields.get(0).getColumnName().replace("(PK)", "").trim();
        return "IF (NOT EXISTS (SELECT * \n" +
                "\t\t\tFROM INFORMATION_SCHEMA.TABLES \n" +
                "\t\t\tWHERE TABLE_SCHEMA = 'dbo' \n" +
                "\t\t\tAND  TABLE_NAME = 'Z_CS_" + targetExtract + "_DELTA'))\n" +
                "BEGIN\n" +
                "\tCREATE TABLE [dbo].[Z_CS_" + targetExtract + "_DELTA](\n" +
                "\t [" + primaryKeyColumnName.replace("KEY", "DID") + "]\t\t[int] IDENTITY(1,1) NOT NULL\n" +
                "\t,[" + primaryKeyColumnName + "]\t\t[varchar](100)\t\tNOT NULL\n" +
                "\t,[ETL_ROW_EFF_DTS]\t\tdatetime2(7)\t\tNOT NULL\n" +
                "\t,[ETL_ROW_EXP_DTS]\t\tdatetime2(7)\t\tNOT NULL\n" +
                "\t," + generateRowsForScript(allFields
                .stream()
                .filter(x -> !x.getScdType().equals("1"))
                .collect(Collectors.toList()), 1) +
                "[ETL_CURR_ROW_FL]\t\tvarchar(1)\t\tNOT NULL\n" +
                "\t,[ETL_LATE_ARRIVING_FL] varchar(1)\t\tNOT NULL\n" +
                "\t,[ETL_ACTIVE_FL]\t\tvarchar(1)\t\tNOT NULL\n" +
                "\t,[ETL_ADD_DTS]\t\t\tdatetime2(7)\t\tNULL\n" +
                "\t,[ETL_LAST_UPDATE_DTS]\tdatetime2(7)\t\tNOT NULL\n" +
                " CONSTRAINT [Z_C" + generateShortcut(targetExtract, "DELTA") + "_PK] PRIMARY KEY NONCLUSTERED \n" +
                "(\n" +
                "\t[" + primaryKeyColumnName + "] ASC,\n" +
                "\t[ETL_ROW_EFF_DTS] ASC\n" +
                "),\n" +
                " CONSTRAINT [Z_C" + generateShortcut(targetExtract, "DELTA") + "_AK1] UNIQUE NONCLUSTERED \n" +
                "(\n" +
                "\t[" + primaryKeyColumnName.replace("KEY", "DID") + "] ASC\n" +
                ")\n" +
                ")\n" +
                "END\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][ODS] P17152-" +
                primaryKeyColumnName.replace("KEY", "DID") + ": Z_CS_" + targetExtract + "_DELTA'\n";
    }

    public String generateConstraintsPart(List<Field> allFields) {
        List<Field> allKeyFields = getKeyFieldsList(allFields);
        String constraintPartScript ="";
        for (Field field : allKeyFields) {
            constraintPartScript = constraintPartScript + generateSingleConstraint(field);
        }
        return "\n/* Constraints */\n" + constraintPartScript;
    }

    public String generateIndexesPart(List<Field> allFields) {
        List<Field> allKeyFields = getKeyFieldsList(allFields);
        String indexPartScript ="";
        for (Field field : allKeyFields) {
            indexPartScript = indexPartScript + generateSingleIndex(field);
        }
        return "\n/* Indexes */\n" + indexPartScript;
    }

    public String generateDMLscript(List<Field> allFields) {
        List<Field> allKeyFields = getKeyFieldsList(allFields);
        String dmlPartScript ="";
        for (Field field : allKeyFields) {
            dmlPartScript = dmlPartScript + generateSingleInsertion(field);
        }
        return "/* User Story: P17152-31319_Bde_DiscountTransaction */\n" +
                "\n" +
                "/* MD_FK_REF Inserts */" +
                dmlPartScript;
    }

    private String generateSingleConstraint(Field field) {
        String targetExtract = field.getTargetExtract();
        String userStoryNumber = field.getReasonAdded();
        String scdType;
        if (field.getScdType().equals("1")){
            scdType = "BASE";
        } else {
            scdType = "DELTA";
        }
        String foreginField = field.getColumnName().replace("_KEY", "");
        return "\nIF (EXISTS (SELECT * \n" +
                "\t\t\tFROM INFORMATION_SCHEMA.TABLES \n" +
                "\t\t\tWHERE TABLE_SCHEMA = 'dbo' \n" +
                "\t\t\tAND  TABLE_NAME = 'Z_CS_" + targetExtract + "_" + scdType + "')\n" +
                "\tAND EXISTS (SELECT * \n" +
                "\t\t\t\tFROM INFORMATION_SCHEMA.TABLES \n" +
                "\t\t\t\tWHERE TABLE_SCHEMA = 'dbo' \n" +
                "\t\t\t\tAND  TABLE_NAME = 'Z_CS_" + foreginField + "_BASE')\n" +
                "\tAND NOT EXISTS (SELECT * \n" +
                "\t\t\t\t\tFROM sys.foreign_keys\n" +
                "\t\t\t\t\tWHERE name = 'Z_FK_C" + generateShortcut(targetExtract, scdType) +
                "_C" + generateShortcut(foreginField, "BASE") + "')\n" +
                "\t\t\t\t)\n" +
                "BEGIN\n" +
                "\n" +
                "\tALTER TABLE [dbo].[Z_CS_" + targetExtract + "_" + scdType + "] ADD CONSTRAINT " +
                "[Z_FK_C" + generateShortcut(targetExtract, scdType) + "_C" +generateShortcut(foreginField, "BASE") +
                "] FOREIGN KEY([" + field.getColumnName() + "])\n" +
                "\tREFERENCES [dbo].[Z_CS_" + foreginField + "_BASE] ([" + field.getColumnName() + "])\n" +
                "END\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][FK] P17152-" +
                userStoryNumber + ": Z_FK_C" +
                generateShortcut(targetExtract, scdType) + "_C" + generateShortcut(foreginField, "BASE") + "'\n";
    }


    private String generateSingleIndex(Field field) {
        String userStoryNumber = field.getReasonAdded();
        String targetExtract = field.getTargetExtract();
        String scdType;
        if (field.getScdType().equals("1")){
            scdType = "BASE";
        } else {
            scdType = "DELTA";
        }
        String foreginField = field.getColumnName().replace("_KEY", "");
        return "\nIF (EXISTS (SELECT *\n" +
                "\t\t\tFROM INFORMATION_SCHEMA.COLUMNS\n" +
                "\t\t\tWHERE TABLE_SCHEMA = 'dbo'\n" +
                "\t\t\tAND TABLE_NAME = 'Z_CS_" + targetExtract + "_" + scdType + "'\n" +
                "\t\t\tAND COLUMN_NAME = '" + field.getColumnName() + "')\n" +
                "\tAND NOT EXISTS (SELECT *\n" +
                "\t\t\t\t\tFROM sys.indexes\n" +
                "\t\t\t\t\tWHERE name = 'Z_FK_C"  +
                generateShortcut(targetExtract, scdType) + "_C" + generateShortcut(foreginField, "BASE") + "_XFK')\n" +
                "\t)\n" +
                "BEGIN\n" +
                "\tcreate nonclustered index Z_FK_C" + generateShortcut(targetExtract, scdType) + "_C" +
                generateShortcut(foreginField, "BASE") + "_XFK on Z_CS_" + targetExtract + "_" + scdType +
                " (" + field.getColumnName() + " ASC)\n" +
                "END\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][index] " +
                userStoryNumber +": Z_FK_C" + generateShortcut(targetExtract, scdType) + "_C" +
                generateShortcut(foreginField, "BASE") + "_XFK'\n";
    }

    private String generateSingleInsertion(Field field) {
        String targetExtract = field.getTargetExtract();
        String userStoryNumber = field.getReasonAdded();
        String scdType;
        if (field.getScdType().equals("1")){
            scdType = "BASE";
        } else {
            scdType = "DELTA";
        }
        String foreginField = field.getColumnName().replace("_KEY", "");
        return "\n/* Insert MD_FK_REF record: Z_FK_C" + generateShortcut(targetExtract, scdType) + "_C" +
                generateShortcut(foreginField, "BASE") + " */\n" +
                "IF (EXISTS (SELECT * \n" +
                "\t\t\tFROM sys.foreign_keys\n" +
                "\t\t\tWHERE name = 'Z_FK_C" + generateShortcut(targetExtract, scdType) + "_C" +
                generateShortcut(foreginField, "BASE") + "')\n" +
                "\tAND NOT EXISTS (SELECT *\n" +
                "\t\t\t\t\tFROM MD_FK_REF\n" +
                "\t\t\t\t\tWHERE FK_CONSTR_NAME = 'Z_FK_C" + generateShortcut(targetExtract, scdType) + "_C" +
                generateShortcut(foreginField, "BASE") + "')\n" +
                ")\n" +
                "BEGIN\n" +
                "\tINSERT INTO MD_FK_REF(TRF_NAME,FK_TAB_NAME,FK_COL_NAME,PK_TAB_NAME,PK_COL_NAME,LOB_CD,FK_CONSTR_NAME," +
                "OOTB_FUTURE_USE_FL,MULTI_SRCE_FL,SELF_REF_FL) VALUES " +
                "('Z_TRF_" + targetExtract + "','Z_CS_" + targetExtract + "_" + scdType + "','" + field.getColumnName() +
                "','Z_CS_" + foreginField + "_BASE','" +field.getColumnName() + "','All','" +
                "Z_FK_C" + generateShortcut(targetExtract, scdType) + "_C" +
                generateShortcut(foreginField, "BASE") + "','N','N','N');\n" +
                "END\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][MD_FK_REF] " + userStoryNumber +
                ": Z_FK_C" + generateShortcut(targetExtract, scdType) + "_C" +
                generateShortcut(foreginField, "BASE") + "'\n";
    }
}
