import dev.isxander.mtk.manifests.spec.ModManifestSpec

plugins {
    `java-library`
    alias(libs.plugins.fabric.loom) apply false
    alias(libs.plugins.neogradle) apply false
    alias(libs.plugins.modstitch.multiloader)
    alias(libs.plugins.modstitch.manifests)
    alias(libs.plugins.modstitch.accessx)
}

var projectModId: String = project.findProperty("mod_id") as? String ?: "examplemod"
var javaVersion: Int = libs.versions.java.get().toInt()
group = project.findProperty("group") as? String ?: "com.example"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
}

val canonicalAW = layout.projectDirectory.file("$projectModId.accesswidener")
loom.accessWidenerPath = canonicalAW

val fabricAWTask = accessx.convert("fabric", sourceSets.fabric.name) {
    inputFiles.from(canonicalAW)
    outputFormat = accessx.AW_V1
}

val neoforgeAWTask = accessx.convert("neoforge", sourceSets.neoforge.name) {
    inputFiles.from(canonicalAW)
    outputFormat = accessx.AT
}

accessTransformers.files.from(neoforgeAWTask.flatMap { it.outputFile })
tasks.named { it in listOf("neoFormTransformSource", "applyAccessTransformer") }.configureEach {
    dependsOn(neoforgeAWTask)
}


repositories {
    mavenCentral()
}

dependencies {
    minecraft(libs.minecraft)

    fabricLoader(libs.fabric.loader)
    fabricImplementation(libs.fabric.api)

    neoforgeImplementation(libs.neoforge)

    testImplementation(sourceSets.main.get().output)
    testImplementation(libs.junit)
    testImplementation(libs.compile.testing)
}

fun prop(name: String) = project.findProperty(name) as? String ?: throw GradleException("property '$name' not found")

manifests {

    val common = manifests.manifest {
        modId = projectModId
        version = prop("version")
        displayName = prop("mod_name")
        description = prop("description")
        authors = prop("authors").split(",").map(String::trim).toList()
        licenses = listOf(prop("license"))
        iconPath = "assets/$projectModId/icon.png"

        dependency("minecraft", ModManifestSpec.DependencyType.REQUIRED, prop("minecraft_support"))
    }

    fabricModJson(sourceSets.fabric.get()) {
        from(common)
        accessWidener(fabricAWTask)
        dependency("fabric-api", DEPENDS, libs.versions.fabric.api.get())
    }

    neoForgeModsToml(sourceSets.neoforge.get()) {
        from(common)
        accessTransformer(neoforgeAWTask)
    }

}