cmake_minimum_required(VERSION 3.18.1)

project(biocore)

add_subdirectory(vendor/fmt EXCLUDE_FROM_ALL)

add_library(biocore MODULE)
target_sources(biocore PRIVATE
        jni_frontend.cpp
        runtime_resources.cpp
        ipa/format.cpp)

target_include_directories(biocore PRIVATE ${CMAKE_SOURCE_DIR})

find_library(log-lib log)
target_link_libraries(biocore PRIVATE ${log-lib} fmt::fmt)