package com.br.biofit.Service;

import com.br.biofit.Model.ImagemProduto;
import com.br.biofit.Model.Produto;
import com.br.biofit.Repository.ImagemProdutoRepository;
import com.br.biofit.Repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ImagemProdutoRepository imagemProdutoRepository;

    public Produto salvar(Produto produto) {
        return produtoRepository.save(produto);
    }

    public List<Produto> listar() {
        return produtoRepository.findAll();
    }

    public Produto atualizar(Long id, Produto dadosAtualizados) {
        Optional<Produto> produtoOpt = produtoRepository.findById(id);
        if (produtoOpt.isPresent()) {
            Produto produto = produtoOpt.get();
            produto.setNome(dadosAtualizados.getNome());
            produto.setCodigo(dadosAtualizados.getCodigo());
            produto.setQuantidade(dadosAtualizados.getQuantidade());
            produto.setValor(dadosAtualizados.getValor());
            produto.setAtivo(dadosAtualizados.isAtivo());
            produto.setDescricao(dadosAtualizados.getDescricao());
            produto.setAvaliacao(dadosAtualizados.getAvaliacao());
            produto.getImagens().clear();
            if (dadosAtualizados.getImagens() != null) {
                for (var imagem : dadosAtualizados.getImagens()) {
                    imagem.setProduto(produto); // vincula novamente
                    produto.getImagens().add(imagem);
                }
            }

            return produtoRepository.save(produto);
        }
        throw new RuntimeException("Produto não encontrado");
    }

    public Optional<Produto> buscarPorId(Long id) {
        return produtoRepository.findById(id);
    }

    public Produto alternarStatus(Long id) {
        Optional<Produto> produtoOpt = produtoRepository.findById(id);
        if (produtoOpt.isPresent()) {
            Produto produto = produtoOpt.get();
            produto.setAtivo(!produto.isAtivo()); // Alterna o status
            return produtoRepository.save(produto);
        }
        throw new RuntimeException("Produto não encontrado");
    }

    public boolean removerImagem(Long id) {
        Optional<ImagemProduto> imagem = imagemProdutoRepository.findById(id);
        if (imagem.isPresent()) {
            imagemProdutoRepository.delete(imagem.get());
            return true;
        }
        return false;
    }
    
    /**
     * Atualiza a quantidade de um produto (substituindo o valor atual)
     */
    @Transactional
    public Produto atualizarQuantidade(Long id, Integer novaQuantidade) {
        Optional<Produto> produtoOpt = produtoRepository.findById(id);
        if (produtoOpt.isEmpty()) {
            throw new RuntimeException("Produto não encontrado");
        }
        
        if (novaQuantidade < 0) {
            throw new RuntimeException("A quantidade não pode ser negativa");
        }
        
        Produto produto = produtoOpt.get();
        produto.setQuantidade(novaQuantidade);
        return produtoRepository.save(produto);
    }
    
    /**
     * Adiciona uma quantidade ao estoque atual do produto
     */
    @Transactional
    public Produto adicionarAoEstoque(Long id, Integer quantidade) {
        Optional<Produto> produtoOpt = produtoRepository.findById(id);
        if (produtoOpt.isEmpty()) {
            throw new RuntimeException("Produto não encontrado");
        }
        
        if (quantidade <= 0) {
            throw new RuntimeException("A quantidade a adicionar deve ser maior que zero");
        }
        
        Produto produto = produtoOpt.get();
        produto.setQuantidade(produto.getQuantidade() + quantidade);
        return produtoRepository.save(produto);
    }
    
    /**
     * Remove uma quantidade do estoque atual do produto
     */
    @Transactional
    public Produto removerDoEstoque(Long id, Integer quantidade) {
        Optional<Produto> produtoOpt = produtoRepository.findById(id);
        if (produtoOpt.isEmpty()) {
            throw new RuntimeException("Produto não encontrado");
        }
        
        if (quantidade <= 0) {
            throw new RuntimeException("A quantidade a remover deve ser maior que zero");
        }
        
        Produto produto = produtoOpt.get();
        
        if (produto.getQuantidade() < quantidade) {
            throw new RuntimeException("Estoque insuficiente. Quantidade atual: " + produto.getQuantidade());
        }
        
        produto.setQuantidade(produto.getQuantidade() - quantidade);
        return produtoRepository.save(produto);
    }
}