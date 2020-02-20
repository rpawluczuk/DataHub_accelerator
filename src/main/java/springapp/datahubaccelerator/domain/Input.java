package springapp.datahubaccelerator.domain;

import javax.persistence.*;

@Entity
public class Input {

    static final private int COLUMNS_LENGTH = 5000;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    String userStoryNumber;
    String entityName;

    @Column(length = COLUMNS_LENGTH)
    String targetExtract;

    @Column(length = COLUMNS_LENGTH)
    String datatype;

    @Column(length = COLUMNS_LENGTH)
    String columnName;

    @Column(length = COLUMNS_LENGTH)
    String scdType;

    @Column(length = COLUMNS_LENGTH)
    String generalRuleApplied;

    public Input() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserStoryNumber() {
        return userStoryNumber;
    }

    public void setUserStoryNumber(String userStoryNumber) {
        this.userStoryNumber = userStoryNumber;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
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
}
