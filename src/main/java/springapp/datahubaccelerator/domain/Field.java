package springapp.datahubaccelerator.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Field {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    String targetExtract;
    String datatype;

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

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }
}
