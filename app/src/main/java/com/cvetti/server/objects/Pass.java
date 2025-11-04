package com.cvetti.server.objects;

import java.util.HashMap;

public class Pass {

    private String salt;
    private String hash;

    public Pass(String salt, String hash) {
        this.salt = salt;
        this.hash = hash;
    }

    public HashMap<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("salt", salt);
        result.put("hash", hash);
        return result;
    }
    
}
