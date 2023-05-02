package com.laughing.lib.base.debug;


import java.util.Map;

import androidx.annotation.NonNull;


/**
 * IRegisterDebugMethod
 *
 * @author xl
 * @version V1.0
 * @since 18/11/2016
 */
public interface IRegisterDebugMethod {
    void registerDebugMethod(@NonNull final Map<String, Object> map);
}
