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
import me.zhengjie.entity.LocalStorage;
import me.zhengjie.entity.h5.IconZipReader;
import me.zhengjie.entity.app.AppIcon;
import me.zhengjie.repository.AppIconRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Service
public class AppIconService {
    @Autowired
    private AppIconRepository repository;
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

    public void delete(Long id) {
        Optional<AppIcon> optional = repository.findById(id);
        if (optional.isPresent()) {
            AppIcon item = optional.get();
            localStorageService.delete(item.getMainIconFile());
            localStorageService.delete(item.getZipResFile());
        }
    }
}