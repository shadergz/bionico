cmake_minimum_required(VERSION 3.18.1)

project(hackback)

add_subdirectory(dependencies/fmt EXCLUDE_FROM_ALL)

add_library(hackback MODULE)
target_sources(hackback PRIVATE jni_frontend.cpp runtime_resources.cpp ipa/format.cpp)

target_include_directories(hackback PRIVATE ${CMAKE_SOURCE_DIR})

find_library(log-lib log)
target_link_libraries(hackback PRIVATE ${log-lib} fmt::fmt)