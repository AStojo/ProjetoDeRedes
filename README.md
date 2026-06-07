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

## 📐 4. Arquitetura do Sistema

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
└── 📄 run.bat
```

---

## 🔌 5. Portas Usadas

| Serviço | Protocolo | Porta |
|---|---|---|
| Servidor principal | TCP | `Definida pelo utilizador ao iniciar` (ex: `5000`) |
| Descoberta de Servidor | UDP | `6000` |

---

## 🚀 6. Como Compilar e Executar

### Como Compilar
O projeto pode ser compilado no Eclipse IDE (importando o projeto inteiro) ou através da linha de comandos na raiz do projeto:
```bash
javac -d bin src/server/*.java src/client/*.java src/udp/*.java
```

### Como Executar o Servidor
Para iniciar o servidor (em ambiente Windows), execute o ficheiro de automação `run.bat` presente na raiz do projeto, ou inicie via terminal:
```bash
cd bin
java server.Servidor
```
O servidor irá pedir no terminal que indique a **porta TCP** a ser utilizada. O serviço secundário de descoberta (UDP) inicia automaticamente, em background, na porta 6000.

### Como Executar o Cliente
Para executar o cliente principal (TCP), abra um novo terminal na pasta `bin` e execute:
```bash
java client.Cliente
```
Serão pedidos o endereço do host (ex: `127.0.0.1`) e a porta TCP(ex: `5000`) especificada durante o arranque do servidor.

---

## 💬 7. Protocolo de Comunicação e Comandos

Após estabelecer a ligação e inserir um nome de utilizador válido, o cliente tem os seguintes comandos ao dispor:

| Comando | Descrição | Exemplo |
|---|---|---|
| `HELP` | Lista os comandos disponíveis | `HELP` |
| `WHO` | Lista todos os utilizadores ativos e logados | `WHO` |
| `MSG <texto>` | Envia mensagem pública para todos | `MSG bom dia a todos` |
| `PM <nick> <texto>` | Envia mensagem privada | `PM joao ola` |
| `SEND <ficheiro>` | Envia ficheiro para o servidor | `SEND foto.jpg` |
| `QUIT` | Termina a sessão e desliga do servidor | `QUIT` |

---

## 🧪 8. Como Testar Funcionalidades Específicas

### Como Testar a Funcionalidade UDP (Descoberta do Servidor)
O grupo implementou a **Descoberta UDP do Servidor**. Para testar:
1. Certifique-se de que o Servidor está em execução.
2. Num terminal na pasta `bin`, execute a classe do cliente UDP:
   ```bash
   java udp.UdpClienteDescoberta
   ```
3. O cliente UDP enviará uma mensagem de broadcast `DISCOVER` para a rede.
4. O servidor interceptará a mensagem na porta 6000 e responderá com o seu endereço IP e a porta TCP standard (ex: 5000), devolvendo no formato: `SERVER 192.168.1.100 5000`.

### Como Testar o Envio de Ficheiros
1. Com o `Cliente` a correr e conectado ao servidor, assegure-se de que possui um ficheiro local válido (ex: `teste.txt`) na mesma pasta a partir de onde executou o cliente (ou insira o caminho completo).
2. Utilize o comando `SEND`:
   ```text
   SEND teste.txt
   ```
3. O servidor processa o envio através do protocolo de framing binário e, sendo bem-sucedido, o ficheiro ficará guardado na diretoria `/uploads/` do lado do servidor.

---

## ⚠️ 9. Limitações Conhecidas
Durante o desenvolvimento do programa depará-mos nos com algumas restrições, sendo elas:
- **Tamanho Limite de Ficheiros:** A transferência está limitada a ficheiros com um tamanho máximo de **5 MB**. Ficheiros de tamanho zero (vazios) também não são aceites.
- **Segurança de Nomenclatura:** Para prevenir vulnerabilidades do tipo _Path Traversal_, nomes de ficheiros que contenham os carateres `..`, `/` ou `\` não são permitidos no comando `SEND`.
- **Registo de Utilizador:** O nome (Username) deve ter obrigatoriamente entre 3 e 20 carateres, não aceitando os mesmos tal como não são admitidos nomes duplicados na mesma sessão.
- **Timeout Inatividade:** O socket de cliente está configurado com um limite de inatividade de 30 minutos, de forma a conseguir fazer os testes sem a preocupação do tempo. Findo esse tempo, a conexão é encerrada automaticamente.
- **UDP Timeout:** Ao procurar um servidor na rede, o cliente de descoberta UDP aguardará por uma resposta num limite máximo de 3 segundos antes de abortar a operação.

---

## 📜 10. Regras de Nomenclatura (GitHub)

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

## 📜 11. Licença

Projeto académico desenvolvido no âmbito da Universidade Portucalense.  
Uso restrito — não autorizado para fins comerciais ou reprodução sem permissão dos autores.
