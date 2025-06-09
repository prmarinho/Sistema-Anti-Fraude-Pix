package ui;

import db.DatabaseConnection;
import service.TransacaoService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class MainFrame extends JFrame {
    private Long usuarioId;
    private String nomeUsuario;
    private JTextField destinatarioIdField;
    private JTextField valorField;
    private JTextField transacaoIdField;
    private JTextArea resultadoArea;
    private JComboBox<String> motivoDenunciaCombo;
    private JTextField observacaoField;
    private JTable transacoesTable;
    private TransacaoService transacaoService;

    public MainFrame(Long usuarioId, String nomeUsuario) {
        this.usuarioId = usuarioId;
        this.nomeUsuario = nomeUsuario;
        transacaoService = new TransacaoService();
        initComponents();
    }

    private void initComponents() {
        setTitle("Sistema Anti-Fraude Pix - commitOuDesiste");
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Painel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(240, 240, 240));

        // Título com saudação
        JLabel titleLabel = new JLabel("Bem-vindo, " + nomeUsuario + "!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 102, 204));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Painel de entrada
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        inputPanel.setBackground(new Color(240, 240, 240));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Nova Transação"));
        inputPanel.add(new JLabel("ID do Destinatário:"));
        destinatarioIdField = new JTextField(10);
        destinatarioIdField.setFont(new Font("Arial", Font.PLAIN, 14));
        inputPanel.add(destinatarioIdField);
        inputPanel.add(new JLabel("Valor (R$):"));
        valorField = new JTextField(10);
        valorField.setFont(new Font("Arial", Font.PLAIN, 14));
        inputPanel.add(valorField);

        JButton verificarButton = new JButton("Verificar Reputação");
        verificarButton.setFont(new Font("Arial", Font.BOLD, 14));
        verificarButton.setBackground(new Color(0, 102, 204));
        verificarButton.setForeground(Color.WHITE);
        verificarButton.setFocusPainted(false);
        inputPanel.add(verificarButton);
        JButton realizarTransacaoButton = new JButton("Realizar Transação");
        realizarTransacaoButton.setFont(new Font("Arial", Font.BOLD, 14));
        realizarTransacaoButton.setBackground(new Color(0, 153, 51));
        realizarTransacaoButton.setForeground(Color.WHITE);
        realizarTransacaoButton.setFocusPainted(false);
        inputPanel.add(realizarTransacaoButton);

        // Painel de denúncia
        JPanel denunciaPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        denunciaPanel.setBackground(new Color(240, 240, 240));
        denunciaPanel.setBorder(BorderFactory.createTitledBorder("Denunciar Transação"));
        denunciaPanel.add(new JLabel("ID da Transação:"));
        transacaoIdField = new JTextField(10);
        transacaoIdField.setFont(new Font("Arial", Font.PLAIN, 14));
        denunciaPanel.add(transacaoIdField);
        denunciaPanel.add(new JLabel("Motivo da Denúncia:"));
        String[] motivos = {"Selecione", "Serviço não realizado", "Contato perdido com fornecedor"};
        motivoDenunciaCombo = new JComboBox<>(motivos);
        motivoDenunciaCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        denunciaPanel.add(motivoDenunciaCombo);
        denunciaPanel.add(new JLabel("Observação:"));
        observacaoField = new JTextField(10);
        observacaoField.setFont(new Font("Arial", Font.PLAIN, 14));
        denunciaPanel.add(observacaoField);

        JButton denunciarButton = new JButton("Pix Suspeito");
        denunciarButton.setFont(new Font("Arial", Font.BOLD, 14));
        denunciarButton.setBackground(new Color(204, 0, 0));
        denunciarButton.setForeground(Color.WHITE);
        denunciarButton.setFocusPainted(false);
        denunciaPanel.add(denunciarButton);

        // Tabela de transações
        transacoesTable = new JTable();
        transacoesTable.setFont(new Font("Arial", Font.PLAIN, 12));
        transacoesTable.setRowHeight(25);
        atualizarTabelaTransacoes();
        JScrollPane tableScrollPane = new JScrollPane(transacoesTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Transações Recentes"));

        // Área de resultado
        resultadoArea = new JTextArea(5, 40);
        resultadoArea.setEditable(false);
        resultadoArea.setFont(new Font("Arial", Font.PLAIN, 12));
        JScrollPane resultadoScrollPane = new JScrollPane(resultadoArea);
        resultadoScrollPane.setBorder(BorderFactory.createTitledBorder("Resultados"));

        // Painel central
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(new Color(240, 240, 240));
        centerPanel.add(denunciaPanel, BorderLayout.NORTH);
        centerPanel.add(resultadoScrollPane, BorderLayout.CENTER);

        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Ações dos botões
        verificarButton.addActionListener(e -> verificarReputacao());
        realizarTransacaoButton.addActionListener(e -> realizarTransacao());
        denunciarButton.addActionListener(e -> registrarDenuncia());
    }

    private void verificarReputacao() {
        try {
            Long destinatarioId = Long.parseLong(destinatarioIdField.getText());
            String reputacao = transacaoService.consultarReputacao(destinatarioId);
            resultadoArea.setText("=== Reputação do Destinatário ===\n" + reputacao);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Por favor, insira um ID válido.", "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao consultar reputação: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void realizarTransacao() {
        try {
            Long destinatarioId = Long.parseLong(destinatarioIdField.getText());
            double valor = Double.parseDouble(valorField.getText());

            // Validar destinatário
            if (!validarDestinatario(destinatarioId)) {
                JOptionPane.showMessageDialog(this, "Destinatário não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Inserir transação
            Long transacaoId = inserirTransacao(usuarioId, destinatarioId, valor);
            String alertas = transacaoService.detectarPadroesSuspeitos(transacaoId);

            if (!alertas.equals("Nenhum padrão suspeito detectado.")) {
                int resposta = JOptionPane.showConfirmDialog(this,
                        "=== Alerta de Transação Suspeita ===\n" + alertas + "\nDeseja continuar com a transação?",
                        "Confirmação", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (resposta == JOptionPane.YES_OPTION) {
                    resultadoArea.setText("Transação realizada com sucesso!\nID da transação: " + transacaoId + "\n" + alertas);
                    atualizarTabelaTransacoes();
                } else {
                    resultadoArea.setText("Transação cancelada pelo usuário.");
                    removerTransacao(transacaoId);
                }
            } else {
                resultadoArea.setText("Transação realizada com sucesso!\nID da transação: " + transacaoId + "\nNenhum alerta detectado.");
                atualizarTabelaTransacoes();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Por favor, insira valores válidos para ID e valor.", "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao realizar transação: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private boolean validarDestinatario(Long destinatarioId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id FROM usuarios WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, destinatarioId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    private Long inserirTransacao(Long remetenteId, Long destinatarioId, double valor) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO transacoes (remetente_id, destinatario_id, valor, data_hora) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setLong(1, remetenteId);
            stmt.setLong(2, destinatarioId);
            stmt.setDouble(3, valor);
            stmt.setTimestamp(4, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }
            throw new SQLException("Falha ao obter ID da transação.");
        }
    }

    private void removerTransacao(Long transacaoId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM transacoes WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, transacaoId);
            stmt.executeUpdate();
        }
    }

    private void registrarDenuncia() {
        try {
            Long transacaoId = Long.parseLong(transacaoIdField.getText());
            int motivoIndex = motivoDenunciaCombo.getSelectedIndex();
            if (motivoIndex == 0) {
                JOptionPane.showMessageDialog(this, "Selecione um motivo para a denúncia.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Long motivoId = (long) motivoIndex;
            String observacao = observacaoField.getText();

            transacaoService.registrarDenuncia(transacaoId, motivoId, observacao);
            resultadoArea.setText("Denúncia registrada com sucesso!\nTransação ID: " + transacaoId + "\nMotivo: " + motivoDenunciaCombo.getSelectedItem() + "\nObservação: " + observacao);

            // Atualizar score de confiança
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "SELECT destinatario_id FROM transacoes WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setLong(1, transacaoId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    Long destinatarioId = rs.getLong("destinatario_id");
                    int novoScore = transacaoService.calcularScoreConfianca(destinatarioId);
                    atualizarScoreConfianca(destinatarioId, novoScore);
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Por favor, insira um ID de transação válido.", "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao registrar denúncia: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void atualizarScoreConfianca(Long usuarioId, int novoScore) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO score_confianca (usuario_id, score, ultima_atualizacao) VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE score = ?, ultima_atualizacao = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, usuarioId);
            stmt.setInt(2, novoScore);
            stmt.setTimestamp(3, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(4, novoScore);
            stmt.setTimestamp(5, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();
        }
    }

    private void atualizarTabelaTransacoes() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT t.id, u.nome, t.valor, t.data_hora " +
                    "FROM transacoes t JOIN usuarios u ON t.destinatario_id = u.id " +
                    "WHERE t.remetente_id = ? ORDER BY t.data_hora DESC LIMIT 10";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, usuarioId);
            ResultSet rs = stmt.executeQuery();

            DefaultTableModel model = new DefaultTableModel(
                    new String[]{"ID Transação", "Destinatário", "Valor (R$)", "Data/Hora"}, 0);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getLong("id"),
                        rs.getString("nome"),
                        String.format("%.2f", rs.getDouble("valor")),
                        rs.getTimestamp("data_hora").toLocalDateTime().toString()
                });
            }
            transacoesTable.setModel(model);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar transações: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}