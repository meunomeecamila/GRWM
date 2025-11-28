# 🌟 **GRWM - Get Ready With Me** 🌟

> Seu guarda roupa-virtual de forma **sustentável**. 

## ✨ Visão Geral

O GRWM (Get Ready With Me) nasce como uma plataforma digital que combina tecnologia, moda e impacto social para transformar a relação das pessoas com suas roupas. Em um cenário onde o consumo rápido e descartável domina, o projeto traz uma alternativa prática e moderna para incentivar o reaproveitamento, a circulação de peças e o consumo consciente.

A plataforma foi pensada para facilitar tanto a organização do guarda-roupa quanto a doação de roupas, criando um ecossistema onde cada peça pode ganhar uma nova história. Ao permitir que usuários cadastrem suas roupas, recebam sugestões de combinação e participem de uma rede ativa de doações, o GRWM contribui diretamente para a redução do desperdício têxtil e para a construção de comunidades mais solidárias e conectadas.

Seu objetivo principal é tornar o ato de vestir-se mais sustentável e acessível, utilizando tecnologia para diminuir desigualdades e promover hábitos responsáveis. Esses propósitos dialogam com metas globais importantes, como a **ODS 10 (Redução das Desigualdades)** e a **ODS 12 (Consumo e Produção Responsáveis)**, reforçando o compromisso da plataforma com uma moda ética e circular.

Com backend em Java, banco de dados PostgreSQL e integração de IA para análise automática de imagens, o GRWM demonstra como soluções tecnológicas podem fortalecer iniciativas sociais. A assistente "Iza Estiliza" automatiza o reconhecimento das peças, tornando o processo inclusivo, rápido e acessível para qualquer pessoa. Idealizado e desenvolvido por mulheres na tecnologia, o projeto também carrega um significado simbólico poderoso: ele representa a força da colaboração, a criatividade feminina e a ocupação de espaços de protagonismo em um setor que ainda enfrenta desafios de equidade. Assim, o GRWM vai além de uma ferramenta, ele é um movimento em direção a uma moda mais consciente, comunitária e transformadora.

## 👩‍💻 **Desenvolvedoras**

-   Ane Madjarian
-   Camila Menezes
-   Maria Clara Galvão
-   Mariana Temporim

## 🎓 **Professores Responsáveis**

-   Ilo Amy Saldanha Riveiro
-   Luciana Mara Freita Diniz

## 🚀 **Como Executar**

1.  📥 Clone o repositório.
2.  🛠️ Instale as dependências com o Maven.
3.  🗄️ Configure o banco de dados PostgreSQL.
(No seu banco de dados, coloque os seguintes comandos)

```sql
CREATE TABLE usuario (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL
);

CREATE TABLE doacao (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    tamanho VARCHAR(20),
    categoria VARCHAR(50),
    foto BYTEA
);

CREATE TABLE peca (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cor VARCHAR(50),
    ocasiao VARCHAR(100),
    descricao TEXT,
    categoria VARCHAR(50),
    foto BYTEA,
    id_usuario INTEGER NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id)
);

```

4.  ▶️ Execute o servidor localmente.
5.  🌐 Acesse o frontend pelo navegador.

## 🧰 **Tecnologias Utilizadas**

-   **Front-End:** HTML / CSS / JavaScript
-   **Back-End:** Java
-   **Gerenciamento de dependências:** Maven
-   **API:** Spark Framework
-   **Banco de dados:** PostgreSQL
-   **IDE:** Eclipse
-   **Treinamento de IA:** Azure Custom Vision

## 🧶 **Funcionalidades Principais**

### 1️⃣ **Cadastro de Peças**

-   Registro de itens do guarda-roupa com detalhes como foto, nome, cor, tamanho e descrição.
-   Preenchimento automático dos dados pela assistente de IA "Iza Estiliza", usando análise inteligente de imagem.

### 2️⃣ **Exibição de Peças no Perfil**

-   Página pessoal exibindo todas as peças cadastradas pelo usuário.
-   Visualização rápida e organizada do guarda-roupa digital.

### 3️⃣ **Feed Principal**

-   Exibição de todas as peças disponíveis para doação na plataforma.
-   Navegação intuitiva para visualizar roupas, tamanhos e estado de conservação.

### 4️⃣ **Aba de Doações**

-   Cadastro de peças destinadas à doação com foto e descrição.
-   Identificação automática dos atributos pela Iza Estiliza, agilizando o processo.


Confira o vídeo Pitch: https://youtu.be/X9V3dmS7OZM?si=bYxcvA-g09knhHc3