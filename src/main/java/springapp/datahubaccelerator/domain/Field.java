package springapp.datahubaccelerator.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
public class Field {

    private static final int COLUMNS_LENGTH = 5000;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(length = COLUMNS_LENGTH)
    private String targetExtract;

    @Column(length = COLUMNS_LENGTH)
    private String columnName;

    @Column(length = COLUMNS_LENGTH)
    private String datatype;

    @Column(length = COLUMNS_LENGTH)
    private String sourceTable;

    @Column(length = COLUMNS_LENGTH)
    private String scdType;

    @NotNull
    @Column(length = COLUMNS_LENGTH)
    private String generalRuleApplied;

    @Column(length = COLUMNS_LENGTH)
    private String reasonAdded;

    @Column(length = COLUMNS_LENGTH)
    private String columnMapping;

    @Column(length = COLUMNS_LENGTH)
    private static String fromJoinWhere;

    private String joinedTable;
    private String primaryKeyOfJoinedTable;
    private String sourceColumnName;
    private static String primaryTable;

    public Field() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTargetExtract() {
        return targetExtract;
    }

    public void setTargetExtract(String targetExtract) {
        this.targetExtract = targetExtract;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public String getScdType() {
        return scdType;
    }

    public void setScdType(String scdType) {
        this.scdType = scdType;
    }

    public String getGeneralRuleApplied() {
        return generalRuleApplied;
    }

    public void setGeneralRuleApplied(String generalRuleApplied) {
        this.generalRuleApplied = generalRuleApplied;
    }

    public String getReasonAdded() {
        return reasonAdded;
    }

    public void setReasonAdded(String reasonAdded) {
        this.reasonAdded = reasonAdded;
    }

    public String getJoinedTable() {
        return joinedTable;
    }

    public void setJoinedTable(String joinedTable) {
        this.joinedTable = joinedTable;
    }

    public String getSourceTable() {
        return sourceTable;
    }

    public void setSourceTable(String sourceTable) {
        this.sourceTable = sourceTable;
    }

    public String getColumnMapping() {
        return columnMapping;
    }

    public void setColumnMapping(String columnMapping) {
        this.columnMapping = columnMapping;
    }

    public static String getFromJoinWhere() {
        return fromJoinWhere;
    }

    public static void setFromJoinWhere(String fromJoinWhere) {
        Field.fromJoinWhere = fromJoinWhere;
    }

    public String getPrimaryKeyOfJoinedTable() {
        return primaryKeyOfJoinedTable;
    }

    public void setPrimaryKeyOfJoinedTable(String primaryKeyOfJoinedTable) {
        this.primaryKeyOfJoinedTable = primaryKeyOfJoinedTable;
    }

    public String getSourceColumnName() {
        return sourceColumnName;
    }

    public void setSourceColumnName(String sourceColumnName) {
        this.sourceColumnName = sourceColumnName;
    }

    public static String getPrimaryTable() {
        return primaryTable;
    }

    public static void setPrimaryTable(String primaryTable) {
        Field.primaryTable = primaryTable;
    }
}
