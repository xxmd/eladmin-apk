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
package me.zhengjie.service;

import lombok.extern.slf4j.Slf4j;
import me.zhengjie.entity.app.AppIcon;
import me.zhengjie.entity.app.AppSign;
import me.zhengjie.entity.h5.*;
import me.zhengjie.repository.H5AppInfoRepository;
import me.zhengjie.entity.app.query.AppSignQueryCriteria;
import me.zhengjie.entity.h5.query.H5AppInfoQueryCriteria;
import me.zhengjie.utils.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
public class H5AppInfoService {
    @Autowired
    private H5AppInfoRepository repository;
    @Autowired
    private AppIconService appIconService;
    @Autowired
    private AppSignService appSignService;
    @Autowired
    private H5PackTaskService h5PackTaskService;

    public PageResult<H5AppInfo> queryAll(H5AppInfoQueryCriteria criteria, Pageable pageable) {
        Page<H5AppInfo> page = repository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        return PageUtil.toPage(page);
    }

    public List<H5AppInfo> queryAll(AppSignQueryCriteria criteria) {
        return repository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
    }

    @Transactional
    public H5AppInfo findById(Long signId) {
        H5AppInfo H5AppInfo = repository.findById(signId).orElseGet(H5AppInfo::new);
        ValidationUtil.isNull(H5AppInfo.getId(), "ApkSign", "signId", signId);
        return H5AppInfo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(H5AppInfo item) throws Exception {
        if (item.getIcon().getId() == null) {
            AppIcon savedAppIcon = appIconService.create(item.getIcon());
            item.getIcon().setId(savedAppIcon.getId());
        }
        if (item.getSignature() == null) {
            item.setSignature(createAppSign(item));
        }
        H5AppInfo savedItem = repository.save(item);
        h5PackTaskService.createByAppInfoId(savedItem.getId());
    }

    private AppSign createAppSign(H5AppInfo h5AppInfo) throws Exception {
        AppSign appSign = new AppSign();
        appSign.setValidityDays(365 * 100);
        String password = RandomStringUtils.randomAlphanumeric(8);
        appSign.setStorePass(password);
        appSign.setAlias("alias");
        appSign.setKeyPass(password);
        String name = h5AppInfo.getName().replaceAll(" ", "_");
        appSign.setRemark(name + "_" + h5AppInfo.getPubPlatType().toLowerCase());
        return appSignService.create(appSign);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(H5AppInfo item) throws Exception {
        if (item.getIcon().getZipResFile().getId() == null) {
            AppIcon savedAppIcon = appIconService.create(item.getIcon());
            item.getIcon().setId(savedAppIcon.getId());
        }
        if (item.getSignature() == null) {
            item.setSignature(createAppSign(item));
        }
        repository.save(item);
    }

    @Transactional
    public void deleteAll(Long[] ids) {
        for (Long id : ids) {
            Optional<H5AppInfo> optional = repository.findById(id);
            if (optional.isPresent()) {
                H5AppInfo item = optional.get();
                // 删除图标
                appIconService.delete(item.getIcon());
                // 删除签名
                appSignService.delete(item.getSignature());
                // 删除打包任务
                h5PackTaskService.deleteByAppInfo(item);
                // 删除自己
                repository.delete(item);
            }
        }
    }

//    public void download(List<ApkSignDto> all, HttpServletResponse response) throws IOException {
//        List<Map<String, Object>> list = new ArrayList<>();
//        for (ApkSignDto apkSign : all) {
//            Map<String, Object> map = new LinkedHashMap<>();
//            map.put("密钥库密码", apkSign.getStorePass());
//            map.put("密钥别名", apkSign.getAlias());
//            map.put("密钥密码", apkSign.getKeyPass());
//            map.put("文件id", apkSign.getFileId());
//            map.put("备注", apkSign.getRemark());
//            map.put("创建者", apkSign.getCreateBy());
//            map.put("创建日期", apkSign.getCreateTime());
//            map.put("更新者", apkSign.getUpdateBy());
//            map.put("更新时间", apkSign.getUpdateTime());
//            list.add(map);
//        }
//        FileUtil.downloadExcel(list, response);
//    }
}