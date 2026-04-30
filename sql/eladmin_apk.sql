CREATE TABLE `file_info` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` VARCHAR(255) COMMENT '文件名称',
  `size` BIGINT COMMENT '文件大小',
  `url` TEXT COMMENT '链接地址',
  `remark` TEXT COMMENT '备注',
  `create_by` VARCHAR(255)  COMMENT '创建者',
  `create_time` DATETIME COMMENT '创建日期',
  `update_by` VARCHAR(255)  COMMENT '更新者',
  `update_time` DATETIME COMMENT '更新时间',
  PRIMARY KEY (`id`)
);

CREATE TABLE `apk_sign` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `store_pass` VARCHAR(255) COMMENT '库密码',
  `alias` VARCHAR(255) COMMENT '别名',
  `key_pass` VARCHAR(255) COMMENT '密钥密码',
  `validity_days` INT COMMENT '有效天数',
  `common_name` VARCHAR(255) COMMENT '通用名称',
  `organization` VARCHAR(255) COMMENT '组织名称',
  `organization_unit` VARCHAR(255)  COMMENT '组织部门',
  `country_code` VARCHAR(255)  COMMENT '国家短码',
  `city` VARCHAR(255)  COMMENT '城市',
  `state` VARCHAR(255)  COMMENT '州',
  `file_id` BIGINT COMMENT '签名文件id',
  `remark` TEXT  COMMENT '备注',
  `create_by` VARCHAR(255)  COMMENT '创建者',
  `create_time` DATETIME COMMENT '创建日期',
  `update_by` VARCHAR(255)  COMMENT '更新者',
  `update_time` DATETIME COMMENT '更新时间',
  PRIMARY KEY (`id`)
) COMMENT='签名表';