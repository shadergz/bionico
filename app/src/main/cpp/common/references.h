#pragma once

#include <cassert>

namespace bionic::common {

    template<typename T>
    class RawPointer {
    public:
        RawPointer<T>() {
            checkSize();
        }

        inline RawPointer<T>(const T reference) : m_reference(reference) {
            checkSize();
        }

        inline auto get() const {
            return m_reference;
        }

        /*
        inline T get_as_mut() const {
            return const_cast<T>(m_reference);
        }
        */

        inline bool isValid() const {
            return m_reference != nullptr;
        }

        inline void assign(const T reference) {
            trapInvalid();
            m_reference = reference;
        }

        [[maybe_unused]] inline void assign(const RawPointer<T> &other_raw) {
            trapInvalid();
            m_reference = other_raw.get();
        }

        inline void trapInvalid() {
            if (isValid()) {
                assert("A pointer already exist inside the container" == nullptr);
            }
        }

        auto operator->() {
            return get();
        }

    private:
        void inline checkSize() const {
            if (sizeof(T) != 8) {
                assert("Type must be a raw C pointer" == nullptr);
            }
        }

        T m_reference = nullptr;
    };

}