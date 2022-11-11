#pragma once

#include <vector>

static constexpr std::string_view gBackEndLogTag = "HackinARM Backend";

namespace HackinARM::Formats {
    class IPAArchive {
        IPAArchive() = default;

    };

    class [[maybe_unused]] IPAManager {
        [[maybe_unused]] std::vector<IPAArchive> mIPAList;

    public:
        IPAManager() = default;
    };
}

