#pragma once

#include <memory>

#include "IPA.h"

namespace HackinARM::Resources {
    [[maybe_unused]] std::shared_ptr<Formats::IPAManager> gMainIPAManager;

}

namespace HackinARM::Env {
    [[maybe_unused]] JNIEnv* mInitializedEnv;
    [[maybe_unused]] jobject mAtCreateObject;
}

