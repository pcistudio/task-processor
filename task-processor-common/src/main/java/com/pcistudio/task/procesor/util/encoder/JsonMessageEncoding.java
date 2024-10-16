package com.pcistudio.task.procesor.util.encoder;

import com.pcistudio.task.procesor.util.JsonUtil;

public class JsonMessageEncoding implements MessageEncoding {
    @Override
    public byte[] encode(Object object) {
        return JsonUtil.toJsonBytes(object);
    }
}
