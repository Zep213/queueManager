package com.umari.queueManager.Model;

public record UsuarioDTO(String id, String username, String role) {
    public static UsuarioDTO from(Usuario u) {
        return new UsuarioDTO(u.getId(), u.getUsername(), u.getRole());
    }
}
