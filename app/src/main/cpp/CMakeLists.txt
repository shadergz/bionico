cmake_minimum_required(VERSION 3.18.1)

project(hackinarm-native)

add_library(hackinarm SHARED)
target_sources(hackinarm PRIVATE JNI_frontend.cpp)

find_library(log-lib log)
target_link_libraries(hackinarm PRIVATE ${log-lib})