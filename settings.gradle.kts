pluginManagement {
    repositories {
        // 阿里云Maven仓库，加速国内依赖下载
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        // 腾讯云Maven仓库，用于获取腾讯相关依赖
        maven { url = uri("https://maven.tencent.com/repository/maven-public/") }
        // Google和Maven Central是官方推荐的仓库
        google()
        mavenCentral()
        // Gradle插件门户，用于获取Gradle插件
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 阿里云Maven仓库，加速国内依赖下载
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        // 腾讯云Maven仓库，用于获取腾讯相关依赖
        maven { url = uri("https://maven.tencent.com/repository/maven-public/") }
        // 使用腾讯镜像仓库，提供更快的下载速度
        maven { url = uri("https://mirrors.tencent.com/repository/maven/tencent_public/") }
        // Google和Maven Central是官方推荐的仓库
        google()
        mavenCentral()
    }
}

rootProject.name = "Mobile Software Implementation"
include(":app")