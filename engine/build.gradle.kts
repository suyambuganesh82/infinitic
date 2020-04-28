import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.io.BufferedReader
import java.io.InputStreamReader

group = "com.zenaton.engine"
version = "1.0-SNAPSHOT"

plugins {
    kotlin("jvm") version "1.3.70"
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.apache.pulsar:pulsar-client:2.5.+")
    implementation("org.apache.pulsar:pulsar-functions-api:2.5.+")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.10.+")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.+")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:4.0.+")
    testImplementation("io.kotest:kotest-property-jvm:4.0.+")
    testImplementation("io.kotest:kotest-core-jvm:4.0.+")
    testImplementation("io.mockk:mockk:1.9.+")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

tasks {
    named<ShadowJar>("shadowJar") {
        mergeServiceFiles()
    }
}

tasks {
    build {
        dependsOn("ktlintFormat")
        dependsOn(shadowJar)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register("setupZenaton") {
    group = "Zenaton"
    description = "Setup Zenaton into Pulsar"
    dependsOn(":build")
    doLast {
        setZenatonFunction(
            className = "com.zenaton.pulsar.topics.workflows.functions.StateFunction",
            topicIn = "workflows",
            action = "create"
        )
        setZenatonFunction(
            className = "com.zenaton.pulsar.topics.workflows.functions.TraceFunction",
            topicIn = "workflows",
            topicOut = "traces",
            action = "create"
        )
    }
}

tasks.register("uploadZenatonFunctions") {
    group = "Zenaton"
    description = "Upload Zenaton functions to Pulsar"
    dependsOn(":build")
    doLast {
        setZenatonFunction(
            className = "com.zenaton.pulsar.topics.workflows.functions.StateFunction",
            topicIn = "workflows"
        )
        setZenatonFunction(
            className = "com.zenaton.pulsar.topics.workflows.functions.TraceFunction",
            topicIn = "workflows",
            topicOut = "traces"
        )
    }
}

tasks.register("cleanZenaton") {
    group = "Zenaton"
    description = "Clean Zenaton from Pulsar"
    doLast {
        deleteZenatonFunction("StateFunction")
        deleteZenatonFunction("TraceFunction")
        forceDeleteTopic("workflows")
        forceDeleteTopic("tasks")
        forceDeleteTopic("decisions")
        forceDeleteTopic("traces")
        forceDeleteTopic("logs")
    }
}

fun setZenatonFunction(
    className: String,
    topicIn: String,
    topicOut: String? = null,
    topicLogs: String = "logs",
    action: String = "update",
    tenant: String = "public",
    namespace: String = "default"
) {
    println("$action $className in $topicIn")
    val cmd = mutableListOf("docker-compose", "exec", "-T", "pulsar", "bin/pulsar-admin", "functions", action,
        "--jar", "/zenaton/engine/build/engine-1.0-SNAPSHOT-all.jar",
        "--classname", className,
        "--log-topic", "persistent://$tenant/$namespace/$topicLogs",
        "--inputs", "persistent://$tenant/$namespace/$topicIn"
    )
    if (topicOut != null) {
        cmd.add("--output")
        cmd.add("persistent://$tenant/$namespace/$topicOut")
    }
    exec(cmd.toTypedArray())
}

fun deleteZenatonFunction(name: String, tenant: String = "public", namespace: String = "default") {
    println("Deleting $name function from $tenant/$namespace")
    val cmd = arrayOf("docker-compose", "exec", "-T", "pulsar", "bin/pulsar-admin", "functions", "delete",
        "--tenant", tenant,
        "--namespace", namespace,
        "--name", name
    )
    exec(cmd)
}

fun forceDeleteTopic(topic: String, tenant: String = "public", namespace: String = "default") {
    println("Deleting $topic topic from $tenant/$namespace")
    val cmd = arrayOf("docker-compose", "exec", "-T", "pulsar", "bin/pulsar-admin", "topics", "delete",
        "persistent://$tenant/$namespace/$topic",
        "--deleteSchema",
        "--force"
    )
    exec(cmd)
}

fun exec(cmd: Array<String>) {
    val p = Runtime.getRuntime().exec(cmd)
    val output = BufferedReader(InputStreamReader(p.inputStream))
    val error = BufferedReader(InputStreamReader(p.errorStream))
    var line: String? = ""
    while (output.readLine().also { line = it } != null) println(line)
    while (error.readLine().also { line = it } != null) println(line)
    p.waitFor()
}
