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
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<?> cadastrarProduto(@RequestBody Produto produto, @RequestHeader("Authorization") String token) {
        try {
            String role = jwtUtil.extractRole(token.replace("Bearer ", ""));
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(403).body("Apenas administradores podem cadastrar produtos.");
            }

            produto.setAtivo(true);
            if (produto.getImagens() != null) {
                for (ImagemProduto img : produto.getImagens()) {
                    img.setProduto(produto);
                }
            }
            Produto novoProduto = produtoService.salvar(produto);
            return ResponseEntity.ok(novoProduto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erro ao cadastrar produto: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Produto>> listarTodos() {
        List<Produto> produtos = produtoService.listar();
        return ResponseEntity.ok(produtos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarProduto(@PathVariable Long id, @RequestBody Produto produto, @RequestHeader("Authorization") String token) {
        try {
            String role = jwtUtil.extractRole(token.replace("Bearer ", ""));
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(403).body("Apenas administradores podem editar produtos.");
            }

            Produto atualizado = produtoService.atualizar(id, produto);
            return ResponseEntity.ok(atualizado);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao atualizar produto: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
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

    @PatchMapping("/{id}/alternar-status")
    public ResponseEntity<?> alternarStatus(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        try {
            String role = jwtUtil.extractRole(token.replace("Bearer ", ""));
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(403).body("Apenas administradores podem alterar o status de produtos.");
            }

            Produto produto = produtoService.alternarStatus(id);
            return ResponseEntity.ok(produto);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao alternar status do produto: " + e.getMessage());
        }
    }

    @PostMapping("/upload-imagem")
    public ResponseEntity<String> uploadImagem(@RequestParam("file") MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extensao = originalFilename.substring(originalFilename.lastIndexOf("."));
            String novoNome = UUID.randomUUID().toString() + extensao;

            Path destino = Paths.get("uploads", novoNome);
            Files.createDirectories(destino.getParent());
            Files.write(destino, file.getBytes());

            return ResponseEntity.ok("/uploads/" + novoNome);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Erro ao salvar imagem: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/imagens")
    public ResponseEntity<?> listarImagens(@PathVariable Long id) {
        try {
            Optional<Produto> produtoOpt = produtoService.buscarPorId(id);
            if (produtoOpt.isEmpty()) return ResponseEntity.status(404).body("Produto não encontrado");

            List<ImagemProduto> imagens = produtoOpt.get().getImagens();
            return ResponseEntity.ok(imagens);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao listar imagens.");
        }
    }

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
                return ResponseEntity.status(403).body("Apenas administradores podem adicionar imagens.");
            }

            Optional<Produto> produtoOpt = produtoService.buscarPorId(id);
            if (produtoOpt.isEmpty()) return ResponseEntity.status(404).body("Produto não encontrado");

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

    @DeleteMapping("/imagens/{id}")
    public ResponseEntity<?> removerImagem(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        try {
            String role = jwtUtil.extractRole(token.replace("Bearer ", ""));
            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(403).body("Apenas administradores podem remover imagens.");
            }

            boolean removido = produtoService.removerImagem(id);
            if (!removido) return ResponseEntity.status(404).body("Imagem não encontrada");

            return ResponseEntity.ok("Imagem removida com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao remover imagem.");
        }
    }
}
