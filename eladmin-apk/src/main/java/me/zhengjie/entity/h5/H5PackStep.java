package me.zhengjie.entity.h5;

public enum H5PackStep {
    CREATE_TEMP_DIR(1, "创建临时目录"),
    COPY_SRC_CODE(2, "复制H5小游戏模板代码"),
    REPLACE_TEMPLATE_ARGUMENTS(3, "替换应用包名，版本，签名参数"),
    EXECUTE_GRADLE_CMD(4, "执行gradle打包命令"),
    UPLOAD_RELEASE_APK(5, "上传打包产物"),
    ;

    H5PackStep(int index, String message) {
        this.index = index;
        this.description = message;
    }

    private int index;
    private String description;

    public int getIndex() {
        return index;
    }

    public String getDescription() {
        return description;
    }
}
