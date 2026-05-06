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
import me.zhengjie.annotation.Log;
import me.zhengjie.entity.h5.H5PackTask;
import me.zhengjie.service.H5PackTaskService;
import me.zhengjie.entity.h5.query.H5PackTaskQueryCriteria;
import me.zhengjie.utils.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RestController
@Api(tags = "H5打包任务")
@RequestMapping("/api/h5/pack/task")
public class H5PackTaskController {

    @Autowired
    private H5PackTaskService service;

    @GetMapping
    @ApiOperation("查询H5打包任务")
    @PreAuthorize("@el.check('h5:pack:task:list')")
    public ResponseEntity<PageResult<H5PackTask>> query(H5PackTaskQueryCriteria criteria, Pageable pageable) {
        PageResult<H5PackTask> list = service.queryAll(criteria, pageable);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/run/{id}")
    @Log("运行H5打包任务")
    @ApiOperation("运行H5打包任务")
    @PreAuthorize("@el.check('h5:pack:task:run')")
    public ResponseEntity<Object> run(@PathVariable("id") Long id) {
        service.run(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/reset/{id}")
    @Log("重置H5打包任务")
    @ApiOperation("重置H5打包任务")
    @PreAuthorize("@el.check('h5:pack:task:reset')")
    public ResponseEntity<Object> reset(@PathVariable("id") Long id) {
        service.resetById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}