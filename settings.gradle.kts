pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "foldable-app"

// 공용 모듈
include(":core:fold")

// 앱들 (새 앱은 apps/<이름> 폴더를 만들고 여기에 한 줄 추가)
include(":apps:pick")
include(":apps:magic")
