// ===================================================================
// GRWM — DOACAO.JS
// Funciona tanto em doacao.html (feed) quanto em cadastroDoacoes.html (cadastro)
// Inclui: Sidebar, Cadastro, e Feed automático
// ===================================================================

document.addEventListener("DOMContentLoaded", () => {
  console.log("✅ doacao.js carregado!");

  // ==============================
  // 🩷 1️⃣ SIDEBAR INTERATIVA
  // ==============================
  const sidebar = document.getElementById('sidebar');
  const logoBtn = document.getElementById('openSideBar');

  if (sidebar && logoBtn) {
    const SIDEBAR_WIDTH = 250; // mesma largura definida no CSS (.sidebar)
    const PADDING_LEFT = 35;   // distância inicial

    // deixa a animação mais suave
    logoBtn.style.transition = 'left 0.3s ease';
    logoBtn.style.zIndex = 1101;

    // Toggle de abrir/fechar
    logoBtn.addEventListener('click', () => {
      sidebar.classList.toggle('active');

      if (sidebar.classList.contains('active')) {
        logoBtn.style.left = (SIDEBAR_WIDTH + PADDING_LEFT) + 'px';
      } else {
        logoBtn.style.left = PADDING_LEFT + 'px';
      }
    });
  }

  // ==============================
  // 🩷 3️⃣ FEED DE DOAÇÕES AUTOMÁTICO
  // ==============================
  const container = document.getElementById("cards-explorar");
  if (container) {
    console.log("📰 Página de feed detectada.");

    // Mostra um “loading” enquanto carrega
    container.innerHTML = `
      <div class="loading" style="text-align:center; margin-top:30px;">
        <i class="bi bi-arrow-repeat" style="font-size:2rem; animation:spin 1s linear infinite;"></i>
        <p>Carregando doações...</p>
      </div>
    `;

    async function carregarDoacoes() {
      try {
        const res = await fetch("/doacoes");
        const doacoes = await res.json();

        console.log("📦 Doações recebidas:", doacoes);

        container.innerHTML = "";

        if (!doacoes || doacoes.length === 0) {
          container.innerHTML = "<p>Nenhuma doação cadastrada ainda 😢</p>";
          return;
        }

        // Cria cards dinamicamente
        doacoes.forEach(d => {
          console.log("🧱 Montando card de:", d.nome);

          const card = document.createElement("div");
          card.classList.add("look-card");

          const media = document.createElement("div");
          media.classList.add("look-card__media");

          const img = document.createElement("img");
          img.src = d.fotoBase64 && d.fotoBase64.length > 0
            ? `data:image/*;base64,${d.fotoBase64}`
            : "imgs/imagem-padrao.png";
          img.alt = d.nome;
          media.appendChild(img);

          const label = document.createElement("div");
          label.classList.add("look-card__label");
          label.textContent = `${d.nome} - ${d.categoria}`;

          card.appendChild(media);
          card.appendChild(label);
		  
		  //cards clicaveis
		  card.addEventListener('click', () => {
		              window.location.href = `detalhesDoacao.html?id=${d.id}`;
		            });
		  
          container.appendChild(card);
        });
      } catch (error) {
        console.error("❌ Erro ao carregar doações:", error);
        container.innerHTML = "<p>Erro ao carregar doações :(</p>";
      }
    }

    carregarDoacoes();
  }
  

  // ==============================
  // LÓGICA DE PREENCHIMENTO E ESCOLHA IA E MANUAL
  // ==============================
  const btnAuto = document.getElementById('btn-cadastro-auto');
  const fileInput = document.getElementById('file-upload');
  const nomeInput = document.getElementById('input-nome');
  const descricaoInput = document.getElementById('input-descricao');
  const categoriaSelect = document.getElementById('input-categoria');
  const submitBtn = document.getElementById('submit-btn');

  if (fileInput) {
      fileInput.addEventListener('change', () => {
          // Habilita o botao "Enviar" se um arquivo foi selecionado, permitindo o preenchimento manual
          if (fileInput.files && fileInput.files.length > 0) {
              submitBtn.disabled = false;
          } else {
              submitBtn.disabled = true;
          }
      });
  }

  if (btnAuto && fileInput) {
      btnAuto.addEventListener('click', async () => {
          if (!fileInput.files || fileInput.files.length === 0) {
              alert("⚠️ Por favor, escolha uma foto primeiro!");
              return;
          }

          const formData = new FormData();
          formData.append('imagem', fileInput.files[0]);

          //Trava a interface
          btnAuto.disabled = true;
          btnAuto.textContent = "Processando IA...";
          
          try {
              //chama a rota de classificação rapida no backend Java
              const response = await fetch("/classificar", {
                  method: "POST",
                  body: formData
              });

              const iaResults = await response.json();

              if (response.ok && iaResults.tipo) {
                  
                  //obter e formatar resultados
                  const tipo = iaResults.tipo;
                  const cor = iaResults.cor || ''; 
                  
                  const tipoDisplay = tipo.charAt(0).toUpperCase() + tipo.slice(1);
                  const corDisplay = cor.charAt(0).toUpperCase() + cor.slice(1);

                  //preenchendo campos
                  // Nome: Tipo + Cor
                  nomeInput.value = `${tipoDisplay} ${corDisplay}`.trim();
                  
                  // Categoria: Tipo (Busca o VALUE correspondente no <select>)
                  let categoriaValue = tipo.toLowerCase().replace(" ", "-");
                  
                  if (categoriaSelect.querySelector(`option[value="${categoriaValue}"]`)) {
                      categoriaSelect.value = categoriaValue;
                  } else if (tipo.toLowerCase().includes('camisa') || tipo.toLowerCase().includes('blusa')) {
                      // Exemplo de Mapeamento para categoria ampla
                      categoriaSelect.value = "blusas-e-camisetas"; 
                  } 
                  
                  // Descrição: Complementa com a Cor
  				descricaoInput.value = (descricaoInput.value ? descricaoInput.value + ". " : "") + `${tipoDisplay} da cor ${corDisplay}.`;
                  
                  alert("✅ Cadastro preenchido automaticamente pela IZA! Revise antes de Enviar.");
                  submitBtn.disabled = false; // Garante que o botao Enviar esta habilitado
                  
              } else {
                  alert(`❌ Falha na classificação da IZA: ${iaResults.message || "Erro desconhecido. Preencha manualmente."}`);
              }

          } catch (error) {
              console.error("❌ Erro de rede ou parse:", error);
              alert("❌ Erro de rede ao tentar conectar com a IZA. Preencha manualmente.");
          } finally {
              //destrava o botão da IZA
              btnAuto.disabled = false;
              btnAuto.textContent = "Cadastro Automático";
          }
      });
  }
});

// ===================================================================
// CSS Inline extra para o spinner
// ===================================================================
const style = document.createElement("style");
style.textContent = `
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
`;
document.head.appendChild(style);

