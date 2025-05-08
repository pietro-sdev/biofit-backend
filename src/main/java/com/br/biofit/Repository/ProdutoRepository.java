package com.br.biofit.Repository;

import com.br.biofit.Model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    Optional<Produto> findById(Long id);

    List<Produto> findByAtivoTrue();

    List<Produto> findByAtivoFalse();

    List<Produto> findByNomeContainingIgnoreCase(String nome);

    Optional<Produto> findByCodigo(String codigo);
}