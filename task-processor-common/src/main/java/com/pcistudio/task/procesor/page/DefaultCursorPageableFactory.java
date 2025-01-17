package com.pcistudio.task.procesor.page;


import com.pcistudio.task.procesor.util.JsonUtil;

import java.util.Base64;

@SuppressWarnings("PMD.GenericsNaming")
public abstract class DefaultCursorPageableFactory<ITEM, OFFSET> extends CursorPageableFactory<ITEM, OFFSET> {

    @Override
    protected String encode(Cursor<OFFSET> cursor) {
        byte[] json = JsonUtil.toJsonBytes(cursor);
        return Base64.getUrlEncoder().encodeToString(json);
    }

    @Override
    protected Cursor<OFFSET> decode(String token) {
        byte[] decode = Base64.getUrlDecoder().decode(token);
        String json = new String(decode);
        return JsonUtil.fromJson(json, Cursor.class, offsetClass);
    }

}
