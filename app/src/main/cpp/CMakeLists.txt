cmake_minimum_required(VERSION 3.18.1)

project(akane)

add_subdirectory(deps/fmt EXCLUDE_FROM_ALL)

add_library(akane MODULE)
target_sources(akane PRIVATE
        jni_frontend.cpp
        runtime_resources.cpp
        ipa/format.cpp)

target_include_directories(akane PRIVATE ${CMAKE_SOURCE_DIR})

find_library(log-lib log)
target_link_libraries(akane PRIVATE ${log-lib} fmt::fmt)