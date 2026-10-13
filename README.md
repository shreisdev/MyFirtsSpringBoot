# ✂️ Barbearia — Sistema de Agendamento

Aplicação web de agendamento para barbearia, feita com **Spring Boot + Thymeleaf**.
O cliente agenda **sem login** (informando nome, telefone e e-mail) e recebe um **código**
para consultar, remarcar ou cancelar o horário. O barbeiro acessa um **painel administrativo**
para gerenciar a agenda.

---

## 🛠️ Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4.1.1 (Spring MVC) |
| Views | Thymeleaf + Bootstrap 5.3.3 (webjars) |
| Persistência | Spring Data JPA / Hibernate |
| Banco | H2 (em memória) |
| Build | Maven (wrapper `mvnw`) |
| Senhas | BCrypt (`spring-security-crypto`) |
| Utilitários | Lombok |

---

## ✅ Pré-requisitos

- **JDK 21** instalado. Em Windows, este projeto foi testado com o
  *Microsoft OpenJDK 21* em `C:\Program Files\Microsoft\jdk-21.0.12.101-hotspot`.
- Não é preciso instalar Maven nem banco de dados (wrapper Maven + H2 em memória).

Confirme o Java:

```powershell
java -version
```

---

## ▶️ Como executar

### Windows (PowerShell)

```powershell
# aponte o JAVA_HOME para o JDK 21 (ajuste o caminho se necessario)
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-21.0.12.101-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# na pasta do projeto
.\mvnw.cmd spring-boot:run
```

### Linux / macOS

```bash
export JAVA_HOME=/caminho/para/jdk-21
./mvnw spring-boot:run
```

Depois acesse: **http://localhost:8080/agendar**

Para gerar o JAR executável:

```powershell
.\mvnw.cmd clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

---

## 🔗 Telas e acessos

| Rota | Descrição |
|---|---|
| `/agendar` | Página inicial — cliente cria o agendamento (sem login) |
| `/cliente/status` | Cliente consulta o agendamento pelo **código** |
| `/cliente/remarcar` | Remarcar um agendamento |
| `/cliente/cancelar` | Cancelar um agendamento |
| `/admin/login` | Login do barbeiro |
| `/admin/painel-agenda` | Painel com filtros (barbeiro / dia / semana / manhã / tarde / noite) e ações |
| `/admin/servicos` | Cadastro de serviços (nome, duração, preço, ativo/inativo) |
| `/admin/horarios` | Horários de trabalho por barbeiro (com almoço) |
| `/admin/bloqueios` | Bloqueios de agenda (pontuais e recorrentes) |
| `/admin/fidelidade` | Consulta de fidelidade por e-mail (RF20) |
| `/admin/avaliacoes` | Avaliações recebidas e média das notas |
| `/avaliar` | Cliente avalia um atendimento concluído (link na tela de status) |
| `/h2-console` | Console do banco H2 |

### Usuários de teste (criados pelo `DataSeeder`)

| Usuário | Senha | Perfil |
|---|---|---|
| `admin` | `admin` | Administrador |
| `joao` | `joao` | Barbeiro |

### Console do H2

- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:barbearia`
- Usuário: `sa`
- Senha: *(vazia)*

> ⚠️ O banco é **em memória**: os dados são recriados a cada reinicialização da aplicação.
> Para usar PostgreSQL/MySQL em produção, basta trocar a dependência no `pom.xml` e as
> propriedades `spring.datasource.*` em `src/main/resources/application.properties`.

---

## 📋 Regras de negócio implementadas

- **RF05/RF06** — Cliente agenda sem cadastro; limite de **3 agendamentos ativos por e-mail**.
- **RF07** — Não permite dois agendamentos do mesmo barbeiro no mesmo horário
  (restrição de unicidade `(barbeiro, data_hora)` + validação de conflito por duração).
- **Disponibilidade** — Respeita o horário de trabalho do barbeiro (incluindo almoço) e os
  bloqueios de agenda; não permite agendar no passado.
- **Código de acesso** — Cada agendamento recebe um código curto e único para o cliente
  consultar/remarcar/cancelar.
- **Cancelamento** — Vira `CANCELADO` dentro do prazo ou `CANCELADO_TARDIO` fora do prazo.
- **Fidelidade (RF20)** — Ao **concluir** um atendimento, acumula um registro de fidelidade
  por e-mail; a cada 10 atendimentos o cliente fica elegível a recompensa.
- **Autenticação** — Senha do barbeiro guardada com **BCrypt**.

---

## 📁 Estrutura do projeto

```
src/main/java/com/example/demo
├── DemoApplication.java          # ponto de entrada
├── config/DataSeeder.java        # carga inicial (barbeiros, horários, serviços)
├── controller/                   # HomeController, AgendamentoController,
│                                 # ClienteController, AdminController
├── dto/AgendamentoForm.java
├── exception/                    # RegraNegocioException, RecursoNaoEncontradoException
├── model/                        # entidades JPA + enums
├── repository/                   # interfaces Spring Data JPA
├── service/                      # regras de negócio
└── util/SenhaUtil.java           # BCrypt

src/main/resources
├── application.properties        # H2, JPA, Thymeleaf
├── static/                       # css, js, imagens
└── templates/                    # páginas Thymeleaf
    ├── admin/  cliente/  agendar/
    ├── error/404.html
    └── layout/fragments/
```

---

## 📝 Observações

- Alterações feitas sobre o front-end que já existia estão detalhadas em
  **[`ALTERACOES.md`](./ALTERACOES.md)**.
- O projeto usa **H2 em memória** para facilitar a execução local; não há Docker obrigatório.
- Sem REST por enquanto — a aplicação é **MVC (server-side rendering)**, alinhada aos
  templates HTML existentes.
