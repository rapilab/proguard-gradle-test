package com.phodal.gradle.template.plugin

import com.phodal.gradle.template.plugin.internal.DependencyManager
import com.phodal.gradle.template.plugin.internal.tasks.*
import com.phodal.gradle.template.plugin.internal.variant.ApkVariantOutputData
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.compile.JavaCompile

class ApplicationTaskManager(val project: Project, dependencyManager: DependencyManager) :
    TaskManager {
    fun createMockableJarTask() {

    }

    fun createAssembleTask(variantOutputData: ApkVariantOutputData): Task {
        val assembleTask = project.tasks.create("assemble")
        return assembleTask
    }

    fun createTasksForVariantData(
        tasks: TaskContainer,
        variantData: ApkVariantOutputData
    ) {
        createAnchorTasks(variantData)
        createCheckManifestTask()

        createMergeAppManifestsTask()
        createGenerateResValuesTask()
        createRenderscriptTask()

        createMergeResourcesTask()
        createMergeAssetsTask()

        createBuildConfigTask()
        createPreprocessResourcesTask()
        createProcessResTask(variantData)
        createProcessJavaResTask()
        createAidlTask()

        // compile java
        createJavaCompileTask()
        createJarTask()
        createPostCompilationTasks(variantData)

        val is_ndk = false
        if (is_ndk) {
            createNdkTasks()
        }

        createSplitResourcesTasks();
        createSplitAbiTasks();


        createPackagingTask(tasks, variantData)

        handleMicroApp()
    }

    private fun createAnchorTasks(variantData: ApkVariantOutputData) {
        createPreBuildTasks(variantData)
    }

    private fun createPreBuildTasks(variantData: ApkVariantOutputData) {
        variantData.preBuildTask = project.tasks.create("preBuild")
        val prepareDependenciesTask = project.tasks.create("prepareDependencies", PrepareDependenciesTask::class.java)

        variantData.prepareDependenciesTask = prepareDependenciesTask
        prepareDependenciesTask.dependsOn(variantData.preBuildTask)
    }

    private fun createCheckManifestTask() {}
    private fun createMergeAppManifestsTask() {}
    private fun createGenerateResValuesTask() {}
    private fun createRenderscriptTask() {}
    private fun createMergeResourcesTask() {}
    private fun createMergeAssetsTask() {}
    private fun createBuildConfigTask() {}
    private fun createPreprocessResourcesTask() {}
    private fun createProcessResTask(variantOutputData: ApkVariantOutputData) {
        val processAndroidResources = project.tasks.create("processResources", ProcessAndroidResources::class.java)
        variantOutputData.processResourcesTask = processAndroidResources
    }

    private fun createProcessJavaResTask() {}
    private fun createAidlTask() {}
    private fun createJavaCompileTask() {
        val javaCompileTask = project.tasks.create("compileDebugJava", JavaCompile::class.java)
    }

    private fun createJarTask() {}

    /**
     * Creates the post-compilation tasks for the given Variant.
     *
     * These tasks create the dex file from the .class files, plus optional intermediary steps
     * like proguard and jacoco
     *
     * @param variantData the variant data.
     */
    private fun createPostCompilationTasks(variantData: ApkVariantOutputData) {
        val dexTask = project.tasks.create("dex", Dex::class.java)
        variantData.dexTask = dexTask

        maybeCreateProguardTasks(variantData)

        variantData.dexTask.dependsOn(variantData.obfuscationTask)
    }

    private fun maybeCreateProguardTasks(variantData: ApkVariantOutputData) {
        val proguardTask = project.tasks.create("proguard", AndroidProGuardTask::class.java)
        variantData.obfuscationTask = proguardTask
    }

    private fun createNdkTasks() {}

    private fun createSplitAbiTasks() {}
    private fun createSplitResourcesTasks() {}
    private fun createPackagingTask(
        tasks: TaskContainer,
        variantOutputData: ApkVariantOutputData
    ) {
        val packageApp = project.tasks.create("package", PackageApplication::class.java)

        packageApp.dependsOn(variantOutputData.processResourcesTask)

        val shrink = createShrinkResourcesTask(variantOutputData)
        packageApp.dependsOn(shrink)

//        packageApp.convention
        var appTask: Task = packageApp

//        variantOutputData.assembleTask = createAssembleTask(variantOutputData)
//        variantOutputData.assembleTask!!.dependsOn(appTask)

    }

    private fun createShrinkResourcesTask(variantOutputData: ApkVariantOutputData): ShrinkResources {
        val task = project.tasks.create("shrinkResources", ShrinkResources::class.java)
        return task
    }

    /**
     * Configure variantData to generate embedded wear application.
     */
    private fun handleMicroApp() {

    }

    fun optionalDependsOn(main: Task, vararg dependencies: Task) {
        for (dependency in dependencies) {
            main.dependsOn(dependency)
        }

    }

}
