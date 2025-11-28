package app;

import static spark.Spark.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.servlet.MultipartConfigElement;

import com.google.gson.Gson;

import model.Doacao;
import model.Peca;
import model.Usuario;
import service.DoacaoService;
import service.PecaService;
import service.UsuarioService;
import service.ClassificarRoupaIAService;
import java.util.ArrayList;

public class Aplicacao {
    public static void main(String[] args) {
        port(8081);
        staticFileLocation("/public");

        // Serviços
        DoacaoService doacaoService = new DoacaoService();
        PecaService pecaService = new PecaService();
        UsuarioService usuarioService = new UsuarioService();
        ClassificarRoupaIAService classificarRoupaIAService = new ClassificarRoupaIAService();
        
     // ===================================================
     // ROTA DE CLASSIFICAR IMAGEM 
     // ===================================================
        
        post("/classificar", (req, res) -> {
            req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
            
            byte[] foto = null;
            
            //obter a foto
            try (InputStream is = req.raw().getPart("imagem").getInputStream()) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[1024];
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                foto = buffer.toByteArray();
            } catch (Exception e) {
                // Ignorar erro se não houver imagem
            }
            
            res.type("application/json");

            if (foto != null && foto.length > 0) {
                try {
                    // Chama o serviço de IA para obter tipo e cor
                    Map<String, String> iaResults = classificarRoupaIAService.classificaRoupa(foto);
                    res.status(200);
                    return new Gson().toJson(iaResults);
                } catch (Exception e) {
                    System.err.println("Erro na rota /classificar: " + e.getMessage());
                    res.status(500);
                    return "{\"message\": \"Erro interno da IA.\" + e.getMessage()}";
                }
            }
            res.status(400);
            return "{\"message\": \"Nenhuma imagem fornecida.\"}";
        });

        // ===================== ROTA DE DOAÇÃO =====================
        post("/doacao", (req, res) -> {
            req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));

            String nome = req.queryParams("nome");
            String descricao = req.queryParams("descricao");
            String tamanho = req.queryParams("tamanho");
            String categoria = req.queryParams("categoria");
            String cor = "";

            byte[] foto = null;

            try {
                if (req.raw().getPart("imagem") != null) {
                    try (InputStream is = req.raw().getPart("imagem").getInputStream()) {
                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                        int nRead;
                        byte[] data = new byte[1024];

                        while ((nRead = is.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                        }

                        foto = buffer.toByteArray();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        // Se não enviou foto, usa padrão
        if (foto == null || foto.length == 0) {
            try (InputStream is = Aplicacao.class.getResourceAsStream("/public/imgs/imagem-padrao.png")) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[1024];

                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }

                foto = buffer.toByteArray();
            } catch (Exception ex) {
                ex.printStackTrace();
                foto = new byte[0]; // fallback
            }
        }

            Doacao d = new Doacao(nome, descricao, tamanho, categoria, foto);
            boolean ok = doacaoService.cadastrar(d);

            if (ok) {
                res.redirect("/doacao/sucesso.html");
                return null;
            } else {
                res.status(400);
                return "<script>alert('Erro ao cadastrar doação.'); history.back();</script>";
            }
        });

        // ===================== ROTA DE PEÇA =====================
        post("/peca", (req, res) -> {
            req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));

            String nome = req.queryParams("nome");
            String cor = req.queryParams("cor");
            String ocasiao = req.queryParams("ocasiao");
            String descricao = req.queryParams("descricao");
            String categoria = req.queryParams("categoria");
            int idUsuario = Integer.parseInt(req.queryParams("idUsuario")); // <-- vem do front

            byte[] foto = null;

            try (InputStream is = req.raw().getPart("imagem").getInputStream()) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[1024];
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                foto = buffer.toByteArray();
            } catch (Exception e) {
                e.printStackTrace();
                res.status(400);
                return "<script>alert('Erro ao processar a imagem :('); history.back();</script>";
            }

            // Monta objeto
            Peca p = new Peca(nome, cor, ocasiao, descricao, categoria, foto);
            p.setIdUsuario(idUsuario); // associa ao dono

            boolean ok = pecaService.cadastrar(p);

            if (ok) {
                res.redirect("/perfil/sucesso.html");
                return null;
            } else {
                res.status(400);
                res.type("text/html");
                return "<script>alert('Erro ao cadastrar peça.'); history.back();</script>";
            }
        });

        // ===================== ROTA DE USUÁRIO =====================
        post("/usuario", (req, res) -> {
            String username = req.queryParams("username");
            String senha = req.queryParams("senha");

            Usuario u = new Usuario(username, senha);

            // ✅ agora passa pelo Service, que faz o hash com BCrypt
            boolean ok = usuarioService.cadastrar(u);

            if (ok) {
                res.redirect("/login/login.html");
                return null;
            } else {
                res.status(500);
                return "Erro ao cadastrar usuário.";
            }
        });

        // ===================== ROTA DE LOGIN =====================
        post("/login", (req, res) -> {
            String username = req.queryParams("username");
            String senha = req.queryParams("senha");

            // ✅ agora usa autenticação segura (BCrypt)
            boolean autenticado = usuarioService.autenticar(username, senha);

            res.type("application/json");

            if (autenticado) {
                // Recupera o usuário completo pra enviar o ID
                Usuario usuario = usuarioService.getByUsername(username);

                res.status(200);
                return "{\"id\": " + usuario.getId() + ", \"username\": \"" + usuario.getUsername() + "\"}";
            } else {
                res.status(401);
                return "{\"message\": \"Usuário ou senha incorretos.\"}";
            }
        });


        // ===================== ROTA DE PERFIL =====================
        get("/usuario/:id", (req, res) -> {
            int id = Integer.parseInt(req.params(":id"));
            Usuario usuario = usuarioService.getById(id);

            res.type("application/json");

            if (usuario != null) {
                res.status(200);
                return "{\"id\": " + usuario.getId() +
                       ", \"username\": \"" + usuario.getUsername() + "\"}";
            } else {
                res.status(404);
                return "{\"message\":\"Usuário não encontrado.\"}";
            }
        });

     // ===================================================
     // ROTA: Lista todas as peças de um usuário
     // ===================================================
        get("/pecas/:idUsuario", (req, res) -> {
            int idUsuario = Integer.parseInt(req.params(":idUsuario"));
            res.type("application/json");

            List<Peca> pecas = pecaService.listarPorUsuario(idUsuario);

            for (Peca p : pecas) {
                byte[] bytes = p.getFoto();
                if (bytes != null && bytes.length > 0) {
                    String base64 = Base64.getEncoder().encodeToString(bytes);
                    p.setFotoBase64(base64);
                    System.out.println("✅ FOTO CONVERTIDA DE " + p.getNome() + ": " + base64.substring(0, 20) + "..."); // debug
                } else {
                    p.setFotoBase64("");
                    System.out.println("⚠️ FOTO NULA DE " + p.getNome());
                }
                p.setFoto(null); // remove array bruto
            }

            String json = new Gson().toJson(pecas);
            System.out.println("📦 JSON ENVIADO: " + json.substring(0, Math.min(200, json.length())) + "...");
            return json;
        });
        
     // ===================================================
     // ROTA: Lista todas as doações
     // ===================================================
     get("/doacoes", (req, res) -> {
         res.type("application/json");

         List<Doacao> doacoes = doacaoService.listarTodas();

         // converte fotos BYTEA -> Base64
         for (Doacao d : doacoes) {
             byte[] bytes = d.getFoto();
             if (bytes != null && bytes.length > 0) {
                 String base64 = Base64.getEncoder().encodeToString(bytes);
                 d.setFotoBase64(base64);
             } else {
                 d.setFotoBase64("");
             }
             d.setFoto(null); // remove bytes do JSON
         }

         return new Gson().toJson(doacoes);
     });
     
  // ===================================================
  // ROTA: Lista todas as peças e doações (para o início)
  // ===================================================
  get("/inicio", (req, res) -> {
      res.type("application/json");

      List<Peca> pecas = pecaService.listarTodas();   // você vai criar isso no service já já
      List<Doacao> doacoes = doacaoService.listarTodas();

      // converte fotos para Base64 antes de enviar
      for (Peca p : pecas) {
          if (p.getFoto() != null) {
              p.setFotoBase64(Base64.getEncoder().encodeToString(p.getFoto()));
          }
      }

      for (Doacao d : doacoes) {
          if (d.getFoto() != null) {
              d.setFotoBase64(Base64.getEncoder().encodeToString(d.getFoto()));
          }
      }

      // junta tudo em uma lista só
      List<Object> tudo = new ArrayList<>();
      tudo.addAll(pecas);
      tudo.addAll(doacoes);

      return new com.google.gson.Gson().toJson(tudo);
  });
  
  
  //===================================================
  // ROTA: Busca peça por ID 
  // ===================================================
  get("/api/peca/:id", (req, res) -> {
      res.type("application/json");
      int id = Integer.parseInt(req.params(":id"));

      Peca p = pecaService.getById(id); 

      if (p != null) {
          //converte foto
          byte[] bytes = p.getFoto();
          if (bytes != null && bytes.length > 0) {
              String base64 = Base64.getEncoder().encodeToString(bytes);
              p.setFotoBase64(base64);
          }
          p.setFoto(null); 

          return new Gson().toJson(p);
      } else {
          res.status(404);
          return "{\"message\":\"Peça não encontrada.\"}";
      }
  });

  // ===================================================
  // ROTA: Excluir peça
  // ===================================================
  delete("peca/:id", (req, res) -> {
      res.type("application/json");
      
      int id = Integer.parseInt(req.params(":id"));

      boolean ok = pecaService.excluir(id);

      if (ok) {
          res.status(200);
          return "{\"message\": \"Peça excluída com sucesso!\"}";
      } else { //Erro
          res.status(500); 
          return "{\"message\": \"Erro ao excluir peça.\"}";
      }
  });
  
  
    //===================================================
    // ROTA: Busca doação por ID
   // ===================================================
 get("/api/doacao/:id", (req, res) -> {
     res.type("application/json");
     int id = Integer.parseInt(req.params(":id"));

     Doacao d = doacaoService.getById(id); 

     if (d != null) {
    	 //converte foto
         byte[] bytes = d.getFoto();
         if (bytes != null && bytes.length > 0) {
             String base64 = Base64.getEncoder().encodeToString(bytes);
             d.setFotoBase64(base64);
         }
         d.setFoto(null); 

         return new Gson().toJson(d);
     } else {
         res.status(404);
         return "{\"message\":\"Doação não encontrada.\"}";
     }
 });


    }
}
