plugins {
  id("java")
  id("maven-publish")
  id("java-library")
}

group = "com.nookure.inventory"
version = "1.1.0"

repositories {
  mavenCentral()
  maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
  api("io.pebbletemplates:pebble:4.1.3")
  api("org.jetbrains:annotations:24.1.0")
  api("jakarta.xml.bind:jakarta.xml.bind-api:4.0.2")
  api("org.glassfish.jaxb:jaxb-runtime:4.0.5")

  compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
  testImplementation(platform("org.junit:junit-bom:5.10.0"))
  testImplementation("com.google.code.gson:gson:2.11.0")
  testImplementation("org.junit.jupiter:junit-jupiter")
  testImplementation("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
}

tasks.test {
  useJUnitPlatform()
}

publishing {
  repositories {
    maven {
      name = "local"
      url = uri("${rootProject.rootDir}/maven")
    }

    maven {
      name = "Reposilite"
      val isSnapshot = rootProject.version.toString().endsWith("SNAPSHOT")
      url = uri("https://repo.nookure.com/${if (isSnapshot) "snapshots" else "releases"}")

      credentials {
        username = findProperty("reposiliteUser")?.toString() ?: System.getenv("REPOSILITE_USER")
        password = findProperty("reposiliteToken")?.toString() ?: System.getenv("REPOSILITE_TOKEN")
      }
    }
  }

  publications {
    create<MavenPublication>("maven") {
      groupId = "com.nookure.inventory"
      artifactId = "api"
      version = "${rootProject.version}"
      from(components["java"])
    }
  }
}

allprojects {
  tasks {
    withType<JavaCompile> {
      options.encoding = "UTF-8"
    }

    withType<Javadoc> {
      options.encoding = "UTF-8"
    }
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
  withJavadocJar()
  withSourcesJar()
}

tasks {
  withType<Javadoc> {
    val o = options as StandardJavadocDocletOptions
    o.encoding = "UTF-8"
    o.source = "17"
  }
}