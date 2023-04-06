#pragma once

#include <vector>
#include <ipa/format.h>

namespace bionic::ipa {
    class IpaManager {
    public:
        IpaManager() = default;

        ~IpaManager() = default;

        bool managerNewIpa(std::shared_ptr<DetailedFormat> ipa_archive) {

            // Testing logical Ipa UNIX file descriptor
            if (ipa_archive->isFdValid()) {
                mIpaList.push_back(ipa_archive);
                gLogger->backEcho("New Ipa item now begin controlled by our backend\n");
                return true;
            }
            gLogger->backEcho("Unix fd associated with the Ipa item isn't valid for now!\n");
            return false;
        }

        bool attemptRmIpa(std::shared_ptr<DetailedFormat> ipa_archive) {
            bool cmp = false;
            std::remove_if(mIpaList.begin(), mIpaList.end(),
                           [cmp, ipa_archive](const ipa_object ipa_package) mutable {
                               const bool fd_matches = ipa_archive->mFdAccess ==
                                                       ipa_package->mFdAccess;
                               const bool filename_matches = ipa_archive->mIpaFilename ==
                                                             ipa_package->mIpaFilename;
                               return cmp = fd_matches && filename_matches;
                           });
            return cmp;
        }

        int findIpaIndex(std::shared_ptr<DetailedFormat> ipa_archive) {
            auto inter_object = std::find(mIpaList.begin(), mIpaList.end(), ipa_archive);

            if (inter_object != mIpaList.end()) {
                return std::distance(mIpaList.begin(), inter_object);
            }
            return -1;
        }

    private:
        std::vector<ipa_object> mIpaList;
    };
}