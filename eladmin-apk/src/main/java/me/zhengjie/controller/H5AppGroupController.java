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
import me.zhengjie.entity.h5.H5AppGroup;
import me.zhengjie.service.H5AppGroupService;
import me.zhengjie.entity.h5.query.H5AppGroupQueryCriteria;
import me.zhengjie.utils.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;


@RestController
@Api(tags = "H5应用分组")
@RequestMapping("/api/h5/app/group")
public class H5AppGroupController {

    @Autowired
    private H5AppGroupService service;

    @GetMapping("/getAll")
    @ApiOperation("获取所有H5应用分组")
    @PreAuthorize("@el.check('h5:app:group:list')")
    public ResponseEntity<List<H5AppGroup>> getAll() {
        return new ResponseEntity<>(service.getAll(), HttpStatus.OK);
    }

    @GetMapping
    @ApiOperation("查询H5应用分组")
    @PreAuthorize("@el.check('h5:app:group:list')")
    public ResponseEntity<PageResult<H5AppGroup>> query(H5AppGroupQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(service.queryAll(criteria, pageable), HttpStatus.OK);
    }

    @PostMapping
    @Log("新增H5应用分组")
    @ApiOperation("新增H5应用分组")
    @PreAuthorize("@el.check('h5:app:group:add')")
    public ResponseEntity<Object> create(@Validated(BaseEntity.Create.class) @RequestBody H5AppGroup item) throws IOException, InterruptedException {
        service.create(item);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping
    @Log("修改H5应用分组")
    @ApiOperation("修改H5应用分组")
    @PreAuthorize("@el.check('h5:app:group:edit')")
    public ResponseEntity<Object> update(@Validated @RequestBody H5AppGroup item) {
        service.update(item);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    @Log("删除H5应用分组")
    @ApiOperation("删除H5应用分组")
    @PreAuthorize("@el.check('h5:app:group:del')")
    public ResponseEntity<Object> delete(@ApiParam(value = "传ID数组[]") @RequestBody Long[] ids) {
        service.deleteAll(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}