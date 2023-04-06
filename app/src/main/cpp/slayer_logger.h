#pragma once

#include <cassert>
#include <android/log.h>

#include <fmt/format.h>
#include <fmt/color.h>

static constexpr std::string_view gsBackendTag = "Biocore";

namespace bionic {
    class SlayerLogger {
    public:
        template<typename... Args>
        void backEcho(const std::string format, const Args... variables) {

            auto message_buffer = fmt::memory_buffer();
            // Pre-reserve 30% the final format size, this may glow alongside when we adds more
            // content to string buffer!
            message_buffer.reserve(fmt::formatted_size(format, variables...) * 1.30);

            formatLevel(message_buffer);

            fmt::format_to(std::back_inserter(message_buffer), format, variables...);
            (message_buffer.size())[message_buffer.data()] = '\0';
            __android_log_write(static_cast<int>(mCurrentPriority), gsBackendTag.data(),
                                reinterpret_cast<const char *>(message_buffer.data()));
        }

        template<typename... Args>
        [[maybe_unused]] void backEcho(android_LogPriority prior, std::string_view format,
                                        const Args... variables) {
            changeLevel(prior);
            backEcho(format, variables...);
        }

        void changeLevel(android_LogPriority new_level) {
            mCurrentPriority = new_level;
        }

    private:
        android_LogPriority mCurrentPriority = ANDROID_LOG_DEBUG;

        void trapNotImpl() {
            assert("Logging level not implemented yet!" == nullptr);
        }

        void formatLevel(fmt::memory_buffer &out_buffer) {

            switch (mCurrentPriority) {
                case ANDROID_LOG_UNKNOWN:
                    assert("Unknown priority level specified..." == nullptr);
                case ANDROID_LOG_DEFAULT:
                    trapNotImpl();
                    break;
                case ANDROID_LOG_VERBOSE:
                    trapNotImpl();
                    break;
                case ANDROID_LOG_DEBUG:
                    fmt::format_to(std::back_inserter(out_buffer), "Slayer [Debug]: ");
                    break;
                case ANDROID_LOG_INFO:
                    trapNotImpl();
                    break;
                case ANDROID_LOG_WARN:
                    trapNotImpl();
                    break;
                case ANDROID_LOG_ERROR:
                    trapNotImpl();
                    break;
                case ANDROID_LOG_FATAL:
                    trapNotImpl();
                    break;
                case ANDROID_LOG_SILENT:
                    trapNotImpl();
                    break;
            }

        }
    };
}
