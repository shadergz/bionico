#pragma once

#include <cassert>

namespace hackback::common {

    template <typename T>
    class RawPointer {
    public:
        RawPointer<T>() {
            check_size();
        }

        inline RawPointer<T>(const T reference) : m_reference(reference) {
            check_size();
        }
        inline auto get() const {
            return m_reference;
        }

        [[maybe_unused]] inline T get_as_mut() const {
          return const_cast<T>(m_reference);
        }

        inline bool is_valid() const {
            return m_reference != nullptr;
        }
        inline void assign(const T reference) {
            trap_invalid();
            m_reference = reference;
        }

        [[maybe_unused]] inline void assign(const RawPointer<T>& other_raw) {
            trap_invalid();
            m_reference = other_raw.get();
        }
        inline void trap_invalid() {
            if (is_valid()) {
                assert("A pointer already exist inside the container" == nullptr);
            }
        }
        auto operator->() {
            return get();
        }
    private:
        void inline check_size() const {
            if (sizeof(T) != 8) {
                assert("Type must be a raw C pointer" == NULL);
            }
        }
        T m_reference = nullptr;
    };

}