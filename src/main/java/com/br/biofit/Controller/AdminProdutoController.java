package com.br.biofit.Controller;

import com.br.biofit.Model.ImagemProduto;
import com.br.biofit.Model.Produto;
import com.br.biofit.Security.JwtUtil;
import com.br.biofit.Service.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Controlador dedicado às operações administrativas de produtos
 * Aqui ficam as operações específicas do administrador
 */
@RestController
@RequestMapping("/admin/produtos")
public class AdminProdutoController {

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Endpoint para listar todos os produtos (com informações completas para administração)
     */
    @GetMapping
    public ResponseEntity<?> listarTodos(@RequestHeader("Authorization") String token) {
        try {
            String role = jwtUtil.extractRole(token.replace("Bearer ", ""));
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(403).body("Acesso negado. Apenas administradores podem acessar esta função.");
            }

            List<Produto> produtos = produtoService.listar();
            return ResponseEntity.ok(produtos);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao listar produtos: " + e.getMessage());
        }
    }

    /**
     * Endpoint para buscar um produto específico pelo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        try {
            String role = jwtUtil.extractRole(token.replace("Bearer ", ""));
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(403).body("Acesso negado. Apenas administradores podem acessar esta função.");
            }

            Optional<Produto> produto = produtoService.buscarPorId(id);
            if (produto.isPresent()) {
                return ResponseEntity.ok(produto.get());
            } else {
                return ResponseEntity.status(404).body("Produto não encontrado.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao buscar produto: " + e.getMessage());
        }
    }

    /**
     * Endpoint para atualizar todas as informações de um produto
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarProduto(@PathVariable Long id, 
                                           @RequestBody Produto produto,
                                           @RequestHeader("Authorization") String token) {
        try {
            String role = jwtUtil.extractRole(token.replace("Bearer ", ""));
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(403).body("Acesso negado. Apenas administradores podem atualizar produtos.");
            }

            Produto atualizado = produtoService.atualizar(id, produto);
            return ResponseEntity.ok(Map.of(
                    "message", "Produto atualizado com sucesso",
                    "produto", atualizado
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao atualizar produto: " + e.getMessage());
        }
    }

    /**
     * Endpoint para alternar o status de um produto (ativo/inativo)
     */
    @PatchMapping("/{id}/alternar-status")
    public ResponseEntity<?> alternarStatus(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        try {
            String role = jwtUtil.extractRole(token.replace("Bearer ", ""));
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(403).body("Acesso negado. Apenas administradores podem alterar o status de produtos.");
            }

            Produto produto = produtoService.alternarStatus(id);
            return ResponseEntity.ok(Map.of(
                    "message", "Status do produto alterado com sucesso",
                    "produto", produto
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao alternar status do produto: " + e.getMessage());
        }
    }

    /**
     * Endpoint para upload de imagem
     */
    @PostMapping("/upload-imagem")
    public ResponseEntity<?> uploadImagem(@RequestParam("file") MultipartFile file, 
                                       @RequestHeader("Authorization") String token) {
        try {
            String role = jwtUtil.extractRole(token.replace("Bearer ", ""));
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(403).body("Acesso negado. Apenas administradores podem fazer upload de imagens.");
            }

            String originalFilename = file.getOriginalFilename();
            String extensao = originalFilename.substring(originalFilename.lastIndexOf("."));
            String novoNome = UUID.randomUUID().toString() + extensao;

            Path destino = Paths.get("uploads", novoNome);
            Files.createDirectories(destino.getParent());
            Files.write(destino, file.getBytes());

            return ResponseEntity.ok(Map.of(
                    "url", "/uploads/" + novoNome
            ));
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Erro ao salvar imagem: " + e.getMessage());
        }
    }

    /**
     * Endpoint para listar imagens de um produto
     */
    @GetMapping("/{id}/imagens")
    public ResponseEntity<?> listarImagens(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        try {
            String role = jwtUtil.extractRole(token.replace("Bearer ", ""));
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(403).body("Acesso negado. Apenas administradores podem acessar esta função.");
            }

            Optional<Produto> produtoOpt = produtoService.buscarPorId(id);
            if (produtoOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Produto não encontrado");
            }

            List<ImagemProduto> imagens = produtoOpt.get().getImagens();
            return ResponseEntity.ok(imagens);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao listar imagens.");
        }
    }

    /**
     * Endpoint para adicionar imagem a um produto
     */
    @PostMapping("/{id}/imagens")
    public ResponseEntity<?> salvarImagem(@PathVariable Long id,
                                       @RequestParam("imagem") MultipartFile imagem,
                                       @RequestParam("nome") String nome,
                                       @RequestParam("diretorio") String diretorio,
                                       @RequestParam("principal") boolean principal,
                                       @RequestHeader("Authorization") String token) {
        try {
            String role = jwtUtil.extractRole(token.replace("Bearer ", ""));
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(403).body("Acesso negado. Apenas administradores podem adicionar imagens.");
            }

            Optional<Produto> produtoOpt = produtoService.buscarPorId(id);
            if (produtoOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Produto não encontrado");
            }

            Produto produto = produtoOpt.get();

            String extensao = imagem.getOriginalFilename().substring(imagem.getOriginalFilename().lastIndexOf("."));
            String novoNome = UUID.randomUUID().toString() + extensao;
            Path destino = Paths.get("uploads", novoNome);
            Files.createDirectories(destino.getParent());
            Files.write(destino, imagem.getBytes());

            if (principal) {
                produto.getImagens().forEach(img -> img.setPrincipal(false));
            }

            ImagemProduto img = new ImagemProduto();
            img.setCaminho("/uploads/" + novoNome);
            img.setNome(nome);
            img.setDiretorio(diretorio);
            img.setPrincipal(principal);
            img.setProduto(produto);

            produto.getImagens().add(img);
            produtoService.salvar(produto);

            return ResponseEntity.ok("Imagem salva com sucesso.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erro ao salvar imagem.");
        }
    }

    /**
     * Endpoint para remover uma imagem de produto
     */
    @DeleteMapping("/imagens/{id}")
    public ResponseEntity<?> removerImagem(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        try {
            String role = jwtUtil.extractRole(token.replace("Bearer ", ""));
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(403).body("Acesso negado. Apenas administradores podem remover imagens.");
            }

            boolean removido = produtoService.removerImagem(id);
            if (!removido) {
                return ResponseEntity.status(404).body("Imagem não encontrada");
            }

            return ResponseEntity.ok("Imagem removida com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao remover imagem.");
        }
    }
}