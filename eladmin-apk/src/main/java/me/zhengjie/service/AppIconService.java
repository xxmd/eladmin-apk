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

import cn.hutool.core.util.ZipUtil;
import lombok.extern.slf4j.Slf4j;
import me.zhengjie.entity.LocalStorage;
import me.zhengjie.entity.h5.H5AppInfo;
import me.zhengjie.entity.h5.IconZipReader;
import me.zhengjie.entity.app.AppIcon;
import me.zhengjie.repository.AppIconRepository;
import me.zhengjie.repository.H5AppInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class AppIconService {
    @Autowired
    private AppIconRepository repository;
    @Autowired
    private H5AppInfoRepository appInfoRepository;
    @Autowired
    private LocalStorageService localStorageService;

    @Transactional(rollbackFor = Exception.class)
    public AppIcon create(AppIcon item) throws IOException {
        IconZipReader iconZipReader = new IconZipReader(item.getZipResFile().getRaw());
        LocalStorage iconFile = localStorageService.create(iconZipReader.getPlayStorePng());
        item.setMainIconFile(iconFile);
        File androidDir = iconZipReader.getAndroidDir();
        File zipDir = ZipUtil.zip(androidDir);
        LocalStorage iconZipFile = localStorageService.create(zipDir);
        item.setZipResFile(iconZipFile);
        iconZipReader.release();
        return repository.save(item);
    }

    @Transactional
    public void delete(@NotNull AppIcon item) {
        List<H5AppInfo> list = appInfoRepository.findByIcon(item);
        if (list.size() > 1) {
            log.info("id为 {} 的图标绑定应用数量为 {}, 不执行删除操作", item.getId(), list.size());
            return;
        }
        repository.delete(item);
        localStorageService.delete(item.getMainIconFile());
        localStorageService.delete(item.getZipResFile());
    }
}