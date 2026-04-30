package me.zhengjie.entity.h5;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import me.zhengjie.entity.BaseEntity;
import me.zhengjie.entity.app.AppIcon;
import me.zhengjie.entity.app.AppSign;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@Entity
@Table(name = "h5_app_info")
public class H5AppInfo extends BaseEntity implements Serializable {
    @NotNull
    @ManyToOne
    @JoinColumn(name = "group_id", referencedColumnName = "id")
    private H5AppGroup group;

    @NotBlank
    private String dlChannelId;

    @NotBlank
    private String dlProductName;

    @NotBlank
    private String pubPlatType;

    @NotBlank
    @Column(name = "attr_sdk_type")
    private String attrSDKType;

    @NotBlank
    @Column(name = "attr_sdk_key")
    private String attrSDKKey;

    @NotNull
    @OneToOne
    @JoinColumn(name = "icon_id")
    private AppIcon icon;

    @NotBlank
    private String name;

    @NotBlank
    private String version;

    @NotBlank
    private String packageName;

    @NotBlank
    private String websiteUrl;

    @ManyToOne
    @JoinColumn(name = "sign_id")
    @JsonBackReference
    private AppSign signature;
}
