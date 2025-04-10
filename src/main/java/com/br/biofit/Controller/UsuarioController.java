package com.br.biofit.Controller;

import com.br.biofit.Model.Usuario;
import com.br.biofit.Security.JwtUtil;
import com.br.biofit.Service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<Usuario>> getAllUsuarios() {
        List<Usuario> usuarios = usuarioService.findAllUsuarios();
        if (usuarios.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(usuarios);
    }

    @PostMapping("/cadastro")
    public ResponseEntity<?> cadastrarUsuario(@RequestBody Usuario usuario, @RequestHeader("Authorization") String token) {
        try {
            String role = jwtUtil.extractRole(token.replace("Bearer ", ""));
            System.out.println("Tentativa de cadastro de usuário com role: " + role); // Log de Role
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(403).body("Acesso negado. Somente administradores podem cadastrar usuários.");
            }
            usuario.setAtivo(true);
            System.out.println("Cadastrando usuário: " + usuario.getEmail()); // Log de cadastro
            Usuario novoUsuario = usuarioService.cadastrarUsuario(usuario);

            return ResponseEntity.ok(Map.of(
                    "message", "Usuário cadastrado com sucesso",
                    "usuario", novoUsuario
            ));
        } catch (Exception e) {
            System.err.println("Erro ao cadastrar usuário: " + e.getMessage()); // Log de erro
            return ResponseEntity.status(500).body("Erro ao cadastrar usuário: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getUsuarioById(@PathVariable Long id) {
        Optional<Usuario> usuario = usuarioService.findById(id);
        if (usuario.isPresent()) {
            System.out.println("Usuário encontrado: " + usuario.get().getEmail()); // Log de busca por ID
            return ResponseEntity.ok(usuario.get());
        } else {
            System.out.println("Usuário com ID " + id + " não encontrado"); // Log de não encontrado
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUsuario(
            @PathVariable Long id,
            @RequestBody Usuario usuario,
            @RequestHeader("Authorization") String token) {

        try {
            String role = jwtUtil.extractRole(token.replace("Bearer ", ""));
            System.out.println("Tentativa de atualização de usuário com role: " + role); // Log de Role
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(403).body("Acesso negado. Somente administradores podem editar usuários.");
            }
            System.out.println("Atualizando usuário com ID: " + id); // Log de atualização
            Usuario updatedUsuario = usuarioService.updateUsuario(id, usuario);

            return ResponseEntity.ok(Map.of(
                    "message", "Usuário atualizado com sucesso",
                    "usuario", updatedUsuario
            ));
        } catch (Exception e) {
            System.err.println("Erro ao atualizar usuário: " + e.getMessage()); // Log de erro
            return ResponseEntity.status(500).body("Erro ao atualizar usuário: " + e.getMessage());
        }
    }
}
