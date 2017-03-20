licenses(["notice"])

java_plugin(
    name = "autovalue_plugin",
    processor_class = "com.google.auto.value.processor.AutoValueProcessor",
    deps = ["@autovalue//jar"],
)

java_library(
    name = "autovalue",
    exported_plugins = [":autovalue_plugin"],
    exports = ["@autovalue//jar"],
    visibility = ["//visibility:public"],
)
