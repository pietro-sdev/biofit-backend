package com.br.biofit.Config;

import com.br.biofit.Model.Role;
import com.br.biofit.Model.Usuario;
import com.br.biofit.Repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class SeederEstoquista implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public void run(String... args) throws Exception {
        String email = "estoquista@biofit.com";
        String senha = "admin123";

        if (usuarioRepository.findByEmail(email).isEmpty()) {
            Usuario admin = Usuario.builder()
                    .nome("Estoquista")
                    .email(email)
                    .senha(BCrypt.hashpw(senha, BCrypt.gensalt()))
                    .roles(Role.ESTOQUISTA)
                    .ativo(true)
                    .build();

            usuarioRepository.save(admin);
            System.out.println("✅ Usuário admin criado com sucesso!");
        } else {
            System.out.println("ℹ️ Usuário admin já existe. Seed ignorado.");
        }
    }
}
