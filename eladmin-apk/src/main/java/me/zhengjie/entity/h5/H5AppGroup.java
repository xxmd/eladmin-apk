package me.zhengjie.entity.h5;

import lombok.Data;
import me.zhengjie.entity.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Entity
@Table(name = "h5_app_group")
public class H5AppGroup extends BaseEntity implements Serializable {
    private String name;
}
