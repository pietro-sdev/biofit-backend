package com.br.biofit.Service;

import com.br.biofit.Model.Role;
import com.br.biofit.Model.Usuario;
import com.br.biofit.Repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Usuario autenticar(String email, String senha) {
        Optional<Usuario> usuarioOptional = usuarioRepository.findByEmail(email);
        if (usuarioOptional.isEmpty()) return null;
        Usuario usuario = usuarioOptional.get();
        if (usuario.getRoles() == Role.CLIENTE) return null;
        if (!usuario.isAtivo()) return null;
        if (!BCrypt.checkpw(senha, usuario.getSenha())) return null;

        return usuario;
    }

    public List<Usuario> findAllUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario cadastrarUsuario(Usuario usuario) {
        if (usuario.getRoles() == null || (!usuario.getRoles().equals(Role.ADMIN) && !usuario.getRoles().equals(Role.ESTOQUISTA))) {
            throw new IllegalArgumentException("Cargo inválido.");
        }
        String hashedPassword = BCrypt.hashpw(usuario.getSenha(), BCrypt.gensalt());
        usuario.setSenha(hashedPassword);
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    public Usuario updateUsuario(Long id, Usuario usuario) {
        Optional<Usuario> existingUser = usuarioRepository.findById(id);
        if (!existingUser.isPresent()) {
            throw new RuntimeException("Usuário não encontrado");
        }
        Usuario updatedUsuario = existingUser.get();
        updatedUsuario.setNome(usuario.getNome());
        updatedUsuario.setEmail(usuario.getEmail());
        updatedUsuario.setRoles(usuario.getRoles());
        if (usuario.getSenha() != null && !usuario.getSenha().isEmpty()) {
            updatedUsuario.setSenha(BCrypt.hashpw(usuario.getSenha(), BCrypt.gensalt()));
        }
        updatedUsuario.setAtivo(usuario.isAtivo()); // Atualiza o status do usuário (ativo/inativo)
        return usuarioRepository.save(updatedUsuario);
    }
}
