# =========================================
# Dockerfile: Android SDK 30 + Build-Tools 35 + JDK 17 + gradle
# =========================================

FROM openjdk:17.0.1

ENV DEBIAN_FRONTEND=noninteractive
ENV ANDROID_SDK_ROOT=/opt/android/sdk
ENV ANDROID_HOME=$ANDROID_SDK_ROOT
ENV PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$PATH

RUN microdnf install -y \
    wget \
    unzip \
    bash \
    glibc \
    libstdc++ \
    zlib \
    && microdnf clean all

# 下载 Android Command-line Tools 最新版本
RUN mkdir -p $ANDROID_SDK_ROOT/cmdline-tools \
    && wget -O /tmp/cmdline-tools.zip https://dl.google.com/android/repository/commandlinetools-linux-14742923_latest.zip \
    && mkdir -p $ANDROID_SDK_ROOT/cmdline-tools \
    && unzip /tmp/cmdline-tools.zip -d $ANDROID_SDK_ROOT/cmdline-tools \
    && mv $ANDROID_SDK_ROOT/cmdline-tools/cmdline-tools $ANDROID_SDK_ROOT/cmdline-tools/latest \
    && rm /tmp/cmdline-tools.zip

# sdkmanager所需安装内容对应安卓项目app目录中build.gradle设置
# compileSdkVersion: platforms版本
# buildToolsVersion: build-tools版本
RUN yes | bash $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager \
    "platform-tools" \
    "build-tools;35.0.0" \
    "platforms;android-30"

COPY h5-mini-game /app/h5-mini-game
WORKDIR /app

# 运行gradle命令自动安装gradle
RUN bash /app/h5-mini-game/gradlew --version

CMD ["/bin/bash"]