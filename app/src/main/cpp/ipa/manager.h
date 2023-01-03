#pragma once

#include <vector>
#include <ipa/format.h>

namespace hackback::ipa {
    class IpaManager {
    public:
        IpaManager() = default;
        ~IpaManager() = default;

        bool manager_new_ipa(std::shared_ptr<detailed_format> ipa_archive) {

            // Testing logical IPA UNIX file descriptor
            if (ipa_archive->is_fd_valid()) {
                m_ipa_list.push_back(ipa_archive);
                g_logger->back_echo("New IPA item now begin controlled by our backend\n");

                return !m_ipa_list.empty();
            }
            g_logger->back_echo("Unix file descriptor associated with the IPA item "
                                "isn't valid for now!\n");
            return false;
        }

        bool attempt_rm_ipa(int ipa_describer) {
            bool cmp = false;
            std::remove_if(m_ipa_list.begin(), m_ipa_list.end(),
                           [cmp, ipa_describer](auto ipa_package) mutable {
                               return cmp = ipa_package->m_fd_access == ipa_describer;
                           });
            return cmp;
        }

    private:
        std::vector<ipa_object> m_ipa_list;
    };
}