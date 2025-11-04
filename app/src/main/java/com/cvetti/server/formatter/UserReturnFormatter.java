package com.cvetti.server.formatter;
import com.cvetti.server.objects.User;
import org.json.JSONObject;

public class UserReturnFormatter {

    private User user;

    public UserReturnFormatter(
        User user   
    ) {
        this.user = user;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("id", user.getId());
        json.put("name", user.getName());
        json.put("email", user.getEmail());
        json.put("cpf", user.getCpf());
        json.put("saldo", user.getSaldo());
        json.put("nascimento", user.getNascimento());
        json.put("status", "success");
        json.put("message", "User retrieved successfully");
        return json;
    }

}
