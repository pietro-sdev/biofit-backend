package com.br.biofit.Controller;

import com.br.biofit.Model.Produto;
import com.br.biofit.Security.JwtUtil;
import com.br.biofit.Service.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Controlador dedicado às operações de estoque
 */
@RestController
@RequestMapping("/estoque")
public class EstoqueController {

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Endpoint para visualizar um produto com detalhes de estoque
     * Permite ao estoquista ver informações do produto e quantidade em estoque
     */
    @GetMapping("/produtos/{id}")
    public ResponseEntity<?> visualizarProdutoEstoque(@PathVariable Long id, 
                                                     @RequestHeader("Authorization") String token) {
        try {
            String role = jwtUtil.extractRole(token.replace("Bearer ", ""));
            if (!"ESTOQUISTA".equals(role) && !"ADMIN".equals(role)) {
                return ResponseEntity.status(403).body("Acesso negado. Apenas estoquistas e administradores podem acessar esta função.");
            }

            Optional<Produto> produtoOpt = produtoService.buscarPorId(id);
            if (produtoOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Produto não encontrado.");
            }

            return ResponseEntity.ok(produtoOpt.get());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao buscar produto: " + e.getMessage());
        }
    }

    /**
     * Endpoint para atualizar a quantidade de um produto no estoque
     */
    @PatchMapping("/produtos/{id}/atualizar-quantidade")
    public ResponseEntity<?> atualizarQuantidade(@PathVariable Long id,
                                               @RequestBody Map<String, Integer> request,
                                               @RequestHeader("Authorization") String token) {
        try {
            String role = jwtUtil.extractRole(token.replace("Bearer ", ""));
            if (!"ESTOQUISTA".equals(role) && !"ADMIN".equals(role)) {
                return ResponseEntity.status(403).body("Acesso negado. Apenas estoquistas e administradores podem atualizar quantidades.");
            }

            Integer quantidade = request.get("quantidade");
            if (quantidade == null) {
                return ResponseEntity.badRequest().body("O campo 'quantidade' é obrigatório.");
            }

            Produto produtoAtualizado = produtoService.atualizarQuantidade(id, quantidade);
            return ResponseEntity.ok(Map.of(
                    "message", "Quantidade atualizada com sucesso",
                    "produto", produtoAtualizado
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao atualizar quantidade: " + e.getMessage());
        }
    }

    /**
     * Endpoint para adicionar produtos ao estoque (entrada)
     */
    @PostMapping("/produtos/{id}/entrada")
    public ResponseEntity<?> registrarEntrada(@PathVariable Long id,
                                           @RequestBody Map<String, Integer> request,
                                           @RequestHeader("Authorization") String token) {
        try {
            String role = jwtUtil.extractRole(token.replace("Bearer ", ""));
            if (!"ESTOQUISTA".equals(role) && !"ADMIN".equals(role)) {
                return ResponseEntity.status(403).body("Acesso negado. Apenas estoquistas e administradores podem registrar entradas.");
            }

            Integer quantidade = request.get("quantidade");
            if (quantidade == null || quantidade <= 0) {
                return ResponseEntity.badRequest().body("É necessário informar uma quantidade válida maior que zero.");
            }

            Produto produtoAtualizado = produtoService.adicionarAoEstoque(id, quantidade);
            return ResponseEntity.ok(Map.of(
                    "message", "Entrada registrada com sucesso",
                    "produto", produtoAtualizado
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao registrar entrada: " + e.getMessage());
        }
    }

    /**
     * Endpoint para remover produtos do estoque (saída)
     */
    @PostMapping("/produtos/{id}/saida")
    public ResponseEntity<?> registrarSaida(@PathVariable Long id,
                                         @RequestBody Map<String, Integer> request,
                                         @RequestHeader("Authorization") String token) {
        try {
            String role = jwtUtil.extractRole(token.replace("Bearer ", ""));
            if (!"ESTOQUISTA".equals(role) && !"ADMIN".equals(role)) {
                return ResponseEntity.status(403).body("Acesso negado. Apenas estoquistas e administradores podem registrar saídas.");
            }

            Integer quantidade = request.get("quantidade");
            if (quantidade == null || quantidade <= 0) {
                return ResponseEntity.badRequest().body("É necessário informar uma quantidade válida maior que zero.");
            }

            Produto produtoAtualizado = produtoService.removerDoEstoque(id, quantidade);
            return ResponseEntity.ok(Map.of(
                    "message", "Saída registrada com sucesso",
                    "produto", produtoAtualizado
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao registrar saída: " + e.getMessage());
        }
    }
}