# Alterações no código antigo (explicação detalhada)

Este documento explica **cada alteração feita nos arquivos que já existiam** no projeto
(o "código antigo") para integrar o back-end da barbearia com o front-end que você já tinha.

- **Estado antigo (referência):** commit `bbe43ed` ("Ajuste no Layout").
- **Estado atual:** `HEAD`.
- Todas as alterações abaixo podem ser conferidas com:

```bash
git diff bbe43ed HEAD -- pom.xml src/main/resources
```

---

## 1. `pom.xml` — dependências adicionadas

Adicionei 5 dependências, sem remover nenhuma:

| Dependência | Para que serve |
|---|---|
| `spring-boot-starter-thymeleaf` | Renderizar os HTML que já existiam em `src/main/resources/templates`. Sem isso, os templates não são processados (`th:*`) e o `#temporals` não funciona. |
| `com.h2database:h2` (runtime) | Banco de dados **em memória**, para o projeto rodar sem instalar PostgreSQL/MySQL. |
| `org.springframework.boot:spring-boot-h2console` (runtime) | Habilita o console web do H2 em `/h2-console`. **No Spring Boot 4** essa autoconfiguração virou um módulo separado — não basta `spring.h2.console.enabled=true`. |
| `org.springframework.security:spring-security-crypto` | Só o **BCrypt** (hash da senha do barbeiro), sem trazer o Spring Security inteiro. |
| `org.webjars:bootstrap` (5.3.3) | Servir o Bootstrap via `/webjars/**`, que é como os templates antigos já referenciavam o CSS/JS. |

---

## 2. `src/main/resources/application.properties` — configuração

O arquivo antigo tinha **apenas** `spring.application.name=demo`. Acrescentei:

- **Datasource H2 em memória:** `jdbc:h2:mem:barbearia` (com `DB_CLOSE_DELAY=-1` para o banco não sumir entre conexões), usuário `sa`, senha vazia.
- **Console do H2:** `spring.h2.console.enabled=true` e `path=/h2-console`.
- **JPA/Hibernate:** `ddl-auto=update` (cria/atualiza as tabelas sozinho), `show-sql=true` e `format_sql=true` (para enxergar o SQL no log).
- **Thymeleaf:** `cache=false` (em desenvolvimento, alterações em HTML aparecem sem reiniciar).

---

## 3. Templates (o front-end antigo)

### 3.1 `templates/agendar/index.html`
- **Removido um `<footer th:replace="...">` que estava dentro do `<head>`.** Isso era inválido: um fragmento de rodapé não pode ficar no `<head>`.
- **Adicionado o `<link>` do `app.css`.** A página usava classes do tema (`card-glass`, `bg-dark-blue`, `hero`, `btn-accent`) mas não carregava o CSS — então ficava sem estilo.
- **Corrigido `<` faltando no hero:** `div class="hero-overlay"></div>` → `<div class="hero-overlay"></div>`.
- **Adicionada a classe de tema no body:** `<body>` → `<body class="bg-dark-900">`.
- **Adicionados os blocos de flash** (`erro` / `mensagem`) para mostrar as mensagens de validação do back-end.
- **O rodapé manual duplicado** (que usava `#dates`) foi trocado pelo fragmento único `~{layout/fragments/footer :: footer}`.

### 3.2 `templates/cliente/status.html`
- **Corrigido `<<body ...>`** (dois `<`).
- **Removido um `<a class="navbar-brand">` aninhado dentro de outro** (a marca ficava com âncora dupla/quebrada).
- **`#dates.format(...)` → `#temporals.format(...)`.** `Agendamento.dataHora` é `LocalDateTime` (java.time); o dialeto certo no Thymeleaf é `#temporals` (`#dates` é para `java.util.Date`).
- **Adicionados os blocos de flash** (`erro` / `mensagem`).
- **Botão "Avaliar atendimento"** (abaixo dos botões Cancelar/Remarcar): aparece **só quando o status é `CONCLUIDO` e o agendamento ainda não tem avaliação**, levando a `/avaliar`. Se já houver avaliação, a página passa a mostrar "Sua avaliação: nota/5".

### 3.3 `templates/cliente/cancelar.html`
- **Removido o `th:replace="~{layout/fragments/header :: header}"`** de dentro do `<nav>`: esse fragmento não existe e era o motivo de a página dar **erro 500**.
- **Removido o texto lixo `///back and////`.**
- **Corrigidos os `class = "..."` com espaços** (HTML aceita, mas estava inconsistente) e o `contanier` → **`container`**.
- **Adicionados os `<link>` de Bootstrap e `app.css`** (a página não tinha CSS nenhum).
- **Adicionado o bloco de flash de erro.**
- **Adicionada a quebra de linha no fim do arquivo** (`\ No newline at end of file`).

### 3.4 `templates/cliente/remarcar.html`
- **Removido um `<body>` duplicado** (havia `<body>` e `<body class="bg-dark-900">`).

### 3.5 `templates/admin/login.html`
- **Removido um bloco solto** `<main><h1>Login administrativo</h1></main>` **e um `<body>` extra** antes do `<body class="bg-dark-900">` (gerava body aninhado).
- **Adicionado o bloco de flash de erro** (`th:if="${erro}"`), que é o que exibe "Usuário ou senha inválidos.".

### 3.6 `templates/admin/painel-agenda.html`
- **`#dates.format(...)` → `#temporals.format(...)`** (mesmo motivo do status).
- **Adicionados os blocos de flash** (`erro` / `mensagem`).
- **Coluna "Ações" ganhou os botões `Concluir` e `Falta`**, visíveis **só quando o status é `CONFIRMADO`** (o `Cancelar` também passou a ser condicional). Isso torna utilizáveis as regras de **concluir atendimento** e **registrar falta** (o "concluir" é o que alimenta a fidelidade — RF20). Antes esses botões não existiam e as regras ficavam inacessíveis pela interface.
- **Menu padronizado:** a `<nav>` antiga (com a marca quebrada) foi substituída pelo fragmento compartilhado `~{layout/fragments/admin-nav :: adminNav}`, que agora concentra os links das telas administrativas (Painel, Serviços, Horários, Bloqueios, Fidelidade, Avaliações, Cliente e Sair).

### 3.7 `templates/layout/fragments/footer.html` — correção importante
- **O arquivo não declarava o fragmento `footer`.** Ele era referenciado como `~{layout/fragments/footer :: footer}`, mas sem `th:fragment`. O seletor `:: footer` acabava casando só com a tag `<footer>`, **deixando de fora os `<script>` que vinham depois** → o Bootstrap JS e o `app.js` **não eram carregados** em nenhuma página.
- **Agora declara `th:fragment="footer"`** englobando o rodapé **e** os dois `<script>` (`bootstrap.bundle.min.js` e `app.js`).
- **`#dates` → `#temporals`** também aqui (ano do rodapé).
- Ajustada a estrutura (o arquivo começava com um `</div>` solto) e adicionada quebra de linha no fim.

### 3.8 `templates/layout/fragments/header.html`
- **Corrigido um `</a>` sem `<a>` de abertura**: a marca estava assim:
  ```html
  <span class="brand-badge">✂️</span> Barbearia
  </a>
  ```
  e passou a ser:
  ```html
  <a class="navbar-brand" th:href="@{/}"><span class="brand-badge">✂️</span> Barbearia</a>
  ```

### 3.9 `templates/errors/404.html` → `templates/error/404.html`
- **Pasta renomeada de `errors` para `error`.** A convenção do Spring Boot é `templates/error/` (singular). Com o nome errado, a página 404 personalizada não era usada — o erro voltava como JSON padrão.

### 3.10 `static/js/app.js`
- **O arquivo estava vazio.** Adicionei comportamentos leves:
  - define `min` (data/hora atual) nos campos `datetime-local`, impedindo agendar no passado;
  - define `step` de 30 minutos nesses campos;
  - esconde automaticamente as mensagens de alerta após alguns segundos.

### 3.11 `static/images/barbearia-hero.jpg` (arquivo novo)
- **A imagem do banner não existia** (a pasta só tinha um `.gitkeep`), então `/images/barbearia-hero.jpg` dava **404 no console**. Gerei uma imagem JPEG com a paleta escura/azul do `app.css` para o hero ficar completo.

---

## 4. Código novo (para contexto)

Além das alterações acima, foram **criados** os arquivos do back-end (nenhum fazia parte do código antigo):

- **Entidades** (`model/`): `Barbeiro`, `Servico`, `HorarioTrabalho`, `Agendamento`, `BloqueioHorario`, `RegistroFidelidade`, `Avaliacao` + enums `StatusAgendamento`, `TipoBloqueio`.
- **Repositórios** (`repository/`): 7 interfaces Spring Data JPA.
- **Serviços** (`service/`): 7 classes com as regras de negócio (RF05/RF06/RF07, disponibilidade, fidelidade, autenticação com BCrypt).
- **Controllers** (`controller/`): `HomeController`, `AgendamentoController`, `ClienteController`, `AdminController` (MVC/Thymeleaf).
- **Controllers de cadastro** (`controller/`): `ServicoController`, `HorarioTrabalhoController`, `BloqueioHorarioController`, `FidelidadeController` e `AvaliacaoController` — CRUD/consulta das classes que ainda não tinham interface, mais o formulário público de avaliação (`/avaliar`).
- **Telas novas** (`templates/`): `layout/fragments/admin-nav.html` e as pastas `admin/servicos/`, `admin/horarios/`, `admin/bloqueios/`, `admin/fidelidade/`, `admin/avaliacoes/` e `cliente/avaliar.html`.
- **Apoio**: `dto/AgendamentoForm`, `exception/RegraNegocioException`, `exception/RecursoNaoEncontradoException`, `util/SenhaUtil`, `util/SessaoAdmin`, `util/DiasSemana`, `config/DataSeeder` (dados iniciais: admin/admin, joao/joao, horários e 3 serviços) e `DemoApplication`.

---

## 5. Como rodar

```powershell
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-21.0.12.101-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
.\mvnw.cmd spring-boot:run
```

- Aplicação: http://localhost:8080/agendar
- Console do H2: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:barbearia`, usuário `sa`, senha vazia)
- Login admin: **admin / admin**
