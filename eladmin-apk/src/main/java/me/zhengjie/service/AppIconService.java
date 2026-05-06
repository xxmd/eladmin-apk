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
import me.zhengjie.utils.FileUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    public AppIcon create(MultipartFile multipartFile) throws IOException {
        AppIcon appIcon = new AppIcon();
        IconZipReader iconZipReader = new IconZipReader(multipartFile);
        LocalStorage iconFile = localStorageService.create(iconZipReader.getPlayStorePng());
        appIcon.setMainIconFile(iconFile);
        File androidZipDir = iconZipReader.getAndroidZipDir();
        LocalStorage iconZipFile = localStorageService.create(androidZipDir);
        appIcon.setZipResFile(iconZipFile);
        iconZipReader.release();
        return repository.save(appIcon);
    }

    @Transactional(rollbackFor = Exception.class)
    public AppIcon copy(AppIcon src) throws IOException {
        AppIcon item = new AppIcon();
        item.setMainIconFile(localStorageService.copy(src.getMainIconFile()));
        item.setZipResFile(localStorageService.copy(src.getZipResFile()));
        return repository.save(item);
    }

    @Transactional
    public void delete(@NotNull AppIcon item) {
        repository.delete(item);
        localStorageService.delete(item.getMainIconFile());
        localStorageService.delete(item.getZipResFile());
    }
}