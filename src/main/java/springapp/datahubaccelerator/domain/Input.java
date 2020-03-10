package springapp.datahubaccelerator.domain;

import javax.persistence.*;

@Entity
public class Input {

    private static final int COLUMNS_LENGTH = 5000;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(length = COLUMNS_LENGTH)
    private String targetExtract;

    @Column(length = COLUMNS_LENGTH)
    private String datatype;

    @Column(length = COLUMNS_LENGTH)
    private String columnName;

    @Column(length = COLUMNS_LENGTH)
    private String scdType;

    @Column(length = COLUMNS_LENGTH)
    private String sourceTable;

    @Column(length = COLUMNS_LENGTH)
    private String generalRuleApplied;

    @Column(length = COLUMNS_LENGTH)
    private String reasonAdded;

    public Input() {
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

    public String getSourceTable() {
        return sourceTable;
    }

    public void setSourceTable(String sourceTable) {
        this.sourceTable = sourceTable;
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
}
