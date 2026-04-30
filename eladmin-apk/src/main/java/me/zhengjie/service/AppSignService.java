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

import me.zhengjie.entity.LocalStorage;
import me.zhengjie.entity.app.AppSign;
import me.zhengjie.entity.app.KeyStoreReader;
import me.zhengjie.entity.app.query.AppSignQueryCriteria;
import me.zhengjie.repository.AppSignRepository;
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

@Service
public class AppSignService {
    @Autowired
    private AppSignRepository repository;
    @Autowired
    private LocalStorageService localStorageService;

    public List<AppSign> getAll() {
        List<AppSign> all = repository.findAll();
        return all;
    }

    public PageResult<AppSign> queryAll(AppSignQueryCriteria criteria, Pageable pageable) {
        Page<AppSign> page = repository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        return PageUtil.toPage(page);
    }

    public List<AppSign> queryAll(AppSignQueryCriteria criteria) {
        return repository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
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
            Optional<AppSign> apkSignOptional = repository.findById(id);
            if (apkSignOptional.isPresent()) {
                LocalStorage localStorage = apkSignOptional.get().getFileInfo();
                localStorageService.delete(localStorage);
            }
            repository.deleteById(id);
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