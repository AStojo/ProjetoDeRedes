# Projeto de Redes de Computadores

## Universidade Portucalense (UPT)

**Curso:** Engenharia Informática
**Unidade Curricular:** Redes de Computadores
**Docente:** Fernando Moreira
**Ano Letivo:** 2025/2026

### Grupo 2

| Nome           | Nº Aluno |
| -------------- | -------- |
| Ana Souto      | 53986    |
| António Santos | 47303    |
| Diogo Ferreira | 53501    |
| João Pinto     | 53255    |
| Tomás Santos   | 34379    |

---

# 1. Descrição do Projeto

Este projeto consiste no desenvolvimento de um sistema distribuído cliente/servidor em Java para comunicação e partilha de ficheiros através da rede.

O sistema permite:

* Ligação simultânea de múltiplos clientes;
* Troca de mensagens públicas;
* Envio de mensagens privadas;
* Consulta dos utilizadores ativos;
* Transferência de ficheiros para o servidor;
* Descoberta automática do servidor através de UDP Broadcast.

A implementação utiliza TCP para a comunicação principal e UDP para a funcionalidade de descoberta do servidor na rede local.

---

# 2. Tecnologias Utilizadas

| Tecnologia   | Descrição                    |
| ------------ | ---------------------------- |
| Java JDK 17+ | Linguagem de programação     |
| Eclipse IDE  | Ambiente de desenvolvimento  |
| TCP          | Comunicação cliente/servidor |
| UDP          | Descoberta do servidor       |
| GitHub       | Controlo de versões          |

Bibliotecas Java utilizadas:

* ServerSocket
* Socket
* DatagramSocket
* DatagramPacket
* BufferedReader
* BufferedWriter
* DataInputStream
* DataOutputStream
* ExecutorService

---

# 3. Estrutura do Projeto

```text
ProjetoSockets_GrupoXX/ 
│ 
├── src/ 
│   ├── server/ 
│   │   ├── Servidor.java
│   │   ├── Atendente.java
│   │   ├── GerirCliente.java
│   │   └── Logger.java
│   ├── client/ 
│   │   ├── Cliente.java 
│   │   └── ThreadRecetor.java 
│   └── udp/
│       ├── UdpServidorDescoberta.java
│       └── UdpClienteDescoberta.java     
├── uploads/ 
│   └── .gitkeep 
│ 
├── tests/ 
│   ├── plano_testes.md 
│   └── evidencias/ 
│ 
├── docs/ 
│   ├── relatorio.pdf 
│   └── protocolo.md 
│ 
├── README.md 
└── run.bat  
```

---

# 4. Portas Utilizadas

| Serviço                | Protocolo | Porta                    |
| ---------------------- | --------- | ------------------------ |
| Servidor Principal     | TCP       | Definida pelo utilizador |
| Descoberta de Servidor | UDP       | 6000                     |

---

# 5. Compilação

O projeto pode ser compilado através do Eclipse IDE ou pela linha de comandos.

```bash
javac -d bin src/server/*.java src/client/*.java src/udp/*.java
```

---

# 6. Execução do Servidor
Após a compilação do projeto, execute a classe Servidor.

cd bin
java server.Servidor

Ao iniciar, o servidor solicitará a porta TCP onde ficará à escuta:

Indique a porta:
5001

Depois de introduzida uma porta válida, o servidor inicia os serviços TCP e UDP:

Servidor iniciado na porta: 5001
A aguardar clientes...

O serviço de descoberta UDP é iniciado automaticamente numa thread separada e fica à escuta na porta UDP 6000.

Enquanto estiver em execução, o servidor:

 + Aceita múltiplos clientes em simultâneo;
 + Gere cada cliente através de uma thread do ExecutorService;
 + Regista eventos e operações através do sistema de logs;
 + Fecha automaticamente ligações inativas após 3 minutos de inatividade.

---

# 7. Execução do Cliente
7.1 Abrir um novo terminal e executar a classe Cliente:

cd bin
java client.Cliente

7.2 O cliente solicitará o endereço IP (ou hostname) e a porta TCP do servidor:

IP/Host do servidor:
127.0.0.1

Porta do servidor:
5001

7.3 Após estabelecer a ligação, será apresentada uma mensagem de confirmação:

Ligado ao servidor 127.0.0.1:5001
Escreva HELP para ver os comandos.

7.4 Antes de utilizar os restantes comandos, o utilizador deve efetuar o registo através do comando:

NICK joao

Depois do login, ficam disponíveis os restantes comandos do sistema, incluindo mensagens públicas, mensagens privadas, transferência de ficheiros e consulta de utilizadores ativos.
---

# 8. Comandos Disponíveis

## 8.1 Comandos Disponíveis Antes do Login

| Comando     | Descrição                        | Exemplo   |
| ----------- | -------------------------------- | --------- |
| NICK <nome> | Regista o utilizador no servidor | NICK joao |
| HELP        | Mostra ajuda                     | HELP      |
| QUIT        | Termina a ligação                | QUIT      |

---

## 8.2 Comandos Disponíveis Após Login

| Comando                 | Descrição                              | Exemplo             |
| ----------------------- | -------------------------------------- | ------------------- |
| HELP                    | Mostra a lista de comandos             | HELP                |
| WHO                     | Lista os utilizadores ligados          | WHO                 |
| MSG <texto>             | Envia uma mensagem pública             | MSG Bom dia a todos |
| PM <utilizador> <texto> | Envia uma mensagem privada             | PM ana Olá Ana      |
| SEND <ficheiro>         | Envia um ficheiro para o servidor      | SEND teste.txt      |
| NICK <novo_nome>        | Altera o nome de utilizador atual      | NICK joao123        |
| PING                    | Verifica a disponibilidade do servidor | PING                |
| TIME                    | Obtém a data e hora atual do servidor  | TIME                |
| QUIT                    | Termina a sessão                       | QUIT                |

### 8.3 Exemplos de Utilização

```text
NICK joao

WHO

MSG Bom dia a todos

PM ana Olá Ana

PING

TIME

SEND teste.txt

QUIT
```

---

## 8.4 Códigos de Resposta

| Código | Significado                          |
| ------ | ------------------------------------ |
| 200    | Operação realizada com sucesso       |
| 400    | Pedido inválido                      |
| 401    | Utilizador não autenticado           |
| 404    | Utilizador ou recurso não encontrado |
| 408    | Timeout da ligação                   |
| 409    | Nome já utilizado                    |
| 500    | Erro interno do servidor             |

```
```

# 9. Protocolo de Descoberta UDP

A funcionalidade UDP permite localizar automaticamente um servidor disponível na rede local.

## 9.1 Pedido

```text
DISCOVER
```

## 9.2 Resposta

```text
SERVER <IP> <PORTA_TCP>
```

Exemplo:

```text
SERVER 192.168.1.100 5001
```

---

# 10. Teste da Funcionalidade UDP

1. Iniciar o servidor.
2. Confirmar que o serviço UDP está ativo.
3. Executar o cliente de descoberta UDP.
4. O cliente envia uma mensagem DISCOVER.
5. O servidor responde com o endereço IP e a porta TCP configurada.

Resultado esperado:

```text
A procurar servidor na rede...

Servidor encontrado:
SERVER 192.168.1.100 5001
```

---

# 11. Teste da Transferência de Ficheiros

1. Ligar um cliente ao servidor.
2. Garantir a existência de um ficheiro válido.
3. Executar:

```text
SEND teste.txt
```

4. O servidor recebe e valida o ficheiro.
5. O ficheiro é armazenado na diretoria uploads.

Resultado esperado:

```text
Ficheiro recebido com sucesso.
```

---

# 12. Limitações Conhecidas

## 12.1 Transferência de Ficheiros

* Tamanho máximo permitido: 5 MB.
* Ficheiros vazios não são aceites.

## 12.2 Segurança

Para evitar ataques de Path Traversal não são permitidos nomes de ficheiros contendo:

```text
..
/
\
```

## 12.3 Usernames

* Comprimento mínimo: 3 caracteres.
* Comprimento máximo: 20 caracteres.
* Não são permitidos nomes duplicados.

## 12.4 Timeout

* Clientes inativos são desligados automaticamente após o período configurado no servidor.

## 12.5 UDP

* O cliente UDP aguarda resposta durante um máximo de 3 segundos.
-----
#13. Limitações conhecidas

O programa limita em pouco comandos e respostas simples.
