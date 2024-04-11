plugins {
    java
    jacoco
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "com.thread"
version = "0.1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.reactivestreams:reactive-streams")
    testRuntimeOnly("org.apache.logging.log4j:log4j-core")
}

tasks.all {
    outputs.cacheIf { true }
}

tasks {
    test {
        jvmArgs("-Xshare:off", "-XX:+EnableDynamicAgentLoading")
        useJUnitPlatform()
        finalizedBy(jacocoTestReport)
    }
    jacocoTestReport {
        dependsOn(test)
        reports {
            xml.required = true
            csv.required = false
            html.required = false
        }
    }
}

tasks.withType<Test>().configureEach {
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
}

apply(from = "dumpJsa.gradle.kts")
tasks.withType<JavaExec> {
    dependsOn("dumpJsa")
}
