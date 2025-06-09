CREATE DATABASE Just;
USE Just;

CREATE TABLE usuarios (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    cpf_cnpj VARCHAR(20) NOT NULL UNIQUE,
    data_criacao DATETIME NOT NULL,
    tipo ENUM('PESSOA_FISICA', 'PESSOA_JURIDICA') NOT NULL
);

CREATE TABLE transacoes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    remetente_id BIGINT NOT NULL,
    destinatario_id BIGINT NOT NULL,
    valor DECIMAL(10,2) NOT NULL,
    data_hora DATETIME NOT NULL,
    FOREIGN KEY (remetente_id) REFERENCES usuarios(id),
    FOREIGN KEY (destinatario_id) REFERENCES usuarios(id)
);

CREATE TABLE motivos_denuncia (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    descricao TEXT NOT NULL
);

CREATE TABLE denuncias (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transacao_id BIGINT NOT NULL,
    motivo_id BIGINT NOT NULL,
    observacao TEXT,
    data_ocorrencia DATETIME NOT NULL,
    FOREIGN KEY (transacao_id) REFERENCES transacoes(id),
    FOREIGN KEY (motivo_id) REFERENCES motivos_denuncia(id)
);

CREATE TABLE score_confianca (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    score INT NOT NULL CHECK (score BETWEEN 0 AND 100),
    ultima_atualizacao DATETIME NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE TABLE alertas_suspeitos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transacao_id BIGINT NOT NULL,
    descricao TEXT,
    data_alerta DATETIME NOT NULL,
    FOREIGN KEY (transacao_id) REFERENCES transacoes(id)
);

CREATE INDEX idx_remetente ON transacoes(remetente_id);
CREATE INDEX idx_destinatario ON transacoes(destinatario_id);
CREATE INDEX idx_usuario_score ON score_confianca(usuario_id);
CREATE INDEX idx_transacao_denuncia ON denuncias(transacao_id);
CREATE INDEX idx_transacao_alerta ON alertas_suspeitos(transacao_id);

DELIMITER //

CREATE TRIGGER trg_validar_cpf_cnpj
BEFORE INSERT ON usuarios
FOR EACH ROW
BEGIN
    SET @cpf_cnpj_limpo = REPLACE(REPLACE(REPLACE(REPLACE(NEW.cpf_cnpj, '.', ''), '-', ''), '/', ''), ' ', '');

    IF CHAR_LENGTH(@cpf_cnpj_limpo) NOT IN (11, 14) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'CPF ou CNPJ inválido: tamanho incorreto';
    END IF;

    IF @cpf_cnpj_limpo REGEXP '^(\\d)\\1{10,13}$' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'CPF ou CNPJ inválido: todos os dígitos são iguais';
    END IF;
END;
//

DELIMITER ;

INSERT INTO usuarios (nome, cpf_cnpj, data_criacao, tipo) VALUES
('Renata Lima', '123.456.789-00', '2023-11-20 09:00:00', 'PESSOA_FISICA'),
('Eduardo Mendes', '234.567.890-11', '2024-01-15 10:15:00', 'PESSOA_FISICA'),
('Loja Brasil Eletrônicos', '12.345.678/0001-90', '2022-08-10 08:00:00', 'PESSOA_JURIDICA'),
('Juliana Costa', '345.678.901-22', '2025-01-05 14:00:00', 'PESSOA_FISICA'),
('TechPro Serviços', '98.765.432/0001-12', '2023-09-30 12:30:00', 'PESSOA_JURIDICA'),
('Bruno Silva', '456.789.012-33', '2024-12-12 16:00:00', 'PESSOA_FISICA'),
('Vanessa Souza', '567.890.123-44', '2024-03-01 11:20:00', 'PESSOA_FISICA'),
('Alpha Comércio', '87.654.321/0001-55', '2022-05-10 09:45:00', 'PESSOA_JURIDICA'),
('Gabriel Rocha', '678.901.234-55', '2024-10-11 13:15:00', 'PESSOA_FISICA'),
('Loja Novo Mundo', '76.543.210/0001-66', '2023-07-22 15:30:00', 'PESSOA_JURIDICA');

INSERT INTO transacoes (remetente_id, destinatario_id, valor, data_hora) VALUES
(1, 3, 1299.90, '2025-06-01 10:00:00'),
(2, 3, 299.90, '2025-06-01 11:00:00'),
(4, 5, 540.00, '2025-06-01 14:30:00'),
(6, 8, 850.00, '2025-06-01 17:15:00'),
(1, 10, 750.00, '2025-06-02 09:45:00'),
(7, 3, 2300.00, '2025-06-02 12:20:00'),
(9, 5, 670.50, '2025-06-02 13:00:00'),
(4, 10, 450.00, '2025-06-03 08:40:00'),
(2, 5, 1100.00, '2025-06-03 10:00:00'),
(6, 5, 950.00, '2025-06-03 11:30:00');

INSERT INTO motivos_denuncia (descricao) VALUES
('Serviço técnico não realizado após pagamento.'),
('Pagamento realizado e contato perdido com fornecedor.');

INSERT INTO denuncias (transacao_id, motivo_id, observacao, data_ocorrencia) VALUES
(4, 1, NULL, '2025-06-01 18:00:00'),
(9, 2, NULL, '2025-06-03 11:00:00');

INSERT INTO score_confianca (usuario_id, score, ultima_atualizacao) VALUES
(3, 92, '2025-06-01 00:00:00'),
(5, 38, '2025-06-02 00:00:00'),
(10, 77, '2025-06-03 00:00:00'),
(8, 85, '2025-06-01 00:00:00'),
(6, 70, '2025-06-03 00:00:00');

INSERT INTO alertas_suspeitos (transacao_id, descricao, data_alerta) VALUES
(4, 'Destinatário com baixa reputação e sem histórico de feedbacks positivos.', '2025-06-01 17:30:00'),
(9, 'Repetição de valores altos para fornecedor com histórico negativo.', '2025-06-03 10:15:00'),
(10, 'Transação com valor elevado para empresa com denúncias recentes.', '2025-06-03 11:45:00');
