#pragma once

#include <vector>
#include <ipa/format.h>

namespace akane::ipa {
    class IpaManager {
    public:
        IpaManager() = default;

        ~IpaManager() = default;

        bool manager_new_ipa(std::shared_ptr<detailed_format> ipa_archive) {

            // Testing logical Ipa UNIX file descriptor
            if (ipa_archive->is_fd_valid()) {
                m_ipa_list.push_back(ipa_archive);
                g_logger->back_echo("New Ipa item now begin controlled by our backend\n");
                return true;
            }
            g_logger->back_echo("Unix fd associated with the Ipa item isn't valid for now!\n");
            return false;
        }

        bool attempt_rm_ipa(std::shared_ptr<detailed_format> ipa_archive) {
            bool cmp = false;
            std::remove_if(m_ipa_list.begin(), m_ipa_list.end(),
                           [cmp, ipa_archive](const ipa_object ipa_package) mutable {
                               const bool fd_matches = ipa_archive->m_fd_access ==
                                                       ipa_package->m_fd_access;
                               const bool filename_matches = ipa_archive->m_ipa_filename ==
                                                             ipa_package->m_ipa_filename;
                               return cmp = fd_matches && filename_matches;
                           });
            return cmp;
        }

        int find_ipa_index(std::shared_ptr<detailed_format> ipa_archive) {
            auto inter_object = std::find(m_ipa_list.begin(), m_ipa_list.end(), ipa_archive);

            if (inter_object != m_ipa_list.end()) {
                return std::distance(m_ipa_list.begin(), inter_object);
            }
            return -1;
        }

    private:
        std::vector<ipa_object> m_ipa_list;
    };
}