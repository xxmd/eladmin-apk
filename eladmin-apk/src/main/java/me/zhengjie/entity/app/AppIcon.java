package me.zhengjie.entity.app;

import lombok.Data;
import me.zhengjie.entity.BaseEntity;
import me.zhengjie.entity.LocalStorage;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.io.Serializable;

@Data
@Entity
public class AppIcon extends BaseEntity implements Serializable {
    @OneToOne
    @JoinColumn(name = "file_id", referencedColumnName = "storage_id")
    private LocalStorage mainIconFile;

    @OneToOne
    @JoinColumn(name = "zip_file_id", referencedColumnName = "storage_id")
    private LocalStorage zipResFile;
}
