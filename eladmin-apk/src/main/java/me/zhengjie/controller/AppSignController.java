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
import me.zhengjie.entity.app.AppSign;
import me.zhengjie.service.AppSignService;
import me.zhengjie.entity.app.query.AppSignQueryCriteria;
import me.zhengjie.utils.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.naming.InvalidNameException;
import java.io.IOException;
import java.util.List;


@RestController
@Api(tags = "应用签名")
@RequestMapping("/api/app/sign")
public class AppSignController {

    @Autowired
    private AppSignService service;

//    @ApiOperation("导出数据")
//    @GetMapping(value = "/download")
//    @PreAuthorize("@el.check('app:sign:list')")
//    public void exportApkSign(HttpServletResponse response, ApkSignQueryCriteria criteria) throws IOException {
//        apkSignService.download(apkSignService.queryAll(criteria), response);
//    }

    @GetMapping("/getAll")
    @ApiOperation("获取所有应用签名")
    @PreAuthorize("@el.check('app:sign:list')")
    public ResponseEntity<List<AppSign>> getAll() {
        return new ResponseEntity<>(service.getAll(), HttpStatus.OK);
    }

    @GetMapping
    @ApiOperation("查询应用签名")
    @PreAuthorize("@el.check('app:sign:list')")
    public ResponseEntity<PageResult<AppSign>> query(AppSignQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(service.queryAll(criteria, pageable), HttpStatus.OK);
    }

    @PostMapping
    @Log("新增应用签名")
    @ApiOperation("新增应用签名")
    @PreAuthorize("@el.check('app:sign:add')")
    public ResponseEntity<Object> create(@Validated(BaseEntity.Create.class) @RequestBody AppSign item) throws Exception {
        service.create(item);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/upload")
    @Log("上传应用签名")
    @ApiOperation("上传应用签名")
    @PreAuthorize("@el.check('app:sign:add')")
    public ResponseEntity<Object> upload(@Validated @ModelAttribute AppSign item) throws InvalidNameException, IOException {
        service.upload(item);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping
    @Log("修改应用签名")
    @ApiOperation("修改应用签名")
    @PreAuthorize("@el.check('app:sign:edit')")
    public ResponseEntity<Object> update(@Validated @RequestBody AppSign item) {
        service.update(item);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    @Log("删除应用签名")
    @ApiOperation("删除应用签名")
    @PreAuthorize("@el.check('app:sign:del')")
    public ResponseEntity<Object> delete(@ApiParam(value = "传ID数组[]") @RequestBody Long[] ids) {
        service.deleteAll(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}