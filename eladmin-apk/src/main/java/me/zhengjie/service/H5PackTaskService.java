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
import com.android.apksig.apk.ApkFormatException;
import lombok.extern.slf4j.Slf4j;
import me.zhengjie.entity.LocalStorage;
import me.zhengjie.entity.app.AppSign;
import me.zhengjie.entity.h5.*;
import me.zhengjie.repository.H5AppInfoRepository;
import me.zhengjie.repository.H5PackTaskRepository;
import me.zhengjie.entity.h5.query.H5PackTaskQueryCriteria;
import me.zhengjie.util.GradleUtil;
import me.zhengjie.util.SignatureUtil;
import me.zhengjie.utils.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.CertificateException;
import java.sql.Timestamp;
import java.util.*;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
public class H5PackTaskService {
    @Autowired
    private H5PackTaskRepository repository;
    @Autowired
    private H5AppInfoRepository appInfoRepository;
    @Autowired
    private LocalStorageService localStorageService;
    @Autowired
    private ResourceLoader resourceLoader;

    public PageResult<H5PackTask> queryAll(H5PackTaskQueryCriteria criteria, Pageable pageable) {
        Page<H5PackTask> page = repository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        return PageUtil.toPage(page);
    }

    @Transactional(rollbackFor = Exception.class)
    public void createByH5AppInfo(H5AppInfo h5AppInfo) {
        H5PackTask h5PackTask = new H5PackTask();
        h5PackTask.setH5AppInfo(h5AppInfo);
        repository.save(h5PackTask);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(H5PackTask item) throws IOException {
        repository.save(item);
    }

    @Transactional
    public void deleteByAppInfo(H5AppInfo h5AppInfo) {
        List<H5PackTask> itemList = repository.findByH5AppInfo(h5AppInfo);
        if (itemList != null && !itemList.isEmpty()) {
            for (H5PackTask h5PackTask : itemList) {
                delete(h5PackTask);
            }
        }
    }

    @Transactional
    private void delete(H5PackTask item) {
        deleteRelatedFiles(item);
        repository.delete(item);
    }

    private Timestamp getNow() {
        return new Timestamp(System.currentTimeMillis());
    }

    public void resetById(Long id) {
        Optional<H5PackTask> optional = repository.findById(id);
        if (optional.isPresent()) {
            reset(optional.get());
        } else {
            throw new IllegalArgumentException(String.format("id为%d的打包任务不存在", id));
        }
    }

    private void deleteRelatedFiles(H5PackTask item) {
        LocalStorage srcApkFile = item.getApkFile();
        LocalStorage srcXMApkFile = item.getXmApkFile();
        if (srcApkFile != null) {
            localStorageService.delete(srcApkFile);
        }
        if (srcXMApkFile != null) {
            localStorageService.delete(srcXMApkFile);
        }
    }

    private void reset(H5PackTask item) {
        deleteRelatedFiles(item);
        item.setTaskStartTime(null);
        item.setTaskEndTime(null);
        item.setException(null);
        item.setApkFile(null);
        item.setXmApkFile(null);
        repository.save(item);
    }

    public void run(Long id) {
        // TODO 并发运行数量限制
        Optional<H5PackTask> optional = repository.findById(id);
        if (optional.isPresent()) {
            H5PackTask item = optional.get();
            reset(item);
            item.setTaskStartTime(getNow());
            repository.save(item);
            H5PackTaskService proxy = SpringBeanHolder.getBean(H5PackTaskService.class);
            proxy.run(item);
        } else {
            throw new IllegalArgumentException(String.format("id为%d的打包任务不存在", id));
        }
    }

    private void updateApks(H5MiniGameProject h5MiniGameProject, H5PackTask item) throws IOException, UnrecoverableKeyException, ApkFormatException, CertificateException, NoSuchAlgorithmException, SignatureException, KeyStoreException, InvalidKeyException {
        Optional<File> apkFileOptional = h5MiniGameProject.getReleaseApkFile();
        if (apkFileOptional.isPresent()) {
            LocalStorage apkFile = localStorageService.create(apkFileOptional.get());
            item.setApkFile(apkFile);
            if ("XIAOMI".equals(item.getH5AppInfo().getPubPlatType())) {
                File xiaoMiVerifyApk = h5MiniGameProject.getXiaoMiVerifyApk();
                AppSign signature = item.getH5AppInfo().getSignature();
                File signedXMApk = SignatureUtil.signApk(signature, xiaoMiVerifyApk);
                LocalStorage xmApkFile = localStorageService.create(signedXMApk);
                item.setXmApkFile(xmApkFile);
            }
        }
    }

    @Async("packTaskExecutor")
    public void run(H5PackTask item) {
        log.info("开始执行H5小游戏打包任务 id: {}, thread: {}",
                item.getId(),
                Thread.currentThread().getName());
        File tempDir = null;
        try {
            // 1. 复制模板工程
            item.setCopyStartTime(getNow());
            tempDir = Files.createTempDirectory("unzip").toFile();
            Resource resource = resourceLoader.getResource("classpath:templates/h5-mini-game.zip");
            ZipUtil.unzip(new ZipInputStream(resource.getInputStream()), tempDir);
            File projectDir = new File(tempDir, "h5-mini-game");
            item.setCopyEndTime(getNow());

            // 2. 替换工程中参数
            item.setReplaceStartTime(getNow());
            H5MiniGameProject h5MiniGameProject = new H5MiniGameProject(projectDir);
            h5MiniGameProject.replaceAll(item.getH5AppInfo());
            item.setReplaceEndTime(getNow());

            // 3. gradle打包
            item.setGradleStartTime(getNow());
            GradleUtil.assembleRelease(projectDir);
            item.setGradleEndTime(getNow());

            // 4. 更新release安装包和小米签名校验包
            updateApks(h5MiniGameProject, item);
        } catch (Exception e) {
            log.error("H5小游戏打包异常", e);
            item.setException(ExceptionUtils.getStackTrace(e));
        } finally {
            item.setTaskEndTime(getNow());
            repository.save(item);
            if (tempDir != null) {
                try {
                    FileUtils.deleteDirectory(tempDir);
                } catch (IOException ignored) {
                    log.error("删除H5小游戏临时打包目录异常", ignored);
                }
            }
        }
    }
}