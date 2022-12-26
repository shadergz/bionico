#pragma once

#include <memory>

#include <IPA_modulator.h>

namespace HackinARM::Resources {
    [[maybe_unused]] std::shared_ptr<Formats::IPAManager> g_MainIPAManager;

}

namespace HackinARM::Env {
    [[maybe_unused]] JNIEnv* m_InitializedEnv;
    [[maybe_unused]] jobject m_AtCreateObject;
}

