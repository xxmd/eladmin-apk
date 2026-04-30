/*
 *  Copyright 2019-2025 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package me.zhengjie.entity.app;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import me.zhengjie.entity.BaseEntity;
import me.zhengjie.entity.LocalStorage;
import me.zhengjie.entity.h5.H5AppInfo;

import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;


@Entity
@Data
public class AppSign extends BaseEntity implements Serializable {

    @NotBlank(message = "密钥库密码不能为空")
    private String storePass;

    @NotBlank(message = "密钥别名不能为空")
    private String alias;

    @NotBlank(message = "密钥密码不能为空")
    private String keyPass;

    @Min(value = 1, message = "有效天数必须是正整数", groups = Create.class)
    private int validityDays;

    private String commonName;

    private String organization;

    private String organizationUnit;

    private String countryCode;

    private String city;

    private String state;

    @OneToOne
    @JoinColumn(name = "file_id", referencedColumnName = "storage_id")
    private LocalStorage fileInfo;

    @OneToMany(mappedBy = "signature")
    @JsonManagedReference
    private List<H5AppInfo> appInfoList;

    @JsonIgnore
    public String getDName() {
        return String.format("CN=%s, OU=%s, O=%s, L=%s, ST=%s, C=%s", commonName, organizationUnit, organization, city, state, countryCode);
    }

    @JsonIgnore
    public void setDName(LdapName ldapName) {
        for (Rdn rdn : ldapName.getRdns()) {
            String type = rdn.getType();
            String value = String.valueOf(rdn.getValue());
            switch (type) {
                case "CN":
                    setCommonName(value);
                    break;
                case "O":
                    setOrganization(value);
                    break;
                case "OU":
                    setOrganizationUnit(value);
                    break;
                case "C":
                    setCountryCode(value);
                    break;
                case "L":
                    setCity(value);
                    break;
                case "ST":
                    setState(value);
                    break;
            }
        }
    }
}
