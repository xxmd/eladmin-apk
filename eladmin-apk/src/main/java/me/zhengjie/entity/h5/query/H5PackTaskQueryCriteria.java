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
package me.zhengjie.entity.h5.query;

import lombok.Data;
import me.zhengjie.annotation.Query;

import java.sql.Timestamp;
import java.util.List;


@Data
public class H5PackTaskQueryCriteria {

    @Query(propName = "id", joinName = "h5AppInfo>group")
    private String groupId;

    @Query(joinName = "h5AppInfo", type = Query.Type.INNER_LIKE)
    private String dlChannelId;

    @Query(joinName = "h5AppInfo", type = Query.Type.EQUAL)
    private String pubPlatType;

    @Query(joinName = "h5AppInfo", type = Query.Type.INNER_LIKE)
    private String attrSDKKey;

    @Query(joinName = "h5AppInfo", type = Query.Type.INNER_LIKE)
    private String name;

    @Query(joinName = "h5AppInfo", type = Query.Type.INNER_LIKE)
    private String packageName;

    @Query(joinName = "h5AppInfo", type = Query.Type.INNER_LIKE)
    private String websiteUrl;

    @Query(joinName = "h5AppInfo", type = Query.Type.INNER_LIKE)
    private String version;

    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> updateTime;
}