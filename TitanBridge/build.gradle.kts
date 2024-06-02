listOf("iphoneos", "iphonesimulator").forEach { sdk ->
    tasks.create<Exec>("build${sdk.capitalize()}") {
        group = "build"

        commandLine(
            "xcodebuild",
            "-project", "TitanBridge.xcodeproj",
            "-target", "TitanBridge",
            "-sdk", sdk
        )
        workingDir(projectDir)

        inputs.files(
            fileTree("$projectDir/TitanBridge.xcodeproj") { exclude("**/xcuserdata") },
            fileTree("$projectDir/TitanBridge")
        )
        outputs.files(
            fileTree("$projectDir/build/Release-${sdk}")
        )
    }
}

tasks.create<Delete>("clean") {
    group = "build"

    delete("$projectDir/build")
}
