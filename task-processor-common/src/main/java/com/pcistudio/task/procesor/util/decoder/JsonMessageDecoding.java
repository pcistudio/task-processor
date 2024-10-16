package com.pcistudio.task.procesor.util.decoder;

import com.pcistudio.task.procesor.util.JsonUtil;

public class JsonMessageDecoding implements MessageDecoding {
    @Override
    public <T> T decode(byte[] data, Class<T> clazz) {
        return JsonUtil.from(new String(data), clazz);
    }
}
