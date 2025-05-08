package com.br.biofit.Service;

import com.br.biofit.Model.ImagemProduto;
import com.br.biofit.Model.Produto;
import com.br.biofit.Repository.ImagemProdutoRepository;
import com.br.biofit.Repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
