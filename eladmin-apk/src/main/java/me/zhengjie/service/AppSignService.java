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
import me.zhengjie.entity.LocalStorage;
import me.zhengjie.entity.app.AppSign;
import me.zhengjie.entity.app.KeyStoreReader;
import me.zhengjie.entity.app.query.AppSignQueryCriteria;
import me.zhengjie.entity.h5.H5AppInfo;
import me.zhengjie.repository.AppSignRepository;
import me.zhengjie.repository.H5AppInfoRepository;
import me.zhengjie.util.BcKeystoreUtil;
import me.zhengjie.utils.PageResult;
import me.zhengjie.utils.PageUtil;
import me.zhengjie.utils.QueryHelp;
import me.zhengjie.utils.ValidationUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.InvalidNameException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class AppSignService {
    @Autowired
    private AppSignRepository repository;
    @Autowired
    private H5AppInfoRepository appInfoRepository;
    @Autowired
    private LocalStorageService localStorageService;

    public List<AppSign> getAll() {
        return repository.findAll();
    }

    public PageResult<AppSign> queryAll(AppSignQueryCriteria criteria, Pageable pageable) {
        Page<AppSign> page = repository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        return PageUtil.toPage(page);
    }

    @Transactional
    public AppSign findById(Long signId) {
        AppSign appSign = repository.findById(signId).orElseGet(AppSign::new);
        ValidationUtil.isNull(appSign.getId(), "ApkSign", "signId", signId);
        return appSign;
    }

    @Transactional(rollbackFor = Exception.class)
    public AppSign create(AppSign item) throws Exception {
        File jksFile = BcKeystoreUtil.createJks(item.getValidityDays(), item.getStorePass(), item.getAlias(), item.getKeyPass(), item.getDName());
        LocalStorage localStorage = localStorageService.create(jksFile);
        FileUtils.delete(jksFile);
        item.setFileInfo(localStorage);
        return repository.save(item);
    }

    @Transactional(rollbackFor = Exception.class)
    public void upload(AppSign item) throws InvalidNameException, IOException {
        MultipartFile multipartFile = item.getFileInfo().getRaw();
        KeyStoreReader keyStoreReader = new KeyStoreReader(multipartFile, item.getStorePass(), item.getAlias(), item.getKeyPass());
        item.setValidityDays(keyStoreReader.getValidityDays());
        item.setDName(keyStoreReader.getDName());
        LocalStorage localStorage = localStorageService.create(multipartFile);
        item.setFileInfo(localStorage);
        repository.save(item);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(AppSign item) {
        repository.save(item);
    }

    public void deleteAll(Long[] ids) {
        for (Long id : ids) {
            Optional<AppSign> optional = repository.findById(id);
            optional.ifPresent(this::delete);
        }
    }

    @Transactional
    public void delete(AppSign signature) {
        if (signature == null || signature.getId() == null) {
            return;
        }
        List<H5AppInfo> list = appInfoRepository.findBySignature(signature);
        if (list.size() > 1) {
            log.info("id为 {} 的签名绑定应用数量为 {}, 不执行删除操作", signature.getId(), list.size());
            return;
        }
        LocalStorage localStorage = signature.getFileInfo();
        localStorageService.delete(localStorage);
        repository.delete(signature);
    }
}