package com.pcistudio.task.procesor.page;


import com.pcistudio.task.procesor.util.JsonUtil;

import java.util.Base64;

public abstract class DefaultCursorPageableFactory<ITEM, OFFSET> extends CursorPageableFactory<ITEM, OFFSET> {

    protected String encode(Cursor<OFFSET> cursor) {
        String json = JsonUtil.toJson(cursor);
        return Base64.getUrlEncoder().encodeToString(json.getBytes());
    }

    protected Cursor<OFFSET> decode(String token) {
        byte[] decode = Base64.getUrlDecoder().decode(token);
        String json = new String(decode);
        return JsonUtil.fromJson(json, Cursor.class, offsetClass);
    }

}
