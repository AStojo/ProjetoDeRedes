# 🌐 Projeto de Redes de Computadores

### 🏛️ Universidade Portucalense (UPT) | Engenharia Informática
### 📋 UC: Redes de Computadores
### 👨‍🏫 Docente: Fernando Moreira
### 📅 Ano Letivo: 2025/2026

---

## ✨ 1. Sobre o Projeto

> **TEMA:** Sistema Distribuído de Comunicação e Partilha de Ficheiros em Java

* **DESCRIÇÃO:** Este projeto consiste no desenvolvimento de um sistema **cliente/servidor em Java** que permite a vários utilizadores ligarem-se a um servidor central para comunicar, consultar utilizadores ativos e transferir ficheiros de pequena dimensão.
* **OBJETIVO:** Demonstrar domínio dos conceitos fundamentais de redes — ligação, protocolo, framing, concorrência, validação, robustez, tratamento de erros e observação do sistema através de logs.
* O sistema combina comunicação **TCP** (autenticação, mensagens, comandos, listagem de utilizadores e transferência de ficheiros) e **UDP** (medição de latência / descoberta de servidor).

---

## 🛠️ 2. Tecnologias Usadas

| Tecnologia | Detalhe |
|---|---|
| Java | JDK 17+ |
| IDE | Eclipse |
| Protocolo de Transporte | TCP + UDP |
| Controlo de Versões | GitHub & GitHub Desktop |
| Codificação | UTF-8 |

**Classes principais da biblioteca padrão Java utilizadas:**
- `Socket` / `ServerSocket`
- `DatagramSocket` / `DatagramPacket`
- `BufferedReader` / `BufferedWriter`
- `DataInputStream` / `DataOutputStream`
- `ExecutorService`

---

## 👥 3. Equipa de Desenvolvimento (Grupo 2)

| Nome | Número de Aluno |
|---|---|
| Ana Souto | `53986` |
| António Santos | `47303` |
| Diogo Ferreira | `53501` |
| João Pinto | `53255` |
| Tomás Santos | `34379` |

---

## 📐 4. Arquitetura do Sistema (Planeada)

```
📦 ProjetoSockets_Grupo02/
├── 📂 src/
│   ├── 📂 server/
│   │   ├── ...
│   ├── 📂 client/
│   │   ├── ...
│   └── 📂 udp/
│       ├── ...
├── 📂 uploads/
│   └── 📄 .gitkeep
├── 📂 tests/
│   ├── 📄 plano_testes.md
│   └── 📂 evidencias/
├── 📂 docs/
│   ├── 📄 relatorio.pdf
│   └── 📄 protocolo.md
├── 📄 README.md
└── 📄 run.bat / run.sh
```

---

## 🔌 5. Portas Planeadas

| Serviço | Protocolo | Porta (default) |
|---|---|---|
| Servidor principal | TCP | `5000` |
| UDP Ping / Discover | UDP | `5001` |

> ⚠️ Poderá ser introduzido, nos argumentos de arranque, outras portas alterativas conforme escolha do utilizador.

---

## 💬 6. Protocolo de Comunicação (Planeado)

### Comandos Disponíveis

| Comando | Descrição | Exemplo |
|---|---|---|
| `NICK <nome>` | Regista o utilizador no servidor | `NICK ana` |
| `HELP` | Lista os comandos disponíveis | `HELP` |
| `WHO` | Lista os utilizadores ativos | `WHO` |
| `MSG <texto>` | Envia mensagem pública para todos | `MSG bom dia a todos` |
| `PM <nick> <texto>` | Envia mensagem privada | `PM joao ola` |
| `SEND <ficheiro>` | Envia ficheiro para o servidor | `SEND teste.txt` |
| `PING` / `DISCOVER` | Funcionalidade UDP | `PING` |
| `QUIT` | Termina a sessão | `QUIT` |

### 📟 Códigos de Resposta

| Código | Significado |
|---|---|
| `200` | Operação realizada com sucesso |
| `400` | Pedido inválido |
| `401` | Cliente ainda não registado |
| `404` | Recurso ou utilizador não encontrado |
| `408` | Timeout |
| `409` | Conflito (ex: nome já utilizado) |
| `500` | Erro interno do servidor |

---

## 📁 7. Transferência de Ficheiros (Planeada)

O cliente poderá enviar ficheiros para o servidor com o comando `SEND`.

**Protocolo de transferência (framing binário):**
```
[int nameLen][nameBytes UTF-8][long fileLen][fileBytes]
```

**Validações previstas no servidor:**
- Tamanho máximo por ficheiro: **5 MB**
- Rejeitar nomes inválidos e tentativas de path traversal (ex: `../../ficheiro.txt`)
- Rejeitar ficheiros vazios
- Tratar fim inesperado de ligação

---

## 📡 8. Funcionalidade UDP (Planeada)

> ⚠️ **O grupo ainda está a decidir entre as duas opções. A secção será atualizada quando a decisão for tomada.**

### Opção A — UDP Ping
O cliente enviará vários pacotes `PING` e receberá `PONG`, calculando estatísticas de RTT (mínimo, médio, máximo) e percentagem de perdas.

### Opção B — Descoberta UDP do Servidor
O cliente enviará `DISCOVER` em broadcast e o servidor responderá com o seu endereço e porta. Inclui timeout controlado caso não haja resposta.

---

## 📜 9. Regras de Nomenclatura (GitHub)

> 🌿 **Relativamente à nomeação de branches:**
- Procurar seguir o formato nome_branch;
- Separar com underline para melhor organização e leitura;
- Apenas um branch por utilizador para evitar a multiplicidade desnecessária de branches.

**Exemplos:**
```
Antonio_branch
Ana_branch
```

---
> 💬 **Relativamente à nomeação de Commits:**
- `[ADD]` -- adicionar algo
- `[COR]` -- correção de um erro
- `[ORG]` -- organizar
- `[ATL]` -- atualização de algo
- `[REM]` -- remoção de código / ficheiros
- `[DOC]` -- relativo a documentação

**Exemplos:**
```
[ADD] classe Cliente
[COR] erro na apresentação do codigo de resposta
[DOC] atualizar README com instruções de execução
```

---

## 📜 10. Licença

Projeto académico desenvolvido no âmbito da Universidade Portucalense.  
Uso restrito — não autorizado para fins comerciais ou reprodução sem permissão dos autores.
