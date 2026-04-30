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
package me.zhengjie.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import me.zhengjie.annotation.Log;
import me.zhengjie.entity.BaseEntity;
import me.zhengjie.entity.h5.H5AppInfo;
import me.zhengjie.service.H5AppInfoService;
import me.zhengjie.entity.h5.query.H5AppInfoQueryCriteria;
import me.zhengjie.utils.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@Api(tags = "H5应用信息")
@RequestMapping("/api/h5/app/info")
public class H5AppInfoController {

    @Autowired
    private H5AppInfoService service;

    @GetMapping
    @ApiOperation("查询H5应用信息")
    @PreAuthorize("@el.check('h5:app:info:list')")
    public ResponseEntity<PageResult<H5AppInfo>> query(H5AppInfoQueryCriteria criteria, Pageable pageable) {
        PageResult<H5AppInfo> list = service.queryAll(criteria, pageable);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @PostMapping
    @Log("新增H5应用信息")
    @ApiOperation("新增H5应用信息")
    @PreAuthorize("@el.check('h5:app:info:add')")
    public ResponseEntity<Object> create(@Validated(BaseEntity.Create.class) @ModelAttribute H5AppInfo item) throws Exception {
        service.create(item);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping
    @Log("修改H5应用信息")
    @ApiOperation("修改H5应用信息")
    @PreAuthorize("@el.check('h5:app:info:edit')")
    public ResponseEntity<Object> update(@Validated @ModelAttribute H5AppInfo item) throws Exception {
        service.update(item);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    @Log("删除H5应用信息")
    @ApiOperation("删除H5应用信息")
    @PreAuthorize("@el.check('h5:app:info:del')")
    public ResponseEntity<Object> delete(@ApiParam(value = "传ID数组[]") @RequestBody Long[] ids) {
        service.deleteAll(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}