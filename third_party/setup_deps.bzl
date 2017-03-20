load("@bazel_tools//tools/build_defs/repo:maven_rules.bzl", "maven_jar")

def setup_deps():
    maven_jar(
        name = "autovalue",
        artifact = "com.google.auto.value:auto-value:1.4",
        sha1 = "6d1448fcd13074bd3658ef915022410b7c48343b",
    )

    maven_jar(
        name = "guava",
        artifact = "com.google.guava:guava:21.0",
        sha1 = "3a3d111be1be1b745edfa7d91678a12d7ed38709",
    )

    maven_jar(
        name = "junit",
        artifact = "junit:junit:4.12",
        sha1 = "2973d150c0dc1fefe998f834810d68f278ea58ec",
    )

    maven_jar(
        name = "truth",
        artifact = "com.google.truth:truth:0.32",
        sha1 = "e996fb4b41dad04365112786796c945f909cfdf7",
    )
