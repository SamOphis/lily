plugins {
    application
}

repositories {
    jcenter()
    maven(url = "https://m2.dv8tion.net/releases")
}

dependencies {
    implementation("net.dv8tion:JDA:4.2.1_262") {
        exclude(module = "opus-java")
    }
    implementation("io.sentry:sentry-logback:5.0.0-beta.3")
    implementation("redis.clients:jedis:3.6.0")
}

application {
    mainClass.set("gg.samantha.lily.startup.Main")
}