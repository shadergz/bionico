package com.beloncode.hackinarm.ipa;


import com.beloncode.hackinarm.IpaObject;
import com.beloncode.hackinarm.MainActivity;

public class IpaInstaller extends MainActivity {
    public IpaInstaller() {}

    public void installNewIpa(IpaObject validObject) {
        assert validObject.fDescriptor != null;

    }
}

