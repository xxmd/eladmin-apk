package me.zhengjie.entity.h5;

import lombok.Data;
import me.zhengjie.entity.BaseEntity;
import me.zhengjie.entity.LocalStorage;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "h5_pack_task")
public class H5PackTask extends BaseEntity implements Serializable {
    @OneToOne
    @JoinColumn(name = "app_info_id", referencedColumnName = "id")
    private H5AppInfo h5AppInfo;

    private Timestamp taskStartTime;

    private Timestamp copyStartTime;

    private Timestamp copyEndTime;

    private Timestamp replaceStartTime;

    private Timestamp replaceEndTime;

    private Timestamp gradleStartTime;

    private Timestamp gradleEndTime;

    private Timestamp taskEndTime;

    @OneToOne
    @JoinColumn(name = "apk_file_id", referencedColumnName = "storage_id")
    private LocalStorage apkFile;

    @OneToOne
    @JoinColumn(name = "xm_apk_file_id", referencedColumnName = "storage_id")
    private LocalStorage xmApkFile;

    private String exception;
}
