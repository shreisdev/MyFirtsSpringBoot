package com.example.demo.service;

import com.example.demo.exception.RecursoNaoEncontradoException;
import com.example.demo.exception.RegraNegocioException;
import com.example.demo.model.Agendamento;
import com.example.demo.model.Barbeiro;
import com.example.demo.model.Servico;
import com.example.demo.model.StatusAgendamento;
import com.example.demo.repository.AgendamentoRepository;
import com.example.demo.repository.BarbeiroRepository;
import com.example.demo.repository.ServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Regras de negocio do {@link Agendamento}.
 *
 * Regras implementadas:
 * - RF05/RF20: cliente sem login/cadastro; identificado por e-mail e por um token (codigo).
 * - RF06: limite de agendamentos ativos por e-mail.
 * - RF07: (barbeiro, dataHora) unico - nao permite dois agendamentos no mesmo horario.
 */
@Service
@RequiredArgsConstructor
public class AgendamentoService {

    /** RF06: maximo de agendamentos CONFIRMADOS por e-mail. */
    public static final int MAX_AGENDAMENTOS_ATIVOS_POR_EMAIL = 3;

    private final AgendamentoRepository agendamentoRepository;
    private final BarbeiroRepository barbeiroRepository;
    private final ServicoRepository servicoRepository;
    private final RegistroFidelidadeService registroFidelidadeService;

    /**
     * Cria um novo agendamento (DCU 001 - Cliente informa dados e confirma).
     */
    @Transactional
    public Agendamento criar(Long barbeiroId, Long servicoId, LocalDateTime dataHora,
                             String nomeCliente, String telefoneCliente, String emailCliente) {

        if (nomeCliente == null || nomeCliente.isBlank()) {
            throw new RegraNegocioException("Informe o seu nome.");
        }
        if (telefoneCliente == null || telefoneCliente.isBlank()) {
            throw new RegraNegocioException("Informe o seu telefone.");
        }
        if (emailCliente == null || emailCliente.isBlank()) {
            throw new RegraNegocioException("Informe o seu e-mail.");
        }

        Barbeiro barbeiro = barbeiroRepository.findById(barbeiroId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Barbeiro não encontrado: " + barbeiroId));
        Servico servico = servicoRepository.findById(servicoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Serviço não encontrado: " + servicoId));

        if (!servico.isAtivo()) {
            throw new RegraNegocioException("Este serviço está inativo e não pode ser agendado.");
        }

        String email = emailCliente.trim().toLowerCase();
        validarLimiteDeAgendamentosAtivos(email);
        validarDisponibilidade(barbeiro, dataHora, servico.getDuracaoMinutos(), null);

        Agendamento agendamento = new Agendamento();
        agendamento.setBarbeiro(barbeiro);
        agendamento.setServico(servico);
        agendamento.setDataHora(dataHora);
        agendamento.setNomeCliente(nomeCliente.trim());
        agendamento.setTelefoneCliente(telefoneCliente.trim());
        agendamento.setEmailCliente(email);
        agendamento.setTokenAcesso(gerarTokenAcesso());
        agendamento.setCriadoEm(LocalDateTime.now());
        agendamento.confirmar();

        return agendamentoRepository.save(agendamento);
    }

    @Transactional(readOnly = true)
    public Agendamento buscarPorIdOuFalhar(Long id) {
        return agendamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento não encontrado: " + id));
    }

    /** Consulta feita pelo cliente usando o codigo (token) recebido por e-mail. */
    @Transactional(readOnly = true)
    public Optional<Agendamento> consultarPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        return agendamentoRepository.findByTokenAcesso(codigo.trim());
    }

    @Transactional(readOnly = true)
    public List<Agendamento> listarTodos() {
        return agendamentoRepository.findAllByOrderByDataHoraAsc();
    }

    /** Cancela o agendamento do cliente (marca CANCELADO ou CANCELADO_TARDIO). */
    @Transactional
    public Agendamento cancelar(Long agendamentoId) {
        Agendamento agendamento = buscarPorIdOuFalhar(agendamentoId);
        if (!agendamento.podeSerAlterado()) {
            throw new RegraNegocioException(
                    "Este agendamento não pode ser cancelado (status atual: " + agendamento.getStatus() + ").");
        }
        agendamento.cancelar();
        return agendamentoRepository.save(agendamento);
    }

    /** Remarca o agendamento para uma nova data/hora. */
    @Transactional
    public Agendamento remarcar(Long agendamentoId, LocalDateTime novaDataHora) {
        Agendamento agendamento = buscarPorIdOuFalhar(agendamentoId);
        if (!agendamento.podeSerAlterado()) {
            throw new RegraNegocioException(
                    "Este agendamento não pode ser remarcado (status atual: " + agendamento.getStatus() + ").");
        }
        validarDisponibilidade(agendamento.getBarbeiro(), novaDataHora,
                agendamento.getDuracaoMinutos(), agendamento.getId());

        agendamento.remarcar(novaDataHora);
        return agendamentoRepository.save(agendamento);
    }

    /** Marca o atendimento como concluido e acumula fidelidade para o e-mail do cliente. */
    @Transactional
    public Agendamento concluir(Long agendamentoId) {
        Agendamento agendamento = buscarPorIdOuFalhar(agendamentoId);
        if (!agendamento.podeSerAlterado()) {
            throw new RegraNegocioException(
                    "Só é possível concluir um agendamento confirmado (status atual: " + agendamento.getStatus() + ").");
        }
        agendamento.concluir();
        Agendamento salvo = agendamentoRepository.save(agendamento);
        registroFidelidadeService.registrarAtendimento(salvo.getEmailCliente());
        return salvo;
    }

    /** Marca que o cliente nao compareceu. */
    @Transactional
    public Agendamento registrarFalta(Long agendamentoId) {
        Agendamento agendamento = buscarPorIdOuFalhar(agendamentoId);
        if (!agendamento.podeSerAlterado()) {
            throw new RegraNegocioException(
                    "Só é possível registrar falta de um agendamento confirmado (status atual: " + agendamento.getStatus() + ").");
        }
        agendamento.registrarFalta();
        return agendamentoRepository.save(agendamento);
    }

    /**
     * Lista os agendamentos do painel administrativo conforme os filtros da tela.
     *
     * @param barbeiroId filtro por barbeiro (null = todos)
     * @param periodo    DIA, SEMANA, manha, tarde ou noite (null = todos)
     * @param data       dia de referencia (null = hoje)
     */
    @Transactional(readOnly = true)
    public List<Agendamento> listarParaPainel(Long barbeiroId, String periodo, LocalDate data) {
        LocalDate dia = (data != null) ? data : LocalDate.now();

        LocalDateTime inicio;
        LocalDateTime fim;
        if ("SEMANA".equalsIgnoreCase(periodo)) {
            inicio = dia.with(DayOfWeek.MONDAY).atStartOfDay();
            fim = inicio.plusDays(7).minusSeconds(1);
        } else {
            inicio = dia.atStartOfDay();
            fim = dia.atTime(LocalTime.MAX);
        }

        List<Agendamento> lista = (barbeiroId == null)
                ? agendamentoRepository.findByDataHoraBetweenOrderByDataHoraAsc(inicio, fim)
                : agendamentoRepository.findByBarbeiroIdAndDataHoraBetweenOrderByDataHoraAsc(barbeiroId, inicio, fim);

        if (periodo == null) {
            return lista;
        }
        return switch (periodo.toLowerCase()) {
            case "manha" -> lista.stream().filter(a -> a.getDataHora().getHour() < 12).toList();
            case "tarde" -> lista.stream()
                    .filter(a -> a.getDataHora().getHour() >= 12 && a.getDataHora().getHour() < 18).toList();
            case "noite" -> lista.stream().filter(a -> a.getDataHora().getHour() >= 18).toList();
            default -> lista;
        };
    }

    // ------------------------------------------------------------------
    // Regras internas
    // ------------------------------------------------------------------

    /** RF06: limita a quantidade de agendamentos ativos por e-mail. */
    private void validarLimiteDeAgendamentosAtivos(String email) {
        long ativos = agendamentoRepository
                .countByEmailClienteIgnoreCaseAndStatus(email, StatusAgendamento.CONFIRMADO);
        if (ativos >= MAX_AGENDAMENTOS_ATIVOS_POR_EMAIL) {
            throw new RegraNegocioException("Limite de " + MAX_AGENDAMENTOS_ATIVOS_POR_EMAIL
                    + " agendamentos ativos por e-mail atingido (RF06).");
        }
    }

    /**
     * Verifica se o barbeiro pode atender no horario pedido.
     * Cobre RF07 e a disponibilidade (horario de trabalho, almoco e bloqueios).
     *
     * @param ignorarAgendamentoId usado no remarcar, para nao conflitar com o proprio agendamento
     */
    private void validarDisponibilidade(Barbeiro barbeiro, LocalDateTime dataHora, int duracaoMinutos,
                                        Long ignorarAgendamentoId) {
        if (dataHora == null) {
            throw new RegraNegocioException("Informe a data e o horário.");
        }
        if (duracaoMinutos <= 0) {
            throw new RegraNegocioException("Duração do serviço inválida.");
        }
        if (dataHora.isBefore(LocalDateTime.now())) {
            throw new RegraNegocioException("Não é possível agendar para um horário que já passou.");
        }

        boolean dentroDoHorarioDeTrabalho = barbeiro.getHorariosTrabalho().stream()
                .anyMatch(horario -> horario.cobre(dataHora, duracaoMinutos));
        if (!dentroDoHorarioDeTrabalho) {
            throw new RegraNegocioException("O barbeiro não atende nesse dia/horário.");
        }

        LocalDateTime fim = dataHora.plusMinutes(duracaoMinutos);
        boolean emBloqueio = barbeiro.getBloqueios().stream()
                .anyMatch(bloqueio -> bloqueio.abrange(dataHora) || bloqueio.abrange(fim.minusSeconds(1)));
        if (emBloqueio) {
            throw new RegraNegocioException("O barbeiro está indisponível nesse período (bloqueio na agenda).");
        }

        // RF07 - nao pode existir outro agendamento ativo se sobrepondo
        boolean conflito = barbeiro.getAgendamentos().stream()
                .filter(Agendamento::estaAtivo)
                .filter(a -> ignorarAgendamentoId == null || !ignorarAgendamentoId.equals(a.getId()))
                .anyMatch(a -> a.conflitaCom(dataHora, duracaoMinutos));
        if (conflito) {
            throw new RegraNegocioException("Já existe um agendamento nesse horário para este barbeiro (RF07).");
        }

        // Reforca a restricao de unicidade (barbeiro, dataHora) do banco, inclusive para registros cancelados
        boolean horarioJaRegistrado = agendamentoRepository.existsByBarbeiroIdAndDataHora(barbeiro.getId(), dataHora);
        if (horarioJaRegistrado && !ehOMesmoAgendamento(ignorarAgendamentoId, dataHora)) {
            throw new RegraNegocioException("Já existe um registro de agendamento nesse horário para este barbeiro.");
        }
    }

    private boolean ehOMesmoAgendamento(Long agendamentoId, LocalDateTime dataHora) {
        if (agendamentoId == null) {
            return false;
        }
        return agendamentoRepository.findById(agendamentoId)
                .map(a -> dataHora.equals(a.getDataHora()))
                .orElse(false);
    }

    /** Gera um codigo curto e unico para o cliente consultar o agendamento. */
    private String gerarTokenAcesso() {
        String token;
        do {
            token = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        } while (agendamentoRepository.findByTokenAcesso(token).isPresent());
        return token;
    }
}
