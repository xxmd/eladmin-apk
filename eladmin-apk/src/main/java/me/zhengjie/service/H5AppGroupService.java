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

import me.zhengjie.entity.h5.H5AppGroup;
import me.zhengjie.repository.H5AppGroupRepository;
import me.zhengjie.entity.h5.query.H5AppGroupQueryCriteria;
import me.zhengjie.utils.PageResult;
import me.zhengjie.utils.PageUtil;
import me.zhengjie.utils.QueryHelp;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class H5AppGroupService {
    @Autowired
    private H5AppGroupRepository repository;

    public List<H5AppGroup> getAll() {
        return repository.findAll();
    }

    public PageResult<H5AppGroup> queryAll(H5AppGroupQueryCriteria criteria, Pageable pageable) {
        Page<H5AppGroup> page = repository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        return PageUtil.toPage(page);
    }

    public List<H5AppGroup> queryAll(H5AppGroupQueryCriteria criteria) {
        return repository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
    }

    @Transactional
    public H5AppGroup findById(Long signId) {
        H5AppGroup H5AppGroup = repository.findById(signId).orElseGet(H5AppGroup::new);
        ValidationUtil.isNull(H5AppGroup.getId(), "ApkSign", "signId", signId);
        return H5AppGroup;
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(H5AppGroup item) {
        repository.save(item);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(H5AppGroup item) {
        repository.save(item);
    }

    public void deleteAll(Long[] ids) {
        for (Long id : ids) {
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