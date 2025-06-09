package service;

import db.DatabaseConnection;
import model.Denuncia;
import model.ScoreConfianca;
import model.Transacao;
import model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class TransacaoService {

    // Consulta reputação do destinatário
    public String consultarReputacao(Long destinatarioId) throws SQLException {
        StringBuilder reputacao = new StringBuilder();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sqlScore = "SELECT score FROM score_confianca WHERE usuario_id = ? AND ultima_atualizacao = (SELECT MAX(ultima_atualizacao) FROM score_confianca WHERE usuario_id = ?)";
            PreparedStatement stmtScore = conn.prepareStatement(sqlScore);
            stmtScore.setLong(1, destinatarioId);
            stmtScore.setLong(2, destinatarioId);
            ResultSet rsScore = stmtScore.executeQuery();
            int score = 0;
            if (rsScore.next()) {
                score = rsScore.getInt("score");
                reputacao.append("Score de confiança: ").append(score).append("/100\n");
            } else {
                reputacao.append("Nenhum score de confiança registrado.\n");
            }

            String sqlDenuncias = "SELECT COUNT(*) AS total FROM denuncias d JOIN transacoes t ON d.transacao_id = t.id WHERE t.destinatario_id = ?";
            PreparedStatement stmtDenuncias = conn.prepareStatement(sqlDenuncias);
            stmtDenuncias.setLong(1, destinatarioId);
            ResultSet rsDenuncias = stmtDenuncias.executeQuery();
            if (rsDenuncias.next()) {
                int totalDenuncias = rsDenuncias.getInt("total");
                reputacao.append("Número de denúncias: ").append(totalDenuncias).append("\n");
            }

            String sqlUsuario = "SELECT data_criacao FROM usuarios WHERE id = ?";
            PreparedStatement stmtUsuario = conn.prepareStatement(sqlUsuario);
            stmtUsuario.setLong(1, destinatarioId);
            ResultSet rsUsuario = stmtUsuario.executeQuery();
            if (rsUsuario.next()) {
                LocalDateTime dataCriacao = rsUsuario.getTimestamp("data_criacao").toLocalDateTime();
                long diasDesdeCriacao = ChronoUnit.DAYS.between(dataCriacao, LocalDateTime.now());
                if (diasDesdeCriacao < 30) {
                    reputacao.append("Aviso: Conta criada há menos de 30 dias.\n");
                }
            }

            return reputacao.toString();
        }
    }

    // Calcular score de confiança
    public int calcularScoreConfianca(Long usuarioId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            int score = 100;

            String sqlDenuncias = "SELECT COUNT(*) AS total FROM denuncias d JOIN transacoes t ON d.transacao_id = t.id WHERE t.destinatario_id = ?";
            PreparedStatement stmtDenuncias = conn.prepareStatement(sqlDenuncias);
            stmtDenuncias.setLong(1, usuarioId);
            ResultSet rsDenuncias = stmtDenuncias.executeQuery();
            if (rsDenuncias.next()) {
                int totalDenuncias = rsDenuncias.getInt("total");
                score -= totalDenuncias * 20;
            }

            String sqlUsuario = "SELECT data_criacao FROM usuarios WHERE id = ?";
            PreparedStatement stmtUsuario = conn.prepareStatement(sqlUsuario);
            stmtUsuario.setLong(1, usuarioId);
            ResultSet rsUsuario = stmtUsuario.executeQuery();
            if (rsUsuario.next()) {
                LocalDateTime dataCriacao = rsUsuario.getTimestamp("data_criacao").toLocalDateTime();
                long diasDesdeCriacao = ChronoUnit.DAYS.between(dataCriacao, LocalDateTime.now());
                if (diasDesdeCriacao < 30) {
                    score -= 10;
                }
            }

            String sqlTransacoes = "SELECT COUNT(*) AS total, SUM(valor) AS total_valor FROM transacoes WHERE destinatario_id = ?";
            PreparedStatement stmtTransacoes = conn.prepareStatement(sqlTransacoes);
            stmtTransacoes.setLong(1, usuarioId);
            ResultSet rsTransacoes = stmtTransacoes.executeQuery();
            if (rsTransacoes.next()) {
                int totalTransacoes = rsTransacoes.getInt("total");
                double totalValor = rsTransacoes.getDouble("total_valor");
                if (totalTransacoes > 5) {
                    score += 10;
                }
                if (totalValor > 5000) {
                    score += 10;
                }
            }

            return Math.max(0, Math.min(100, score));
        }
    }

    // Detectar padrões suspeitos
    public String detectarPadroesSuspeitos(Long transacaoId) throws SQLException {
        StringBuilder alertas = new StringBuilder();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sqlTransacao = "SELECT t.destinatario_id, t.valor, u.data_criacao FROM transacoes t JOIN usuarios u ON t.destinatario_id = u.id WHERE t.id = ?";
            PreparedStatement stmtTransacao = conn.prepareStatement(sqlTransacao);
            stmtTransacao.setLong(1, transacaoId);
            ResultSet rsTransacao = stmtTransacao.executeQuery();
            if (rsTransacao.next()) {
                Long destinatarioId = rsTransacao.getLong("destinatario_id");
                double valor = rsTransacao.getDouble("valor");
                LocalDateTime dataCriacao = rsTransacao.getTimestamp("data_criacao").toLocalDateTime();

                long diasDesdeCriacao = ChronoUnit.DAYS.between(dataCriacao, LocalDateTime.now());
                if (diasDesdeCriacao < 30) {
                    alertas.append("Aviso: Destinatário com conta criada há menos de 30 dias.\n");
                }

                if (valor > 1000) {
                    alertas.append("Aviso: Transação com valor elevado (R$").append(valor).append(").\n");
                }

                String sqlRepeticao = "SELECT COUNT(*) AS total FROM transacoes WHERE destinatario_id = ? AND valor BETWEEN ? AND ? AND data_hora >= ?";
                PreparedStatement stmtRepeticao = conn.prepareStatement(sqlRepeticao);
                stmtRepeticao.setLong(1, destinatarioId);
                stmtRepeticao.setDouble(2, valor * 0.9);
                stmtRepeticao.setDouble(3, valor * 1.1);
                stmtRepeticao.setTimestamp(4, java.sql.Timestamp.valueOf(LocalDateTime.now().minusHours(24)));
                ResultSet rsRepeticao = stmtRepeticao.executeQuery();
                if (rsRepeticao.next() && rsRepeticao.getInt("total") > 3) {
                    alertas.append("Aviso: Múltiplas transações com valores semelhantes nas últimas 24 horas.\n");
                }

                String sqlDenuncias = "SELECT COUNT(*) AS total FROM denuncias d JOIN transacoes t ON d.transacao_id = t.id WHERE t.destinatario_id = ?";
                PreparedStatement stmtDenuncias = conn.prepareStatement(sqlDenuncias);
                stmtDenuncias.setLong(1, destinatarioId);
                ResultSet rsDenuncias = stmtDenuncias.executeQuery();
                if (rsDenuncias.next() && rsDenuncias.getInt("total") > 0) {
                    alertas.append("Aviso: Destinatário possui denúncias registradas.\n");
                }
            }

            return alertas.length() > 0 ? alertas.toString() : "Nenhum padrão suspeito detectado.";
        }
    }

    // Registrar denúncia
    public void registrarDenuncia(Long transacaoId, Long motivoId, String observacao) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO denuncias (transacao_id, motivo_id, observacao, data_ocorrencia) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, transacaoId);
            stmt.setLong(2, motivoId);
            stmt.setString(3, observacao);
            stmt.setTimestamp(4, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();
        }
    }

    // Método auxiliar para testar a lógica
    public static void main(String[] args) {
        TransacaoService service = new TransacaoService();
        try {
            System.out.println("Reputação do usuário 5:\n" + service.consultarReputacao(5L));
            System.out.println("Score calculado para usuário 5: " + service.calcularScoreConfianca(5L));
            System.out.println("Padrões suspeitos na transação 4:\n" + service.detectarPadroesSuspeitos(4L));
            service.registrarDenuncia(1L, 1L, "Teste de denúncia");
            System.out.println("Denúncia registrada com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro: " + e.getMessage());
        }
    }
}