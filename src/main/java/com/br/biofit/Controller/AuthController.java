package com.br.biofit.Controller;

import com.br.biofit.Model.Usuario;
import com.br.biofit.Service.UsuarioService;
import com.br.biofit.Security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuario loginRequest) {
        try {
            Usuario usuario = usuarioService.autenticar(
                    loginRequest.getEmail(),
                    loginRequest.getSenha()
            );

            if (usuario == null) {
                return ResponseEntity.status(403).body("Acesso negado.");
            }

            String token = jwtUtil.generateToken(
                    usuario.getEmail(),
                    usuario.getRoles().name()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("email", usuario.getEmail());
            response.put("role", usuario.getRoles().name());
            response.put("nome", usuario.getNome());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erro interno: " + e.getMessage());
        }
    }
}
