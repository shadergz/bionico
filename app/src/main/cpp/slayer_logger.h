#pragma once

#include <cassert>
#include <android/log.h>

#include <fmt/format.h>
#include <fmt/color.h>

static constexpr std::string_view gs_backend_tag = "HackinARM backend";

namespace hackback {
    class SlayerLogger {
    public:
        template<typename... Args>
        void back_echo(const std::string format, Args... variables) {

            auto message_buffer = fmt::memory_buffer();
            // Pre-reserve 30% the final format size, this may glow alongside when we adds more
            // content to string buffer!
            message_buffer.reserve(fmt::formatted_size(format, variables...) * 1.30);

            format_level(message_buffer);

            fmt::format_to(std::back_inserter(message_buffer), format, variables...);
            (message_buffer.size())[message_buffer.data()] = '\0';
            __android_log_write(static_cast<int>(m_current_priority),gs_backend_tag.data(),
                                reinterpret_cast<const char*>(message_buffer.data()));
        }

        template<typename... Args>
        [[maybe_unused]] void back_echo(android_LogPriority prior, std::string_view format,
                                        Args... variables) {
            change_level(prior);
            back_echo(format, variables...);
        }

        void change_level(android_LogPriority new_level) {
            m_current_priority = new_level;
        }
    private:
        android_LogPriority m_current_priority = ANDROID_LOG_DEBUG;

        void trap_not_impl() {
            assert("Logging level not implemented yet!" == nullptr);
        }

        void format_level(fmt::memory_buffer& out_buffer) {

            switch (m_current_priority) {
            case ANDROID_LOG_UNKNOWN:
                assert("Unknown priority level specified" == nullptr);
            case ANDROID_LOG_DEFAULT:   trap_not_impl(); break;
            case ANDROID_LOG_VERBOSE:   trap_not_impl(); break;
            case ANDROID_LOG_DEBUG:
                fmt::format_to(std::back_inserter(out_buffer), "Slayer [Debug]: ");
                break;
            case ANDROID_LOG_INFO:      trap_not_impl(); break;
            case ANDROID_LOG_WARN:      trap_not_impl(); break;
            case ANDROID_LOG_ERROR:     trap_not_impl(); break;
            case ANDROID_LOG_FATAL:     trap_not_impl(); break;
            case ANDROID_LOG_SILENT:    trap_not_impl(); break;
            }

        }
    };
}
