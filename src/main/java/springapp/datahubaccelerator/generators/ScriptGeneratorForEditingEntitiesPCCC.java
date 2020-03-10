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
        this.targetExtract = newFields.get(0).getTargetExtract().toUpperCase();
    }

    public String generateDDLScript() {
        return generateTRFPart() +
                "\n/* ODS Changes */\n" +
                generateODSBasePart() +
                generateODSDeltaPart() +
                generateConstraintsPart() +
                generateIndexesPart();
    }

    public String generateDMLScript() {
        List<Field> allKeyFields = getKeyFieldsList(newFields);
        String dmlPartScript ="";
        for (Field field : allKeyFields) {
            dmlPartScript = dmlPartScript + generateSingleInsertion(field);
        }
        return "/* User Story: " + userStoryNumber + "_Bde_" + generateEntityName(targetExtract) + " */\n" +
                "\n" +
                "/* MD_FK_REF Inserts */" +
                dmlPartScript;
    }

    private String generateTRFPart() {
        return "/* User Story: P17152_" +  userStoryNumber + "_" + targetExtract + " */\n" +
                "\n" +
                "/* TRF Changes */\n" +
                "IF (NOT EXISTS (SELECT * \n" +
                "\t\t\tFROM INFORMATION_SCHEMA.COLUMNS\n" +
                "\t\t\tWHERE TABLE_SCHEMA = 'dbo' \n" +
                "\t\t\tAND  TABLE_NAME = 'TRF_" + targetExtract + "'\n" +
                "\t\t\tAND COLUMN_NAME IN (" +
                listingOfColumnNames(newFields, 0) + ")))\n"+
                "BEGIN\n" +
                "\tALTER TABLE TRF_" + targetExtract + " \n" +
                "\tADD" +
                "\t" + generateRowsForScript(newFields, 0) +
                "\nEND\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][TRF] P17152-" + userStoryNumber +
                ": TRF_" + targetExtract + "'\n";
    }

    private String generateODSBasePart(){
        if (newFields.stream().noneMatch(f -> f.getScdType().equals("1"))){
            return "";
        }
        return "\nIF (NOT EXISTS (SELECT * \n" +
                "\t\t\tFROM INFORMATION_SCHEMA.COLUMNS\n" +
                "\t\t\tWHERE TABLE_SCHEMA = 'dbo' \n" +
                "\t\t\tAND  TABLE_NAME = 'CS_" + targetExtract + "_BASE'\n" +
                "\t\t\tAND COLUMN_NAME IN (" +
                listingOfColumnNames(newFields
                        .stream()
                        .filter(x -> !x.getScdType().equals("2"))
                        .collect(Collectors.toList()), 0) +
                ")))\n" +
                "BEGIN\n" +
                "\tALTER TABLE CS_" + targetExtract + "_BASE\n" +
                "\tADD" +
                generateRowsForScript(newFields
                        .stream()
                        .filter(x -> !x.getScdType().equals("2"))
                        .collect(Collectors.toList()), 0) +
                "\nEND\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][ODS] P17152-" + userStoryNumber +
                ": CS_" + targetExtract + "_BASE'\n";
    }

    private String generateODSDeltaPart(){
        if (newFields.stream().noneMatch(f -> f.getScdType().equals("2"))){
            return "";
        }
        return "\nIF (NOT EXISTS (SELECT * \n" +
                "\t\t\tFROM INFORMATION_SCHEMA.COLUMNS\n" +
                "\t\t\tWHERE TABLE_SCHEMA = 'dbo' \n" +
                "\t\t\tAND  TABLE_NAME = 'CS_" + targetExtract +"_DELTA'\n" +
                "\t\t\tAND COLUMN_NAME IN (" +
                listingOfColumnNames(newFields
                        .stream()
                        .filter(x -> !x.getScdType().equals("1"))
                        .collect(Collectors.toList()), 0) +
                ")))\n" +
                "BEGIN\n" +
                "\tALTER TABLE CS_" + targetExtract + "_DELTA\n" +
                "\tADD" +
                generateRowsForScript(newFields
                        .stream()
                        .filter(x -> !x.getScdType().equals("1"))
                        .collect(Collectors.toList()), 0) +
                "\nEND\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][ODS] P17152-" + userStoryNumber +
                ": CS_" + targetExtract + "_DELTA'";
    }

    private String generateConstraintsPart() {
        List<Field> allKeyFields = getKeyFieldsList(newFields);
        String constraintPartScript ="";
        for (Field field : allKeyFields) {
            constraintPartScript = constraintPartScript + generateSingleConstraint(field);
        }
        if (!constraintPartScript.equals("")) {
            return "\n/* Constraints */\n" + constraintPartScript;
        } else {
            return "";
        }
    }

    private String generateSingleConstraint(Field field) {
        String scdType;
        if (field.getScdType().equals("1")){
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
                "\tREFERENCES [dbo].[" + field.getJoinedTable() + "] ([" + field.getColumnName() + "])\n" +
                "END\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][FK] " +
                userStoryNumber + ": Z_FK_" +
                generateShortcut(targetExtract, scdType) + "_" + generateShortcut(pureJoinedTableName, "BASE") + "'\n";
    }

    private String generateIndexesPart() {
        List<Field> allKeyFields = getKeyFieldsList(newFields);
        String indexPartScript ="";
        for (Field field : allKeyFields) {
            indexPartScript = indexPartScript + generateSingleIndex(field.getScdType(), field.getColumnName());
        }
        return "\n/* Indexes */\n" + indexPartScript;
    }

    private String generateSingleIndex(String scdType, String columnName) {
        if (scdType.equals("1")){
            scdType = "BASE";
        } else {
            scdType = "DELTA";
        }
        String foreginField = columnName.replace("_KEY", "");
        return "\nIF (EXISTS (SELECT *\n" +
                "\t\t\tFROM INFORMATION_SCHEMA.COLUMNS\n" +
                "\t\t\tWHERE TABLE_SCHEMA = 'dbo'\n" +
                "\t\t\tAND TABLE_NAME = 'Z_CS_" + targetExtract + "_" + scdType + "'\n" +
                "\t\t\tAND COLUMN_NAME = '" + columnName + "')\n" +
                "\tAND NOT EXISTS (SELECT *\n" +
                "\t\t\t\t\tFROM sys.indexes\n" +
                "\t\t\t\t\tWHERE name = 'Z_FK_"  +
                generateShortcut(targetExtract, scdType) + "_" + generateShortcut(foreginField, "BASE") + "_XFK')\n" +
                "\t)\n" +
                "BEGIN\n" +
                "\tcreate nonclustered index Z_FK_" + generateShortcut(targetExtract, scdType) + "_" +
                generateShortcut(foreginField, "BASE") + "_XFK on Z_CS_" + targetExtract + "_" + scdType +
                " (" + columnName + " ASC)\n" +
                "END\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][index] " +
                userStoryNumber +": Z_FK_" + generateShortcut(targetExtract, scdType) + "_" +
                generateShortcut(foreginField, "BASE") + "_XFK'\n";
    }

    private String generateSingleInsertion(Field field) {
        String scdType;
        if (field.getScdType().equals("1")){
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
                "OOTB_FUTURE_USE_FL,MULTI_SRCE_FL,SELF_REF_FL) VALUES " +
                "('Z_TRF_" + targetExtract + "','Z_CS_" + targetExtract + "_" + scdType + "','" + field.getColumnName() +
                "','Z_CS_" + foreginField + "_BASE','" +field.getColumnName() + "','All','" +
                "Z_FK_" + generateShortcut(targetExtract, scdType) + "_" +
                generateShortcut(foreginField, "BASE") + "','N','N','N');\n" +
                "END\n" +
                "ELSE\n" +
                "PRINT '['+CONVERT( VARCHAR(24), GETDATE(), 120)+'][SCRIPT OMITTED][MD_FK_REF] " + userStoryNumber +
                ": Z_FK_" + generateShortcut(targetExtract, scdType) + "_" +
                generateShortcut(foreginField, "BASE") + "'\n";
    }
}
